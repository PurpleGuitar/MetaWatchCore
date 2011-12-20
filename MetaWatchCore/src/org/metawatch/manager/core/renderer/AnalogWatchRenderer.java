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
package org.metawatch.manager.core.renderer;

import java.util.ArrayList;
import java.util.List;

import org.metawatch.manager.core.constants.Constants;
import org.metawatch.manager.core.lib.constants.WatchMode;
import org.metawatch.manager.core.lib.intents.DisplayIdleScreenWidget;
import org.metawatch.manager.core.lib.intents.DisplayNotification;
import org.metawatch.manager.core.packets.WatchPacket;
import org.metawatch.manager.core.packets.outgoing.WriteOLEDBuffer;
import org.metawatch.manager.core.packets.outgoing.WriteOLEDBuffer.OLEDPosition;
import org.metawatch.manager.core.packets.outgoing.WriteOLEDScrollBuffer;
import org.metawatch.manager.core.service.WatchMessage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

public class AnalogWatchRenderer extends DefaultWatchRenderer {

	@Override
	public WatchMessage renderNotification(Context context,
			DisplayNotification req) {

		Log.d(Constants.LOG_TAG,
				"AnalogWatchRenderer.renderNotification(): req=" + req);

		WatchMessage message = super.renderNotification(context, req);
		List<WatchPacket> packets = message.getPackets();

		/* Render top OLED. */
		if (req.oledTopText.length() > 0) {
			packets.addAll(convertDisplayDataToWriteOLEDBufferPackets(context,
					WatchMode.NOTIFICATION, OLEDPosition.TOP,
					renderOLED1Line(context, req.oledTopText)));
		} else if (req.oledTopLine1Text.length() > 0
				|| req.oledTopLine2Text.length() > 0) {
			packets.addAll(convertDisplayDataToWriteOLEDBufferPackets(
					context,
					WatchMode.NOTIFICATION,
					OLEDPosition.TOP,
					renderOled2lines(context, req.oledTopLine1Text,
							req.oledTopLine2Text)));
		}

		/* Render bottom OLED. */
		if (req.oledBottomText.length() > 0) {
			packets.addAll(convertDisplayDataToWriteOLEDBufferPackets(context,
					WatchMode.NOTIFICATION, OLEDPosition.BOTTOM,
					renderOLED1Line(context, req.oledBottomText)));
		} else if (req.oledBottomLine1Text.length() > 0
				|| req.oledBottomLine2Text.length() > 0) {
			packets.addAll(convertDisplayDataToWriteOLEDBufferPackets(
					context,
					WatchMode.NOTIFICATION,
					OLEDPosition.BOTTOM,
					renderOled2lines(context, req.oledBottomLine1Text,
							req.oledBottomLine2Text)));
		}

		/* Set watch to notification mode */
		packets.add(new WriteOLEDBuffer(context, WatchMode.NOTIFICATION,
				OLEDPosition.TOP, true, 0, null));
		packets.add(new WriteOLEDBuffer(context, WatchMode.NOTIFICATION,
				OLEDPosition.BOTTOM, true, 0, null));

		/* Generate scroll data. */
		if (req.oledBottomLine2Text.length() > 0) {
			byte[] scrollBuffer = renderOledScrollBuffer(context,
					req.oledBottomLine2Text);
			if (scrollBuffer == null) {
				Log.d(Constants.LOG_TAG,
						"AnalogWatchRenderer.renderNotification(): null scroll buffer");
			} else {
				Log.d(Constants.LOG_TAG,
						"AnalogWatchRenderer.renderNotification(): scroll buffer length="
								+ scrollBuffer.length);
				packets.addAll(convertDisplayDataToWriteOLEDScrollBufferPackets(scrollBuffer));
			}
		}

		return message;
	}

	@Override
	public WatchMessage renderIdleScreen(Context context) {
		// TODO Auto-generated method stub
		WatchMessage message = super.renderIdleScreen(context);
		List<WatchPacket> packets = message.getPackets();

		Paint paint = new Paint();
		paint.setColor(Color.BLACK);

		Bitmap topImage = Bitmap.createBitmap(80, 16, Bitmap.Config.RGB_565);
		Canvas topCanvas = new Canvas(topImage);
		topCanvas.drawColor(Color.WHITE);
		// topCanvas.drawLine(0, 0, 79, 15, paint);
		// topCanvas.drawLine(0, 15, 79, 0, paint);

		Bitmap bottomImage = Bitmap.createBitmap(80, 16, Bitmap.Config.RGB_565);
		Canvas bottomCanvas = new Canvas(bottomImage);
		bottomCanvas.drawColor(Color.WHITE);
		// bottomCanvas.drawLine(0, 0, 79, 15, paint);
		// bottomCanvas.drawLine(0, 15, 79, 0, paint);

		/* Draw widgets */
		Canvas canvas = topCanvas;
		int widgetX = 0;
		for (DisplayIdleScreenWidget req : widgets.values()) {
			Bitmap widgetBitmap = BitmapFactory.decodeByteArray(
					req.oledBitmapBytes, 0, req.oledBitmapBytes.length);
			canvas.drawBitmap(widgetBitmap, widgetX, 0, null);
			widgetX += 27;
			if (widgetX >= 80) {
				widgetX = 0;
				canvas = bottomCanvas;
			}
		}

		byte[] topDisplayData = createDisplayDataFromBitmap(topImage);
		packets.addAll(convertDisplayDataToWriteOLEDBufferPackets(context,
				WatchMode.IDLE, OLEDPosition.TOP, topDisplayData));
		byte[] bottomDisplayData = createDisplayDataFromBitmap(bottomImage);
		packets.addAll(convertDisplayDataToWriteOLEDBufferPackets(context,
				WatchMode.IDLE, OLEDPosition.BOTTOM, bottomDisplayData));

		return message;
	}

