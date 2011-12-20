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

public class PacketUtils {

	/** Generates a CRC from the given bytes.  The CRC can then be sent immediately after. */
	public static byte[] generateCRC(byte[] bytes) {
		byte[] result = new byte[2];
		short crc = (short) 0xFFFF;
		for (int j = 0; j < bytes.length; j++) {
			byte c = bytes[j];
			for (int i = 7; i >= 0; i--) {
				boolean c15 = ((crc >> 15 & 1) == 1);
				boolean bit = ((c >> (7 - i) & 1) == 1);
				crc <<= 1;
				if (c15 ^ bit)
					crc ^= 0x1021; // 0001 0000 0010 0001 (0, 5, 12)
			}
		}
		int crc2 = crc - 0xffff0000;
		result[0] = (byte) (crc2 % 256);
		result[1] = (byte) (crc2 / 256);
		return result;
	}	
	
}
