package com.agileblaze.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private ArrayAdapter<String> mForecastadapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_main,menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_refresh)
        {
            updateweather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateweather() {
        FetchWeatherTask weatherTask=new FetchWeatherTask(getActivity(),mForecastadapter);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location=prefs.getString(getString(R.string.pref_key),getString(R.string.pref_default_value));
        weatherTask.execute(location);


    }

    @Override
    public void onStart() {
        super.onStart();
        updateweather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview=inflater.inflate(R.layout.fragment_main,container);


        mForecastadapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_text, new ArrayList<String>());
        ListView listView = (ListView) rootview.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastadapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast=mForecastadapter.getItem(position);
                Intent intent=new Intent(getActivity(),DetailedActivity.class).
                        putExtra(Intent.EXTRA_TEXT,forecast);
                               startActivity(intent);
            }
        });
        return rootview;
    }


}
