package com.cs4274.news_butler;

import java.util.ArrayList;
import java.util.Arrays;

import com.androidquery.AQuery;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListViewAdapter extends BaseAdapter {
	private ArrayList<NewsItem> newsList;
	//private Activity activity;
	private static LayoutInflater inflater=null;
	//private AQuery aq;
	
	  static class ViewHolder {
		    public TextView text1;
		    public TextView text2;
		  }
	
	public ListViewAdapter(Activity a, ArrayList<NewsItem> nl){
		//newsList = nl;
		/*
		String[] ITEMS = {"Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam",
	            "Abondance", "Ackawi", "Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu",
	            "Airag", "Airedale", "Aisy Cendre", "Allgauer Emmentaler", "Abbaye de Belloc",
	            "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi", "Acorn", "Adelost",
	            "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
	            "Allgauer Emmentaler"};
		newsList = new ArrayList<String>(Arrays.asList(ITEMS));
		*/
		newsList = nl;
		inflater = (LayoutInflater)a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//activity = a;
		//aq = new AQuery(a); 
	}

	@Override
	public int getCount() {
		return newsList.size();
	}

	@Override
	public NewsItem getItem(int arg0) {
		return newsList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		View vi = convertView;
		ViewHolder holder;
		
		if (vi==null){
            vi = inflater.inflate(R.layout.list_row, null);
            holder = new ViewHolder();
            holder.text1 = (TextView) vi.findViewById(R.id.title);
            holder.text2 = (TextView) vi.findViewById(R.id.content);
            vi.setTag(holder);
		}
		
		else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.text1.setText(newsList.get(arg0).getTitle());
		holder.text2.setText(newsList.get(arg0).getDescription());
		return vi;

	}
    
}