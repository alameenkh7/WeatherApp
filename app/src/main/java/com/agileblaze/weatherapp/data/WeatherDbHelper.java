package com.agileblaze.weatherapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by manager on 17/9/16.
 */
public class WeatherDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "weather.db";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + WeatherContract.LocationEntry.TABLE_NAME + " (" +
                WeatherContract.LocationEntry._ID + " INTEGER PRIMARY KEY," +
                WeatherContract.LocationEntry.COL_LOCATION_SETTINGS + " TEXT UNIQUE NOT NULL, " +
                WeatherContract.LocationEntry.COL_CITY_NAME + " TEXT NOT NULL, " +
                WeatherContract.LocationEntry.COL_LAT + " REAL NOT NULL, " +
                WeatherContract.LocationEntry.COL_LONG + " REAL NOT NULL " +
                " );";



        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherContract.WeatherEntry.TABLE_NAME + " (" +
                WeatherContract.WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WeatherContract.WeatherEntry.COL_LOC_KEY + " INTEGER NOT NULL, " +
                WeatherContract.WeatherEntry.COL_DATE + " INTEGER NOT NULL, " +
                WeatherContract.WeatherEntry.COL_SHORT_DESC + " TEXT NOT NULL, " +
                WeatherContract.WeatherEntry.COL_WEATHER_ID + " INTEGER NOT NULL," +
                /*these constrains helps us without column being
                filled out */
                WeatherContract.WeatherEntry.COL_MIN_TEMP + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COL_MAX_TEMP + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COL_HUMDITY + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COL_PRESSURE + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COL_WIND + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COL_DEGREE + " REAL NOT NULL, " +
                             " FOREIGN KEY (" + WeatherContract.WeatherEntry.COL_LOC_KEY + ") REFERENCES " +
                WeatherContract.LocationEntry.TABLE_NAME + " (" + WeatherContract.LocationEntry._ID + "), " +

                " UNIQUE (" + WeatherContract.WeatherEntry.COL_DATE + ", " +
                WeatherContract.WeatherEntry.COL_LOC_KEY + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_WEATHER_TABLE);
        db.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WeatherContract.LocationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WeatherContract.WeatherEntry.TABLE_NAME);
        onCreate(db);
    }
}
