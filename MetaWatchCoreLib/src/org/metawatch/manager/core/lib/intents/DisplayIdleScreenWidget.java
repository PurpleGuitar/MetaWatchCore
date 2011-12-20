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
package org.metawatch.manager.core.lib.intents;

import android.content.Intent;

public class DisplayIdleScreenWidget {

	public static final String	DISPLAY_IDLE_SCREEN_WIDGET_INTENT_ACTION	= WatchIntentConstants.INTENT_PACKAGE
																					+ "."
																					+ "DISPLAY_IDLE_SCREEN_WIDGET";
	public static final String	KEY_EXTRA									= "key";
	public String				key											= "";
	public static final String	LCD_BITMAP_BYTES_EXTRA						= "lcdBitmapBytes";
	public byte[]				lcdBitmapBytes								= new byte[0];
	public static final String	OLED_BITMAP_BYTES_EXTRA						= "oledBitmapBytes";
	public byte[]				oledBitmapBytes								= new byte[0];

	public Intent toIntent() {
		Intent intent = new Intent(DISPLAY_IDLE_SCREEN_WIDGET_INTENT_ACTION);
		intent.putExtra(KEY_EXTRA, key);
		intent.putExtra(LCD_BITMAP_BYTES_EXTRA, lcdBitmapBytes);
		intent.putExtra(OLED_BITMAP_BYTES_EXTRA, oledBitmapBytes);
		return intent;
	}

	public static DisplayIdleScreenWidget fromIntent(Intent intent) {
		DisplayIdleScreenWidget request = new DisplayIdleScreenWidget();
		if (intent.getAction().equals(DISPLAY_IDLE_SCREEN_WIDGET_INTENT_ACTION) == false) {
			throw new IllegalArgumentException(
					"Not a DisplayIdleScreenWidgetRequest!");
		}
		if (intent.hasExtra(KEY_EXTRA)) {
			request.key = intent.getStringExtra(KEY_EXTRA);
		}
		if (intent.hasExtra(LCD_BITMAP_BYTES_EXTRA)) {
			request.lcdBitmapBytes = intent
					.getByteArrayExtra(LCD_BITMAP_BYTES_EXTRA);
		}
		if (intent.hasExtra(OLED_BITMAP_BYTES_EXTRA)) {
			request.oledBitmapBytes = intent
					.getByteArrayExtra(OLED_BITMAP_BYTES_EXTRA);
		}
		return request;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DisplayIdleScreenWidgetRequest");
		sb.append(" key='" + key + "'");
		return sb.toString();
	}
}
