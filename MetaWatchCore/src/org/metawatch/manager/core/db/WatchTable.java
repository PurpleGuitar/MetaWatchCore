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

package org.metawatch.manager.core.db;

import org.metawatch.manager.core.constants.Constants;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WatchTable {

	private static final String		TABLE_NAME		= "watch";

	public static final String		COLUMN_ID		= "_id";
	public static final String		COLUMN_MAC		= "mac";
	public static final String		COLUMN_NAME		= "name";
	public static final String		COLUMN_ACTIVE	= "active";
	public static final String[]	ALL_COLUMNS		= { COLUMN_ID, COLUMN_MAC,
			COLUMN_NAME, COLUMN_ACTIVE				};

	private static final String		TABLE_CREATE	= "create table "
															+ TABLE_NAME
															+ " ("
															+ COLUMN_ID
															+ " integer primary key autoincrement, "
															+ COLUMN_MAC
															+ " text not null, "
															+ COLUMN_ACTIVE
															+ " integer not null, "
															+ COLUMN_NAME
															+ " text not null);";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(TABLE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		/*
		 * This just blows away the table. We'll want something more robust when
		 * we actually change the schema.
		 */
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}

	public static boolean watchWithMacExists(SQLiteDatabase db, String mac) {
		Cursor c = db.query(TABLE_NAME, new String[] { "count(*)" }, COLUMN_MAC
				+ "=?", new String[] { mac }, null, null, null);
		try {
			if (c.moveToNext()) {
				int numRows = c.getInt(0);
				return numRows > 0;
			}
			return false;
		} finally {
			c.close();
		}
	}

	public static boolean isWatchActive(SQLiteDatabase db, String mac) {
		Cursor c = db.query(TABLE_NAME, new String[] { "active" }, COLUMN_MAC
				+ "=?", new String[] { mac }, null, null, null);
		try {
			if (c.moveToNext()) {
				int activeBit = c.getInt(0);
				return activeBit > 0;
			}
			return false;
		} finally {
			c.close();
		}
	}

	public static void addWatch(SQLiteDatabase db, String mac, String name) {
		if (watchWithMacExists(db, mac)) {
			throw new IllegalStateException(
					"Watch with this mac already exists!");
		}
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_MAC, mac);
		cv.put(COLUMN_NAME, name);
		cv.put(COLUMN_ACTIVE, true);
		long id = db.insert(TABLE_NAME, null, cv);
		Log.d(Constants.LOG_TAG, "WatchTable.addWatch(): New row id=" + id);
	}

	public static Cursor getAllWatches(SQLiteDatabase db) {
		return db.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null, null);
	}

	public static void setWatchActiveByMac(SQLiteDatabase db, String mac,
			boolean active) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ACTIVE, active ? 1 : 0);
		db.update(TABLE_NAME, cv, COLUMN_MAC + "=?", new String[] { mac });
	}

	public static void deleteWatch(SQLiteDatabase db, long id) {
		db.delete(TABLE_NAME, COLUMN_ID + "=?",
				new String[] { Long.toString(id) });
	}

	public static void deleteWatchByMac(SQLiteDatabase db, String mac) {
		db.delete(TABLE_NAME, COLUMN_MAC + "=?", new String[] { mac });
	}
}
