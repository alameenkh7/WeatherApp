package com.agileblaze.weatherapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;
import android.util.Log;

/**
 * Created by manager on 17/9/16.
 */
public class WeatherContract {
    public static final String CONTENT_AUTHORITY = "com.agileblaze.weatherapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static long normalizeDate(long startDate) {
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }



    public static final class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "location";
        public static final String COL_LOCATION_SETTINGS = "location_settings";
        public static final String COL_CITY_NAME = "city_name";
        public static final String COL_LAT = "coord_lat";
        public static final String COL_LONG = "coord_long";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath("location").build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + "location" ;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + "location" ;

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }



    }

    public static final class WeatherEntry implements BaseColumns{
        public static final String TABLE_NAME="weather";
        public static final String COL_LOC_KEY="location_id";
        public static final String COL_DATE="date";
        public static final String COL_WEATHER_ID="weather_id";
        public static final String COL_SHORT_DESC="short_desc";
        public static final String COL_MIN_TEMP="min_temp";
        public static final String COL_MAX_TEMP="max_temp";
        public static final String COL_HUMDITY="humidity";
        public static final String COL_WIND="wind";
        public static final String COL_PRESSURE="pressure";
        public static final String COL_DEGREE="degree";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("weather").build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + "weather";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + "weather";

        public static Uri buildWeatherUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);

        }

        public static Uri buildWeatherLocationWithStartDate(String locationsettings, long startDate) {
            long Date = normalizeDate(startDate);
            return CONTENT_URI.buildUpon().appendPath(locationsettings)
                    .appendQueryParameter(COL_DATE, Long.toString(Date)).build();
        }

        public static Uri buildWeatherLocation(String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithDate(String locationSetting, long date) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                    .appendPath(Long.toString(normalizeDate(date))).build();
        }

        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getDateFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static long getStartDateFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(COL_DATE);
            if (null != dateString && dateString.length() > 0)
                return Long.parseLong(dateString);
            else
                return 0;
        }
    }


}
