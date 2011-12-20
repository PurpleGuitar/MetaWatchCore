/*****************************************************************************
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
package org.metawatch.manager.core.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.metawatch.manager.core.R;
import org.metawatch.manager.core.constants.Constants;
import org.metawatch.manager.core.db.WatchDBHelper;
import org.metawatch.manager.core.db.WatchTable;
import org.metawatch.manager.core.lib.intents.RefreshConnections;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class WatchDiscovery extends Activity {

	class Receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(
					BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				Log.d(Constants.LOG_TAG,
						"WatchDiscovery.Receiver.onReceive(): Discovery finished.");

				pdWait.dismiss();

				if (list.size() == 0) {
					Toast.makeText(context, "No watches found",
							Toast.LENGTH_SHORT).show();
					finish();
				}

				unregisterReceiver(this);
			}

			if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {

				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (device.getBondState() == BluetoothDevice.BOND_BONDED)
					return;

				String deviceName = device.getName();
				String deviceMac = device.getAddress();
				Log.d(Constants.LOG_TAG,
						"WatchDiscovery.Receiver.onReceive(): Discovered device name='"
								+ deviceName + "' mac='" + deviceMac + "'");

				// int cl = device.getBluetoothClass().getMajorDeviceClass();
				// Log.d(MetaWatch.TAG, "device class: " + cl);

				addToList(deviceMac, deviceName);
			}
		}
	}

	ProgressDialog				pdWait	= null;

	Context						context;
	ListView					listView;
	// static ArrayList<String> menuList = new ArrayList<String>();
	List<Map<String, String>>	list	= new ArrayList<Map<String, String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		pdWait = ProgressDialog.show(this, "Please wait",
				"Searching Bluetooth devices...");
		pdWait.setCancelable(true);
		pdWait.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				BluetoothAdapter bluetooth = BluetoothAdapter
						.getDefaultAdapter();
				if (bluetooth.isDiscovering())
					bluetooth.cancelDiscovery();
				finish();
			}
		});
		pdWait.show();

		setContentView(R.layout.watch_discovery);
		// constructMenuList();
		listView = (ListView) findViewById(android.R.id.list);
		// listView.setAdapter(new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, menuList));

		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {

				Map<String, String> map = list.get(position);
				String mac = map.get("mac");
				String name = map.get("name");

				onWatchSelected(mac, name);
			}

		});

		BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

		Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				addToList(device.getAddress(), device.getName());
			}
		}

		Receiver receiver = new Receiver();
		IntentFilter intentFilter = new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

		registerReceiver(receiver, intentFilter);

		Log.d(Constants.LOG_TAG,
				"WatchDiscovery.onCreate(): Starting Bluetooth discovery");
		bluetooth.startDiscovery();

	}

	void onWatchSelected(String mac, String name) {
		Log.d(Constants.LOG_TAG,
				"WatchDiscovery.onWatchSelected(): User selected device name='"
						+ name + "' mac='" + mac + "'");

		/* Is this device already in the database? */
		WatchDBHelper dbHelper = new WatchDBHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (WatchTable.watchWithMacExists(db, mac)) {
			Log.d(Constants.LOG_TAG,
					"WatchDiscovery.onWatchSelected(): Found matching watch in db.");
		} else {
			WatchTable.addWatch(db, mac, name);
			Log.d(Constants.LOG_TAG,
					"WatchDiscovery.onWatchSelected(): Added watch to db.  Requesting refresh.");
			RefreshConnections req = new RefreshConnections();
			req.refreshBattery = true;
			sendBroadcast(req.toIntent());
		}
		db.close();
		finish();
	}

	void addToList(String mac, String name) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("mac", mac);
		map.put("name", name);
		list.add(map);
		displayList();
	}

	void displayList() {

		listView.setAdapter(new SimpleAdapter(this, list,
				R.layout.watch_discovery_list_item, new String[] { "name",
						"mac" }, new int[] {
						R.id.watch_discovery_list_item_first_line,
						R.id.watch_discovery_list_item_second_line }));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
