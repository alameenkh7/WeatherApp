package com.agileblaze.weatherapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.agileblaze.weatherapp.data.WeatherContract;

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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;


/**
 * Created by manager on 19/9/16.
 */
public class FetchWeatherTask extends AsyncTask<String, Void ,String[]>  {
    private Context mcontext;
    private ArrayAdapter<String> mforecastadapter;
    private boolean debug=true;

    @Override
    protected String[] doInBackground(String... params) {
        if(params.length==0)
        {
            return null;
        }

        String locationquery=params[0];
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;
        String format="json";
        String units="metric";
        int numDay=7;
        String appid="7f6e15fd6faa2077cd1d3f7cc17ff0c3";

        try {
            final String BASEURL="http://api.openweathermap.org/data/2.5/forecast/daily?";

            final String QUERYPARAM="q";
            final String FORMATPARAM="mode";
            final String UNITPARAM="units";
            final String COUNTPARAM="cnt";
            final String APPIDPARAM="APPID";

            Uri builurl=Uri.parse(BASEURL).buildUpon().appendQueryParameter(QUERYPARAM,locationquery)
                    .appendQueryParameter(FORMATPARAM,format)
                    .appendQueryParameter(UNITPARAM,units)
                    .appendQueryParameter(COUNTPARAM,Integer.toString(numDay)).
                            appendQueryParameter(APPIDPARAM,appid).build();

            URL url=new URL(builurl.toString());

            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {

                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr=buffer.toString();


        } catch (Exception e) {
            Log.e("PlaceholderFragment", "Error ", e);
            return null;
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if(reader!=null)
            {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }

        try {
            return getWeatherData(forecastJsonStr,locationquery);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getReadableDateString(long time)
    {
        Date date=new Date(time);
        SimpleDateFormat format= new SimpleDateFormat("E,MMM,d");
        return format.format(date).toString();
    }

    public FetchWeatherTask (Context context, ArrayAdapter<String> forecastAdapter)
    {
        mcontext = context;
        mforecastadapter = forecastAdapter;
    }

    public String formathighlow(double high, double low)
    {
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(mcontext);

        String utype= preferences.getString(mcontext.getString(R.string.pref_temp_key),mcontext.getString(R.string.pref_default_value));
        if(utype.equals(mcontext.getString(R.string.pref_units_imperial)))
        {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;

        }
        else if (!utype.equals(mcontext.getString(R.string.pref_units_metric)))
        {
            Log.d("Error","Unit type Not found" +utype);
        }
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationid;

        Cursor locationcursor=mcontext.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID}, WeatherContract.LocationEntry.COL_LOCATION_SETTINGS +"=?",
                new String[]{locationSetting},null);

        if(locationcursor.moveToFirst())
        {
            int locationIndex=locationcursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationid=locationcursor.getLong(locationIndex);

        }else
        {
            ContentValues contentvalues=new ContentValues();

            contentvalues.put(WeatherContract.LocationEntry.COL_CITY_NAME,cityName);
            contentvalues.put(WeatherContract.LocationEntry.COL_LAT,lat);
            contentvalues.put(WeatherContract.LocationEntry.COL_LONG,lon);
            contentvalues.put(WeatherContract.LocationEntry.COL_LOCATION_SETTINGS,locationSetting);

            Uri inserturi=mcontext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI,
                    contentvalues);
            locationid= ContentUris.parseId(inserturi);
       }
        locationcursor.close();
        return locationid;
    }

    String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv) {
        String[] resultStrs = new String[cvv.size()];
        for ( int i = 0; i < cvv.size(); i++ ) {
            ContentValues weatherValues = cvv.elementAt(i);
            String highAndLow = formathighlow(
                    weatherValues.getAsDouble(WeatherContract.WeatherEntry.COL_MAX_TEMP),
                    weatherValues.getAsDouble(WeatherContract.WeatherEntry.COL_MIN_TEMP));
            resultStrs[i] = getReadableDateString(
                    weatherValues.getAsLong(WeatherContract.WeatherEntry.COL_DATE)) +
                    " - " + weatherValues.getAsString(WeatherContract.WeatherEntry.COL_SHORT_DESC) +
                    " - " + highAndLow;
        }
        return resultStrs;
    }

