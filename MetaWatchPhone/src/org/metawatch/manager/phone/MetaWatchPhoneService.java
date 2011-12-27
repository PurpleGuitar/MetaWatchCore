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

import org.metawatch.manager.core.lib.intents.DisplayNotification;
import org.metawatch.manager.core.lib.utils.Utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MetaWatchPhoneService extends Service {

	/**
	 * From: http://stackoverflow.com/questions/3665183/broadcast-receiver-for-
	 * missed-call-in-android
	 */
	private class MissedCallsContentObserver extends ContentObserver {
		public MissedCallsContentObserver() {
			super(null);
		}

		@Override
		public void onChange(boolean selfChange) {

			// Cursor cursor = getContentResolver().query(Calls.CONTENT_URI,
			// null,
			// Calls.TYPE + " = ? AND " + Calls.NEW + " = ?",
			// new String[] { Integer.toString(Calls.MISSED_TYPE), "1" },
			// Calls.DATE + " DESC ");
			//
			// // this is the number of missed calls
			// // for your case you may need to track this number
			// // so that you can figure out when it changes
			// int missedCalls = cursor.getCount();

			IdleScreenWidgetRenderer
					.sendIdleScreenWidgetUpdate(MetaWatchPhoneService.this);

		}
	}

	private class MWPhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);

			if (incomingNumber == null)
				incomingNumber = "";

			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				Log.d(Constants.LOG_TAG,
						"MetaWatchPhoneService.MWPhoneStateListener.onCallStateChanged(): Phone is ringing!");
				onStartPhoneRinging(Utils.getContactNameFromNumber(
						MetaWatchPhoneService.this, incomingNumber),
						incomingNumber);
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				Log.d(Constants.LOG_TAG,
						"MetaWatchPhoneService.MWPhoneStateListener.onCallStateChanged(): Phone is idle.");
				// IdleScreenWidgetRenderer
				// .sendIdleScreenWidgetUpdate(MetaWatchPhoneService.this);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				Log.d(Constants.LOG_TAG,
						"MetaWatchPhoneService.MWPhoneStateListener.onCallStateChanged(): Phone is off the hook.");
				// IdleScreenWidgetRenderer
				// .sendIdleScreenWidgetUpdate(MetaWatchPhoneService.this);
				break;
			}
		}

	}

	private void onStartPhoneRinging(String name, String number) {
		DisplayNotification req = new DisplayNotification();

		req.vibrateOnDuration = 1000;
		req.vibrateOffDuration = 500;
		req.vibrateNumberOfCycles = 3;

		req.oledTopText = name;
		req.oledBottomText = number;

		req.lcdText = name + "\n\n" + number;

		sendBroadcast(req.toIntent());
	}

	public static int getMissedCallsCount(Context context) {
		int missed = 0;
		try {
			Cursor cursor = context.getContentResolver().query(
					android.provider.CallLog.Calls.CONTENT_URI, null, null,
					null, null);
			cursor.moveToFirst();

			while (true) {
				if (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)) == 3)
					missed += cursor.getInt(cursor
							.getColumnIndex(CallLog.Calls.NEW));

				if (cursor.isLast())
					break;

				cursor.moveToNext();
			}

		} catch (Exception x) {
		}
		Log.d(Constants.LOG_TAG,
				"MetaWatchPhoneService.getMissedCallsCount(): " + missed
						+ " missed calls.");
		return missed;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		/* Register for incoming calls */
		MWPhoneStateListener phoneListener = new MWPhoneStateListener();
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		int phoneEvents = PhoneStateListener.LISTEN_CALL_STATE;
		telephonyManager.listen(phoneListener, phoneEvents);

		/* Register for changes to missed calls */
		MissedCallsContentObserver mcco = new MissedCallsContentObserver();
		getApplicationContext().getContentResolver().registerContentObserver(
				Calls.CONTENT_URI, true, mcco);

		Log.d(Constants.LOG_TAG,
				"MetaWatchPhoneService.onCreate(): Service started.");
	}

}
