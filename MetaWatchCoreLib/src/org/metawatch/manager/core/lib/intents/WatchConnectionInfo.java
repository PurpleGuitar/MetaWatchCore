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

import org.metawatch.manager.core.lib.constants.WatchConnectionState;
import org.metawatch.manager.core.lib.constants.WatchType;

import android.content.Intent;

public class WatchConnectionInfo implements Comparable<WatchConnectionInfo> {

	public String				macAddress							= "";
	public String				name								= "";
	public WatchType			watchType							= null;
	public WatchConnectionState	connectionState						= WatchConnectionState.Disconnected;
	public float				batteryVoltage						= 0;
	public boolean				batteryIsCharging					= false;
	/*
	 * Intent actions
	 */
	public static final String	WATCH_CONNECTION_INFO_INTENT_ACTION	= WatchIntentConstants.INTENT_PACKAGE
																			+ "."
																			+ "WATCH_CONNECTION_INFO";
	/*
	 * Intent extra field names
	 */
	public static final String	MAC_ADDRESS_EXTRA_FIELD				= "macAddress";
	public static final String	NAME_EXTRA_FIELD					= "name";
	public static final String	WATCH_TYPE_EXTRA_FIELD				= "watchType";
	public static final String	CONNECTION_STATE_EXTRA_FIELD		= "connectionState";
	public static final String	BATTERY_VOLTAGE_EXTRA_FIELD			= "batteryVoltage";
	public static final String	BATTERY_CHARGING_EXTRA_FIELD		= "batteryCharging";

	public Intent toIntent() {
		Intent intent = new Intent(WATCH_CONNECTION_INFO_INTENT_ACTION);
		intent.putExtra(MAC_ADDRESS_EXTRA_FIELD, macAddress);
		intent.putExtra(NAME_EXTRA_FIELD, name);
		if (watchType != null) {
			intent.putExtra(WATCH_TYPE_EXTRA_FIELD, watchType.toString());
		}
		intent.putExtra(CONNECTION_STATE_EXTRA_FIELD,
				connectionState.toString());
		intent.putExtra(BATTERY_VOLTAGE_EXTRA_FIELD, batteryVoltage);
		intent.putExtra(BATTERY_CHARGING_EXTRA_FIELD, batteryIsCharging);
		return intent;
	}

	public static WatchConnectionInfo fromIntent(Intent intent) {
		WatchConnectionInfo info = new WatchConnectionInfo();
		if (intent.getAction().equals(WATCH_CONNECTION_INFO_INTENT_ACTION) == false) {
			throw new IllegalArgumentException(
					"Not a WatchConnectionInfo intent!");
		}
		if (intent.hasExtra(MAC_ADDRESS_EXTRA_FIELD)) {
			info.macAddress = intent.getStringExtra(MAC_ADDRESS_EXTRA_FIELD);
		}
		if (intent.hasExtra(NAME_EXTRA_FIELD)) {
			info.name = intent.getStringExtra(NAME_EXTRA_FIELD);
		}
		if (intent.hasExtra(WATCH_TYPE_EXTRA_FIELD)) {
			info.watchType = WatchType.valueOf(WatchType.class,
					intent.getStringExtra(WATCH_TYPE_EXTRA_FIELD));
		}
		if (intent.hasExtra(CONNECTION_STATE_EXTRA_FIELD)) {
			info.connectionState = WatchConnectionState.valueOf(intent
					.getStringExtra(CONNECTION_STATE_EXTRA_FIELD));
		}
		if (intent.hasExtra(BATTERY_VOLTAGE_EXTRA_FIELD)) {
			info.batteryVoltage = intent.getFloatExtra(
					BATTERY_VOLTAGE_EXTRA_FIELD, 0);
		}
		if (intent.hasExtra(BATTERY_CHARGING_EXTRA_FIELD)) {
			info.batteryIsCharging = intent.getBooleanExtra(
					BATTERY_CHARGING_EXTRA_FIELD, false);
		}
		return info;
	}

	@Override
	public int compareTo(WatchConnectionInfo that) {
		return this.macAddress.compareTo(that.macAddress);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("WatchConnectionInfo:");
		sb.append(" macAddress=" + macAddress);
		sb.append(" name=" + name);
		sb.append(" type=" + watchType);
		sb.append(" connectionState=" + connectionState);
		sb.append(" batteryVoltage=" + batteryVoltage);
		sb.append(" batteryIsCharging=" + batteryIsCharging);
		return sb.toString();
	}

}
