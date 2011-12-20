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

package org.metawatch.manager.core.packets;

import org.metawatch.manager.core.packets.PacketConstants.MessageType;
import org.metawatch.manager.core.packets.incoming.GetDeviceTypeResponse;
import org.metawatch.manager.core.packets.incoming.ReadBatteryVoltageResponse;
import org.metawatch.manager.core.packets.incoming.StatusChangeEvent;
import org.metawatch.manager.core.packets.incoming.UnknownPacket;

public class PacketReader {

	public static WatchPacket readPacket(byte[] bytes) {

		MessageType msgType = MessageType
				.getByID(bytes[PacketConstants.MESSAGE_TYPE_BYTE_LOCATION]);

		switch (msgType) {
		
		case GetDeviceTypeResponse:
			return new GetDeviceTypeResponse(bytes);
			
		case ReadBatteryVoltageResponse:
			return new ReadBatteryVoltageResponse(bytes);
			
		case StatusChangeEvent:
			return new StatusChangeEvent(bytes);

		default:
			return new UnknownPacket(bytes);

		}

	}

}
