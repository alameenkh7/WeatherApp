package com.agileblaze.weatherapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.media.UnsupportedSchemeException;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Switch;

import com.agileblaze.weatherapp.data.WeatherContract;
import com.agileblaze.weatherapp.data.WeatherDbHelper;

/**
 * Created by manager on 19/9/16.
 */
public class WeatherProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WeatherDbHelper mOpenHelper;
    static final int WEATHER = 100;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;

    private static final SQLiteQueryBuilder querybuilder;

    static{
        querybuilder = new SQLiteQueryBuilder();

        querybuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COL_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);
    }

    private static final String LocationSettingSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COL_LOCATION_SETTINGS + " = ? ";

    private static final String LocationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COL_LOCATION_SETTINGS + " = ? AND " +
                    WeatherContract.WeatherEntry.COL_DATE + " >= ? ";


    private static final String LocationSettingAndDaySelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    "." + WeatherContract.LocationEntry.COL_LOCATION_SETTINGS + " = ? AND " +
                    WeatherContract.WeatherEntry.COL_DATE + " = ? ";


    private Cursor getweatherbylocationsettings(Uri uri, String[] projection,String sortoder)
    {
        String locationsetting= WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long startdate= WeatherContract.WeatherEntry.getStartDateFromUri(uri);
        String[] selectionArray;
        String selection;

        if(startdate==0)
        {
            selection=LocationSettingSelection;
            selectionArray=new String[]{locationsetting};
        }
        else
        {
            selection=LocationSettingWithStartDateSelection;
            selectionArray=new String[]{locationsetting,Long.toString(startdate)};
        }

        return querybuilder.query(mOpenHelper.getReadableDatabase(),projection,selection,selectionArray,null,null,
                sortoder);
    }

    private Cursor getWeatherByLocationSettingAndDate(Uri uri,String[] projection,String sortorder)
    {
    String locationsetting= WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
    long date= WeatherContract.WeatherEntry.getDateFromUri(uri);
        return querybuilder.query(mOpenHelper.getReadableDatabase(),projection,LocationSettingAndDaySelection,
                new String[]{locationsetting,Long.toString(date)},null,null,sortorder);
    }


    public static UriMatcher buildUriMatcher() {
      final UriMatcher matcher=new UriMatcher(UriMatcher.NO_MATCH);
      final String authority=WeatherContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "weather", WEATHER);
        matcher.addURI(authority, "weather" + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(authority, "weather" + "/*/#", WEATHER_WITH_LOCATION_AND_DATE);
        matcher.addURI(authority, "location", LOCATION);

        return matcher;

    }

    @Override
    public boolean onCreate() {
        mOpenHelper=new WeatherDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArray, String sortOrder) {
        Cursor retCursor;
       final int match=sUriMatcher.match(uri);

        switch (sUriMatcher.match(uri)) {

            case WEATHER_WITH_LOCATION_AND_DATE:
            {
                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }

            case WEATHER_WITH_LOCATION: {
                retCursor = getweatherbylocationsettings(uri, projection, sortOrder);
                break;
            }

            case WEATHER: {
                retCursor =mOpenHelper.getReadableDatabase().query(WeatherContract.WeatherEntry.TABLE_NAME,projection,
                        selection,selectionArray,null,null,sortOrder);
                break;
            }

            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(WeatherContract.LocationEntry.TABLE_NAME,
                        projection,selection,selectionArray,null,null,sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match=sUriMatcher.match(uri);
        switch (match) {
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db=mOpenHelper.getWritableDatabase();
        final int match=sUriMatcher.match(uri);
        Uri returnuri;



        switch (match) {
            case WEATHER:{
                normalizedate(values);
                long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnuri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                else
                    throw new SQLException("failed to insert row"+uri);
                break;
            }

            case LOCATION: {

                long _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnuri = WeatherContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new SQLException("failed to insert row" + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri"+uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return returnuri;
    }

    private void normalizedate(ContentValues values) {
        if(values.containsKey(WeatherContract.WeatherEntry.COL_DATE))
        {
            long datevalue=values.getAsLong(WeatherContract.WeatherEntry.COL_DATE);
            values.put(WeatherContract.WeatherEntry.COL_DATE,WeatherContract.normalizeDate(datevalue));
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db=mOpenHelper.getWritableDatabase();
        final int match=sUriMatcher.match(uri);
        int rowdeleted;
        if(selection==null){selection="1";}
        switch (match){
            case WEATHER:
                rowdeleted=db.delete(WeatherContract.WeatherEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case LOCATION:
                rowdeleted=db.delete(WeatherContract.LocationEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknow uri"+uri);
        }
        if(rowdeleted!=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowdeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db=mOpenHelper.getWritableDatabase();
        final int match=sUriMatcher.match(uri);
        int rowupdated;
        if(selection==null){selection="1";}
        switch (match){
            case WEATHER:
                rowupdated=db.update(WeatherContract.WeatherEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            case LOCATION:
                rowupdated=db.update(WeatherContract.LocationEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknow uri"+uri);
        }
        if(rowupdated!=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowupdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db=mOpenHelper.getWritableDatabase();
        final int match=sUriMatcher.match(uri);
        switch (match)
        {
            case WEATHER:
                db.beginTransaction();
                int returncount=0;
              try
              {
                  for (ContentValues value : values) {
                      normalizedate(value);
                      long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                      if (_id != -1) {
                          returncount++;
                      }
                  }
                  db.setTransactionSuccessful();
              }
              finally
              {
                db.endTransaction();
              }
                getContext().getContentResolver().notifyChange(uri, null);
                return returncount;
            default:
                return super.bulkInsert(uri, values);

        }
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
