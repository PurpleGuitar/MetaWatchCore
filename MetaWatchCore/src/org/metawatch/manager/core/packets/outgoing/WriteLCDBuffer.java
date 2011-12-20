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

public class WriteLCDBuffer extends DefaultWatchPacket {

	public WriteLCDBuffer(WatchMode watchMode,
			int line1Position, byte[] line1Data, int line2Position,
			byte[] line2Data) {

		initializeBytes(30);
		bytes[PacketConstants.MESSAGE_TYPE_BYTE_LOCATION] = MessageType.WriteLCDBuffer.msg;

		/*
		 * TODO We assume we always write both lines of data. In the future, we
		 * might want to support writing only one line of data. If we did, we'd
		 * need to write a 1 in bit 4 of the options byte, as per the spec.
		 */
		bytes[PacketConstants.OPTIONS_BYTE_LOCATION] += (byte) watchMode.value;

		bytes[4] = (byte) line1Position;
		System.arraycopy(line1Data, 0, bytes, 5, 12);

		bytes[4 + 13] = (byte) line2Position;
		System.arraycopy(line2Data, 0, bytes, 5 + 13, 12);

	}

}
