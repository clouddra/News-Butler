package com.cs4274.news_butler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.androidquery.AQuery;
import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.Transformer;
import com.cs4274.news_butler.helper.FBSQLiteHelper;
import com.cs4274.news_butler.helper.IndexSources;
import com.cs4274.news_butler.helper.ReadGMail;
import com.cs4274.news_butler.helper.ReadSMS;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
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
	private static final String SMS_LAST_LEARNED = "smslastlearned";
	private static final String FACEBOOK_LAST_LEARNED = "facebooklastlearned";
	private static final String GMAIL_LAST_LEARNED = "gmaillastlearned";
	
	public static final String USER_TOP_TERMS_FILENAME = "userTopTerms";
	public static final String SAVED_DOMAINS = "savedDomains";
	public static final String USER_PREFERENCE = "userPreference";
	public static final String DOMAIN_SCORE_FILE = "Score.txt";

	private String applicationDirectory = getIndexDirectory();
	public static final String suffix = ".txt";
	public static final String USER = "user";
	public static final String ARTICLE = "article";
	public static String smsLastLearnedDate = null;
	public static String facebookLastLearnedDate = null;
	public static String gmailLastLearnedDate = null;
	public static boolean facebook = false;
	public static boolean sms = false;
	public static boolean gmail = false;
	public static boolean smsGmail = false;
	public static boolean articles = false;
	public static boolean change = false;
	private static String username = null;
	private static String userToken = null;
	private long gmailpreviousLearned = -100L;
	public List<String> messages = new ArrayList<String>();
	public List<String> gmailMessages = new ArrayList<String>();
	public static List<String> userTopTerms;
	public List<String> domains = new ArrayList<String>();
	public static Set<String> hashedArticles;
	private Set<String> emptySet = new HashSet<String>();
	File myExternalFile;

	public static final String FB_APP_ID = "514365765304596";
	public static final String FB_PERMISSIONS = "read_mailbox";
	public static final String SERVER_URL ="42.60.140.137:5000";
	private AQuery aq;
	private FBSQLiteHelper datasource;

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		accountManager = AccountManager.get(this);

		aq = new AQuery(this);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.settings);


		datasource = new FBSQLiteHelper(this);
		SharedPreferences control = getSharedPreferences(CONTROL, 0);
		sms = control.getBoolean(LEARNT_SMS, false);
		gmail = control.getBoolean(LEARNT_GMAIL, false);
		facebook = control.getBoolean(LEARNT_FACEBOOK, false);
		smsGmail = control.getBoolean(LEARNT_SMS_GMAIL, false);
		articles = control.getBoolean(LEARNT_ARTICLES, false);
		smsLastLearnedDate = control.getString(SMS_LAST_LEARNED, null);
		facebookLastLearnedDate = control.getString(FACEBOOK_LAST_LEARNED, null);
		gmailLastLearnedDate = control.getString(GMAIL_LAST_LEARNED, null);
		
		
		if (smsLastLearnedDate != null) {
			CustomPreference smsPreference = (CustomPreference) findPreference("learn_sms");
			smsPreference.setText(smsLastLearnedDate);
		}
		
		if (facebookLastLearnedDate != null) {
			CustomPreference fbPreference = (CustomPreference) findPreference("learn_facebook");
			fbPreference.setText(facebookLastLearnedDate);			
		}
		
		if (gmailLastLearnedDate != null) {
			CustomPreference gmailPreference = (CustomPreference) findPreference("learn_gmail");
			gmailPreference.setText(gmailLastLearnedDate);
		}
		
		
		SharedPreferences hashedSet = getSharedPreferences(HASHMAP, 0);
		hashedArticles = hashedSet.getStringSet(SET, emptySet);


		CustomPreference smsPreference = (CustomPreference) findPreference("learn_sms");
		smsPreference
				.setOnPreferenceClickListener(new CustomPreference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						Date previousLearned = null;
						if ( ((CustomPreference) preference).getText() == "") {
							sms = true;
							learnSMS(-1L);
							return false;
						}
						else {
							SimpleDateFormat df = new SimpleDateFormat(
									"MM/dd/yyyy HH:mm:ss");							
							try {
								previousLearned = df
										.parse(((CustomPreference) preference)
												.getText());
							} catch (ParseException e) {							
								e.printStackTrace();
							}
							learnSMS(getUnixTime(previousLearned));
							return false;
						}						
					}
				});

		CustomPreference gmailPreference = (CustomPreference) findPreference("learn_gmail");
		gmailPreference
				.setOnPreferenceClickListener(new CustomPreference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						Date previousLearned = null;
						if ( ((CustomPreference) preference).getText() == "") {
							gmail = true;
							learnGmail(-1L);
							return false;
						}
						else {
							SimpleDateFormat df = new SimpleDateFormat(
									"MM/dd/yyyy HH:mm:ss");							
							try {
								previousLearned = df
										.parse(((CustomPreference) preference)
												.getText());
							} catch (ParseException e) {							
								e.printStackTrace();
							}
							learnGmail(getUnixTime(previousLearned));
							return false;
						}
						
					}
				});

		CustomPreference fbPreference = (CustomPreference) findPreference("learn_facebook");
		fbPreference
				.setOnPreferenceClickListener(new CustomPreference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						Date previousLearned = null;
						if (((CustomPreference) preference).getText() == "") {
							Log.d("custom pref", "empty pref");
							facebook = true;
							auth_facebook(-1L);
							return false;
						} else {
							SimpleDateFormat df = new SimpleDateFormat(
									"MM/dd/yyyy HH:mm:ss");
							try {
								Log.d("custom pref",
										((CustomPreference) preference)
												.getText());
								previousLearned = df
										.parse(((CustomPreference) preference)
												.getText());
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								return false;
							}

							auth_facebook(getUnixTime(previousLearned));
							return false;
						}

					}
				});
		

	}

	public void auth_facebook(long previousLearned) {

		FacebookHandle handle = new FacebookHandle(this, FB_APP_ID,
				FB_PERMISSIONS);
		String url;
		if (previousLearned == -1L)
			url = "https://graph.facebook.com/me?fields=inbox.fields(comments.limit(5000))";
		else
			url = "https://graph.facebook.com/me?fields=inbox.fields(comments.limit(5000).since("
					+ previousLearned + "))";
		Log.d("url", url);
		aq.auth(handle).ajax(url, JSONObject.class, this, "facebookCb");

	}

	public void facebookCb(String url, JSONObject json, AjaxStatus status) {
		Log.d("fb", json.toString());		
		new storeFBTask().execute(json);

	}

	private class storeFBTask extends
			AsyncTask<JSONObject, Void, String> {
		Context context = getApplicationContext();

		@Override
		protected String doInBackground(JSONObject... params) {
			// TODO Auto-generated method stub
			JSONObject json = params[0];
			//Queue<Pair<String, Long>> messageEntries = new LinkedList<Pair<String, Long>>();
			try {
				JSONArray conversations = json.getJSONObject("inbox").getJSONArray(
						"data");
				for (int i = 0; i < conversations.length(); i++) {
					JSONArray comments = conversations.getJSONObject(i)
							.getJSONObject("comments").getJSONArray("data");
					// Log.d("fb comments", comments.toString());

					for (int j = 0; j < comments.length(); j++) {
						JSONObject currentMessage = comments.getJSONObject(j);
						try {
							String message = currentMessage.getString("message");
							String createdTime = currentMessage
									.getString("created_time");

	
							final String pattern = "yyyy-MM-dd'T'hh:mm:ssZ";
							final SimpleDateFormat sdf = new SimpleDateFormat(
									pattern);
							Date date = sdf.parse(createdTime);
							date.getTime();
							 Log.d("fb date", String.valueOf(getUnixTime(date)));
							 Log.d("fb string", message + createdTime);

							datasource.addMessage(message, getUnixTime(date));

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			;
			} catch (JSONException e) {
				// e.printStackTrace();
			}

			// datasource.close();
					
			//Log.d("db data", concatenateString(datasource.getMessages()));
			
			// check for messages after timing. Should return empty
			//Log.d("db data after", concatenateString(datasource.getMessagesAfter(1583389550)));
			
			String content = concatenateString(datasource.getMessagesAddWeightRecent(getTodayDateLong()));
			datasource.close();
          indexSources( applicationDirectory, USER, content);

			return content;
		}
		
		protected void onPostExecute(String content){
			//new indexSourcesTask().execute(applicationDirectory, USER, content);
					
			CustomPreference fbPreference = (CustomPreference) findPreference("learn_facebook");
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			
			// Get the date today using Calendar object.
			Date today = Calendar.getInstance().getTime();
			// Using DateFormat format method we can create a string
			// representation of a date with the defined format.
			String reportDate = df.format(today);

			fbPreference.setText(reportDate);
			facebookLastLearnedDate = reportDate;
			
			Toast.makeText(getApplicationContext(),
					"Finished adding FB to database!",
					Toast.LENGTH_SHORT).show();
			
		}

	}

	public long getUnixTime(Date d) {
		return d.getTime() / 1000L;
	}
	
	public String concatenateString(Vector<String> s){
		StringBuilder builder = new StringBuilder("");
		for (String currentString: s)
			builder.append(currentString).append(" ");
		return builder.toString();
	}

	public boolean onOptionsItemsSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();

		SharedPreferences control = getSharedPreferences(CONTROL, 0);
		sms = control.getBoolean(LEARNT_SMS, false);
		gmail = control.getBoolean(LEARNT_GMAIL, false);
		facebook = control.getBoolean(LEARNT_FACEBOOK, false);
		smsGmail = control.getBoolean(LEARNT_SMS_GMAIL, false);
		articles = control.getBoolean(LEARNT_ARTICLES, false);
		smsLastLearnedDate = control.getString(SMS_LAST_LEARNED, null);
		facebookLastLearnedDate = control.getString(FACEBOOK_LAST_LEARNED, null);
		gmailLastLearnedDate = control.getString(GMAIL_LAST_LEARNED, null);
		// username = control.getString(USERNAME, null);
		// userToken = control.getString(TOKEN, null);

		SharedPreferences hashedSet = getSharedPreferences(HASHMAP, 0);
		hashedArticles = hashedSet.getStringSet(SET, emptySet);
	}

	@Override
	public void onPause() {
		super.onPause();

		SharedPreferences control = getSharedPreferences(CONTROL, 0);
		SharedPreferences.Editor editor = control.edit();
		editor.putBoolean(LEARNT_SMS, sms);
		editor.putBoolean(LEARNT_GMAIL, gmail);
		editor.putBoolean(LEARNT_FACEBOOK, facebook);
		editor.putBoolean(LEARNT_SMS_GMAIL, smsGmail);
		editor.putBoolean(LEARNT_ARTICLES, articles);
		editor.putString(SMS_LAST_LEARNED, smsLastLearnedDate);
		editor.putString(FACEBOOK_LAST_LEARNED, facebookLastLearnedDate);
		editor.putString(GMAIL_LAST_LEARNED, gmailLastLearnedDate);
		// editor.putString(USERNAME, username);
		// editor.putString(TOKEN, userToken);
		editor.commit();

		SharedPreferences hashedSet = getSharedPreferences(HASHMAP, 0);
		SharedPreferences.Editor editor2 = hashedSet.edit();
		editor2.putStringSet(SET, hashedArticles);
		editor2.commit();

	}

	/*
	 * This method is invoked when the button getSMS is pressed, to learn the
	 * user's SMS for preference
	 */
	public void learnSMS(long previousLearned) {	
		if ( previousLearned == -1L) {
			new readSMSTask().execute(-1L);
		}
		else {
			new readSMSTask().execute(previousLearned);
		}
		
	}

	/*
	 * This method is invoked when the button getGmail is pressed, to learn the
	 * user's Gmail for preference
	 */
	public void learnGmail(long previousLearned) {
		if (isConnectedToInternet()) {
			gmailpreviousLearned = previousLearned;
			chooseAccount();			
		}
		else {
			Toast.makeText(getApplicationContext(),
					"Unable to connect to Gmail, please turn on your Data",
					Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 * AsyncTask to read SMS and call the indexing method
	 */
	private class readSMSTask extends AsyncTask<Long, Void, String> {
		Context context = getApplicationContext();

		@Override
		protected String doInBackground(Long... params) {
			final ReadSMS ReadSMS = new ReadSMS();
			ReadSMS.storeOutboxSms(context, datasource, params[0] );

			String content = concatenateString(datasource.getMessagesAddWeightRecent(getTodayDateLong()));
			datasource.close();
          indexSources( applicationDirectory, USER, content);

			return content;
		}

		@Override
		protected void onPostExecute(String content) {
			//new indexSourcesTask().execute(applicationDirectory, USER, content);
			
			CustomPreference smsPreference = (CustomPreference) findPreference("learn_sms");
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");		
			Date today = Calendar.getInstance().getTime();
			String reportDate = df.format(today);
			smsPreference.setText(reportDate);
			smsLastLearnedDate = reportDate;		
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
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
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
			Toast.makeText(this, "Disallowed permission", Toast.LENGTH_SHORT)
					.show();
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
		accountManager.invalidateAuthToken("com.google", userToken);

		userToken = null;
	}

	/*
	 * Method to pull Gmail messages when the token is acquired
	 */
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
					Log.e("username", username);
					Log.e("token", userToken);
					
					// Use username and token to read Sent Items
					if (userToken != null && gmailpreviousLearned != -100L) {
						new readGmailTask().execute(username, userToken,
								applicationDirectory);
					} 
					else {
						Toast.makeText(context,
								"Unable to authenticate with Gmail",
								Toast.LENGTH_SHORT).show();
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/*
	 * AsyncTask to pull Gmail messages and call the indexing method
	 */
	private class readGmailTask extends AsyncTask<String, Void, String> {
		Context context = getApplicationContext();

		@Override
		protected String doInBackground(String... params) {
			ReadGMail gmailClass = new ReadGMail();
			StringBuilder gmailMessageString = new StringBuilder();

			try {
				gmailClass.readSentItems(params[0], params[1],
						params[2],datasource, gmailpreviousLearned);
			} catch (Exception e) {
				e.printStackTrace();
			}

			String content = concatenateString(datasource.getMessagesAddWeightRecent(getTodayDateLong()));
			datasource.close();
          indexSources( applicationDirectory, USER, content);

			return content;
		}

		@Override
		protected void onPostExecute(String content) {
			//new indexSourcesTask().execute(applicationDirectory, USER, content );
			
			CustomPreference gmailPreference = (CustomPreference) findPreference("learn_gmail");
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");		
			Date today = Calendar.getInstance().getTime();
			String reportDate = df.format(today);
			gmailPreference.setText(reportDate);
			gmailLastLearnedDate = reportDate;
		}

	}

	/*
	 * AsyncTask to index the sources
	 */
	public class indexSourcesTask extends AsyncTask<String, Void, String> {

		Context context = getApplicationContext();

		@Override
		protected String doInBackground(String... params) {
			try {
				IndexSources.createIndex(params[0], params[1], params[2]);

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
					File indexDir = new File(applicationDirectory + "/"
							+ IndexSources.USER_INDEX + "/");
					userTopTerms = IndexSources.computeTopTermQuery(indexDir);
					saveToInternalStorage(USER_TOP_TERMS_FILENAME, userTopTerms);
					seeUserTopTerms();
					
					fetchTerms();

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
   
	/*
	 * Method to index the sources
	 */
	public class indexSources(String applicationDirectory, String type, String content) {

				IndexSources.createIndex(applicationDirectory, type, content);

			try {
				if (type.equalsIgnoreCase(USER)) {
					File indexDir = new File(applicationDirectory + "/"+ IndexSources.USER_INDEX + "/");
					userTopTerms = IndexSources.computeTopTermQuery(indexDir);
					saveToInternalStorage(USER_TOP_TERMS_FILENAME, userTopTerms);
					seeUserTopTerms();
					
					fetchTerms();

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	
	private void fetchTerms(){
		
		String url = "http://" + SERVER_URL + "/GET/terms";
		Log.d("server_url", url);

        aq.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String response, AjaxStatus status) {
            	List<Domain> topTerms;
            	Gson gson = new Gson();
            	topTerms = gson.fromJson(response, new TypeToken<List<Domain>>() {}.getType());
            				
    			// call scoring function here
            	try {
					List<String> userPreference = getMatchingDomain(getUserTopTerms(), topTerms);
					
					saveToInternalStorage(USER_PREFERENCE, userPreference);
					
					Toast.makeText(getBaseContext(), "Finish learning your preference!",
							Toast.LENGTH_LONG).show();
					change = true;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            
    });

	}

	/******************************************************************
	 * Helper methods
	 ******************************************************************/
	private String getIndexDirectory() {
		File dir;
		String state = Environment.getExternalStorageState();
		if (state.equalsIgnoreCase(Environment.MEDIA_MOUNTED) == true) {
			dir = new File(Environment.getExternalStorageDirectory(),
					"NewsButler");
		} else {
			dir = new File("/mnt/sdcard", "NewsButler");
		}
		if (!dir.exists())
			dir.mkdirs();

		return dir.getAbsolutePath();
	}

	private boolean isConnectedToInternet() {
		Context context = getApplicationContext();
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();
		return isConnected;

	}

	public void saveToInternalStorage(String filename, List<String> data)
			throws IOException {
		try {
			FileOutputStream fos = openFileOutput(filename,
					Context.MODE_PRIVATE);
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
	public List<String> readFromInternalStorage(String filename)
			throws ClassNotFoundException {
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

	public void seeUserTopTerms() throws ClassNotFoundException {
		List<String> userTerms = readFromInternalStorage(USER_TOP_TERMS_FILENAME);
		for (String terms : userTerms) {
			System.out.println(terms);
		}
	}

	private List<String> getUserTopTerms() throws ClassNotFoundException {
		return readFromInternalStorage(USER_TOP_TERMS_FILENAME);
	}

	
	private long getTodayDateLong() {
		Calendar c = Calendar.getInstance();
		Long todayDate = c.getTimeInMillis();
		
		return todayDate/1000;
	}


	private static int COMPARE_TOP = 40;
	private static double SECOND_PREFERENCE_PERCENTAGE = 0.8;

	/*
	 * Method to get the matching domain for the user's preference
	 */
	private List<String> getMatchingDomain(List<String> userTopTerms,
			List<Domain> domainList) throws ClassNotFoundException {
		
		List<String> topMatchingList = new ArrayList<String>();
		Vector<Integer> domainScore = new Vector<Integer>(domainList.size());
		Vector<String> domainNames = new Vector<String>(domainList.size());
		
		int score = 0;
		int[] termScoreArray = new int[COMPARE_TOP];

		for (int n = 0; n < COMPARE_TOP; n++) {
			termScoreArray[n] = COMPARE_TOP - n;
		}

		for (int i = 0; i < domainList.size(); i++) {
			List<String> domainTopTerms = Arrays.asList(domainList.get(i).getDomainTerms());
			domainNames.add(domainList.get(i).getDomainName());
			
			int max = 0;
			int domainSize = domainTopTerms.size();
			int userSize = userTopTerms.size();

			if (domainSize >= COMPARE_TOP && userSize >= COMPARE_TOP)
				max = COMPARE_TOP;
			else
				max = (domainSize > userSize) ? userSize : domainSize;
			
			domainTopTerms = domainTopTerms.subList(0, max);
			List<String> userLess = userTopTerms;
			userLess = userLess.subList(0, max);

			score = 0;
			for (int article = 0; article < max; article++) {
				for (int user = 0; user < max; user++) {
					if (domainTopTerms.get(article).equalsIgnoreCase(
							userLess.get(user)))
						score = score + termScoreArray[article] + termScoreArray[user];
				}
			}

			domainScore.add(score);

			Log.e("Domain Score", "Domain: " + domainNames.get(i) + " ; Score: "
					+ score);
		}

		int bestMatch = -1;
		int secondMatch = -1;
		int highest = 0;
		int secondHighest = 0;

		for (int index = 0; index < domainScore.capacity(); index++) {

			int temp = domainScore.get(index);

			if (temp > highest) {
				highest = temp;
				bestMatch = index;
				continue;
			} else if (temp > secondHighest) {
				secondHighest = temp;
				secondMatch = index;
			}

		}
		if (bestMatch == -1 && secondMatch == -1)
			return null;

		topMatchingList.add(domainNames.get(bestMatch));

		int bestMatchScore = domainScore.get(bestMatch);
		int secondMatchScore = domainScore.get(secondMatch);

		if (secondMatchScore / bestMatchScore >= SECOND_PREFERENCE_PERCENTAGE)
			topMatchingList.add(domainNames.get(secondMatch));

		return topMatchingList;
	}
	
	private class Domain {
		String domain;
		String[] keywords;
		
		public Domain(String d, String[] k){
			this.domain = d;
			this.keywords = k;
		}
		
		public String getDomainName() {
			return this.domain;
		}
		
		public String[] getDomainTerms() {
			return this.keywords;
		}
	}

}
