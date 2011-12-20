package org.metawatch.manager.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.metawatch.manager.core.R;
import org.metawatch.manager.core.constants.Constants;
import org.metawatch.manager.core.db.WatchDBHelper;
import org.metawatch.manager.core.db.WatchTable;
import org.metawatch.manager.core.lib.constants.WatchType;
import org.metawatch.manager.core.lib.intents.RefreshConnections;
import org.metawatch.manager.core.lib.intents.WatchConnectionInfo;
import org.metawatch.manager.core.lib.intents.WatchIntentConstants;
import org.metawatch.manager.core.service.WatchService;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WatchList extends ListActivity {

	private class WatchConnectionInfoBroadcastReceiver extends
			BroadcastReceiver {
		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					WatchConnectionInfo.WATCH_CONNECTION_INFO_INTENT_ACTION)) {
				WatchConnectionInfo newInfo = WatchConnectionInfo
						.fromIntent(intent);

				/* Ignore updates about watches not in the database. */
				if (WatchTable.watchWithMacExists(db, newInfo.macAddress) == false) {
					Log.d(Constants.LOG_TAG,
							"WatchList.WatchConnectionInfoBroadcastReceiver.onReceive(): Received an update for a watch not in the database.  Maybe it was deleted?");
					return;
				}

				/* Look for an existing info in the display list. */
				WatchConnectionInfo existingInfo = null;
				for (WatchConnectionInfo info : watchConnectionInfos) {
					if (info.macAddress.equals(newInfo.macAddress)) {
						existingInfo = info;
						break;
					}
				}
				/* If existing info exists, remove it. */
				if (existingInfo != null) {
					watchConnectionInfos.remove(existingInfo);
				}
				/* Add new info. */
				watchConnectionInfos.add(newInfo);
				Collections.sort(watchConnectionInfos);
				onContentChanged();
			}
		}

	}

	private class WatchConnectionInfoArrayAdapter extends
			ArrayAdapter<WatchConnectionInfo> {
		private List<WatchConnectionInfo>	infos;

		public WatchConnectionInfoArrayAdapter(List<WatchConnectionInfo> infos) {
			super(WatchList.this, R.layout.watch_list_item, infos);
			this.infos = infos;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Recycle existing view if passed as parameter
			// This will save memory and time on Android
			// This only works if the base layout for all classes are the same
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = getLayoutInflater();
				rowView = inflater
						.inflate(R.layout.watch_list_item, null, true);
			}
			WatchConnectionInfo info = infos.get(position);
			ImageView icon = (ImageView) rowView
					.findViewById(R.id.watch_list_item_icon);
			if (info.watchType == WatchType.Analog) {
				icon.setImageResource(R.drawable.analog);
			} else if (info.watchType == WatchType.Digital) {
				icon.setImageResource(R.drawable.digital);
			} else {
				icon.setImageResource(R.drawable.mw_icon);
			}

			/* Name */
			TextView name = (TextView) rowView
					.findViewById(R.id.watch_list_item_name);
			name.setText(info.name);

			/* MAC */
			TextView mac = (TextView) rowView
					.findViewById(R.id.watch_list_item_mac);
			mac.setText(info.macAddress);

			/* Type (analog/digital) */
			TextView type = (TextView) rowView
					.findViewById(R.id.watch_list_item_type);
			if (info.watchType == null) {
				type.setText("Unknown");
			} else {
				type.setText(info.watchType.toString());
			}

			/* Connection status */
			TextView connected = (TextView) rowView
					.findViewById(R.id.watch_list_item_connectionState);
			connected.setText(info.connectionState.toString());
			switch (info.connectionState) {
			case Connected:
				connected.setTextColor(getResources().getColor(
						R.color.connection_connected));
				connected.setTypeface(null, Typeface.BOLD);
				break;
			case Searching:
				connected.setTextColor(getResources().getColor(
						R.color.connection_searching));
				connected.setTypeface(null, Typeface.NORMAL);
				break;
			case Disconnected:
				connected.setTextColor(getResources().getColor(
						R.color.connection_disconnected));
				connected.setTypeface(null, Typeface.NORMAL);
				break;
			default:
				Log.w(Constants.LOG_TAG,
						"WatchList.WatchConnectionInfoArrayAdapter.getView(): Unknown connection state: "
								+ info.connectionState);
			}

			/* Battery status */
			TextView battery = (TextView) rowView
					.findViewById(R.id.watch_list_item_battery);
			if (info.batteryVoltage < 1.0) {
				battery.setText("Unknown");
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append(info.batteryVoltage);
				sb.append("V");
				if (info.batteryIsCharging) {
					sb.append(" (charging)");
				}
				battery.setText(sb.toString());
			}

			return rowView;
		}
	}

	private SQLiteDatabase							db						= null;
	private WatchConnectionInfoBroadcastReceiver	broadcastReceiver		= new WatchConnectionInfoBroadcastReceiver();
	private static final int						CONNECT_WATCH			= Menu.FIRST + 1;
	private static final int						DISCONNECT_WATCH		= Menu.FIRST + 2;
	private static final int						REMOVE_WATCH			= Menu.FIRST + 3;
	private List<WatchConnectionInfo>				watchConnectionInfos	= Collections
																					.synchronizedList(new ArrayList<WatchConnectionInfo>());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.watch_list);
		registerForContextMenu(getListView());
		WatchDBHelper dbHelper = new WatchDBHelper(this);
		db = dbHelper.getWritableDatabase();
		fillData();
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(broadcastReceiver, new IntentFilter(
				WatchConnectionInfo.WATCH_CONNECTION_INFO_INTENT_ACTION));
		WatchService.startServiceIfNecessary(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		requestConnectionRefresh();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(broadcastReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(ContextMenu.NONE, CONNECT_WATCH, 0,
				R.string.watch_list_connect_watch);
		menu.add(ContextMenu.NONE, DISCONNECT_WATCH, 0,
				R.string.watch_list_disconnect_watch);
		menu.add(ContextMenu.NONE, REMOVE_WATCH, 0,
				R.string.watch_list_forget_watch);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		WatchConnectionInfo watchInfo = watchConnectionInfos.get(info.position);
		switch (item.getItemId()) {
		case CONNECT_WATCH:
			WatchTable.setWatchActiveByMac(db, watchInfo.macAddress, true);
			requestWatchConnect(watchInfo.macAddress);
			return true;
		case DISCONNECT_WATCH:
			WatchTable.setWatchActiveByMac(db, watchInfo.macAddress, false);
			requestWatchDisconnect(watchInfo.macAddress);
			return true;
		case REMOVE_WATCH:
			WatchTable.deleteWatchByMac(db, watchInfo.macAddress);
			requestConnectionRefresh();
			return true;
		}

		return super.onContextItemSelected(item);
	}

	private void fillData() {
		Log.d(Constants.LOG_TAG, "WatchList.fillData(): Filling data.");
		WatchConnectionInfoArrayAdapter arrayAdapter = new WatchConnectionInfoArrayAdapter(
				watchConnectionInfos);
		setListAdapter(arrayAdapter);
	}

	public void startDiscovery(View view) {
		startActivity(new Intent(WatchList.this, WatchDiscovery.class));
	}

	private void requestWatchConnect(String mac) {
		Log.d(Constants.LOG_TAG,
				"WatchList.requestWatchConnect(): Requesting connect of mac "
						+ mac);
		Intent intent = new Intent(WatchIntentConstants.CONNECT_INTENT_ACTION);
		intent.putExtra(WatchConnectionInfo.MAC_ADDRESS_EXTRA_FIELD, mac);
		sendBroadcast(intent);
	}

	private void requestWatchDisconnect(String mac) {
		Log.d(Constants.LOG_TAG,
				"WatchList.requestWatchDisconnect(): Requesting disconnect of mac "
						+ mac);
		Intent intent = new Intent(
				WatchIntentConstants.DISCONNECT_INTENT_ACTION);
		intent.putExtra(WatchConnectionInfo.MAC_ADDRESS_EXTRA_FIELD, mac);
		sendBroadcast(intent);
	}

	public void requestConnectionRefresh(View view) {
		requestConnectionRefresh();
	}

	private void requestConnectionRefresh() {
		Log.d(Constants.LOG_TAG,
				"WatchList.requestConnectionRefresh(): Requesting refresh of connections.");
		watchConnectionInfos.clear();
		onContentChanged();
		RefreshConnections req = new RefreshConnections();
		req.refreshBattery = true;
		req.refreshIdleScreen = true;
		sendBroadcast(req.toIntent());
	}

}
