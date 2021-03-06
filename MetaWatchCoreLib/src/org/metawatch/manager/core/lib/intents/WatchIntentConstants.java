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
package org.metawatch.manager.core.lib.intents;

public class WatchIntentConstants {

	/*
	 * Package for intents
	 */
	public static final String	INTENT_PACKAGE						= "org.metawatch.manager.core";

	/*
	 * Intents that don't have their own class
	 */
	public static final String	CONNECT_INTENT_ACTION				= INTENT_PACKAGE
																			+ ".CONNECT";
	public static final String	DISCONNECT_INTENT_ACTION			= INTENT_PACKAGE
																			+ ".DISCONNECT";

	public static final String	DISPLAY_IDLE_SCREEN_WIDGET_REQUEST	= INTENT_PACKAGE
																			+ ".DISPLAY_IDLE_SCREEN_WIDGET_REQUEST";

}
