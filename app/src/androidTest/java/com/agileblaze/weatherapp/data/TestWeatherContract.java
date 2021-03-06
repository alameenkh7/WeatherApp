package com.agileblaze.weatherapp.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by manager on 20/9/16.
 */
public class TestWeatherContract extends AndroidTestCase {

    private static final String TEST_WEATHER_LOCATION = "/Kochi";
    private static final long TEST_WEATHER_DATE = 1419033600L;  // December 20th, 2014


    public void testBuildWeatherLocation() {
        Uri locationUri = WeatherContract.WeatherEntry.buildWeatherLocation(TEST_WEATHER_LOCATION);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildWeatherLocation in " +
                        "WeatherContract.",
                locationUri);
        assertEquals("Error: Weather location not properly appended to the end of the Uri",
                TEST_WEATHER_LOCATION, locationUri.getLastPathSegment());
        assertEquals("Error: Weather location Uri doesn't match our expected result",
                locationUri.toString(),
                "content://com.agileblaze.weatherapp/weather/%2FKochi");
    }

}
