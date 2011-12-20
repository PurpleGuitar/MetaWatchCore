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

import org.metawatch.manager.core.lib.constants.WatchMode;
import org.metawatch.manager.core.lib.intents.DisplayIdleScreenWidget;
import org.metawatch.manager.core.lib.intents.DisplayNotification;
import org.metawatch.manager.core.packets.WatchPacket;
import org.metawatch.manager.core.packets.outgoing.UpdateLCDDisplay;
import org.metawatch.manager.core.packets.outgoing.WriteLCDBuffer;
import org.metawatch.manager.core.service.WatchMessage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;

public class DigitalWatchRenderer extends DefaultWatchRenderer {

	@Override
	public WatchMessage renderNotification(Context context,
			DisplayNotification req) {
		WatchMessage message = super.renderNotification(context, req);

		/* Render text to bitmap */
		if (req.lcdText.length() > 0) {
			Bitmap bitmap = createTextBitmap(context, req.lcdText);
			message.getPackets().addAll(
					createPacketsFromBitmap(bitmap, WatchMode.NOTIFICATION));
		}

		/* Set watch mode to notification */
		message.getPackets().add(new UpdateLCDDisplay(WatchMode.NOTIFICATION));

		return message;
	}

	@Override
	public WatchMessage renderIdleScreen(Context context) {
		WatchMessage message = super.renderIdleScreen(context);
		List<WatchPacket> packets = message.getPackets();

		Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);

		// /* Paint 'X'. */
		// Paint paint = new Paint();
		// paint.setColor(Color.BLACK);
		// canvas.drawLine(0, 30, 95, 95, paint);
		// canvas.drawLine(95, 30, 0, 95, paint);

		/* Draw widgets */
		int widgetX = 0;
		int widgetY = 30;
		for (DisplayIdleScreenWidget req : widgets.values()) {
			Bitmap widgetBitmap = BitmapFactory.decodeByteArray(
					req.lcdBitmapBytes, 0, req.lcdBitmapBytes.length);
			canvas.drawBitmap(widgetBitmap, widgetX, widgetY, null);
			widgetX += 32;
			if (widgetX >= 96) {
				widgetX = 0;
				widgetY += 32;
			}
		}

		packets.addAll(createPacketsFromBitmap(bitmap, WatchMode.IDLE));
		packets.add(new UpdateLCDDisplay(WatchMode.IDLE));

		return message;
	}

	private static List<WatchPacket> createPacketsFromBitmap(Bitmap bitmap,
			WatchMode watchMode) {
		int pixelArray[] = new int[96 * 96];
		bitmap.getPixels(pixelArray, 0, 96, 0, 0, 96, 96);

		return createPacketsFromPixelArray(pixelArray, watchMode);
	}

	private static List<WatchPacket> createPacketsFromPixelArray(
			int[] pixelArray, WatchMode watchMode) {
		byte send[] = new byte[1152];

		for (int i = 0; i < 1152; i++) {
			int p[] = new int[8];

			for (int j = 0; j < 8; j++) {
				if (pixelArray[i * 8 + j] == Color.WHITE)
					/*
					 * if (Preferences.invertLCD) p[j] = 1; else
					 */
					p[j] = 0;
				else
					/*
					 * if (Preferences.invertLCD) p[j] = 0; else
					 */
					p[j] = 1;
			}
			send[i] = (byte) (p[7] * 128 + p[6] * 64 + p[5] * 32 + p[4] * 16
					+ p[3] * 8 + p[2] * 4 + p[1] * 2 + p[0] * 1);
		}
		return createPacketsFromDisplayBuffer(send, watchMode);
	}

	private static List<WatchPacket> createPacketsFromDisplayBuffer(
			byte[] buffer, WatchMode watchMode) {

		List<WatchPacket> packets = new ArrayList<WatchPacket>();

		int rowNum = 0;
		if (watchMode == WatchMode.IDLE) {
			rowNum = 30;
		}

		/*
		 * Each packet contains two rows of data, so we prepare them two at a
		 * time.
		 */
		for (; rowNum < 96; rowNum += 2) {
			/* Create line 1 data */
			byte[] line1Data = new byte[12];
			for (int j = 0; j < 12; j++)
				line1Data[j] = buffer[rowNum * 12 + j];

			byte[] line2Data = new byte[12];
			for (int j = 0; j < 12; j++)
				line2Data[j] = buffer[rowNum * 12 + j + 12];

			packets.add(new WriteLCDBuffer(watchMode, rowNum, line1Data,
					rowNum + 1, line2Data));
		}
		return packets;
	}

	private static Bitmap createTextBitmap(Context context, String text) {

		String font = "metawatch_8pt_7pxl_CAPS.ttf";
		int size = 8;

		Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(size);
		Typeface typeface = Typeface.createFromAsset(context.getAssets(), font);
		paint.setTypeface(typeface);
		canvas.drawColor(Color.WHITE);
		canvas = breakText(canvas, text, paint, 0, 0);
		/*
		 * FileOutputStream fos = new FileOutputStream("/sdcard/test.png");
		 * image.compress(Bitmap.CompressFormat.PNG, 100, fos); fos.close();
		 * Log.d("ow", "bmp ok");
		 */
		return bitmap;
	}

	private static Canvas breakText(Canvas canvas, String text, Paint pen,
			int x, int y) {
		TextPaint textPaint = new TextPaint(pen);
		StaticLayout staticLayout = new StaticLayout(text, textPaint, 98,
				android.text.Layout.Alignment.ALIGN_NORMAL, 1.3f, 0, false);
		canvas.translate(x, y); // position the text
		staticLayout.draw(canvas);
		return canvas;
	}

}
