package com.hsu.aidlab.meetrip.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
//import android.support.annotation.Nullable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.hsu.aidlab.meetrip.Util.CommonUtils;
import com.hsu.aidlab.meetrip.Util.DBHelper;

//import me.hgko.networkinfo.domain.LteSignalInfo;
//import me.hgko.networkinfo.manager.DataManager;
//import me.hgko.networkinfo.util.CommonUtils;

/**
 * Created by hgko on 2018-09-20.
 */
public class LocationService extends Service {

    //    private final DataManager dataManager = DataManager.getInstance();
    private LocationManager locationManager;
    private DBHelper dbHelper;

    String locationProvider;
    double locationLatitude;
    double locationLongitude;
    double locationAltitude;
    float locationSpeed;
    double locationKmSpeed;
    float locationAccuracy;

    String sensorCode = "Coordinate";
    String sensorValues = "";

    public LocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        dbHelper = new DBHelper(getBaseContext());
        locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
        initLocation();

        Log.wtf("Location Service", "Service Started");

        return START_STICKY;
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            locationProvider = location.getProvider();
            locationLatitude = location.getLatitude();
            locationLongitude = location.getLongitude();
            locationAltitude = location.getAltitude();
            locationSpeed = location.getSpeed();
            locationKmSpeed = location.getSpeed() * 3.6;
            locationAccuracy = location.getAccuracy();

            sensorValues = "{" +
                    "\"Provider\":\"" + locationProvider + "\"," +
                    "\"Latitude\":" + locationLatitude + "," +
                    "\"Longitude\":" + locationLongitude + "," +
                    "\"Altitude\":" + locationAltitude + "," +
//                    "\"Speed\":" + locationSpeed + "," +
//                    "\"KmSpeed\":" + locationKmSpeed + "," +
                    "\"Accuracy\":" + locationAccuracy +
                    "}";

            Log.wtf("Location Service", sensorValues);

            String query = "insert into sys_sensor (sensorCode, sensorValue) values ('" + sensorCode + "', '" + sensorValues + "')";
            dbHelper.putData(query);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("networkLog", "provider : " + provider);
            locationManager.removeUpdates(locationListener);
            initLocation();
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private void initLocation() {
        if (!CommonUtils.checkPermission(getBaseContext())) {
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//                    1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 1, locationListener);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}