package com.hsu.aidlab.meetrip.Service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.hsu.aidlab.meetrip.Util.Constants;
import com.hsu.aidlab.meetrip.Util.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

//import mn.mobile.mugi.dailylog.Util.Constants;
//import mn.mobile.mugi.dailylog.Util.DBHelper;

public class WeatherService extends Service {
    private Context context;
    // private YahooWeather yahooWeather;
    private Timer timer;
    private DBHelper dbHelper;

    // I Sew This Page : https://blog.naver.com/PostView.naver?isHttpsRedirect=true&blogId=ivory82&logNo=220797022612


    LocationManager locationManager;
    // 현재 GPS 사용유무
    boolean isGPSEnabled = false;
    // 네트워크 사용유무
    boolean isNetworkEnabled = false;
    // GPS 상태값
    boolean isGetLocation = false;
    Location location;

    // 최소 GPS 정보 업데이트 거리 1000미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1000;

    // 최소 업데이트 시간 1분
    private static final long MIN_TIME_BW_UPDATES = 1000 * 20;


    private double longitude = 0.0, latitude = 0.0;

    public WeatherService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.wtf("WeatherService","WeatherService Is Started");

        context = getBaseContext();
//        yahooWeather = YahooWeather.getInstance(5000, true);
        timer = new Timer();
        dbHelper = new DBHelper(context);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        getLocation();
                        Log.wtf("WeatherService","WeatherService WeatherService getLocation : " + getLocation());
                        if (longitude != 0.0 && latitude != 0.0) {
                            getWeatherData(longitude, latitude);
                        }
                    }
                });

            }
        }, 0, 1000 * 60 * 2);

        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            // GPS 기반 위치정보 사용 가능 확인
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // 네트워크 기반 위치정보 사용 가능 확인
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // GPS 와 네트워크사용이 가능하지 않을때 소스 구현
            } else {
                this.isGetLocation = true;
                // 네트워크 정보로 부터 위치값 가져오기
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            // 위도 경도 저장
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }


    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
