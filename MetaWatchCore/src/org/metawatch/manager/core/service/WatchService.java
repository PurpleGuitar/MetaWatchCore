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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.metawatch.manager.core.constants.Constants;
import org.metawatch.manager.core.db.WatchDBHelper;
import org.metawatch.manager.core.db.WatchTable;
import org.metawatch.manager.core.lib.constants.WatchConnectionState;
import org.metawatch.manager.core.lib.intents.DisplayIdleScreenWidget;
import org.metawatch.manager.core.lib.intents.DisplayNotification;
import org.metawatch.manager.core.lib.intents.RefreshConnections;
import org.metawatch.manager.core.lib.intents.WatchConnectionInfo;
import org.metawatch.manager.core.lib.intents.WatchIntentConstants;
import org.metawatch.manager.core.packets.outgoing.ReadBatteryVoltage;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class WatchService extends Service {

	private static final String				SERVICE_NAME					= Constants.APP_PACKAGE
																					+ ".service.WatchService";
	private static final int				DELAY_BETWEEN_AUTO_REFRESH		= 5 * 60 * 1000;

	private List<WatchConnection>			connections						= Collections
																					.synchronizedList(new ArrayList<WatchConnection>());
	private SQLiteDatabase					db								= null;
	private WatchServiceBroadcastReceiver	watchServiceBroadcastReceiver	= new WatchServiceBroadcastReceiver();
	private static Object					connectionRefreshLock			= new Object();
	private PendingIntent					refreshPendingIntent;

	private class WatchServiceBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					RefreshConnections.REFRESH_CONNECTIONS_INTENT_ACTION)) {
				RefreshConnections req = RefreshConnections.fromIntent(intent);
				refreshConnectionsInBackground(req);
			} else if (intent.getAction().equals(
					WatchIntentConstants.CONNECT_INTENT_ACTION)) {
				String mac = intent
						.getStringExtra(WatchConnectionInfo.MAC_ADDRESS_EXTRA_FIELD);
				new Thread(new WatchConnector(mac), "WatchConnector").start();
			} else if (intent.getAction().equals(
					WatchIntentConstants.DISCONNECT_INTENT_ACTION)) {
				String mac = intent
						.getStringExtra(WatchConnectionInfo.MAC_ADDRESS_EXTRA_FIELD);
				new Thread(new WatchDisconnector(mac), "WatchDisconnector")
						.start();
			} else if (intent
					.getAction()
					.equals(DisplayNotification.DISPLAY_NOTIFICAION_REQUEST_INTENT_ACTION)) {
				DisplayNotification req = DisplayNotification
						.fromIntent(intent);
				for (WatchConnection connection : connections) {
					if (connection.getConnectionState() == WatchConnectionState.Connected) {
						connection.sendNotification(req);
					}
				}
			} else if (intent
					.getAction()
					.equals(DisplayIdleScreenWidget.DISPLAY_IDLE_SCREEN_WIDGET_INTENT_ACTION)) {
				DisplayIdleScreenWidget req = DisplayIdleScreenWidget
						.fromIntent(intent);
				for (WatchConnection connection : connections) {
					if (connection.getConnectionState() == WatchConnectionState.Connected) {
						connection.displayIdleScreenWidget(req);
					}
				}
			}
		}
	}

	private class WatchConnector implements Runnable {
		private String	mac;

		public WatchConnector(String mac) {
			this.mac = mac;
		}

		@Override
		public void run() {
			Looper.prepare();
			WatchConnection connection = getConnectionByMAC(mac);
			connection.start();
		}
	}

	private class WatchDisconnector implements Runnable {
		private String	mac;

		public WatchDisconnector(String mac) {
			this.mac = mac;
		}

		@Override
		public void run() {
			Looper.prepare();
			WatchConnection connection = getConnectionByMAC(mac);
			connection.stop();
		}
	}

	private class ConnectionRefresher implements Runnable {
		private RefreshConnections	req;

		public ConnectionRefresher(RefreshConnections req) {
			this.req = req;
		}

		@Override
		public void run() {

			/* Prepare notification handler */
			Looper.prepare();

			/* Make sure we don't run two refreshes at the same time. */
			synchronized (connectionRefreshLock) {

				Log.d(Constants.LOG_TAG,
						"WatchService.ConnectionRefresher.run(): Received refresh request. "
								+ req);

				/* Create connection to database if necessary. */
				if (db == null) {
					WatchDBHelper dbHelper = new WatchDBHelper(
							WatchService.this);
					db = dbHelper.getReadableDatabase();
				}

				/* Read all connections in database. */
				List<WatchConnection> dbConnections = new ArrayList<WatchConnection>();
				List<WatchConnection> activeConnections = new ArrayList<WatchConnection>();
				Cursor cursor = WatchTable.getAllWatches(db);
				while (cursor.moveToNext()) {
					String mac = cursor.getString(cursor
							.getColumnIndex(WatchTable.COLUMN_MAC));
					String name = cursor.getString(cursor
							.getColumnIndex(WatchTable.COLUMN_NAME));
					boolean active = cursor.getInt(cursor
							.getColumnIndex(WatchTable.COLUMN_ACTIVE)) > 0;
					WatchConnection connection = getConnectionByMAC(mac);
					if (connection == null) {
						Log.d(Constants.LOG_TAG,
								"WatchService.ConnectionRefresher.run(): Found new connection definition in db, adding to connections.");
						connection = new WatchConnection(WatchService.this,
								mac, name);
						connections.add(connection);
					}
					dbConnections.add(connection);
					if (active) {
						activeConnections.add(connection);
					}
					connection.broadcastInfo();
				}
				cursor.close();

				/* Throw away any connections no longer in database. */
				for (Iterator<WatchConnection> iterator = connections
						.iterator(); iterator.hasNext();) {
					WatchConnection connection = iterator.next();
					if (dbConnections.contains(connection) == false) {
						Log.d(Constants.LOG_TAG,
								"WatchService.ConnectionRefresher.run(): Found connection no longer in database, stopping if necessary.");
						if (connection.getConnectionState() != WatchConnectionState.Disconnected) {
							connection.stop();
						}
						iterator.remove();
					}
				}

				/* Try to start active connections that aren't connected. */
				for (WatchConnection connection : activeConnections) {
					if (connection.getConnectionState() != WatchConnectionState.Connected) {
						Log.d(Constants.LOG_TAG,
								"WatchService.ConnectionRefresher.run(): Found active connection that's not connected, starting.");
						connection.start();
					}
				}

				/* Request battery voltage from connected watches. */
				if (req.refreshBattery) {
					for (WatchConnection connection : connections) {
						if (connection.getConnectionState() == WatchConnectionState.Connected) {
							connection.sendPacket(new ReadBatteryVoltage());
						}
					}
				}

				/* Request refresh of idle screens. */
				if (req.refreshIdleScreen) {
					WatchService.this
							.sendBroadcast(new Intent(
									WatchIntentConstants.DISPLAY_IDLE_SCREEN_WIDGET_REQUEST));
				}
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		registerReceiver(watchServiceBroadcastReceiver, new IntentFilter(
				RefreshConnections.REFRESH_CONNECTIONS_INTENT_ACTION));
		registerReceiver(watchServiceBroadcastReceiver, new IntentFilter(
				WatchIntentConstants.CONNECT_INTENT_ACTION));
		registerReceiver(watchServiceBroadcastReceiver, new IntentFilter(
				WatchIntentConstants.DISCONNECT_INTENT_ACTION));
		registerReceiver(watchServiceBroadcastReceiver, new IntentFilter(
				DisplayNotification.DISPLAY_NOTIFICAION_REQUEST_INTENT_ACTION));
		registerReceiver(
				watchServiceBroadcastReceiver,
				new IntentFilter(
						DisplayIdleScreenWidget.DISPLAY_IDLE_SCREEN_WIDGET_INTENT_ACTION));
		refreshConnectionsInBackground(new RefreshConnections());

		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		refreshPendingIntent = PendingIntent.getBroadcast(this, 0,
				new RefreshConnections().toIntent(),
				PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + DELAY_BETWEEN_AUTO_REFRESH,
				DELAY_BETWEEN_AUTO_REFRESH, refreshPendingIntent);

		Log.d(Constants.LOG_TAG,
				"MetaWatchService.onCreate(): Service created.");
	}

	@Override
	public void onStart(Intent intent, int startId) {
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.cancel(refreshPendingIntent);
		if (db != null) {
			db.close();
		}
		unregisterReceiver(watchServiceBroadcastReceiver);
		Log.d(Constants.LOG_TAG,
				"MetaWatchService.onDestroy(): Service destroyed.");
	}

	/** We run continuously, not on bind, so we ignore this method. */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private WatchConnection getConnectionByMAC(String mac) {
		for (WatchConnection connection : connections) {
			if (connection.getMacAddress().equals(mac)) {
				return connection;
			}
		}
		return null;
	}

	private void refreshConnectionsInBackground(RefreshConnections req) {
		new Thread(new ConnectionRefresher(req),
				"WatchService.ConnectionRefresher").start();
	}

	public static boolean isRunning(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (SERVICE_NAME.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static void startServiceIfNecessary(Context context) {
		if (isRunning(context)) {
			Log.d(Constants.LOG_TAG,
					"MetaWatchService.startServiceIfNecessary(): Service is already running.");
		} else {
			Log.d(Constants.LOG_TAG,
					"MetaWatchService.startServiceIfNecessary(): Service doesn't seem to be running; starting it now.");
			context.startService(new Intent(context, WatchService.class));
		}
	}

}
