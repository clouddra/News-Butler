package com.cs4274.news_butler;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class CustomPreference extends Preference {

	public CustomPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.setSummary(super.getPersistedString(""));
	}
	
	public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setSummary(super.getPersistedString(""));
    }

	
	public String getText() {
		return super.getPersistedString("");	
	}
	
	public boolean setText(String s){
		if (super.persistString(s)){
			this.setSummary("Last Learned: " + s);
			return true;
		}
		return false;
	}

	public void reset() {
		setText("");	
	}
	
}
