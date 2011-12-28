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
package org.metawatch.manager.sms;

import org.metawatch.manager.core.lib.intents.DisplayIdleScreenWidget;
import org.metawatch.manager.core.lib.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

public class IdleScreenWidgetRenderer {

	private static String	ANALOG_LABEL	= "SMS";

	private static Typeface	typefaceSmall	= null;

	private static Typeface getSmallTypeface(Context context) {
		if (typefaceSmall == null) {
			typefaceSmall = Typeface.createFromAsset(context.getAssets(),
					"metawatch_8pt_5pxl_CAPS.ttf");
		}
		return typefaceSmall;
	}

	private static Bitmap	idleScreenBitmap	= null;

	private static Bitmap getIdleScreenBitmap(Context context) {
		if (idleScreenBitmap == null) {
			idleScreenBitmap = Utils.loadBitmapFromAssets(context,
					"idle_sms_lcd.bmp");
		}
		return idleScreenBitmap;
	}

	public static void sendIdleScreenWidgetUpdate(Context context) {
		int count = MetaWatchSMSService.getUnreadSmsCount(context);
		DisplayIdleScreenWidget req = new DisplayIdleScreenWidget();
		req.key = "org.metawatch.manager.sms";
		req.lcdBitmapBytes = renderLCDBitmap(context, count);
		req.oledBitmapBytes = renderOLEDBitmap(context, count);
		context.sendBroadcast(req.toIntent());
		Log.d(Constants.LOG_TAG,
				"IdleScreenWidgetRenderer.sendIdleScreenWidgetUpdate(): "
						+ req.toString());
	}

	private static byte[] renderLCDBitmap(Context context, int count) {
		String countString = String.valueOf(count);
		Bitmap lcdBitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(lcdBitmap);
		canvas.drawColor(Color.WHITE);
		Paint paintSmall = new Paint();
		paintSmall.setColor(Color.BLACK);
		paintSmall.setTextSize(8);
		paintSmall.setTypeface(getSmallTypeface(context));
		canvas.drawBitmap(getIdleScreenBitmap(context), 4, 0, null);
		int x = (int) (32 / 2 - paintSmall.measureText(countString) / 2);
		canvas.drawText(countString, x, 28, paintSmall);
		return Utils.compressBitmap(lcdBitmap);
	}

	private static byte[] renderOLEDBitmap(Context context, int count) {
		String countString = String.valueOf(count);
		Bitmap oledBitmap = Bitmap.createBitmap(26, 16, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(oledBitmap);
		canvas.drawColor(Color.WHITE);
		Paint paintSmall = new Paint();
		paintSmall.setColor(Color.BLACK);
		paintSmall.setTextSize(8);
		paintSmall.setTypeface(getSmallTypeface(context));
		int x = (int) (26 / 2 - paintSmall.measureText(ANALOG_LABEL) / 2);
		canvas.drawText(ANALOG_LABEL, x, 7, paintSmall);
		x = (int) (26 / 2 - paintSmall.measureText(countString) / 2);
		canvas.drawText(countString, x, 15, paintSmall);
		return Utils.compressBitmap(oledBitmap);
	}

}
