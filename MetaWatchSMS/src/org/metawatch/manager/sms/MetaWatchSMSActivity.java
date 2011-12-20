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
package org.metawatch.manager.sms;

import org.metawatch.manager.core.lib.intents.DisplayNotification;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MetaWatchSMSActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		startService(new Intent(this, MetaWatchSMSService.class));
    }
    
    public void test(View view) {
		DisplayNotification req = new DisplayNotification();
		req.vibrateOnDuration = 500;
		req.vibrateOffDuration = 500;
		req.vibrateNumberOfCycles = 3;
		
		/* OLED display */
		req.oledTopText = "SMS";
		req.oledBottomLine1Text = "5555551212";
		req.oledBottomLine2Text = "Howdy!";
		
		/* LCD display */
		req.lcdText = "SMS from 5555551212\nHowdy!";
		
		sendBroadcast(req.toIntent());    	
    }
    
    public void testLong(View view) {
		DisplayNotification req = new DisplayNotification();
		req.vibrateOnDuration = 500;
		req.vibrateOffDuration = 500;
		req.vibrateNumberOfCycles = 3;
		
		/* OLED display */
		req.oledTopText = "SMS";
		req.oledBottomLine1Text = "5555551212";
		req.oledBottomLine2Text = "Howdy! Things going OK for you today?";
		
		/* LCD display */
		req.lcdText = "SMS from 5555551212\nHowdy! Things going OK for you today?";
		
		sendBroadcast(req.toIntent());    	
    }
    
    public void testReallyLong(View view) {
		DisplayNotification req = new DisplayNotification();
		req.vibrateOnDuration = 500;
		req.vibrateOffDuration = 500;
		req.vibrateNumberOfCycles = 3;
		
		/* OLED display */
		req.oledTopText = "SMS";
		req.oledBottomLine1Text = "5555551212";
		req.oledBottomLine2Text = "Four score and seven years ago our forefathers brought forth upon this continent a new nation, conceived in liberty and dedicated to the proposition that all men are created equal.";
		
		/* LCD display */
		req.lcdText = "SMS from 5555551212\nFour score and seven years ago our forefathers brought forth upon this continent a new nation, conceived in liberty and dedicated to the proposition that all men are created equal.";
		
		sendBroadcast(req.toIntent());    	
    }
    
    public void testIdleScreenWidget(View view) {
    	IdleScreenWidgetRenderer.sendIdleScreenWidgetUpdate(this);
    }
}