package com.agileblaze.weatherapp;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        bindtopreference(findPreference(getString(R.string.pref_key)));
        bindtopreference(findPreference(getString(R.string.pref_temp_key)));
    }

    private void bindtopreference(Preference preference)
    {
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences
                (preference.getContext()).getString(preference.getKey(),"")
        );
    }



    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue=newValue.toString();
        if(preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefindex = listPreference.findIndexOfValue(stringValue);
            if (prefindex >= 0)
            {
                preference.setSummary(listPreference.getEntries()[prefindex]);
            }
            else
            {
                preference.setSummary(stringValue);
            }
        }
        return true;
    }
}
