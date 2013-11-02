package com.cs4274.news_butler.helper;

import android.util.Log;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

public class FBSQLiteHelper extends SQLiteOpenHelper {
	public static final String TABLE_MESSAGES = "MESSAGES";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_MESSAGE = "message";
	public static final String COLUMN_TIME = "time";
	private String[] messageColumn = { COLUMN_MESSAGE };

	private static final String DATABASE_NAME = "messages.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_MESSAGES + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_MESSAGE
			+ " text not null, " + COLUMN_TIME + " integer not null);";

	public FBSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(FBSQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
		onCreate(db);
	}

	public int addMessage() {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_MESSAGE, "test");
		values.put(COLUMN_TIME, "123");
		this.getWritableDatabase().insert(TABLE_MESSAGES, null, values);
		return 0;
	}

	public String[] getMessages() {

		// StringBuilder allMessages = new StringBuilder("");
		Cursor cursor = this.getReadableDatabase().query(TABLE_MESSAGES,
				messageColumn, null, null, null, null, null);
		String[] allMessages = new String[cursor.getCount()];
		cursor.moveToFirst();
		int i = 0;
		while (!cursor.isAfterLast()) {

			allMessages[i] = cursor.getString(0);
			// allMessages.append(cursor.getString(0));
			cursor.moveToNext();
			i++;
		}
		// make sure to close the cursor
		Log.d("db full count", String.valueOf(cursor.getCount()));
		cursor.close();
		// return allMessages.toString();
		return allMessages;
	}

	public String[] getMessagesAfter(long time) {
		// StringBuilder allMessages = new StringBuilder("");
		String whereClause = COLUMN_TIME + ">" + time;
		Cursor cursor = this.getReadableDatabase().query(TABLE_MESSAGES,
				messageColumn, whereClause, null, null, null, null);

		String[] allMessages = new String[cursor.getCount()];
		cursor.moveToFirst();
		int i = 0;
		while (!cursor.isAfterLast()) {

			allMessages[i] = cursor.getString(0);
			// allMessages.append(cursor.getString(0));
			cursor.moveToNext();
			i++;
		}
		// make sure to close the cursor
		Log.d("db after count", String.valueOf(cursor.getCount()));
		cursor.close();
		// return allMessages.toString();
		return allMessages;
		// Cursor cursor = getReadableDatabase().
		// rawQuery("select * from todo where _id = ?", new String[] { id });
	}

	public long addMessage(String message, long time) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_MESSAGE, message);
		values.put(COLUMN_TIME, time);
		return this.getWritableDatabase().insert(TABLE_MESSAGES, null, values);
	}

}
