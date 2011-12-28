/*****************************************************************************
 *  Some of the code in this project is derived from the                     *
 *  MetaWatch MWM-for-Android project,                                       *
 *  Copyright (c) 2011 Meta Watch Ltd.                                       *
 *  www.MetaWatch.org                                                        *
 *                                                                           *
 =============================================================================
 *                                                                           *
 *  Licensed under the Apache License, Version 2.0 (the "License");          *
 *  you may not use this file except in compliance with the License.         *
 *  You may obtain a copy of the License at                                  *
 *                                                                           *
 *    http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                           *
 *  Unless required by applicable law or agreed to in writing, software      *
 *  distributed under the License is distributed on an "AS IS" BASIS,        *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 *  See the License for the specific language governing permissions and      *
 *  limitations under the License.                                           *
 *                                                                           *
 *****************************************************************************/
package org.metawatch.manager.core.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.metawatch.manager.core.constants.Constants;
import org.metawatch.manager.core.lib.constants.WatchButton;
import org.metawatch.manager.core.lib.constants.WatchButtonPressType;
import org.metawatch.manager.core.lib.constants.WatchConnectionState;
import org.metawatch.manager.core.lib.constants.WatchMode;
import org.metawatch.manager.core.lib.constants.WatchType;
import org.metawatch.manager.core.lib.intents.DisplayIdleScreenWidget;
import org.metawatch.manager.core.lib.intents.DisplayNotification;
import org.metawatch.manager.core.lib.intents.WatchConnectionInfo;
import org.metawatch.manager.core.lib.intents.WatchIntentConstants;
import org.metawatch.manager.core.packets.PacketReader;
import org.metawatch.manager.core.packets.PacketUtils;
import org.metawatch.manager.core.packets.WatchButtonMeaning;
import org.metawatch.manager.core.packets.WatchPacket;
import org.metawatch.manager.core.packets.incoming.ButtonEventMessage;
import org.metawatch.manager.core.packets.incoming.GetDeviceTypeResponse;
import org.metawatch.manager.core.packets.incoming.ReadBatteryVoltageResponse;
import org.metawatch.manager.core.packets.incoming.StatusChangeEvent;
import org.metawatch.manager.core.packets.incoming.StatusChangeEvent.StatusChangeEventType;
import org.metawatch.manager.core.packets.outgoing.DisableButton;
import org.metawatch.manager.core.packets.outgoing.EnableButton;
import org.metawatch.manager.core.packets.outgoing.GetDeviceType;
import org.metawatch.manager.core.packets.outgoing.ReadBatteryVoltage;
import org.metawatch.manager.core.packets.outgoing.SetRealTimeClock;
import org.metawatch.manager.core.packets.outgoing.UpdateLCDDisplay;
import org.metawatch.manager.core.packets.outgoing.WriteOLEDScrollBuffer;
import org.metawatch.manager.core.renderer.AnalogWatchRenderer;
import org.metawatch.manager.core.renderer.DigitalWatchRenderer;
import org.metawatch.manager.core.renderer.WatchRenderer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class WatchConnection {

	private static final UUID	DEFAULT_UUID	= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private class BluetoothListener implements Runnable {
		@Override
		public void run() {
			try {
				InputStream in = btSocket.getInputStream();
				while (true) {
					readPacket(in);
				}

			} catch (IOException ioe) {
				Log.e(Constants.LOG_TAG,
						"WatchConnection.BlueToothListener(): error listening to socket",
						ioe);
				try {
					disconnect();
				} catch (IOException ioe2) {
					/* Don't care. */
				}
			}
		}

		private void readPacket(InputStream in) throws IOException {
			byte[] bytes = new byte[256];
			in.read(bytes);

			/* We've received some bytes. Wake the phone up. */
			PowerManager powerManager = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			WakeLock wakeLock = powerManager.newWakeLock(
					PowerManager.PARTIAL_WAKE_LOCK, Constants.LOG_TAG);
			wakeLock.acquire();
			try {
				WatchPacket packet = PacketReader.readPacket(bytes);
				Log.d(Constants.LOG_TAG,
						"WatchConnection.BluetoothListener.readPacket(mac="
								+ macAddress + "): Received " + packet);
				switch (packet.getMessageType()) {

				case GetDeviceTypeResponse:
					/* The watch has informed us what kind it is. */
					GetDeviceTypeResponse devType = (GetDeviceTypeResponse) packet;
					watchType = devType.getWatchType();
					switch (watchType) {
					case Analog:
					case AnalogDevelopmentBoard:
						watchRenderer = new AnalogWatchRenderer();
						break;
					case Digital:
					case DigitalDevelopmentBoard:
						watchRenderer = new DigitalWatchRenderer();
						break;
					default:
						throw new IllegalStateException(
								"Can't create renderer -- unknown watch type!");
					}
					broadcastInfo();
					/* Request idle screen widgets from other apps. */
					context.sendBroadcast(new Intent(
							WatchIntentConstants.DISPLAY_IDLE_SCREEN_WIDGET_REQUEST));
					/*
					 * Now that we know what kind of watch it is, draw the idle
					 * screen.
					 */
					messageSendQueue.add(watchRenderer
							.renderIdleScreen(context));
					break;

				case ReadBatteryVoltageResponse:
					/* The watch has informed us what its battery level is. */
					ReadBatteryVoltageResponse batteryResp = (ReadBatteryVoltageResponse) packet;
					batteryVoltageAverage = batteryResp.getBatteryAverage();
					batteryIsCharging = batteryResp.isBatteryCharging();
					broadcastInfo();
					break;

				case StatusChangeEvent:
					/* The watch has informed us of a status change. */
					StatusChangeEvent event = (StatusChangeEvent) packet;
					if (event.getStatusChangeEventType() == StatusChangeEventType.SCROLL_REQUEST) {
						int newScrollBufferSize = 240 - event
								.getFreeScrollBufferBytes();
						scrollBufferSize = newScrollBufferSize;
						/*
						 * Wake up the notification sender waiting on the scroll
						 * buffer.
						 */
						synchronized (scrollBufferLock) {
							scrollBufferLock.notify();
						}
						Log.d(Constants.LOG_TAG,
								"WatchConnection.BluetoothListener.readPacket(): Reset scroll buffer size to "
										+ newScrollBufferSize);
					} else if (event.getStatusChangeEventType() == StatusChangeEventType.SCROLL_COMPLETE) {
						/* All done scrolling. */
						scrollBufferSize = 0;
						synchronized (scrollBufferLock) {
							scrollBufferLock.notify();
						}
					}
					break;

				case ButtonEventMessage:
					ButtonEventMessage buttonEventMessage = (ButtonEventMessage) packet;
					if (buttonEventMessage.getButtonMeaning() == WatchButtonMeaning.DISMISS_NOTIFICATION) {
						/* Unhook button and return to idle. */
						sendPacket(new DisableButton(context,
								WatchMode.NOTIFICATION, WatchButton.C,
								WatchButtonPressType.PRESS_AND_RELEASE));
						sendPacket(new UpdateLCDDisplay(WatchMode.IDLE));
						displayingNotification = false;
					}
					break;

				default:
					Log.d(Constants.LOG_TAG,
							"WatchConnection.BluetoothListener.readPacket(): Don't know what to do with this packet.");
					break;

				}
			} finally {
				/* We're done, the phone can go back to sleep now. */
				wakeLock.release();
			}
		}
	}

	private volatile BlockingQueue<byte[]>	packetSendQueue			= new LinkedBlockingQueue<byte[]>();
	private volatile boolean				packetSenderRunning		= false;
	private volatile boolean				displayingNotification	= false;

	private class PacketSender implements Runnable {
		public void run() {
			while (packetSenderRunning) {
				try {
					byte[] bytes = packetSendQueue.take();
					send(bytes);
					Thread.sleep(10);

				} catch (InterruptedException ie) {
					/*
					 * If we 've been interrupted , exit gracefully .
					 */
					Log.d(Constants.LOG_TAG,
							"WatchConnection.packetSender.run(): Sender thread was interrupted.");
					break;
				} catch (IOException e) {
					Log.e(Constants.LOG_TAG,
							"WatchConnection.packetSender.run(): Encountered an I/O error sending packet!");
					packetSendQueue.clear();
					break;
				}
			}
		}

		private void send(byte[] bytes) throws IOException {
			if (bytes == null)
				return;

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byteArrayOutputStream.write(bytes);
			byteArrayOutputStream.write(PacketUtils.generateCRC(bytes));

			OutputStream out = btSocket.getOutputStream();
			if (out == null)
				throw new IOException("OutputStream is null");

			out.write(byteArrayOutputStream.toByteArray());
			out.flush();

			// /* DEBUG */
			// String str = "sent: ";
			// byte[] b = byteArrayOutputStream.toByteArray();
			// for (int i = 0; i < b.length; i++) {
			// str += "0x"
			// + Integer.toString((b[i] & 0xff) + 0x100, 16)
			// .substring(1) + ", ";
			// }
			// Log.d(Constants.LOG_TAG, str);
		}
	};

	private Thread	packetSenderThread	= null;

	private synchronized void startPacketSender() {
		if (packetSenderRunning == false) {
			packetSenderRunning = true;
			packetSenderThread = new Thread(new PacketSender(), "PacketSender");
			packetSenderThread.setDaemon(true);
			packetSenderThread.start();
		}
	}

	private synchronized void stopPacketSender() {
		Log.d(Constants.LOG_TAG, "WatchConnection.stopPacketSender(): ");
		if (packetSenderRunning == true) {
			/* Stops thread gracefully */
			packetSenderRunning = false;
			/* Wakes up thread if it's sleeping on the queue */
			packetSenderThread.interrupt();
			/* Thread is dead, we can mark it for garbage collection. */
			packetSenderThread = null;
		}
	}

	private volatile BlockingQueue<WatchMessage>	messageSendQueue		= new LinkedBlockingQueue<WatchMessage>();
	private volatile boolean						messageSenderRunning	= false;
	private volatile int							scrollBufferSize		= 0;
	private static final int						MAX_SCROLL_BUFFER_SIZE	= 240;
	private final Object							scrollBufferLock		= new Object();

	private class MessageSender implements Runnable {
		public void run() {
			while (messageSenderRunning) {
				try {
					WatchMessage message = messageSendQueue.take();

					/* We've got a message, wake up the phone. */
					PowerManager powerManager = (PowerManager) context
							.getSystemService(Context.POWER_SERVICE);
					WakeLock wakeLock = powerManager.newWakeLock(
							PowerManager.PARTIAL_WAKE_LOCK, Constants.LOG_TAG);
					wakeLock.acquire();

					try {

						// TODO notification guard?
						for (WatchPacket packet : message.getPackets()) {

							boolean suppressPacket = false;

							/*
							 * Special case handling for OLED scroll buffer
							 * packets.
							 */
							if (packet instanceof WriteOLEDScrollBuffer) {
								WriteOLEDScrollBuffer scrollPacket = (WriteOLEDScrollBuffer) packet;
								if (scrollPacket.isStartScroll()) {
									scrollBufferSize = 0;
									Log.d(Constants.LOG_TAG,
											"WatchConnection.MessageSender.run(): Scrolling started.");
								}

								/* Update scroll buffer size. */
								scrollBufferSize += scrollPacket
										.getScrollBufferSize();
								Log.d(Constants.LOG_TAG,
										"WatchConnection.MessageSender.run(): scrollBufferSize="
												+ scrollBufferSize);
							}

							/*
							 * LCD only
							 */
							if (packet instanceof UpdateLCDDisplay) {
								UpdateLCDDisplay update = (UpdateLCDDisplay) packet;
								if (update.getWatchMode() == WatchMode.NOTIFICATION) {
									/*
									 * if we've just jumped into notification
									 * mode, hook the lower right button. We'll
									 * keep the notification on the screen until
									 */
									displayingNotification = true;
									EnableButton enableButton = new EnableButton(
											context,
											WatchMode.NOTIFICATION,
											WatchButton.C,
											WatchButtonPressType.PRESS_AND_RELEASE,
											WatchButtonMeaning.DISMISS_NOTIFICATION);
									sendPacket(enableButton);
								} else if (update.getWatchMode() == WatchMode.IDLE) {
									/*
									 * If we're currently displaying a
									 * notification, don't update the LCD screen
									 * right now.
									 */
									if (displayingNotification) {
										suppressPacket = true;
									}
								}
							}

							/* Send the packet unless it's been suppressed. */
							if (suppressPacket == false) {
								sendPacket(packet);
							}

							/*
							 * OLED only: If scroll buffer is full, wait until
							 * we receive a SCROLL_REQUEST status event change
							 * before sending more packets.
							 */
							if (scrollBufferSize >= MAX_SCROLL_BUFFER_SIZE
									- WriteOLEDScrollBuffer.DISPLAY_BUFFER_SIZE) {
								Log.d(Constants.LOG_TAG,
										"WatchConnection.MessageSender.run(): Scroll buffer nearly full, waiting to send more scroll packets...");
								synchronized (scrollBufferLock) {
									scrollBufferLock.wait(60000);
								}
							}

						}
					} finally {
						/* Release the wake lock. */
						wakeLock.release();
					}

				} catch (InterruptedException ie) {
					/*
					 * If we've been interrupted , exit gracefully .
					 */
					Log.d(Constants.LOG_TAG,
							"WatchConnection.MessageSender.run(): Sender thread was interrupted.");
					break;
				}
			}
		}
	};

	private Thread	messageSenderThread	= null;

	private synchronized void startMessageSender() {
		if (messageSenderRunning == false) {
			messageSenderRunning = true;
			messageSenderThread = new Thread(new MessageSender(),
					"messageSender");
			messageSenderThread.setDaemon(true);
			messageSenderThread.start();
		}
	}

	private synchronized void stopMessageSender() {
		Log.d(Constants.LOG_TAG, "WatchConnection.stopMessageSender(): ");
		if (messageSenderRunning == true) {
			/* Stops thread gracefully */
			messageSenderRunning = false;
			/* Wakes up thread if it's sleeping on the queue */
			messageSenderThread.interrupt();
			/* Thread is dead, we can mark it for garbage collection. */
			messageSenderThread = null;
		}
	}

	private Context					context					= null;
	private String					macAddress				= "";
	private String					name					= "";
	private WatchType				watchType				= null;
	private BluetoothAdapter		btAdapter				= null;
	private BluetoothDevice			btDevice				= null;
	private BluetoothSocket			btSocket				= null;
	private Thread					btListenerThread		= null;
	private WatchConnectionState	connectionState			= WatchConnectionState.Disconnected;
	private float					batteryVoltageAverage	= 0;
	private boolean					batteryIsCharging		= false;
	private WatchRenderer			watchRenderer			= null;

	public WatchConnection(Context context, String macAddress, String name) {
		this.context = context;
		this.macAddress = macAddress;
		this.name = name;
		/* We haven't done anything yet, but let the world know we're here. */
		broadcastInfo();
	}

	public String getMacAddress() {
		return macAddress;
	}

	/** Starts connection to watch if not already started. */
	public void start() {
		Log.d(Constants.LOG_TAG,
				"WatchConnection.start(): Starting connection. name='" + name
						+ "' mac='" + macAddress + "'");
		try {
			connect();
		} catch (IOException e) {
			try {
				disconnect();
			} catch (IOException e1) {
				/* Both connect and disconnect failed. Notify listeners. */
				connectionState = WatchConnectionState.Disconnected;
				broadcastInfo();
			}
			Log.e(Constants.LOG_TAG, "Error starting connection!", e);
		}
	}

	public void stop() {
		Log.d(Constants.LOG_TAG, "WatchConnection.stop(): Stopping connection.");
		try {
			disconnect();
		} catch (IOException e) {
			Log.e(Constants.LOG_TAG, "Error stopping connection!", e);
		}
	}

	public void sendPacket(WatchPacket packet) {
		packetSendQueue.add(packet.getBytes());
	}

	public void sendNotification(DisplayNotification req) {
		Log.d(Constants.LOG_TAG, "WatchConnection.sendNotification(): msq="
				+ messageSendQueue + " wr=" + watchRenderer);
		messageSendQueue.add(watchRenderer.renderNotification(context, req));
	}

	public void displayIdleScreenWidget(DisplayIdleScreenWidget req) {
		Log.d(Constants.LOG_TAG,
				"WatchConnection.displayIdleScreenWidget(): req="
						+ req.toString());
		watchRenderer.updateIdleScreenWidget(context, req);
		messageSendQueue.add(watchRenderer.renderIdleScreen(context));
	}

	private void connect() throws IOException {
		Log.d(Constants.LOG_TAG, "WatchConnection.connect(): ");
		if (connectionState != WatchConnectionState.Disconnected) {
			return;
		}

		/* Create Looper if necessary. */
		if (Looper.myLooper() == null) {
			Looper.prepare();
		}

		/* Notify listeners we're starting to connect. */
		connectionState = WatchConnectionState.Searching;
		broadcastInfo();

		/* Start connection. */
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		btDevice = btAdapter.getRemoteDevice(macAddress);
		btSocket = btDevice.createRfcommSocketToServiceRecord(DEFAULT_UUID);
		btSocket.connect();
		btListenerThread = new Thread(new BluetoothListener(),
				"Bluetooth Listener, mac=" + macAddress);
		btListenerThread.start();
		startPacketSender();
		startMessageSender();
		connectionState = WatchConnectionState.Connected;

		/* Notify listeners we've connected. */
		broadcastInfo();

		/* Tell the watch what time it is. */
		sendPacket(new SetRealTimeClock(context));

		/* Ask the watch what kind it is. */
		sendPacket(new GetDeviceType());

		/* Ask the watch what its battery level is. */
		sendPacket(new ReadBatteryVoltage());

	}

	private void disconnect() throws IOException {
		if (connectionState == WatchConnectionState.Disconnected) {
			return;
		}
		connectionState = WatchConnectionState.Disconnected;
		stopPacketSender();
		stopMessageSender();
		/* btListener will stop on its own when the socket is closed */
		btSocket.close();
		batteryIsCharging = false;
		batteryVoltageAverage = 0;
		broadcastInfo();
	}

	public WatchConnectionState getConnectionState() {
		return connectionState;
	}

	public void broadcastInfo() {
		WatchConnectionInfo info = new WatchConnectionInfo();
		info.macAddress = macAddress;
		info.name = name;
		info.watchType = watchType;
		info.connectionState = connectionState;
		info.batteryVoltage = batteryVoltageAverage;
		info.batteryIsCharging = batteryIsCharging;
		context.sendBroadcast(info.toIntent());
	}

}
