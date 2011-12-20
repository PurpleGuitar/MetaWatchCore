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

import org.metawatch.manager.core.lib.constants.WatchMode;
import org.metawatch.manager.core.packets.DefaultWatchPacket;
import org.metawatch.manager.core.packets.PacketConstants;
import org.metawatch.manager.core.packets.PacketConstants.MessageType;

import android.content.Context;

public class WriteOLEDBuffer extends DefaultWatchPacket {

	public enum OLEDPosition {
		TOP((byte) 0x00), BOTTOM((byte) 0x01);
		public final byte	value;

		OLEDPosition(byte value) {
			this.value = value;
		}
	}

	public WriteOLEDBuffer(Context context, WatchMode watchMode,
			OLEDPosition oledPosition, boolean activate, int startPos,
			byte[] displayData) {

		initializeBytes(27);
		bytes[PacketConstants.MESSAGE_TYPE_BYTE_LOCATION] = MessageType.OledWriteBufferMsg.msg;

		bytes[PacketConstants.OPTIONS_BYTE_LOCATION] += (byte) watchMode.value;
		if (activate) {
			bytes[PacketConstants.OPTIONS_BYTE_LOCATION] += 0x40; // activate
		}

		bytes[4] = oledPosition.value;

		if (displayData != null) {
			bytes[5] = (byte) (startPos);
			bytes[6] = 0x14; // size=20
			System.arraycopy(displayData, startPos, bytes, 7, 20);
		}
	}

}
