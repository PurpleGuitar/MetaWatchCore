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

public class RefreshConnections {

	public static final String	REFRESH_CONNECTIONS_INTENT_ACTION	= WatchIntentConstants.INTENT_PACKAGE
																			+ ".REFRESH_CONNECTIONS";

	public static final String	POLL_BATTERY_EXTRA					= "pollBattery";
	public boolean				refreshBattery						= false;
	public static final String	REFRESH_IDLE_SCREEN_EXTRA			= "refreshIdleScreen";
	public boolean				refreshIdleScreen					= false;

	public Intent toIntent() {
		Intent intent = new Intent(REFRESH_CONNECTIONS_INTENT_ACTION);
		intent.putExtra(POLL_BATTERY_EXTRA, refreshBattery);
		intent.putExtra(REFRESH_IDLE_SCREEN_EXTRA, refreshIdleScreen);
		return intent;
	}

	public static RefreshConnections fromIntent(Intent intent) {
		RefreshConnections request = new RefreshConnections();
		if (intent.getAction().equals(REFRESH_CONNECTIONS_INTENT_ACTION) == false) {
			throw new IllegalArgumentException(
					"Not a RefreshConnectionsRequest!");
		}
		if (intent.hasExtra(POLL_BATTERY_EXTRA)) {
			request.refreshBattery = intent.getBooleanExtra(POLL_BATTERY_EXTRA,
					false);
		}
		if (intent.hasExtra(REFRESH_IDLE_SCREEN_EXTRA)) {
			request.refreshIdleScreen = intent.getBooleanExtra(
					REFRESH_IDLE_SCREEN_EXTRA, false);
		}
		return request;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RefreshConnectionsRequest");
		sb.append(" " + POLL_BATTERY_EXTRA + "=" + refreshBattery);
		sb.append(" " + REFRESH_IDLE_SCREEN_EXTRA + "=" + refreshIdleScreen);
		return sb.toString();
	}

}