    private String[] getWeatherData(String forecastjson,String locationsettings) throws JSONException
    {
        final String CITY = "city";
        final String CITY_NAME = "name";
        final String COORD = "coord";
        final String LAT = "lat";
        final String LONG = "lon";
        final String LIST = "list";
        final String PRESSURE = "pressure";
        final String HUMIDITY = "humidity";
        final String WINDSPEED = "speed";
        final String WIND_DIRECTION = "deg";
        final String TEMPERATURE = "temp";
        final String MAX = "max";
        final String MIN = "min";
        final String WEATHER = "weather";
        final String DESCRIPTION = "main";
        final String WEATHER_ID = "id";
        final String Wind="wind";


        JSONObject forecast_json=new JSONObject(forecastjson);

        JSONArray weatherArray= forecast_json.getJSONArray(LIST);




        JSONObject cityJson = forecast_json.getJSONObject(CITY);
        String cityName = cityJson.getString(CITY_NAME);


        JSONObject cityCoord = cityJson.getJSONObject(COORD);
        double cityLatitude = cityCoord.getDouble(LAT);
        double cityLongitude = cityCoord.getDouble(LONG);

        long locationid= addLocation(locationsettings,cityName,cityLatitude,cityLongitude);

        Vector <ContentValues> cvvector= new Vector<ContentValues>(weatherArray.length());

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        for(int i=0;i<weatherArray.length();i++)
        {

            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;
            double high;
            double low;
            String description;
            int weatherId;

            JSONObject dayForecast = weatherArray.getJSONObject(i);


            dateTime = dayTime.setJulianDay(julianStartDay+i);



            humidity=dayForecast.getInt(HUMIDITY);

            pressure=dayForecast.getDouble(PRESSURE);
            windSpeed=dayForecast.getDouble(WINDSPEED);
            windDirection=dayForecast.getDouble(WIND_DIRECTION);
            JSONObject Descobject=dayForecast.getJSONArray(WEATHER).getJSONObject(0);


            JSONObject tempobject=dayForecast.getJSONObject(TEMPERATURE

            );

            high=tempobject.getDouble(MAX);
            low=tempobject.getDouble(MIN);

            description=Descobject.getString(DESCRIPTION);
            weatherId=Descobject.getInt(WEATHER_ID);

            ContentValues weatherValues= new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COL_LOC_KEY,locationid);
            weatherValues.put(WeatherContract.WeatherEntry.COL_DATE, LONG.valueOf(dateTime));
            weatherValues.put(WeatherContract.WeatherEntry.COL_DEGREE,windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COL_HUMDITY,humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COL_MAX_TEMP,high);
            weatherValues.put(WeatherContract.WeatherEntry.COL_MIN_TEMP,low);
            weatherValues.put(WeatherContract.WeatherEntry.COL_PRESSURE,pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COL_WIND,windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COL_SHORT_DESC,description);
            weatherValues.put(WeatherContract.WeatherEntry.COL_WEATHER_ID,weatherId);

            cvvector.add(weatherValues);

        }
        if ( cvvector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cvvector.size()];
            cvvector.toArray(cvArray);
            mcontext.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI,cvArray);

        }
        String sortOrder = WeatherContract.WeatherEntry.COL_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationsettings, System.currentTimeMillis());

        Cursor cursor=mcontext.getContentResolver().query(weatherForLocationUri,null,null,null,sortOrder);
        cvvector=new Vector<ContentValues>(cursor.getCount());
        if(cursor.moveToFirst())
        {
            do{
                ContentValues contentValues=new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor,contentValues);
                cvvector.add(contentValues);
            }while(cursor.moveToNext());

        }

        Log.d("message","Fetch Task is Completed");
        String[] resultStrs = convertContentValuesToUXFormat(cvvector);
        return resultStrs;
    }


    @Override
    protected void onPostExecute(String[] result) {
        if (result != null && mforecastadapter != null) {
            mforecastadapter.clear();
            for(String dayForecastStr : result) {
                mforecastadapter.add(dayForecastStr);
            }

        }
    }
}