	private static byte[] createDisplayDataFromBitmap(Bitmap image) {
		int poleInt[] = new int[16 * 80];
		image.getPixels(poleInt, 0, 80, 0, 0, 80, 16);

		byte[] display = new byte[160];

		for (int i = 0; i < 160; i++) {
			boolean[] column = new boolean[8];
			for (int j = 0; j < 8; j++) {
				if (i < 80) {
					if (poleInt[80 * j + i] == Color.WHITE)
						column[j] = false;
					else
						column[j] = true;
				} else {
					if (poleInt[80 * 8 + 80 * j + i - 80] == Color.WHITE)
						column[j] = false;
					else
						column[j] = true;
				}
			}
			for (int j = 0; j < 8; j++) {
				if (column[j])
					display[i] += Math.pow(2, j);
			}
		}
		return display;
	}

	/** Renders a packed byte array containing the render of the given text. */
	private static byte[] renderOLED1Line(Context context, String text) {

		int offset = 0;
		Bitmap image = Bitmap.createBitmap(80, 16, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(image);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(16);
		Typeface typeface = Typeface.createFromAsset(context.getAssets(),
				"metawatch_16pt_11pxl.ttf");
		paint.setTypeface(typeface);
		canvas.drawColor(Color.WHITE);
		canvas.drawText(text, offset, 14, paint);

		return createDisplayDataFromBitmap(image);

	}

	private static byte[] renderOled2lines(Context context, String line1,
			String line2) {
		int offset = 0;

		/* Convert newlines to spaces */
		line1 = line1.replace('\n', ' ');
		line2 = line2.replace('\n', ' ');

		Bitmap image = Bitmap.createBitmap(80, 16, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(image);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(8);
		Typeface typeface = Typeface.createFromAsset(context.getAssets(),
				"metawatch_8pt_5pxl_CAPS.ttf");
		paint.setTypeface(typeface);
		canvas.drawColor(Color.WHITE);
		canvas.drawText(line1, offset, 7, paint);
		canvas.drawText(line2, offset, 15, paint);

		int poleInt[] = new int[16 * 80];
		image.getPixels(poleInt, 0, 80, 0, 0, 80, 16);

		return createDisplayDataFromBitmap(image);
	}

	private static byte[] renderOledScrollBuffer(Context context, String line) {

		int offset = 0 - 79;

		/* Replace newlines with spaces */
		line = line.replace('\n', ' ');

		final int width = 800;
		byte[] displayData = new byte[800];

		Bitmap image = Bitmap.createBitmap(width, 8, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(image);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(8);
		Typeface typeface = Typeface.createFromAsset(context.getAssets(),
				"metawatch_8pt_5pxl_CAPS.ttf");
		paint.setTypeface(typeface);
		canvas.drawColor(Color.WHITE);
		canvas.drawText(line, offset, 7, paint);

		int poleInt[] = new int[8 * width];
		image.getPixels(poleInt, 0, width, 0, 0, width, 8);

		for (int i = 0; i < width; i++) {
			boolean[] column = new boolean[8];
			for (int j = 0; j < 8; j++) {
				if (poleInt[width * j + i] == Color.WHITE)
					column[j] = false;
				else
					column[j] = true;
			}
			for (int j = 0; j < 8; j++) {
				if (column[j])
					displayData[i] += Math.pow(2, j);
			}
		}
		int pixelWidth = (int) paint.measureText(line) - 79;
		if (pixelWidth > 0) {
			/* Round up to next multiple of 20 */
			pixelWidth = pixelWidth + (20 - (pixelWidth % 20));
			byte[] resizedDisplayData = new byte[pixelWidth];
			System.arraycopy(displayData, 0, resizedDisplayData, 0, pixelWidth);
			return resizedDisplayData;
		} else {
			return null;
		}
	}

	private static List<WatchPacket> convertDisplayDataToWriteOLEDBufferPackets(
			Context context, WatchMode watchMode, OLEDPosition oledPosition,
			byte[] displayData) {
		List<WatchPacket> packets = new ArrayList<WatchPacket>();
		for (int startPos = 0; startPos < 160; startPos += 20) {
			packets.add(new WriteOLEDBuffer(context, watchMode, oledPosition,
					false, startPos, displayData));
		}
		return packets;
	}

	private static List<WatchPacket> convertDisplayDataToWriteOLEDScrollBufferPackets(
			byte[] displayData) {
		List<WatchPacket> packets = new ArrayList<WatchPacket>();
		for (int pos = 0; pos < displayData.length; pos += WriteOLEDScrollBuffer.DISPLAY_BUFFER_SIZE) {
			boolean scrollStart = (pos == 0);
			boolean scrollComplete = (pos
					+ WriteOLEDScrollBuffer.DISPLAY_BUFFER_SIZE >= displayData.length);
			byte[] displayDataChunk = new byte[WriteOLEDScrollBuffer.DISPLAY_BUFFER_SIZE];
			System.arraycopy(displayData, pos, displayDataChunk, 0,
					WriteOLEDScrollBuffer.DISPLAY_BUFFER_SIZE);
			packets.add(new WriteOLEDScrollBuffer(scrollStart, scrollComplete,
					displayDataChunk));
		}
		return packets;
	}
}
