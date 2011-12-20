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
package org.metawatch.manager.core.packets.outgoing;

import org.metawatch.manager.core.packets.DefaultWatchPacket;
import org.metawatch.manager.core.packets.PacketConstants;
import org.metawatch.manager.core.packets.PacketConstants.MessageType;

import android.content.Context;

public class SetVibrateMode extends DefaultWatchPacket {

	public SetVibrateMode(Context context, int onDuration, int offDuration, int numberOfCycles) {
		
		initializeBytes(10);
		bytes[PacketConstants.MESSAGE_TYPE_BYTE_LOCATION] = MessageType.SetVibrateMode.msg;
		bytes[PacketConstants.OPTIONS_BYTE_LOCATION] = 0x00; // Unused

		bytes[4] = 0x01; // enabled
		bytes[5] = (byte) (onDuration % 256);
		bytes[6] = (byte) (onDuration / 256);
		bytes[7] = (byte) (offDuration % 256);
		bytes[8] = (byte) (offDuration / 256);
		bytes[9] = (byte) numberOfCycles;
	}

}
