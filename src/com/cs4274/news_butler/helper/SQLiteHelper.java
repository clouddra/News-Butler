package com.cs4274.news_butler.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import android.util.Log;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

public class SQLiteHelper extends SQLiteOpenHelper {
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

	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(SQLiteHelper.class.getName(),
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
	
	public Vector<String> getMessagesAddWeightRecent(long todayDate) {

		
		Cursor cursor = this.getReadableDatabase().query(TABLE_MESSAGES,
				null, null, null, null, null, null);
		Vector<String> allMessages = new Vector<String>(cursor.getCount());
		Log.e("Number of messages in DB", Integer.toString(cursor.getCount()));
		cursor.moveToFirst();
		int todayDateFormatted = convertDate(todayDate);
		long msgDate;
		while (!cursor.isAfterLast()) {
			msgDate = cursor.getLong(2);	
			int dateDifference = todayDateFormatted - convertDate(msgDate);

			switch (dateDifference) {
            case 0:
            	for (int i=0;i<140;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break;
            case 1:
            	for (int i=0;i<130;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break;              
            case 2:
            	for (int i=0;i<120;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break; 	 
            case 3:
            	for (int i=0;i<110;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break; 	 
            case 4:
            	for (int i=0;i<100;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break;
            case 5:
            	for (int i=0;i<90;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break; 	 
            case 6:
            	for (int i=0;i<80;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break; 	 
            case 7:
            	for (int i=0;i<70;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break; 	 
            case 8:
            	for (int i=0;i<60;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break; 	 
            case 9:
            	for (int i=0;i<50;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break; 	 
            case 10:
            	for (int i=0;i<40;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break;
            case 11:
            	for (int i=0;i<30;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break; 	 
            case 12:
            	for (int i=0;i<20;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break; 	 
            case 13:
            	for (int i=0;i<10;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break; 	 
            case 14:
            	for (int i=0;i<10;i++) {
            		allMessages.add(cursor.getString(1));
            	}
            	break; 	 
            default:            	
            	allMessages.add(cursor.getString(1));	
            	break;
            }		
			cursor.moveToNext();
		}
		// make sure to close the cursor
		Log.d("db full count", String.valueOf(cursor.getCount()));
		cursor.close();
		// return allMessages.toString();
		return allMessages;
	}

	public long addMessage(String message, long time) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_MESSAGE, message);
		values.put(COLUMN_TIME, time);
		return this.getWritableDatabase().insert(TABLE_MESSAGES, null, values);
	}
	
	private int convertDate(long date) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date*1000);
		
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
		String formattedDate = dateFormater.format(c.getTime());
			
		return Integer.parseInt(formattedDate);
	}

}
