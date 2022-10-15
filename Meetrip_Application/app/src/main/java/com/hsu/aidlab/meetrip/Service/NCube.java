package com.hsu.aidlab.meetrip.Service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hsu.aidlab.meetrip.Util.Constants;
import com.hsu.aidlab.meetrip.nCubeThyme.service.INCubeService;
import com.hsu.aidlab.meetrip.nCubeThyme.service.NCubeService;
import com.hsu.aidlab.meetrip.nCubeThyme.setting.NCubeSettingDataShare;

public class NCube extends Service {
    private Context context;
    private static String nCubeSetupData = "";
    private boolean settingState = false;
    private boolean runningState = false;
    private boolean serviceConnection = false;
    private Intent intent;
    private INCubeService nCubeService = null;
    private Thread thread;
    private ServiceConnection serveConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            nCubeService = INCubeService.Stub.asInterface(service);
            Log.d("bindService", "onServiceConnected");
            serviceConnection = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("bindService", "onServiceDisconnected");
            serviceConnection = false;
        }
    };

    public NCube() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();
        thread = new Thread() {
            @Override
            public void run() {
                setNCubeSetting();
                startNCube(context);
            }
        };
        thread.start();
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        destroyThread();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        /**
         * Stop NCube service and Unbind service connection When service destroying
         */
        destroyThread();
        super.onDestroy();
    }

    private void destroyThread() {
        stopNCube(context);
        if (runningState) {
            unbindService(serveConn);
            runningState = false;
        }
        if (!thread.isInterrupted()) {
            thread.interrupt();
        }
    }

    /**
     * Share server configuration string to NCubeSettingDataShare object
     */
    private void setNCubeSetting() {
        NCubeSettingDataShare settingData = new NCubeSettingDataShare(setDefault());
        nCubeSetupData = settingData.configData;

        if (!nCubeSetupData.isEmpty()) {
            settingState = true;
        }
    }

    /**
     * Start NCube service
     *
     * @param context
     */
    public void startNCube(Context context) {
        /**
         * Check if server configuration is submitted and no connection with the server
         */
        if (settingState && !serviceConnection) {
            /**
             * Define NCubeDataShare object with the configData and put this object to Intent
             * Start NCubeService and Bind server connection
             */
            intent = new Intent(context, NCubeService.class);
            NCubeSettingDataShare regData = new NCubeSettingDataShare(nCubeSetupData);
            intent.putExtra("regData", regData);
            context.startService(intent);
            Intent serviceIntent = new Intent();
            serviceIntent.setClass(context, NCubeService.class);
            context.bindService(serviceIntent, serveConn, Context.BIND_AUTO_CREATE);
            runningState = true;
        }
    }

    private void stopNCube(Context context) {
        if (settingState && runningState) {
            Intent stopIntent = new Intent();
            stopIntent.setClass(context, NCubeService.class);
            try {
                nCubeService.setStopService();
                context.unbindService(serveConn);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            context.stopService(stopIntent);
            runningState = false;
            serviceConnection = false;
        }
    }

    private void checkNCube(Context context) {
        if (settingState && runningState) {
            boolean state = false;
            try {
                state = nCubeService.checkRunningState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (state) {
                runningState = true;
            }
        } else if (settingState && !runningState) {
            runningState = false;
        } else {
        }
    }

    private String setDefault() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString(Constants.USER_ID, "msband");

        String applicationName = "Meetrip";

        String defaultXML = String.format("{\n" +
                        "\"useprotocol\": \"http\",\n" +
                        "\"cse\": {\n" +
                        "\"cbhost\": \"" + Constants.DEF_DEST_IP + "\",\n" +
                        "\"cbport\": \"" + Constants.DEF_DEST_PORT + "\",\n" +
                        "\"cbname\": \"Mobius\",\n" +
                        "\"cbcseid\": \"/Mobius\",\n" +
                        "\"mqttport\": \"1883\"\n" +
                        "},\n" +

                        "\"ae\": {\n" +
                        "\"aeid\": \"S\",\n" +
                        "\"appid\": \"0.2.481.1.1\",\n" +
                        "\"appname\": \"" + applicationName + "\",\n" +
                        "\"appport\": \"9727\",\n" +
                        "\"bodytype\": \"xml\",\n" +
                        "\"tasport\": \"" + Constants.PORT + "\"\n" +
                        "},\n" +

                        "\"cnt\": [\n" +
                        "{\"parentpath\": \"/" + applicationName + "\",\"ctname\": \"userID\"},\n" +
                        "{\"parentpath\": \"/"+applicationName+"/userID\",\"ctname\": \"Band\"}," +         // MS Band Data
                        "{\"parentpath\": \"/"+applicationName+"/userID\",\"ctname\": \"Coordinate\"}," +   // Lon Lat Alt
                        "{\"parentpath\": \"/"+applicationName+"/userID\",\"ctname\": \"Image\"}," +        // Image Location and Tag
                        "{\"parentpath\": \"/"+applicationName+"/userID\",\"ctname\": \"Weather\"}," +      // Weather and Airpollution
                        "{\"parentpath\": \"/"+applicationName+"/userID/Band\",\"ctname\": \"" + Constants.TAG_HeartRate + "\"}," +
                        "{\"parentpath\": \"/"+applicationName+"/userID/Band\",\"ctname\": \"" + Constants.TAG_SkinTemperature + "\"}," +
                        "{\"parentpath\": \"/"+applicationName+"/userID/Band\",\"ctname\": \"" + Constants.TAG_Gsr + "\"}," +
                        "{\"parentpath\": \"/"+applicationName+"/userID/Band\",\"ctname\": \"" + Constants.TAG_Barometer + "\"}" +
//                        "{\n" +
//                        "\"parentpath\": \"/userID/Band\",\n" +
//                        "\"ctname\": \"" + Constants.TAG_Distance + "\"\n" +
//                        "},\n" +
//                        "{\n" +
//                        "\"parentpath\": \"/userID/Band\",\n" +
//                        "\"ctname\": \"" + Constants.TAG_Pedometer + "\"\n" +
//                        "},\n" +
//                        "{\n" +
//                        "\"parentpath\": \"/userID/Band\",\n" +
//                        "\"ctname\": \"" + Constants.TAG_Accelerometer + "\"\n" +
//                        "},\n" +
//                        "{\n" +
//                        "\"parentpath\": \"/userID/Band\",\n" +
//                        "\"ctname\": \"" + Constants.TAG_Gyroscope + "\"\n" +
//                        "},\n" +
//
//                        "{\n" +
//                        "\"parentpath\": \"/userID/Band\",\n" +
//                        "\"ctname\": \"" + Constants.TAG_UV + "\"\n" +
//                        "},\n" +
//                        "{\n" +
//                        "\"parentpath\": \"/userID/Band\",\n" +
//                        "\"ctname\": \"" + Constants.TAG_Contact + "\"\n" +
//                        "},\n" +
//                        "{\n" +
//                        "\"parentpath\": \"/userID/Band\",\n" +
//                        "\"ctname\": \"" + Constants.TAG_RRInterval + "\"\n" +
//                        "},\n" +
//                        "{\n" +
//                        "\"parentpath\": \"/userID/Band\",\n" +
//                        "\"ctname\": \"" + Constants.TAG_AmbientLight + "\"\n" +
//                        "},\n" +
//                        "{\n" +
//                        "\"parentpath\": \"/userID/Band\",\n" +
//                        "\"ctname\": \"" + Constants.TAG_Altimeter + "\"\n" +
//                        "},\n" +
//                        "{\n" +
//                        "\"parentpath\": \"/userID/Band\",\n" +
//                        "\"ctname\": \"" + Constants.TAG_Calories + "\"\n" +
//                        "}\n" +
                        "],\n" +
                        "\"sub\": [\n" +
//                        "{\n" +
//                        "\"parentpath\": \"/userID/Band/actuatorTest\",\n" +
//                        "\"subname\": \"sub-ctrl\",\n" +
//                        "\"nu\": \"mqtt://AUTOSET\"\n" +
//                        "}\n" +
                        "]\n" +
                        "}\n",

                Constants.DEF_DEST_IP,
                Constants.DEF_DEST_PORT).replace("userID", userID.replace("+", "").trim());
        Log.d("xml", defaultXML);
        return defaultXML;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
