package com.cs4274.news_butler.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ReadSMS {
	
	private static final String TAG = "READSMS_DEBUG";
	
	public ReadSMS () {}

	public void storeOutboxSms(Context context, FBSQLiteHelper datasource, long previousLearned ) {
		if (context==null)
			return;
		
		Uri uriSms = Uri.parse("content://sms/sent");
		Cursor cursor = context.getContentResolver().query(uriSms, null,null,null,null); 
	    cursor2SmsArray(cursor, datasource, previousLearned);
	    
	    if(!cursor.isClosed())
	        cursor.close();
	    
	}

	public void cursor2SmsArray(Cursor cursor, FBSQLiteHelper datasource, long previousLearned) {
		 if(null == cursor || 0 == cursor.getCount())
	            return;
	    				 		
		 try
	        {
	            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
	            	
	            	if (previousLearned == -1L) { //  never learn SMS before
	            		String smsBody = cursor.getString(cursor.getColumnIndexOrThrow("body"));  	 	                	                
	 	                byte[] bodyBytes = smsBody.getBytes("UTF8");                
	 	                String singleSms = TextUtils.htmlEncode(new String(bodyBytes, "UTF8"));  
	 	                String smsDate = cursor.getString(cursor.getColumnIndexOrThrow("date"));
	 	                long smsDateinMS = (Long.parseLong(smsDate)) / 1000L;
	 	                	 	                
	 	                datasource.addMessage(singleSms.replaceAll("[0-9]", ""), smsDateinMS);	
	            	}
	            	else {
	            		 String smsDate = cursor.getString(cursor.getColumnIndexOrThrow("date"));
	            		 long smsDateinMS = (Long.parseLong(smsDate)) / 1000L;
	            		 
	            		 if ( smsDateinMS > previousLearned ) { // only learn SMS that has not been learnt before
	            			 String smsBody = cursor.getString(cursor.getColumnIndexOrThrow("body")); 
	            			 byte[] bodyBytes = smsBody.getBytes("UTF8");
	            			 String singleSms = TextUtils.htmlEncode(new String(bodyBytes, "UTF8"));
	            			 datasource.addMessage(singleSms.replaceAll("[0-9]", ""), smsDateinMS);
	            		 }
	            	}	            
	            }

	        }
	        catch (Exception e) 
	        {
	            Log.e(TAG, e.getMessage());
	        } 

		 	finally 
	        {
	            cursor.close();
	        }		 
		 
	}
	
}
