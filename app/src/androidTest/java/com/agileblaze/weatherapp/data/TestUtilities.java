package com.agileblaze.weatherapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.agileblaze.weatherapp.utils.PolingCheck;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Created by manager on 17/9/16.
 */
public class TestUtilities extends AndroidTestCase{

    static final String TEST_LOCATION = "kochi";
    static final long TEST_DATE = 1419033600L;

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherContract.WeatherEntry.COL_LOC_KEY, locationRowId);
        weatherValues.put(WeatherContract.WeatherEntry.COL_DATE, TEST_DATE);
        weatherValues.put(WeatherContract.WeatherEntry.COL_DEGREE, 1.1);
        weatherValues.put(WeatherContract.WeatherEntry.COL_HUMDITY, 1.2);
        weatherValues.put(WeatherContract.WeatherEntry.COL_PRESSURE, 1.3);
        weatherValues.put(WeatherContract.WeatherEntry.COL_MAX_TEMP, 75);
        weatherValues.put(WeatherContract.WeatherEntry.COL_MIN_TEMP, 65);
        weatherValues.put(WeatherContract.WeatherEntry.COL_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherContract.WeatherEntry.COL_WIND, 5.5);
        weatherValues.put(WeatherContract.WeatherEntry.COL_WEATHER_ID, 321);

        return weatherValues;
    }


    static ContentValues createKochiLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(WeatherContract.LocationEntry.COL_LOCATION_SETTINGS, TEST_LOCATION);
        testValues.put(WeatherContract.LocationEntry.COL_CITY_NAME, "kochi");
        testValues.put(WeatherContract.LocationEntry.COL_LAT, 9.9312);
        testValues.put(WeatherContract.LocationEntry.COL_LONG, 76.2673);

        return testValues;
    }


    static long insertKochiLocationValues(Context context) {
        // insert our test records into the database
        WeatherDbHelper dbHelper = new WeatherDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createKochiLocationValues();

        long locationRowId;
        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Kochi Location Values", locationRowId != -1);

        return locationRowId;
    }




    static class TestContentObserver extends ContentObserver{
        final HandlerThread mHT;
        boolean mContentChanged;

        public TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
           mHT = ht;
        }

        static   TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

      public void waitfornotificationorfail()
      {
          new PolingCheck(5000)
          {

              @Override
              protected boolean check() {
                  return mContentChanged;
              }
          }.run();
          mHT.quit();
      }
    }

}
