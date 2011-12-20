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
package org.metawatch.manager.core.renderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.metawatch.manager.core.lib.intents.DisplayIdleScreenWidget;
import org.metawatch.manager.core.lib.intents.DisplayNotification;
import org.metawatch.manager.core.packets.WatchPacket;
import org.metawatch.manager.core.packets.outgoing.SetVibrateMode;
import org.metawatch.manager.core.service.WatchMessage;

import android.content.Context;

public abstract class DefaultWatchRenderer implements WatchRenderer {

	protected Map<String, DisplayIdleScreenWidget>	widgets	= new HashMap<String, DisplayIdleScreenWidget>();

	public WatchMessage renderNotification(Context context,
			DisplayNotification req) {

		/* Create message. */
		WatchMessage message = new WatchMessage();
		List<WatchPacket> packets = message.getPackets();

		/* Vibrate if requested. */
		if (req.vibrateNumberOfCycles > 0) {
			packets.add(new SetVibrateMode(context, req.vibrateOnDuration,
					req.vibrateOffDuration, req.vibrateNumberOfCycles));
		}

		return message;
	}

	@Override
	public WatchMessage renderIdleScreen(Context context) {
		/* Create empty message. */
		WatchMessage message = new WatchMessage();

		return message;
	}

	@Override
	public void updateIdleScreenWidget(Context context,
			DisplayIdleScreenWidget req) {
		widgets.put(req.key, req);
	}
}
