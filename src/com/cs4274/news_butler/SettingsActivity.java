package com.cs4274.news_butler;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity{
	
	 @SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.settings);
	        CustomPreference facebook = (CustomPreference) findPreference("learn_facebook");
	        Log.d("Custm Pref", facebook.getText());
	        //facebook.setText("Learned");
	        Log.d("Custom Pref Add", facebook.getText());
	        
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
    
	 }

}
