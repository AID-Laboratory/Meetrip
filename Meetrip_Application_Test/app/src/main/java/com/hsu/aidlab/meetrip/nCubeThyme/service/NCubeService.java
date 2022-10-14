package com.hsu.aidlab.meetrip.nCubeThyme.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.hsu.aidlab.meetrip.R;
import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.ThymeMain;
import com.hsu.aidlab.meetrip.nCubeThyme.setting.NCubeSettingDataShare;

/**
 * Created by Sung on 2015-03-03.
 */
public class NCubeService extends Service {

    Thread thymeMainThread;

    private boolean destroyFlag = false;
    private boolean state = true;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("service", "onBinded");
        return nCubeServiceBinder;
    }

    @Override
    public void onCreate() {
        Log.d("service", "onCreate실행");
    }

    @Override
    public void onDestroy() {
        Log.d("service", "onDestroy실행");

        if (destroyFlag) {
            stopForeground(true);
            System.exit(0);
        }

        else {
            onCreate();
        }
    }

    private final INCubeService.Stub nCubeServiceBinder = new INCubeService.Stub() {
        public void setStopService() throws RemoteException {
            Log.d("IBinder", "setStopService");
            destroyFlag = true;
        }

        public boolean checkRunningState() throws RemoteException {
            Log.d("IBinder", "checkRunningState");
            return state;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service", "onStartCommand실행");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel("1", "channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("channel description");
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setShowBadge(false);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notice = new NotificationCompat.Builder(getApplicationContext(), "1")
                .setContentTitle("&CUBE Lavender Service")
                .setContentText("&CUBE Lavender Service is running")
                .setSmallIcon(R.drawable.sensor)
                .setTicker("&CUBE Lavender Service is start!!")
                .setAutoCancel(false)
                .setChannelId("1");

        notificationManager.notify(1 /* ID of notification */, notice.build());

        startForeground(startId, notice.build());

        NCubeSettingDataShare regData = (NCubeSettingDataShare) intent.getSerializableExtra("regData");
        thymeMainThread = new ThymeMain(regData.configData);
        thymeMainThread.start();
        //Toast.makeText(getBaseContext(), regData.configData, Toast.LENGTH_SHORT).show();
        return START_REDELIVER_INTENT;
    }
}
