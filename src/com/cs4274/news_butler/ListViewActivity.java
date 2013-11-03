package com.cs4274.news_butler;

/*
 * Copyright 2013 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.auth.BasicHandle;
import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.Transformer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.ListActivity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ActionBar;
import android.content.Intent;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
/**
 * This sample shows how to use ActionBar-PullToRefresh with a
 * {@link android.widget.ListView ListView}, and manually creating (and
 * attaching) a {@link PullToRefreshAttacher} to the view.
 */
public class ListViewActivity extends ListActivity implements
		PullToRefreshAttacher.OnRefreshListener, SearchView.OnQueryTextListener {

	
	private ListView listView;
	private AQuery aq;
	private SearchView mSearchView;
	private TextView mStatusView;

	private static String[] ITEMS = { "Abbaye de Belloc",
			"Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
			"Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu",
			"Airag", "Airedale", "Aisy Cendre", "Allgauer Emmentaler",
			"Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam",
			"Abondance", "Ackawi", "Acorn", "Adelost", "Affidelice au Chablis",
			"Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
			"Allgauer Emmentaler" };

	private PullToRefreshAttacher mPullToRefreshAttacher;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		aq = new AQuery(this);
		/**
		 * Get ListView and give it an adapter to display the sample items
		 */
		listView = getListView();
		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

		// Set the Refreshable View to be the ListView and the refresh listener
		// to be this.
		mPullToRefreshAttacher.addRefreshableView(listView, this);
		
		if (SettingsActivity.smsLastLearnedDate != null || SettingsActivity.facebookLastLearnedDate != null
				|| SettingsActivity.gmailLastLearnedDate != null) {
			List<String> userPreference = new ArrayList<String>();
			mPullToRefreshAttacher.setRefreshing(true);
			try {
				userPreference = readFromInternalStorage(SettingsActivity.USER_PREFERENCE);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			if (userPreference.size()== 0)
				asyncSearch("Singapore");
			else if (userPreference.size() > 0) {
				asyncSearch(userPreference.get(0));
			}
		}
		
	}
	
	@Override
	 public void onResume() {
		super.onResume();
		
		if (SettingsActivity.change == true) {
			if (SettingsActivity.sms == true || SettingsActivity.gmail == true) {
				List<String> userPreference = new ArrayList<String>();
				mPullToRefreshAttacher.setRefreshing(true);
				
				try {
					userPreference = readFromInternalStorage(SettingsActivity.USER_PREFERENCE);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				if (userPreference.size()== 0)
					asyncSearch("Singapore");
				else if (userPreference.size() > 0) {
					asyncSearch(userPreference.get(0));
				}
			}
		}
	}
	
	

	

	@Override
	public void onRefreshStarted(View view) {
		/**
		 * Simulate Refresh with 4 seconds sleep
		 */
		Log.d("refresh", "true");
		if (SettingsActivity.smsLastLearnedDate != null || SettingsActivity.facebookLastLearnedDate != null
				|| SettingsActivity.gmailLastLearnedDate != null) {
			List<String> userPreference = new ArrayList<String>();
			
			try {
				userPreference = readFromInternalStorage(SettingsActivity.USER_PREFERENCE);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			if (userPreference.size()== 0)
				asyncSearch("Singapore");
			else if (userPreference.size() > 0) {
				asyncSearch(userPreference.get(0));
			}
		} else {
			asyncSearch("Singapore");
		}
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		Log.d("check", "Inflating");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		mSearchView = (SearchView) searchItem.getActionView();
		mSearchView.setOnQueryTextListener(this);

		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {

		mPullToRefreshAttacher.setRefreshing(true);
		asyncSearch(query);
		return true;
	}

	private static class GsonTransformer implements Transformer {

		public <T> T transform(String url, Class<T> type, String encoding,
				byte[] data, AjaxStatus status) {
			Log.d("trans", "transforming");
			System.out.println(data);
			Gson g = new Gson();
			T result = g.fromJson(new String(data),
					new TypeToken<ArrayList<NewsItem>>() {
					}.getType());
			return result;
		}
	}

	public void asyncSearch(String query) {

		String url;
		// Bing API key
		BasicHandle handle = new BasicHandle("",
				"YQXjYv6h7vSirRi7qvsx63yhRICOurpWOwG2DSL/lkk");

		try {
			url = "https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27news%27&Query=%27"
					+ URLEncoder.encode(query, "UTF-8")
					+ "%27&Market=%27en-SG%27&$format=JSON";
			Log.d("url",
					"https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27news%27&Query=%27"
							+ URLEncoder.encode(query, "UTF-8")
							+ "%27&Market=%27en-SG%27&$format=JSON");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "Query not supported!", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// String url =
		// "https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27news%27&Query=%27flood+singapore%27&Market=%27en-SG%27&$format=JSON";
		// aq.ajax(url, JSONObject.class, this, "jsonCallback");
		aq.auth(handle).ajax(url, JSONObject.class, this, "jsonCallback");

		// https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27news%27&Query=%27flood+singapore%27&Market=%27en-SG%27&$format=JSON

	}

	public void jsonCallback(String url, JSONObject json, AjaxStatus status) {

		Log.d("check", "here");
		ArrayList<NewsItem> temp = null;
		if (json != null) {
			try {

				Log.d("json", json.getJSONObject("d").getJSONArray("results")
						.optJSONObject(0).getJSONArray("News").toString());
				JSONArray nl = json.getJSONObject("d").getJSONArray("results")
						.optJSONObject(0).getJSONArray("News");
				Gson g = new Gson();
				temp = g.fromJson(nl.toString(),
						new TypeToken<ArrayList<NewsItem>>() {
						}.getType());
				Log.d("converting", "here");
				if (temp != null)
					System.out.println("nl size" + temp.size());
				else
					Log.d("news list", "null");

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// json.toString();
		} else {
			Log.d("json", "null");
		}
		ListViewAdapter adapter = new ListViewAdapter(this, temp);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		mPullToRefreshAttacher.setRefreshComplete();
	}

	public void gsonCallback(String url, ArrayList nl, AjaxStatus status) {
		if (nl != null)
			System.out.println("nl size" + nl.size());
		else
			Log.d("news list", "null");
		mPullToRefreshAttacher.setRefreshComplete();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_refresh:
			mPullToRefreshAttacher.setRefreshing(true);
			
			if (SettingsActivity.smsLastLearnedDate != null || SettingsActivity.facebookLastLearnedDate != null
					|| SettingsActivity.gmailLastLearnedDate != null) {
				List<String> userPreference = new ArrayList<String>();
				
				try {
					userPreference = readFromInternalStorage(SettingsActivity.USER_PREFERENCE);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				if (userPreference.size()== 0)
					asyncSearch("Singapore");
				else if (userPreference.size() > 0) {
					asyncSearch(userPreference.get(0));
				}
			} else {
				asyncSearch("Singapore");
			}
			
			return true;
		case R.id.action_settings:
			Intent settingsPage = new Intent(this, SettingsActivity.class);
			startActivity(settingsPage);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		NewsItem item = (NewsItem) l.getAdapter().getItem(position);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(item.getUrl()));
		startActivity(i);
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

}
