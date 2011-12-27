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

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class MetaWatchSMSService extends Service {

	private SMSObserver	smsObserver;

	public class SMSObserver extends ContentObserver {

		Context	context;
		int		lastObservedCount	= 0;

		public SMSObserver(Context context) {
			super(null);
			this.context = context;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			int count = getUnreadSmsCount(context);
			if (count != lastObservedCount) {
				Log.d(Constants.LOG_TAG,
						"SMSObserver.onChange(): Changed from "
								+ lastObservedCount + " to " + count
								+ ", refreshing idle screen.");
				lastObservedCount = count;
				IdleScreenWidgetRenderer.sendIdleScreenWidgetUpdate(context);
			}
		}
	}

	public static int getUnreadSmsCount(Context context) {
		int count = 0;
		Cursor cursor = context.getContentResolver().query(
				Uri.withAppendedPath(Uri.parse("content://sms"), "inbox"),
				new String[] { "_id" }, "read=0", null, null);
		if (cursor != null) {
			try {
				count = cursor.getCount();
			} finally {
				cursor.close();
			}
		}
		return count;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		smsObserver = new SMSObserver(this);
		Uri uri = Uri.parse("content://mms-sms/conversations/");
		ContentResolver contentResolver = getContentResolver();
		contentResolver.registerContentObserver(uri, true, smsObserver);
	}
	
}
