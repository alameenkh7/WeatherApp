package com.agileblaze.weatherapp.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by manager on 17/9/16.
 */
public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }


    public void setUp() {
        deleteTheDatabase();
    }


    public void testCreateDb() throws Throwable {

        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());


        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );


        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());


        c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());


        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COL_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COL_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COL_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COL_LOCATION_SETTINGS);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());


        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    public void testLocationTable() {
        insertLocation();
    }


    public void testWeatherTable() {
        long locationRowId=insertLocation();
        assertFalse("ERROR: Location is not insert correctly",locationRowId==-1L);

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values=TestUtilities.createWeatherValues(locationRowId);
        long weatherrowId;
        weatherrowId= db.insert(WeatherContract.WeatherEntry.TABLE_NAME,null,values);
        assertTrue(weatherrowId!=-1);
        Cursor weathercursor=db.query(WeatherContract.WeatherEntry.TABLE_NAME,null,null,null,null,null,null);
        assertTrue("Error : no records returned from weather query", weathercursor.moveToFirst());
        TestUtilities.validateCurrentRecord("Error:Location Query validation failed",weathercursor,values);
        weathercursor.close();
        db.close();
    }


    public long insertLocation() {
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createKochiLocationValues();

        long locationRowId;
        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, testValues);

        assertTrue(locationRowId != -1);

        Cursor cursor = db.query(WeatherContract.LocationEntry.TABLE_NAME,
                null,null,null,null,null,null);

        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );
        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);
        cursor.close();
        db.close();
        return locationRowId;
    }
}
