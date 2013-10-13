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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.auth.BasicHandle;
import com.androidquery.callback.AjaxStatus;

import android.app.ListActivity;
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
 * {@link android.widget.ListView ListView}, and manually creating (and attaching) a
 * {@link PullToRefreshAttacher} to the view.
 */
public class ListViewActivity extends ListActivity
        implements PullToRefreshAttacher.OnRefreshListener, SearchView.OnQueryTextListener {
	
	private ListView listView;
	private AQuery aq;
	private SearchView mSearchView;
    private TextView mStatusView;

    private static String[] ITEMS = {"Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam",
            "Abondance", "Ackawi", "Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu",
            "Airag", "Airedale", "Aisy Cendre", "Allgauer Emmentaler", "Abbaye de Belloc",
            "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi", "Acorn", "Adelost",
            "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
            "Allgauer Emmentaler"};

    private PullToRefreshAttacher mPullToRefreshAttacher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        aq = new AQuery(this);
        /**
         * Get ListView and give it an adapter to display the sample items
         */
        ListView listView = getListView();
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                ITEMS);
        listView.setAdapter(adapter);

        /**
         * Here we create a PullToRefreshAttacher manually without an Options instance.
         * PullToRefreshAttacher will manually create one using default values.
         */
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

        // Set the Refreshable View to be the ListView and the refresh listener to be this.
        mPullToRefreshAttacher.addRefreshableView(listView, this);
    }

    @Override
    public void onRefreshStarted(View view) {
        /**
         * Simulate Refresh with 4 seconds sleep
         */
    	Log.d("refresh", "true");
    	asyncSearch("flood singapore");
    	
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
        Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
        asyncSearch(query);
        return true;
    }
    
	public void asyncSearch(String query){
	        
			String url;
	       	// Bing API key
			BasicHandle handle = new BasicHandle("", "YQXjYv6h7vSirRi7qvsx63yhRICOurpWOwG2DSL/lkk");
			
			try {
				url = "https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27news%27&Query=%27" + URLEncoder.encode(query, "UTF-8") + "%27&Market=%27en-SG%27&$format=JSON";
				Log.d("url", "https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27news%27&Query=%27" + URLEncoder.encode(query, "UTF-8") + "%27&Market=%27en-SG%27&$format=JSON");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				Toast.makeText(this, "Query not supported!", Toast.LENGTH_SHORT).show();
				return;
			}
			
	        //String url = "https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27news%27&Query=%27flood+singapore%27&Market=%27en-SG%27&$format=JSON";             
	        aq.ajax(url, JSONObject.class, this, "jsonCallback");
	        aq.auth(handle).ajax(url, JSONObject.class, this, "jsonCallback");

	        
	        //https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27news%27&Query=%27flood+singapore%27&Market=%27en-SG%27&$format=JSON
	       
	}
	
	public void jsonCallback(String url, JSONObject json, AjaxStatus status){
	        
		Log.d("check", "here");
		
	        if(json != null){ 
	        	Log.d("json", json.toString());
	               // json.toString();      
	        }else{          
	        	Log.d("json", "null");
	        }
	    mPullToRefreshAttacher.setRefreshComplete();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_refresh:
	        	mPullToRefreshAttacher.setRefreshing(true);
	        	asyncSearch("flood singapore");
	            return true;
	        case R.id.action_settings:
	        	Intent settingsPage = new Intent(this, SettingsActivity.class);
	        	//settingsPage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
	        	startActivity(settingsPage);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	
	/*
	Intent reviewShop = new Intent(MyApplication.currentContext, WelcomeActivity.class);
	reviewShop.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
	startActivity(reviewShop);
	getActivity().finish();
	*/


    
}
