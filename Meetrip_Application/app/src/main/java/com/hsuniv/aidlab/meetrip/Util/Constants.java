package com.hsuniv.aidlab.meetrip.Util;

import android.annotation.TargetApi;
import android.os.Build;

import com.hsuniv.aidlab.meetrip.BuildConfig;

@TargetApi(Build.VERSION_CODES.M)
public class Constants
{
    public static Integer SYNCHRONIZE_DELAY = 1000 * 30; // 3m => 30s
    public static Integer FLAG_START = 1;
    public static Integer FLAG_STOP = 0;
    public static String DB_NAME = "testDb.db";
    public static String DB_PATH = "/data/data/"+ BuildConfig.APPLICATION_ID+"/databases/";
    public static Integer DB_VERSION = 1;
    public static String sensorCode = "sensorCode";
    public static String PREFERENCE_KEY = "userInfo";
    public static String USER_ID = "Band";
    public static String YES = "Yes";
    public static String NO = "No";
    public static String MISSED_PERMISSION = "Required permissions not granted completely";
    public static String ASK_GRANT_MISSED_PERMISSIONS = "Do you want to grant missed permissions again?";

    public static String TAG_HeartRate= "HeartRate";
    public static String TAG_SkinTemperature = "SkinTemperature";
    public static String TAG_Gsr = "Gsr";
    public static String TAG_Barometer = "Barometer";
    public static String TAG_Distance = "Distance";
    public static String TAG_Pedometer = "Pedometer";
    public static String TAG_Accelerometer = "Accelerometer";
    public static String TAG_Gyroscope = "Gyroscope";
    public static String TAG_UV = "UV";
    public static String TAG_Contact = "Contact";
    public static String TAG_AmbientLight = "AmbientLight";
    public static String TAG_Altimeter = "Altimeter";
    public static String TAG_RRInterval = "RRInterval";
    public static String TAG_Calories = "Calories";

    public static String HOST = "localhost";
    public static Integer PORT = 7622;

    public static String TAG_DEST_IP = "cbhost";
    public static String TAG_DEST_PORT = "cbport";

    //public static String DEF_DEST_IP = "125.138.177.85";
    public static String DEF_DEST_IP = "203.252.23.46";
    public static String DEF_DEST_PORT = "7579";
}
