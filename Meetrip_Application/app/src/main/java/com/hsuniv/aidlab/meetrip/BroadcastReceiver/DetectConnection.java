package com.hsuniv.aidlab.meetrip.BroadcastReceiver;

import static mn.mobile.mugi.dailylog.Util.CommonUtils.checkInternetConnection;
import static mn.mobile.mugi.dailylog.Util.CommonUtils.startServiceOnce;
import static mn.mobile.mugi.dailylog.Util.CommonUtils.stopServiceOnce;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mn.mobile.mugi.dailylog.Service.NCube;

public class DetectConnection extends BroadcastReceiver
{
    /**
     *  Listening for internet connection state
     *  if there is no available internet connection then stopping Bind Service otherwise starting that Service again
     *  Because The Bind service runs in foreground and lasts long time
     *  that means battery consumption will be increased, so we need to stop foreground services for reducing battery consumption
     * */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(checkInternetConnection(context))
        {
            startServiceOnce(NCube.class, context);
        }
        else
        {
            stopServiceOnce(NCube.class, context);
        }
    }
}
