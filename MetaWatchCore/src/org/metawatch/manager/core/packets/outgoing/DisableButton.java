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

import org.metawatch.manager.core.lib.constants.WatchButton;
import org.metawatch.manager.core.lib.constants.WatchButtonPressType;
import org.metawatch.manager.core.lib.constants.WatchMode;
import org.metawatch.manager.core.packets.DefaultWatchPacket;
import org.metawatch.manager.core.packets.PacketConstants;
import org.metawatch.manager.core.packets.PacketConstants.MessageType;

import android.content.Context;

public class DisableButton extends DefaultWatchPacket {

	public DisableButton(Context context, WatchMode watchMode, WatchButton watchButton, WatchButtonPressType watchButtonPressType) {
		
		initializeBytes(PacketConstants.PAYLOAD_START_BYTE_LOCATION + 3);
		bytes[PacketConstants.MESSAGE_TYPE_BYTE_LOCATION] = MessageType.DisableButtonMsg.msg;
		bytes[PacketConstants.OPTIONS_BYTE_LOCATION] = 0x00; // Reserved

		bytes[PacketConstants.PAYLOAD_START_BYTE_LOCATION + 0] = (byte) watchMode.value;
		bytes[PacketConstants.PAYLOAD_START_BYTE_LOCATION + 1] = (byte) watchButton.value;
		bytes[PacketConstants.PAYLOAD_START_BYTE_LOCATION + 2] = (byte) watchButtonPressType.value;
	}

}
