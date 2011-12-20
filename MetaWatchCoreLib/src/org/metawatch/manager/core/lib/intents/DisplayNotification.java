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

public class DisplayNotification {

	public static final String	DISPLAY_NOTIFICAION_REQUEST_INTENT_ACTION	= WatchIntentConstants.INTENT_PACKAGE
																					+ "."
																					+ "DISPLAY_NOTIFICATION_REQUEST";
	/* Vibration */
	public static final String	VIBRATE_ON_DURATION_EXTRA					= "vibrateOnDuration";
	public static final String	VIBRATE_OFF_DURATION_EXTRA					= "vibrateOffDuration";
	public static final String	VIBRATE_NUMBER_OF_CYCLES_EXTRA				= "vibrateNumberOfCycles";
	public int					vibrateOnDuration							= 0;
	public int					vibrateOffDuration							= 0;
	public int					vibrateNumberOfCycles						= 0;

	/* OLED settings */
	public static final String	OLED_TOP_TEXT_EXTRA							= "oledTopText";
	public static final String	OLED_TOP_LINE1_TEXT_EXTRA					= "oledTopLine1Text";
	public static final String	OLED_TOP_LINE2_TEXT_EXTRA					= "oledTopLine2Text";
	public static final String	OLED_BOTTOM_TEXT_EXTRA						= "oledBottomText";
	public static final String	OLED_BOTTOM_LINE1_TEXT_EXTRA				= "oledBottomLine1Text";
	public static final String	OLED_BOTTOM_LINE2_TEXT_EXTRA				= "oledBottomLine2Text";
	public String				oledTopText									= "";
	public String				oledTopLine1Text							= "";
	public String				oledTopLine2Text							= "";
	public String				oledBottomText								= "";
	public String				oledBottomLine1Text							= "";
	public String				oledBottomLine2Text							= "";

	/* LCD settings */
	public static final String	LCD_TEXT_EXTRA								= "lcdText";
	public String				lcdText										= "";

	public Intent toIntent() {
		Intent intent = new Intent(DISPLAY_NOTIFICAION_REQUEST_INTENT_ACTION);
		intent.putExtra(VIBRATE_ON_DURATION_EXTRA, vibrateOnDuration);
		intent.putExtra(VIBRATE_OFF_DURATION_EXTRA, vibrateOffDuration);
		intent.putExtra(VIBRATE_NUMBER_OF_CYCLES_EXTRA, vibrateNumberOfCycles);
		intent.putExtra(OLED_TOP_TEXT_EXTRA, oledTopText);
		intent.putExtra(OLED_TOP_LINE1_TEXT_EXTRA, oledTopLine1Text);
		intent.putExtra(OLED_TOP_LINE2_TEXT_EXTRA, oledTopLine2Text);
		intent.putExtra(OLED_BOTTOM_TEXT_EXTRA, oledBottomText);
		intent.putExtra(OLED_BOTTOM_LINE1_TEXT_EXTRA, oledBottomLine1Text);
		intent.putExtra(OLED_BOTTOM_LINE2_TEXT_EXTRA, oledBottomLine2Text);
		intent.putExtra(LCD_TEXT_EXTRA, lcdText);
		return intent;
	}

	public static DisplayNotification fromIntent(Intent intent) {
		DisplayNotification request = new DisplayNotification();
		if (intent.getAction()
				.equals(DISPLAY_NOTIFICAION_REQUEST_INTENT_ACTION) == false) {
			throw new IllegalArgumentException(
					"Not a DisplayNotificationRequest!");
		}
		if (intent.hasExtra(VIBRATE_ON_DURATION_EXTRA)) {
			request.vibrateOnDuration = intent.getIntExtra(
					VIBRATE_ON_DURATION_EXTRA, 0);
		}
		if (intent.hasExtra(VIBRATE_OFF_DURATION_EXTRA)) {
			request.vibrateOffDuration = intent.getIntExtra(
					VIBRATE_OFF_DURATION_EXTRA, 0);
		}
		if (intent.hasExtra(VIBRATE_NUMBER_OF_CYCLES_EXTRA)) {
			request.vibrateNumberOfCycles = intent.getIntExtra(
					VIBRATE_NUMBER_OF_CYCLES_EXTRA, 0);
		}
		if (intent.hasExtra(OLED_TOP_TEXT_EXTRA)) {
			request.oledTopText = intent.getStringExtra(OLED_TOP_TEXT_EXTRA);
		}
		if (intent.hasExtra(OLED_TOP_LINE1_TEXT_EXTRA)) {
			request.oledTopLine1Text = intent
					.getStringExtra(OLED_TOP_LINE1_TEXT_EXTRA);
		}
		if (intent.hasExtra(OLED_TOP_LINE2_TEXT_EXTRA)) {
			request.oledTopLine2Text = intent
					.getStringExtra(OLED_TOP_LINE2_TEXT_EXTRA);
		}
		if (intent.hasExtra(OLED_BOTTOM_TEXT_EXTRA)) {
			request.oledBottomText = intent
					.getStringExtra(OLED_BOTTOM_TEXT_EXTRA);
		}
		if (intent.hasExtra(OLED_BOTTOM_LINE1_TEXT_EXTRA)) {
			request.oledBottomLine1Text = intent
					.getStringExtra(OLED_BOTTOM_LINE1_TEXT_EXTRA);
		}
		if (intent.hasExtra(OLED_BOTTOM_LINE2_TEXT_EXTRA)) {
			request.oledBottomLine2Text = intent
					.getStringExtra(OLED_BOTTOM_LINE2_TEXT_EXTRA);
		}
		if (intent.hasExtra(LCD_TEXT_EXTRA)) {
			request.lcdText = intent.getStringExtra(LCD_TEXT_EXTRA);
		}
		return request;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DisplayNotificationRequest");
		sb.append(" " + VIBRATE_ON_DURATION_EXTRA + "=" + vibrateOnDuration);
		sb.append(" " + VIBRATE_OFF_DURATION_EXTRA + "=" + vibrateOffDuration);
		sb.append(" " + VIBRATE_NUMBER_OF_CYCLES_EXTRA + "="
				+ vibrateNumberOfCycles);
		sb.append(" " + OLED_TOP_TEXT_EXTRA + "=" + oledTopText);
		sb.append(" " + OLED_TOP_LINE1_TEXT_EXTRA + "=" + oledTopLine1Text);
		sb.append(" " + OLED_TOP_LINE2_TEXT_EXTRA + "=" + oledTopLine2Text);
		sb.append(" " + OLED_BOTTOM_TEXT_EXTRA + "=" + oledBottomText);
		sb.append(" " + OLED_BOTTOM_LINE1_TEXT_EXTRA + "="
				+ oledBottomLine1Text);
		sb.append(" " + OLED_BOTTOM_LINE2_TEXT_EXTRA + "="
				+ oledBottomLine2Text);
		sb.append(" " + LCD_TEXT_EXTRA + "=" + lcdText);
		return sb.toString();
	}
}
