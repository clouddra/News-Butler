package com.cs4274.news_butler.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ReadSMS {
	
	private static final String TAG = "READSMS_DEBUG";
	
	
	public ReadSMS () {}

	public List<String> getOutboxSms(Context context,String todayDate) {
		if (context==null)
			return new ArrayList<String>();
		
		Uri uriSms = Uri.parse("content://sms/sent");
		Cursor cursor = context.getContentResolver().query(uriSms, null,null,null,null); 
	    List<String> outboxSms = cursor2SmsArray(cursor, todayDate);
	    
	    if(!cursor.isClosed())
	        cursor.close();
	    

	    return outboxSms;
	}

	public List<String> cursor2SmsArray(Cursor cursor, String todayDateString) {
		 if(null == cursor || 0 == cursor.getCount())
	            return new ArrayList<String>();
	    
		 int todayDate = Integer.parseInt(todayDateString);
				 
		 List<String> messages = new ArrayList<String>();
		 
		 try
	        {
	            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
	            	
	                String smsBody = cursor.getString(cursor.getColumnIndexOrThrow("body"));  
	                String smsDate = cursor.getString(cursor.getColumnIndexOrThrow("date"));	                
	                byte[] bodyBytes = smsBody.getBytes("UTF8");                
	                String singleSms = TextUtils.htmlEncode(new String(bodyBytes, "UTF8"));      
	               
	                int dateDifference = todayDate - convertDate(smsDate);
	                
	                switch (dateDifference) {
	                case 0:
	                	for (int i=0;i<140;i++) {
	                		messages.add(singleSms);
	                	}
	                	break;
	                case 1:
	                	for (int i=0;i<130;i++) {
	                		messages.add(singleSms);
	                	}
	                	break;              
	                case 2:
	                	for (int i=0;i<120;i++) {
	                		messages.add(singleSms);
	                	}
	                	break; 	 
	                case 3:
	                	for (int i=0;i<110;i++) {
	                		messages.add(singleSms);
	                	}
	                	break; 	 
	                case 4:
	                	for (int i=0;i<100;i++) {
	                		messages.add(singleSms);
	                	}
	                	break;
	                case 5:
	                	for (int i=0;i<90;i++) {
	                		messages.add(singleSms);
	                	}
	                	break; 	 
	                case 6:
	                	for (int i=0;i<80;i++) {
	                		messages.add(singleSms);
	                	}
	                	break; 	 
	                case 7:
	                	for (int i=0;i<70;i++) {
	                		messages.add(singleSms);
	                	}
	                	break; 	 
	                case 8:
	                	for (int i=0;i<60;i++) {
	                		messages.add(singleSms);
	                	}
	                	break; 	 
	                case 9:
	                	for (int i=0;i<50;i++) {
	                		messages.add(singleSms);
	                	}
	                	break; 	 
	                case 10:
	                	for (int i=0;i<40;i++) {
	                		messages.add(singleSms);
	                	}
	                	break;
	                case 11:
	                	for (int i=0;i<30;i++) {
	                		messages.add(singleSms);
	                	}
	                	break; 	 
	                case 12:
	                	for (int i=0;i<20;i++) {
	                		messages.add(singleSms);
	                	}
	                	break; 	 
	                case 13:
	                	for (int i=0;i<10;i++) {
	                		messages.add(singleSms);
	                	}
	                	break; 	 
	                case 14:
	                	for (int i=0;i<10;i++) {
	                		messages.add(singleSms);
	                	}
	                	break; 	 
	                default:
	                	messages.add(singleSms);	                		
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
		 
		 
		return messages;
	}
	
	private int convertDate(String smsDate) {
		Long time = Long.valueOf(smsDate); 
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
		String todayDate = dateFormater.format(c.getTime());
			
		return Integer.parseInt(todayDate);
	}
}
