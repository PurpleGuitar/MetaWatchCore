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
package org.metawatch.manager.core.lib.constants;

public enum WatchButtonPressType {
	IMMEDIATE(0), PRESS_AND_RELEASE(1), HOLD_AND_RELEASE(2), LONG_HOLD_AND_RELEASE(
			3);
	public final int	value;

	WatchButtonPressType(int value) {
		this.value = value;
	}

	public static WatchButtonPressType getByValue(int value) {
		for (WatchButtonPressType mode : WatchButtonPressType.values()) {
			if (mode.value == value) {
				return mode;
			}
		}
		return null;
	}
}
