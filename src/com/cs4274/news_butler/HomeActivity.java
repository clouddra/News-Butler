package com.cs4274.news_butler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;


import com.cs4274.news_butler.helper.IndexSources;
import com.cs4274.news_butler.helper.ReadGMail;
import com.cs4274.news_butler.helper.ReadSMS;


public class HomeActivity extends Activity {
	 
	private AccountManager accountManager;
	private static final int AUTHORIZATION_CODE = 1995;
	private static final int ACCOUNT_CODE = 1602;
	private static final String SCOPE = "oauth2:https://mail.google.com";	
	String username = null;
    String userToken = null;
	
	public List<String> messages;
	
	//private final int ACTIVITY_SSO = 1000;
	//private static final String APP_ID = "247627235387014";
	//private static final String PERMISSIONS = "read_stream,read_friendlists,manage_friendlists,manage_notifications,publish_stream,publish_checkins,offline_access";
	
	private String SMSSourceFile = "SMS.txt";
	private String filepath = getIndexDirectory();
	public static String suffix = ".txt";
	File myExternalFile;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		accountManager = AccountManager.get(this);
			
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	// This method is invoked when the button getSMS is called
	// When merging together with UI, just place this method in the onClickListener or use XML to activate this method
	public void getSMS(View v) {
		//new readSMSTask().execute();
		new indexSourcesTask().execute();
	}
	
	// This method is invoked when the button getGmail is called
	// When merging together with UI, just place this method in the onClickListener or use XML to activate this method
	public void getGmail(View v) {
		
		if(isConnectedToInternet())
			chooseAccount();
		else {
			Toast.makeText(getApplicationContext(), "Unable to connect to Gmail, please turn on your Data", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	// AsyncTask to read SMS from Content Provider
	private class readSMSTask extends AsyncTask <Void,Void,Void> {
		Context context = getApplicationContext();
		
		@Override
		protected Void doInBackground(Void... params) {			
			final ReadSMS ReadSMS = new ReadSMS();
			messages = ReadSMS.getOutboxSms(context);
			
			Iterator<String> iterator = messages.iterator();
			
			if (iterator.hasNext()==false)
				Toast.makeText(context, "No SMS to learn from!.", Toast.LENGTH_SHORT).show();
			
				
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {		
			exportText();	
		}
			
	}
	
	// AsyncTask to index the sources
	public class indexSourcesTask extends AsyncTask <Void,Void,Void> {
		Context context = getApplicationContext();
		
		@Override
		protected Void doInBackground(Void... params) {		
			try {
				IndexSources.createIndex(filepath,suffix);
				
			} catch (Exception e) {				
				e.printStackTrace();
			}
				
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			try {
				IndexSources.computeTopTermQuery();
				Toast.makeText(context, "Finish learning from SMS!.", Toast.LENGTH_SHORT).show();
			} catch (Exception e) {				
				e.printStackTrace();
			}
		}
			
	}
	

	public static Activity getActivity() {
		return HomeActivity.getActivity();
	}
	
	private void chooseAccount() {
		Intent intent = AccountManager.newChooseAccountIntent(null, null,
				new String[] { "com.google" }, false, null, null, null, null);
		startActivityForResult(intent, ACCOUNT_CODE);
	}
	

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			if (requestCode == AUTHORIZATION_CODE) {
				requestToken();
				
			} else if (requestCode == ACCOUNT_CODE) {
				String accountName = data
						.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				username = accountName;
 
				// invalidate old tokens which might be cached. we want a fresh
				// one, which is guaranteed to work
				invalidateToken();
				
				requestToken();							
			}
		}
		
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(this, "Disallowed permission", Toast.LENGTH_SHORT).show();
		}	
		
	}
	
	private void requestToken() {
		Account userAccount = null;
		for (Account account : accountManager.getAccountsByType("com.google")) {
			if (account.name.equals(username)) {
				userAccount = account;
				break;
			}
		}
 
		accountManager.getAuthToken(userAccount, SCOPE, null, this,
				new OnTokenAcquired(), null);
	}
	
	private void invalidateToken() {
		AccountManager accountManager = AccountManager.get(this);
		accountManager.invalidateAuthToken("com.google",
				userToken);
 
		userToken = null;
	}
	
	public class OnTokenAcquired implements AccountManagerCallback<Bundle> {
		Context context = getApplicationContext();
		
		@Override
		public void run(AccountManagerFuture<Bundle> result) {
			try {
				Bundle bundle = result.getResult();
 
				Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
				if (launch != null) {
					startActivityForResult(launch, AUTHORIZATION_CODE);
				} else {
					String token = bundle
							.getString(AccountManager.KEY_AUTHTOKEN);
 
					userToken = token;	
										
					//Use username and token to read Sent Items
					if (userToken != null) {					
						new readGmailTask().execute(username,userToken,filepath);							
					}
					else {
						Toast.makeText(context, "Unable to authenticate with Gmail", Toast.LENGTH_SHORT).show();
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	// AsyncTask to read Gmail
		private class readGmailTask extends AsyncTask <String,Void,Void> {
			Context context = getApplicationContext();
			
			@Override
			protected Void doInBackground(String... params) {			
				ReadGMail gmailClass = new ReadGMail();
				gmailClass.readSentItems(params[0], params[1], params[2]);
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {	
				Toast.makeText(context, "Finish learning from Gmail!.", Toast.LENGTH_SHORT).show();
			}
				
		}
	
	/*
	 * Helper methods
	 */
	
	private String getIndexDirectory(){
		File dir;
		String state = Environment.getExternalStorageState();
		if (state.equalsIgnoreCase(Environment.MEDIA_MOUNTED) == true){
			dir = new File(Environment.getExternalStorageDirectory(), "NewsButler");
		} else {
			dir = new File("/mnt/sdcard", "NewsButler");
		}
		if (!dir.exists())
			dir.mkdirs();
		
		return dir.getAbsolutePath();
	}
	
	private void exportText() {
		myExternalFile = new File(filepath, SMSSourceFile);
		String nextLine = "\n";
		Iterator<String> iterator = messages.iterator();
		
		try {
             FileOutputStream fos = new FileOutputStream(myExternalFile);
             while (iterator.hasNext()) {		
            	 String text = iterator.next().replaceAll("[0-9]","");
            	 fos.write(text.getBytes());
            	 fos.write(nextLine.getBytes());
            }             
             fos.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
        			 
		 new indexSourcesTask().execute();	 
	}
	
	private boolean isConnectedToInternet() {
		Context context = getApplicationContext();
		ConnectivityManager cm =
		        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		return isConnected;
	
	}

}
