package com.cs4274.news_butler.helper;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ReadSMS {
	
	private static final String TAG = "READSMS_DEBUG";
	
	
	public ReadSMS () {}

	public List<String> getOutboxSms(Context context) {
		if (context==null)
			return new ArrayList<String>();
		
		Uri uriSms = Uri.parse("content://sms/sent");
		Cursor cursor = context.getContentResolver().query(uriSms, null,null,null,null); 
	    List<String> outboxSms = cursor2SmsArray(cursor);
	    
	    if(!cursor.isClosed())
	        cursor.close();
	    

	    return outboxSms;
	}

	public static List<String> cursor2SmsArray(Cursor cursor) {
		 if(null == cursor || 0 == cursor.getCount())
	            return new ArrayList<String>();
	    
		 List<String> messages = new ArrayList<String>();
		 
		 try
	        {
	            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
	            	
	                String smsBody = cursor.getString(cursor.getColumnIndexOrThrow("body"));  
	                byte[] bodyBytes = smsBody.getBytes("UTF8");
	                String singleSms = TextUtils.htmlEncode(new String(bodyBytes, "UTF8"));              	               
	                messages.add(singleSms);
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
		 
		 
		return messages;
	}
	
}
