package org.metawatch.manager.phone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MetaWatchPhoneActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		startService(new Intent(this, MetaWatchPhoneService.class));
	}
}