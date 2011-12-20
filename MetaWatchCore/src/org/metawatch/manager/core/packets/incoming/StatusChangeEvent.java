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

public class StatusChangeEvent extends DefaultWatchPacket {

	public enum StatusChangeEventType {
		MODE_CHANGE((byte) 0x01), DISPLAY_TIMEOUT((byte) 0x02), SCROLL_COMPLETE(
				(byte) 0x10), SCROLL_REQUEST((byte) 0x11);
		public final byte	value;

		public static StatusChangeEventType fromValue(byte value) {
			for (StatusChangeEventType scet : values()) {
				if (scet.value == value) {
					return scet;
				}
			}
			return null;
		}

		private StatusChangeEventType(byte value) {
			this.value = value;
		}
	}

	public StatusChangeEvent(byte[] bytes) {
		this.bytes = bytes;
	}

	public StatusChangeEventType getStatusChangeEventType() {
		return StatusChangeEventType.fromValue(bytes[4]);
	}
	
	public int getFreeScrollBufferBytes() {
		return bytes[5];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("StatusChangeEvent:");
		sb.append(" statusChangeEventType=" + getStatusChangeEventType());
		sb.append(" freeScrollBufferBytes=" + getFreeScrollBufferBytes());
		return sb.toString();
	}

}
