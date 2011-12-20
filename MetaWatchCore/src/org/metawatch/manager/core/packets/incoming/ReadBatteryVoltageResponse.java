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

package org.metawatch.manager.core.packets.incoming;

import org.metawatch.manager.core.packets.DefaultWatchPacket;

public class ReadBatteryVoltageResponse extends DefaultWatchPacket {

	public ReadBatteryVoltageResponse(byte[] bytes) {
		this.bytes = bytes;
	}
	
	public boolean isPowerGood() {
		return bytes[4] > 0;
	}
	
	public boolean isBatteryCharging() {
		return bytes[5] > 0;
	}
	
	public float getBatterySense() {
		return (((int) bytes[7] << 8) + (int) bytes[6]) / 1000.0f;
	}
	
	public float getBatteryAverage() {
		return (((int) bytes[9] << 8) + (int) bytes[8]) / 1000.0f;	
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ReadBatteryVoltageResponse:");
		sb.append(" powerGood=" + isPowerGood());
		sb.append(" batteryCharging=" + isBatteryCharging());
		sb.append(" batterySense=" + getBatterySense() + "V");
		sb.append(" batteryAverage=" + getBatteryAverage() + "V");
		return sb.toString();
	}

}
