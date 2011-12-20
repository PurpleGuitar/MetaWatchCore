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

import java.util.Calendar;
import java.util.Date;

import org.metawatch.manager.core.packets.DefaultWatchPacket;
import org.metawatch.manager.core.packets.PacketConstants;
import org.metawatch.manager.core.packets.PacketConstants.MessageType;

import android.content.Context;
import android.text.format.DateFormat;

public class SetRealTimeClock extends DefaultWatchPacket {

	public SetRealTimeClock(Context context) {
		
		initializeBytes(14);
		bytes[PacketConstants.MESSAGE_TYPE_BYTE_LOCATION] = MessageType.SetRealTimeClock.msg;
		bytes[PacketConstants.OPTIONS_BYTE_LOCATION] = 0x00; // not used

		/* Read date format from OS */
		boolean isMMDD = true;
		char[] ch = DateFormat.getDateFormatOrder(context);
		for (int i = 0; i < ch.length; i++) {
			if (ch[i] == DateFormat.DATE) {
				isMMDD = false;
				break;
			}
			if (ch[i] == DateFormat.MONTH) {
				isMMDD = true;
				break;
			}
		}

		/* Get wall clock */
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);

		/* Set date bytes */
		bytes[4] = (byte) (year / 256);
		bytes[5] = (byte) (year % 256);
		bytes[6] = (byte) (calendar.get(Calendar.MONTH) + 1);
		bytes[7] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
		bytes[8] = (byte) (calendar.get(Calendar.DAY_OF_WEEK) - 1);
		bytes[9] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
		bytes[10] = (byte) calendar.get(Calendar.MINUTE);
		bytes[11] = (byte) calendar.get(Calendar.SECOND);
		
		/* Set 12/24 hour format */
		if (DateFormat.is24HourFormat(context))
			bytes[12] = (byte) 1; // 24hr
		else
			bytes[12] = (byte) 0; // 12hr
		if (isMMDD)
			bytes[13] = (byte) 0; // mm/dd
		else
			bytes[13] = (byte) 1; // dd/mm

	}

}