//            Toast("onLocationChanged");
//            getWeatherData( location.getLatitude() , location.getLongitude() );
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
//            Toast("onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider) {
//            Toast("onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
//            Toast("onProviderDisabled");
        }
    };

    /**
     * Weather
     * https://openweathermap.org/current
     *
     * AirPollution
     * https://openweathermap.org/api/air-pollution
     *
     * */

    private void getWeatherData(double lng, double lat) {

//        String API_KEY = "e1eadf27878982a862a09bf281bff4f1";
        String API_KEY = "eaf2be36c7b1596637c1fb60acab4ea1";
        String weatherURL = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lng + "&units=metric&appid=" + API_KEY;
        String airPollutionURL = "http://api.openweathermap.org/data/2.5/air_pollution?lat=" + lat + "&lon=" + lng + "&appid=" + API_KEY;

        //{"coord":{"lon":127.0275,"lat":37.1931},"list":[{"main":{"aqi":2},"components":{"co":534.06,"no":54.99,"no2":42.84,"o3":0,"so2":16.21,"pm2_5":19.78,"pm10":23.15,"nh3":4.18},"dt":1627905600}]}

        Log.wtf("WeatherService","WeatherService Weather Request URL : " + weatherURL);
        Log.wtf("WeatherService","WeatherService AirPollution Request URL : " + airPollutionURL);

        ReceiveWeatherTask receiveUseTask = new ReceiveWeatherTask();
        receiveUseTask.execute(weatherURL);
        ReceiveAirPollutionTask receiveAirPollutionTask = new ReceiveAirPollutionTask();
        receiveAirPollutionTask.execute(airPollutionURL);

    }

    private class ReceiveWeatherTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... datas) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(datas[0]).openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(is);
                    BufferedReader in = new BufferedReader(reader);

                    String readed;
                    while ((readed = in.readLine()) != null) {
                        JSONObject jObject = new JSONObject(readed);
//                        String result = jObject.getJSONArray("weather").getJSONObject(0).getString("icon");
                        return jObject;
                    }
                } else {
                    return null;
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Log.wtf("WeatherService","WeatherService WeatherService Json Result : " + result.toString());
//            Log.i("WeatherService", "Json Result : " + result.toString());
            if (result != null) {

                String iconName = "";
                String nowTemp = "";
                String maxTemp = "";
                String minTemp = "";

                String humidity = "";
                String speed = "";
                String main = "";
                String description = "";

                try {
                    iconName = result.getJSONArray("weather").getJSONObject(0).getString("icon");
                    nowTemp = result.getJSONObject("main").getString("temp");
                    humidity = result.getJSONObject("main").getString("humidity");
                    minTemp = result.getJSONObject("main").getString("temp_min");
                    maxTemp = result.getJSONObject("main").getString("temp_max");
                    speed = result.getJSONObject("wind").getString("speed");
                    main = result.getJSONArray("weather").getJSONObject(0).getString("main");
                    description = result.getJSONArray("weather").getJSONObject(0).getString("description");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                description = transferWeather(description);
//                final String msg = " 습도 " + humidity + "%, 풍속 " + speed + "m/s" + " 온도 현재:" + nowTemp + " / 최저:" + minTemp + " / 최고:" + maxTemp;
                final String msg = "{\"weather\":\"" + main + "\",\"humidity\":" + humidity + ",\"windSpeed_mps\":" + speed + ",\"nowTemp\":" + nowTemp + ",\"minTemp\":" + minTemp + ",\"maxTemp\":" + maxTemp + ",\"dateTime\":\"" + getCurDateStr() + "\"}";

                String query = "insert into sys_sensor (sensorCode, sensorValue) values ('" + Constants.TAG_Weather + "', '" + msg + "')";
                dbHelper.putData(query);

                Log.wtf("WeatherService","WeatherService WeatherService Request msg : " + msg);


            }

        }
    }
    private class ReceiveAirPollutionTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... datas) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(datas[0]).openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(is);
                    BufferedReader in = new BufferedReader(reader);

                    String readed;
                    while ((readed = in.readLine()) != null) {
                        JSONObject jObject = new JSONObject(readed);
//                        String result = jObject.getJSONArray("weather").getJSONObject(0).getString("icon");
                        return jObject;
                    }
                } else {
                    return null;
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Log.wtf("WeatherService","WeatherService AirPollution Json Result : " + result.toString());
//            Log.i("AirPollution", "Json Result : " + result.toString());
            if (result != null) {

                String CO = "";
                String NO = "";
                String NO2 = "";
                String O3 = "";

                String SO2 = "";
                String PM25 = "";
                String PM10 = "";
                String NH3 = "";

                String main_air = "";

                try {
                    main_air = result.getJSONArray("list").getJSONObject(0).getJSONObject("main").getString("aqi");
                    CO = result.getJSONArray("list").getJSONObject(0).getJSONObject("components").getString("co");
                    NO = result.getJSONArray("list").getJSONObject(0).getJSONObject("components").getString("no");
                    NO2 = result.getJSONArray("list").getJSONObject(0).getJSONObject("components").getString("no2");
                    O3 = result.getJSONArray("list").getJSONObject(0).getJSONObject("components").getString("o3");
                    SO2 = result.getJSONArray("list").getJSONObject(0).getJSONObject("components").getString("so2");
                    PM25 = result.getJSONArray("list").getJSONObject(0).getJSONObject("components").getString("pm2_5");
                    PM10 = result.getJSONArray("list").getJSONObject(0).getJSONObject("components").getString("pm10");
                    NH3 = result.getJSONArray("list").getJSONObject(0).getJSONObject("components").getString("nh3");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                description = transferWeather(description);
//                final String msg = " 습도 " + humidity + "%, 풍속 " + speed + "m/s" + " 온도 현재:" + nowTemp + " / 최저:" + minTemp + " / 최고:" + maxTemp;
                final String msg = "{\"AirQualityIndex\":" + main_air + ",\"CO\":" + CO + ",\"NO\":" + NO + ",\"NO2\":" + NO2 + ",\"O3\":" + O3 + ",\"SO2\":" + SO2 + ",\"PM25\":" + PM25 + ",\"PM10\":" + PM10 + ",\"NH3\":" + NH3 + ",\"dateTime\":\"" + getCurDateStr() + "\"}";

                String query = "insert into sys_sensor (sensorCode, sensorValue) values ('" + Constants.TAG_AirPollut + "', '" + msg + "')";
                dbHelper.putData(query);

                Log.wtf("WeatherService","WeatherService AirPollution Request msg : " + msg);


            }

        }
    }

    private String getCurDateStr() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return ft.format(Calendar.getInstance().getTime());
    }

//    private String transferWeather(String weather) {
//
//        weather = weather.toLowerCase();
//
//        if (weather.equals("haze")) {
//            return "안개";
//        } else if (weather.equals("fog")) {
//            return "안개";
//        } else if (weather.equals("clouds")) {
//            return "구름";
//        } else if (weather.equals("few clouds")) {
//            return "구름 조금";
//        } else if (weather.equals("scattered clouds")) {
//            return "구름 낌";
//        } else if (weather.equals("broken clouds")) {
//            return "구름 많음";
//        } else if (weather.equals("overcast clouds")) {
//            return "구름 많음";
//        } else if (weather.equals("clear sky")) {
//            return "맑음";
//        }
//
//        return "";
//    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
//        if (weatherTask != null && !weatherTask.isCancelled()) {
//            weatherTask.cancel(true);
//        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
