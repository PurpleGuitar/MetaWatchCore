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

import org.metawatch.manager.core.lib.intents.DisplayNotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;
import android.util.Log;

public class IntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
			Bundle bundle = intent.getExtras();
			if (bundle.containsKey("pdus")) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] smsMessage = new SmsMessage[pdus.length];
				for (int i = 0; i < smsMessage.length; i++) {
					smsMessage[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					String number = smsMessage[i].getOriginatingAddress();
					String body = smsMessage[i].getDisplayMessageBody();

					/* Attempt to resolve contact name */
					number = getContactNameFromNumber(context, number);

					// NotificationBuilder.createSMS(context, number, body);
					Log.d(Constants.LOG_TAG,
							"IntentReceiver.onReceive(): Received SMS: number='"
									+ number + "' body='" + body + "'");

					DisplayNotification req = new DisplayNotification();

					req.vibrateOnDuration = 500;
					req.vibrateOffDuration = 500;
					req.vibrateNumberOfCycles = 3;

					req.oledTopText = "SMS";
					req.oledBottomLine1Text = number;
					req.oledBottomLine2Text = body;

					req.lcdText = "SMS: " + number + "\n" + body;

					context.sendBroadcast(req.toIntent());
				}
			}
			return;
		} else if (action
				.equals("org.metawatch.manager.core.DISPLAY_IDLE_SCREEN_WIDGET_REQUEST")) {
			context.startService(new Intent(context, MetaWatchSMSService.class));
			IdleScreenWidgetRenderer.sendIdleScreenWidgetUpdate(context);
		}
	}

	public static String getContactNameFromNumber(Context context, String number) {

		if (number.equals(""))
			return "Private number";

		String[] projection = new String[] { PhoneLookup.DISPLAY_NAME,
				PhoneLookup.NUMBER };
		Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		Cursor c = context.getContentResolver().query(contactUri, projection,
				null, null, null);

		if (c.moveToFirst()) {
			String name = c.getString(c
					.getColumnIndex(PhoneLookup.DISPLAY_NAME));

			if (name.length() > 0)
				return name;
			else
				return number;
		}

		return number;
	}
}
