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

public class DefaultWatchPacket implements WatchPacket {

	protected byte[] bytes;
	
	protected byte[] initializeBytes (int length) {
		if (length > 30) {
			throw new IllegalArgumentException("Length can't be greater than 30 (32 - crc bytes)");
		}
		bytes = new byte[length];
		
		/* Start byte */
		bytes[PacketConstants.START_BYTE_LOCATION] = MessageType.start;
		
		/* Length of message */
		bytes[PacketConstants.LENGTH_BYTE_LOCATION] = (byte) (bytes.length+2); 
		
		return bytes;
	}
	
	public MessageType getMessageType() {
		if (bytes.length >= PacketConstants.MESSAGE_TYPE_BYTE_LOCATION) {
			return MessageType.getByID(bytes[PacketConstants.MESSAGE_TYPE_BYTE_LOCATION]);
		}
		return null;
	}
	
	@Override
	public byte[] getBytes() {
		return bytes;
	}

}
