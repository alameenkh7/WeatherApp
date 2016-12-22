package com.agileblaze.weatherapp;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.ShareActionProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailedActivityFragment extends Fragment {
    private String forecaststr;
    private static final String FORECAST_SHARE_HASHTAG="#AgileblazeWeatherApp";


    public DetailedActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        View rootview = inflater.inflate(R.layout.fragment_detailed, container, false);
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
        {
            forecaststr=intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView)rootview.findViewById(R.id.detailed_text)).setText(forecaststr);
        }
        return rootview;


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailedfragment,menu);
        MenuItem item=menu.findItem(R.id.action_share);
        ShareActionProvider mshareactionprovider=(ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if(mshareactionprovider!=null)
        {
            mshareactionprovider.setShareIntent(createshareforcast());
        }
      else
        {
            Log.d("Error","Share Action Provider is empty");
        }

    }

    private Intent createshareforcast() {
        Intent shareIntent=new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,forecaststr+FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }
}
