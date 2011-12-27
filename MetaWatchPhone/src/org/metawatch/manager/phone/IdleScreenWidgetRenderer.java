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
package org.metawatch.manager.phone;

import java.io.ByteArrayOutputStream;

import org.metawatch.manager.core.lib.intents.DisplayIdleScreenWidget;
import org.metawatch.manager.core.lib.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

public class IdleScreenWidgetRenderer {

	public static void sendIdleScreenWidgetUpdate(Context context) {
		int count = MetaWatchPhoneService.getMissedCallsCount(context);
		DisplayIdleScreenWidget req = new DisplayIdleScreenWidget();
		req.key = "org.metawatch.manager.phone";
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
		Typeface typefaceSmall = Typeface.createFromAsset(context.getAssets(), "metawatch_8pt_5pxl_CAPS.ttf");
		paintSmall.setTypeface(typefaceSmall);
		canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "idle_phone_lcd.bmp"),
				4, 0, null);
		int x = (int) (32/2-paintSmall.measureText(countString)/2);
		canvas.drawText(countString, x, 28, paintSmall);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		lcdBitmap.compress(CompressFormat.PNG, 0, bos);
		return bos.toByteArray();
	}

	private static byte[] renderOLEDBitmap(Context context, int count) {
		String countString = String.valueOf(count);
		Bitmap oledBitmap = Bitmap.createBitmap(26, 16, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(oledBitmap);
		canvas.drawColor(Color.WHITE);
		Paint paintSmall = new Paint();
		paintSmall.setColor(Color.BLACK);
		paintSmall.setTextSize(8);
		Typeface typefaceSmall = Typeface.createFromAsset(context.getAssets(), "metawatch_8pt_5pxl_CAPS.ttf");
		paintSmall.setTypeface(typefaceSmall);
		int x = (int) (26/2-paintSmall.measureText("Call")/2);
		canvas.drawText("Call", x, 7, paintSmall);
		x = (int) (26/2-paintSmall.measureText(countString)/2);
		canvas.drawText(countString, x, 15, paintSmall);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		oledBitmap.compress(CompressFormat.PNG, 0, bos);
		return bos.toByteArray();
	}

}
