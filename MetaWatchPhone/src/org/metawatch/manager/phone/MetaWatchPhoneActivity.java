package org.metawatch.manager.phone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MetaWatchPhoneActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		startService(new Intent(this, MetaWatchPhoneService.class));
	}

	public void testPhoneRinging(View view) {
		MetaWatchPhoneService.sendPhoneRingingNotification(this,
				"Frodo Baggins", "(555)555-1418");
	}

	public void updateIdleScreenWidget(View view) {
		IdleScreenWidgetRenderer.sendIdleScreenWidgetUpdate(this);
	}
}