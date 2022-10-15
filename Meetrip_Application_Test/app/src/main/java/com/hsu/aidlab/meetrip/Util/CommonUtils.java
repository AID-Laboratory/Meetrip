package com.hsu.aidlab.meetrip.Util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.hsu.aidlab.meetrip.BroadcastReceiver.DetectConnection;
import com.hsu.aidlab.meetrip.Service.MsBandService;
import com.hsu.aidlab.meetrip.Service.NCube;
import com.hsu.aidlab.meetrip.Service.SynchronizeService;

public class CommonUtils
{
    public CommonUtils() {}

    // TODO: Check Internet connection
    public static boolean checkInternetConnection(Context context)
    {
        ConnectivityManager connec =(ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED )
        {
            return true;
        }else
            if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  )
            {
            return false;
        }
        return false;
    }

    // TODO: Checking backround service is running or not
    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // TODO: Start or Stop backround services
    public static void controlBackgroundServices(Context context, Integer flag)
    {
        // TODO: Changing state of Services depends on variable named flag
        List<Class<?>> serviceList = getServiceClassList();

        if(serviceList != null)
        {
            for(Class<?> serviceClass : serviceList)
            {
                switch (flag)
                {
                    case 0: stopServiceOnce(serviceClass, context); break;
                    case 1: startServiceOnce(serviceClass, context); break;
                    default:break;
                }

                Log.d("controlService", (flag == 1 ? "started" : "stopped") + serviceClass.getName());
            }
        }

        // TODO: Changing state of BroadCast Recievers depends on variable named broadCastFlag
        List<Class<?>> receieverList = getRecieverClassList();

        if(receieverList != null)
        {
            if(receieverList.size() > 0)
            {
                int broadCastFlag = ( flag == 1) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

                for(Class<?> reciever : receieverList)
                {
                    ComponentName component = new ComponentName(context, reciever);
                    context.getPackageManager().setComponentEnabledSetting(component, broadCastFlag, PackageManager.DONT_KILL_APP);
                    Log.d("controlReceiver", (flag == 1 ? "started" : "stopped") + reciever.getName());
                }
            }
        }
    }
    // TODO: Start backround service If it's not running
    public static boolean startServiceOnce(Class<?> serviceClass, Context context)
    {
        try
        {
            if(!isMyServiceRunning(serviceClass, context))
            {
                context.startService(new Intent(context, serviceClass));
            }
            return true;
        }
        catch (Exception e)
        {
            Log.d("serviceStart exception", e.toString());
        }
        return false;
    }

    // TODO: Stop backround service If it's running
    public static boolean stopServiceOnce(Class<?> serviceClass, Context context)
    {
        try
        {
            if(isMyServiceRunning(serviceClass, context))
            {
                context.stopService(new Intent(context, serviceClass));
            }
            return true;
        }
        catch (Exception e)
        {
            Log.d("serviceStop exception", e.toString());
        }
        return false;
    }

    // TODO: Convert null string to default string
    public static String checkNull(String val)
    {
        String defNull = "Empty";
        return (val != null) ? val : defNull;
    }

    // TODO: Convert current date to string
    public static String getCurDateStr()
    {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return ft.format(Calendar.getInstance().getTime());
    }

    // TODO: Convert specific date to string
    public static String parseDatetoStr(Date date)
    {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return ft.format(date);
    }

    // TODO: Convert string to date
    public static String parseDate(Object date)
    {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        long dateInMil = Long.valueOf(date.toString());
        calendar.setTimeInMillis(dateInMil);
        return ft.format(calendar.getTime());
    }


    // TODO: Get list of Backround services by class
    public static List<Class<?>> getServiceClassList()
    {
        ArrayList<Class<?>> serviceList = new ArrayList<>();
        serviceList.add(NCube.class);
        serviceList.add(SynchronizeService.class);
        serviceList.add(MsBandService.class);
        return serviceList;
    }

    public static List<Class<?>> getRecieverClassList()
    {
        ArrayList<Class<?>> receieverList = new ArrayList<>();
        receieverList.add(DetectConnection.class);
        return receieverList;
    }

    // TODO: Get required permissions by arrayString
    public static String[] getPermissions()
    {
        String[] permissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        return permissions;
    }


    public static AlertDialog showAlertDlg(Activity activity, String title, String msg)
    {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity,
                android.R.style.Theme_DeviceDefault_Light_Dialog));

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(msg).setTitle(title);
        return builder.create();
    }


}
