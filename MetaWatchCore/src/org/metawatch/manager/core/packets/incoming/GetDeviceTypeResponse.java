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

import org.metawatch.manager.core.lib.constants.WatchType;
import org.metawatch.manager.core.packets.DefaultWatchPacket;
import org.metawatch.manager.core.packets.PacketConstants;

public class GetDeviceTypeResponse extends DefaultWatchPacket {

	public GetDeviceTypeResponse(byte[] bytes) {
		this.bytes = bytes;
	}
	
	public WatchType getWatchType() {
		return WatchType.getWatchByType(bytes[PacketConstants.PAYLOAD_START_BYTE_LOCATION]);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("GetDeviceTypeResponse: type=");
		sb.append(getWatchType());		
		return sb.toString();
	}

}
