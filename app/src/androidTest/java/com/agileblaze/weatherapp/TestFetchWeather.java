package com.agileblaze.weatherapp;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.agileblaze.weatherapp.data.WeatherContract;

/**
 * Created by manager on 20/9/16.
 */
public class TestFetchWeather extends AndroidTestCase {
    static final String ADD_LOCATION_SETTING = "Sunnydale, CA";
    static final String ADD_LOCATION_CITY = "Sunnydale";
    static final double ADD_LOCATION_LAT = 34.425833;
    static final double ADD_LOCATION_LON = -119.714167;



    public void testAddLocation() {

        getContext().getContentResolver().delete(WeatherContract.LocationEntry.CONTENT_URI,
                WeatherContract.LocationEntry.COL_LOCATION_SETTINGS + " = ?",
                new String[]{ADD_LOCATION_SETTING});

        FetchWeatherTask fwt = new FetchWeatherTask(getContext(), null);
        long locationId = fwt.addLocation(ADD_LOCATION_SETTING, ADD_LOCATION_CITY,
                ADD_LOCATION_LAT, ADD_LOCATION_LON);


        assertFalse("Error: addLocation returned an invalid ID on insert",
                locationId == -1);


        for ( int i = 0; i < 2; i++ ) {

            Cursor locationCursor = getContext().getContentResolver().query(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    new String[]{
                            WeatherContract.LocationEntry._ID,
                            WeatherContract.LocationEntry.COL_LOCATION_SETTINGS,
                            WeatherContract.LocationEntry.COL_CITY_NAME,
                            WeatherContract.LocationEntry.COL_LAT,
                            WeatherContract.LocationEntry.COL_LONG
                    },
                    WeatherContract.LocationEntry.COL_LOCATION_SETTINGS + " = ?",
                    new String[]{ADD_LOCATION_SETTING},
                    null);

            if (locationCursor.moveToFirst()) {
                assertEquals("Error: the queried value of locationId does not match the returned value" +
                        "from addLocation", locationCursor.getLong(0), locationId);
                assertEquals("Error: the queried value of location setting is incorrect",
                        locationCursor.getString(1), ADD_LOCATION_SETTING);
                assertEquals("Error: the queried value of location city is incorrect",
                        locationCursor.getString(2), ADD_LOCATION_CITY);
                assertEquals("Error: the queried value of latitude is incorrect",
                        locationCursor.getDouble(3), ADD_LOCATION_LAT);
                assertEquals("Error: the queried value of longitude is incorrect",
                        locationCursor.getDouble(4), ADD_LOCATION_LON);
            } else {
                fail("Error: the id you used to query returned an empty cursor");
            }

            assertFalse("Error: there should be only one record returned from a location query",
                    locationCursor.moveToNext());

            long newLocationId = fwt.addLocation(ADD_LOCATION_SETTING, ADD_LOCATION_CITY,
                    ADD_LOCATION_LAT, ADD_LOCATION_LON);

            assertEquals("Error: inserting a location again should return the same ID",
                    locationId, newLocationId);
        }

        getContext().getContentResolver().delete(WeatherContract.LocationEntry.CONTENT_URI,
                WeatherContract.LocationEntry.COL_LOCATION_SETTINGS + " = ?",
                new String[]{ADD_LOCATION_SETTING});


        getContext().getContentResolver().
                acquireContentProviderClient(WeatherContract.LocationEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}
