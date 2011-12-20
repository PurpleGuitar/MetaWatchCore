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

public class WriteOLEDScrollBuffer extends DefaultWatchPacket {

	private static int	SCROLL_COMPLETE			= 0x01;
	private static int	SCROLL_COMPLETE_INVERSE	= 0xFE;
	private static int	START_SCROLL			= 0x02;

	public static int	DISPLAY_BUFFER_SIZE		= 20;

	public WriteOLEDScrollBuffer(boolean startScroll, boolean scrollComplete,
			byte[] displayData) {

		if (displayData.length != DISPLAY_BUFFER_SIZE) {
			throw new IllegalArgumentException("displayData length must be "
					+ DISPLAY_BUFFER_SIZE);
		}

		initializeBytes(27);
		bytes[PacketConstants.MESSAGE_TYPE_BYTE_LOCATION] = MessageType.OledWriteScrollBufferMsg.msg;

		if (startScroll) {
			bytes[PacketConstants.OPTIONS_BYTE_LOCATION] |= START_SCROLL;
		}

		if (scrollComplete) {
			bytes[PacketConstants.OPTIONS_BYTE_LOCATION] |= SCROLL_COMPLETE;
		}

		bytes[4] = (byte) DISPLAY_BUFFER_SIZE;
		System.arraycopy(displayData, 0, bytes, 5, DISPLAY_BUFFER_SIZE);

	}

	public int getScrollBufferSize() {
		return bytes[4];
	}

	public void setScrollComplete(boolean scrollComplete) {
		if (scrollComplete) {
			bytes[PacketConstants.OPTIONS_BYTE_LOCATION] |= SCROLL_COMPLETE;
		} else {
			bytes[PacketConstants.OPTIONS_BYTE_LOCATION] &= SCROLL_COMPLETE_INVERSE;
		}
	}

	public boolean isStartScroll() {
		return (bytes[PacketConstants.OPTIONS_BYTE_LOCATION] & START_SCROLL) > 0;
	}

	public boolean isScrollComplete() {
		return (bytes[PacketConstants.OPTIONS_BYTE_LOCATION] & SCROLL_COMPLETE) > 0;
	}

}
