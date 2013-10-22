package com.cs4274.news_butler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;


import com.cs4274.news_butler.helper.IndexSources;
import com.cs4274.news_butler.helper.ReadGMail;
import com.cs4274.news_butler.helper.ReadSMS;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity{
	private AccountManager accountManager;
	private static final int AUTHORIZATION_CODE = 1995;
	private static final int ACCOUNT_CODE = 1602;
	private static final String SCOPE = "oauth2:https://mail.google.com";
	private static final String CONTROL = "control";
	private static final String HASHMAP = "hashmap";
	private static final String LEARNT_SMS = "learntsms";
	private static final String LEARNT_GMAIL = "learntgmail";
	private static final String LEARNT_FACEBOOK = "learntfacebook";
	private static final String LEARNT_SMS_GMAIL = "learnsmsgmail";
	private static final String LEARNT_ARTICLES = "learnarticles";
	private static final String USERNAME = "username";
	private static final String TOKEN = "token";
	private static final String SET = "set";
	
	public static final String USER_TOP_TERMS_FILENAME = "userTopTerms";
	public static final String SAVED_DOMAINS = "savedDomains";
	
	//private final int ACTIVITY_SSO = 1000;
	//private static final String APP_ID = "247627235387014";
	//private static final String PERMISSIONS = "read_stream,read_friendlists,manage_friendlists,manage_notifications,publish_stream,publish_checkins,offline_access";
	
	private String SMSSourceFile = "SMS.txt";
	private String applicationDirectory = getIndexDirectory();
	public static final String suffix = ".txt";
	public static final String USER = "user";
	public static final String ARTICLE = "article";
	public static boolean facebook = false;
	public static boolean sms = false;
	public static boolean gmail = false;
	public static boolean smsGmail = false;
	public static boolean articles = false;
	private static String username = null;
    private static String userToken = null;
    public List<String> messages = new ArrayList<String>();
    public List<String> gmailMessages = new ArrayList<String>();
    public static List<String> userTopTerms;
    public List<String> domains = new ArrayList<String>();
    public static Set<String> hashedArticles;
    private Set<String> emptySet = new HashSet<String>();
	File myExternalFile;
	
	
	 @SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        accountManager = AccountManager.get(this);
	        
	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.settings);
	        CustomPreference facebookPreference = (CustomPreference) findPreference("learn_facebook");
	        Log.d("Custm Pref", facebookPreference.getText());
	        //facebook.setText("Learned");
	        Log.d("Custom Pref Add", facebookPreference.getText());
	       
	        SharedPreferences control = getSharedPreferences(CONTROL,0); 
		       sms = control.getBoolean(LEARNT_SMS,false);
		       gmail = control.getBoolean(LEARNT_GMAIL, false );
		       facebook = control.getBoolean(LEARNT_FACEBOOK, false);
		       smsGmail = control.getBoolean(LEARNT_SMS_GMAIL, false);
		       articles  = control.getBoolean(LEARNT_ARTICLES, false);
			   //username = control.getString(USERNAME, null);
			   //userToken = control.getString(TOKEN, null);	
			   
			   
			SharedPreferences hashedSet = getSharedPreferences(HASHMAP,0);
			   hashedArticles = hashedSet.getStringSet(SET, emptySet);   
	        
	        CheckBoxPreference facebook2 = (CheckBoxPreference) findPreference("enable_facebook");
	        facebook2.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
	        	CustomPreference facebook3 = (CustomPreference) findPreference("learn_facebook");
				@Override
				public boolean onPreferenceChange(Preference arg0, Object arg1) {
					if ((Boolean)arg1)
						 facebook3.setText("Learned2");
					else
						facebook3.setText("TEST");
					
					return true;
				}
            });
	        
	        facebookPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					
					return false;
				}
	        	
	        });
	        
	        
	        
	        Preference smsPreference = (Preference)findPreference("learn_sms");
	        smsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					if (sms == false && gmail == false) {
						getSMS();					
					}
					else if (sms == true && gmail == false) {
						getSMS();
					}
					else if (sms == false && gmail == true){
						getSMSGmail();
					}
					else if (sms == true && gmail == true) {
						getSMSGmail();
					}
						
					return false;
				}
			});
	        
	        Preference gmailPreference = (Preference)findPreference("learn_gmail");
	        gmailPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					if (gmail == false && sms == false){
						getGmail();
					}
					else if (gmail == true && sms == false) {
						getGmail();
					}
					else if (gmail == false && sms == true){
						getSMSGmail();
					}
					else if (gmail == true && sms == true) {
						getSMSGmail();
					}

					return false;
				}
			});		
	        
	        Preference articlesPreference = (Preference)findPreference("learn_articles");
	        articlesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					articles = true;
					getArticleTopTerms();
					return false;
				}
			});
	        
	 }
	 
	 @Override
	 public void onResume() {
		 super.onResume();
		 
		 SharedPreferences control = getSharedPreferences(CONTROL,0); 
	     sms = control.getBoolean(LEARNT_SMS,false);
	     gmail = control.getBoolean(LEARNT_GMAIL, false );
	     facebook = control.getBoolean(LEARNT_FACEBOOK, false);
	     smsGmail = control.getBoolean(LEARNT_SMS_GMAIL, false);
	     articles = control.getBoolean(LEARNT_ARTICLES, false);
	     //username = control.getString(USERNAME, null);
	     //userToken = control.getString(TOKEN, null);	   
	     
	     SharedPreferences hashedSet = getSharedPreferences(HASHMAP,0);
	     hashedArticles = hashedSet.getStringSet(SET, emptySet);		 
	 }
	 
	 @Override
	 public void onPause() {
		 super.onPause();
	    
		 SharedPreferences control = getSharedPreferences(CONTROL,0); 
		 SharedPreferences.Editor editor = control.edit();
		 editor.putBoolean(LEARNT_SMS, sms);
		 editor.putBoolean(LEARNT_GMAIL, gmail);
		 editor.putBoolean(LEARNT_FACEBOOK, facebook);
		 editor.putBoolean(LEARNT_SMS_GMAIL, smsGmail);
		 editor.putBoolean(LEARNT_ARTICLES, articles);
		 //editor.putString(USERNAME, username);
		 //editor.putString(TOKEN, userToken);
		 editor.commit();		
		 
		 
		 SharedPreferences hashedSet = getSharedPreferences(HASHMAP,0);
		 SharedPreferences.Editor editor2 = hashedSet.edit();
		 editor2.putStringSet(SET, hashedArticles);
		 editor2.commit();
		
	 }
	 
	 
		// This method is invoked when the button getSMS is called
		// When merging together with UI, just place this method in the onClickListener or use XML to activate this method
		public void getSMS() {
			sms = true;
			new readSMSTask().execute();
			//new indexSourcesTask().execute(applicationDirectory,USER);
		}
		
		// This method is invoked when the button getGmail is called
		// When merging together with UI, just place this method in the onClickListener or use XML to activate this method
		public void getGmail() {	
			gmail = true;
			if(isConnectedToInternet())
				chooseAccount();
			else {
				Toast.makeText(getApplicationContext(), "Unable to connect to Gmail, please turn on your Data", Toast.LENGTH_SHORT).show();
			}
		}
		
		public void getSMSGmail() {
			smsGmail = true;
			if(isConnectedToInternet()) {
				//new readSMSGmailTask().execute(username,userToken,applicationDirectory);
				chooseAccount();
			}
			else {
				Toast.makeText(getApplicationContext(), "Please turn on your Data to enable better matching of News", Toast.LENGTH_SHORT).show();
			}
		}
		
		public void getArticleTopTerms() {
			if (isConnectedToInternet()) 
				new indexSourcesTask().execute(applicationDirectory,ARTICLE,null);
			else {
				Toast.makeText(getApplicationContext(), "Please turn on your Data to enable better matching of News", Toast.LENGTH_SHORT).show();
			}
		}
			
		// AsyncTask to read SMS from Content Provider
		private class readSMSTask extends AsyncTask <Void,Void,String> {
			Context context = getApplicationContext();
			
			@Override
			protected String doInBackground(Void... params) {			
				final ReadSMS ReadSMS = new ReadSMS();
				messages = ReadSMS.getOutboxSms(context);
				
				Iterator<String> iterator = messages.iterator();
				StringBuilder smsContent = new StringBuilder();
				
				if (iterator.hasNext()==false)
					Toast.makeText(context, "No SMS to learn from!.", Toast.LENGTH_SHORT).show();
				
				else {
					while (iterator.hasNext())
						smsContent.append(iterator.next().replaceAll("[0-9]",""));
				}
								
				return smsContent.toString();
			}
			
			@Override
			protected void onPostExecute(String content) {		
				new indexSourcesTask().execute(applicationDirectory,USER,content);
				Toast.makeText(context, "Finish learning from SMS!.", Toast.LENGTH_SHORT).show();
				//exportText();	
			}
				
		}
		
		// AsyncTask to index the sources
		public class indexSourcesTask extends AsyncTask <String,Void,String> {
					
			Context context = getApplicationContext();
			
			@Override
			protected String doInBackground(String... params) {		
				try {
					IndexSources.createIndex(params[0],params[1],params[2]);
					//IndexSources.createIndex(filepath,suffix,USER);
					
				} catch (Exception e) {				
					e.printStackTrace();
				}
					
				return params[1];
			}
			
			@Override
			protected void onPostExecute(String type) {
				System.out.println("TYPE:" + type);
				try {
					if (type.equalsIgnoreCase(USER)) {
						File indexDir = new File(applicationDirectory + "/" + IndexSources.USER_INDEX + "/");
						userTopTerms = IndexSources.computeTopTermQuery(indexDir);
						saveToInternalStorage(USER_TOP_TERMS_FILENAME,userTopTerms);
						seeUserTopTerms();
					} 
					else if (type.equalsIgnoreCase(ARTICLE)) {									
						File indexDir = new File(applicationDirectory + "/" + IndexSources.ARTICLE_INDEX + "/");
						String[] directory = indexDir.list();
						
						for (String eachDir : directory) {
							File domainDir = new File(applicationDirectory+"/"+ IndexSources.ARTICLE_INDEX + "/" + eachDir + "/");
							List<String> articleTopTerms = IndexSources.computeTopTermQuery(domainDir);
							String filename = eachDir.replace("_index", "");
							domains.add(filename);
							saveToInternalStorage(filename, articleTopTerms);
						}
						for (String domain : domains) {
							System.out.println(domain);
						}
						saveToInternalStorage(SAVED_DOMAINS, domains);
						seeArticleTopTerms();
						
						if (articles && smsGmail) {
							List<String> userPreference = getMatchingDomain(getUserTopTerms(),getDomainList());
							Iterator<String> itr = userPreference.iterator();
							while (itr.hasNext()){
								Log.e("User Preference",itr.next());
							}
						}
					}
					
			
					//IndexSources.computeTopTermQuery();
					Toast.makeText(context, "Finish personalization!", Toast.LENGTH_SHORT).show();
				} catch (Exception e) {				
					e.printStackTrace();
				}
			}
				
		}
		

		public static Activity getActivity() {
			return SettingsActivity.getActivity();
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
			//username = userAccount.name;
			//userName = userAccount.name;
			accountManager.getAuthToken(userAccount, SCOPE, null, this,
					new OnTokenAcquired(), null);
		}
		
		private void invalidateToken() {
			AccountManager accountManager = AccountManager.get(this);
			accountManager.invalidateAuthToken("com.google",
					userToken);
	 
			userToken = null;
		}
		
		private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
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
						Log.e("username",username);
						Log.e("token",userToken);					
						//Use username and token to read Sent Items
						if (userToken != null && smsGmail == false) {					
							new readGmailTask().execute(username,userToken,applicationDirectory);							
						}
						else if (userToken !=null && smsGmail == true) {
							new readSMSGmailTask().execute(username,userToken,applicationDirectory);
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
			private class readGmailTask extends AsyncTask <String,Void,String>{
				Context context = getApplicationContext();
				
				@Override
				protected String doInBackground(String... params) {			
					ReadGMail gmailClass = new ReadGMail();
					StringBuilder gmailMessageString = new StringBuilder();
					
						 
					try {
							gmailMessages = gmailClass.readSentItems(params[0], params[1], params[2]);
					} catch (Exception e) {
							e.printStackTrace();
					}								
					
					if (gmailMessages.size() > 0) {
						Iterator<String> iterator = gmailMessages.iterator();
						while (iterator.hasNext()) 
							gmailMessageString.append(iterator.next());
					}
					
						 					 
					return gmailMessages.toString();
				}
				
				@Override
				protected void onPostExecute(String content) {								
					new indexSourcesTask().execute(applicationDirectory,USER,content);
					Toast.makeText(context, "Finish learning from Gmail!.", Toast.LENGTH_SHORT).show();
				}
					
			}
			
			// AsyncTask to read SMS & Gmail
			private class readSMSGmailTask extends AsyncTask <String,Void,String> {
				Context context = getApplicationContext();
				
				@Override
				protected String doInBackground(String... params) {			
					ReadGMail gmailClass = new ReadGMail();
					StringBuilder messageString = new StringBuilder();
					
					try {
						gmailMessages= gmailClass.readSentItems(params[0], params[1], params[2]);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					Iterator<String> iterator = gmailMessages.iterator();
					while (iterator.hasNext()) 
						messageString.append(iterator.next());
					
					
					final ReadSMS ReadSMS = new ReadSMS();
					messages = ReadSMS.getOutboxSms(context);
					
					Iterator<String> itr = messages.iterator();

					while (itr.hasNext())
						messageString.append(itr.next().replaceAll("[0-9]",""));
									
					
					return messageString.toString();
				}
				
				@Override
				protected void onPostExecute(String content) {	
					new indexSourcesTask().execute(applicationDirectory,USER,content);
					Toast.makeText(context, "Finish learning from Gmail & SMS!.", Toast.LENGTH_SHORT).show();
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
			myExternalFile = new File(applicationDirectory, SMSSourceFile);
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
	        			 
			 new indexSourcesTask().execute(applicationDirectory,USER);	 
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
		
		public void saveToInternalStorage(String filename, List<String> data) throws IOException {
			try {
				FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
				ObjectOutputStream of = new ObjectOutputStream(fos);
				of.writeObject(data);
	            of.flush();
	            of.close();
	            fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	
		}
		
		@SuppressWarnings("unchecked")
		public List<String> readFromInternalStorage(String filename) throws ClassNotFoundException {
			List<String> topTermList = null;
			FileInputStream fis;
			try {
				fis = openFileInput(filename);
				ObjectInputStream oi = new ObjectInputStream(fis);
				topTermList = (List<String>) oi.readObject();
				oi.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return topTermList;
		}

		
		public void seeArticleTopTerms() throws ClassNotFoundException {
			List<String> domainList = readFromInternalStorage(SAVED_DOMAINS);
			
			for (int i=0; i<domainList.size();i++) {
				List<String> domainTopTerms = readFromInternalStorage(domainList.get(i));
				System.out.println("Domain is: " + domainList.get(i));
				for (String terms : domainTopTerms) {
					System.out.println(terms);
				}
			}
			
		}
		
		public void seeUserTopTerms() throws ClassNotFoundException {
			List<String> userTerms = readFromInternalStorage(USER_TOP_TERMS_FILENAME);
			for (String terms : userTerms) {
				System.out.println(terms);
			}		
		}
		
		private List<String> getUserTopTerms() throws ClassNotFoundException {
			return readFromInternalStorage(USER_TOP_TERMS_FILENAME);
		}
		
		private List<String> getDomainList() throws ClassNotFoundException {
			return readFromInternalStorage(SAVED_DOMAINS);
		}
		
		private static int COMPARE_TOP = 50;
		
		private List<String> getMatchingDomain(List<String> userTopTerms, List<String> domainList) throws ClassNotFoundException {
			int bestMatch = -1;
			int secondMatch = -1;
			int matchingNumber = 0;
			int highest = 0;
			int secondHighest = 0;	
			List<String> topMatchingList = new ArrayList<String>();
			
			for (int i=0; i<domainList.size();i++) {
				List<String> domainTopTerms = readFromInternalStorage(domainList.get(i));
				int max=0;
				int domainSize = domainTopTerms.size();
				int userSize = userTopTerms.size();
				
				if (domainSize >= COMPARE_TOP && userSize >= COMPARE_TOP)
					max = COMPARE_TOP;
				else
					max = (domainSize > userSize) ? userSize : domainSize;
				domainTopTerms.subList(0, max);
				List<String> userLess = userTopTerms;
				userLess.subList(0, max);
				
				userLess.retainAll(domainTopTerms);
				matchingNumber = userLess.size();
				if (matchingNumber > highest) {
					highest = matchingNumber;
					bestMatch = i;
					continue;
				}
				else if (matchingNumber > secondHighest ){
					secondHighest = matchingNumber;
					secondMatch = i;
				}
			}
			
			if (bestMatch == -1 && secondMatch == -1)
				return null;
			
			topMatchingList.add(domainList.get(bestMatch));
			topMatchingList.add(domainList.get(secondMatch));
			
			return topMatchingList;
		}
}
