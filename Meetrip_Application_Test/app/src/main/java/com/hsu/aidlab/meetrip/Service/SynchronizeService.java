package com.hsu.aidlab.meetrip.Service;
import android.app.Service;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.hsu.aidlab.meetrip.Util.Constants;
import com.hsu.aidlab.meetrip.Util.DBHelper;
import static com.hsu.aidlab.meetrip.Util.CommonUtils.checkInternetConnection;

public class SynchronizeService extends Service
{
    private Timer timer;
    private DBHelper dbHelper;

    public SynchronizeService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
//        Toast.makeText(getBaseContext(), "sync service started..", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                timer = new Timer();
                dbHelper = new DBHelper(getBaseContext());

                timer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        new SynchronizeTask().execute();
                    }
                }, 0 , Constants.SYNCHRONIZE_DELAY);
            }
        }, 1000 * 20); // 2분 => 20초로 변경
        return START_STICKY;
    }

    private class SynchronizeTask extends AsyncTask<SensorEvent, Void, HashMap<String, String> >
    {
        @Override
        protected HashMap<String, String> doInBackground(SensorEvent... events)
        {
            HashMap<String, String> responseMap =  (checkInternetConnection(getBaseContext())) ? getLocalData() : null;
            return responseMap;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> responseMap)
        {
            super.onPostExecute(responseMap);
            if(responseMap != null)
            {
                for(String responseType : responseMap.keySet())
                {
                    Toast.makeText(getBaseContext(), responseType + responseMap.get(responseType), Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                Toast.makeText(getBaseContext(), "Sync error: No internet connection", Toast.LENGTH_LONG).show();
            }
        }
    }

    private HashMap<String, String> getLocalData()
    {
        //TODO get location, All type of Sensors, Call history, Sms history from local database

        String[] columntoSelect = new String[2];
        columntoSelect[0] = "sensorCode";
        columntoSelect[1] = "sensorValue";
        List<HashMap<String, String>> mappedList = dbHelper.getMapListFromEntity("sys_sensor", columntoSelect, "id", null, -1);

        //TODO Get Calendat events from SQLite
        List<HashMap<String, String>> mappedListEvent = dbHelper.getMapListFromEntity("sys_events", columntoSelect, "id", "type", 0);

        if(mappedListEvent != null)
        {
            for(HashMap<String, String> eventMap : mappedListEvent)
            {
                mappedList.add(eventMap);
            }
        }

        dbHelper.close();
        return sendToServer(mappedList);
    }

    private HashMap<String, String> sendToServer(List<HashMap<String, String>> mappedList)
    {
        HashMap<String, String> responseMap = new HashMap<>();
        try
        {
            if(mappedList != null)
            {
                Integer totalRecords = mappedList.size();
                List<HashMap<String, String>> keysToRemove = new ArrayList<>();

                JSONArray ja = new JSONArray();

                for(HashMap<String, String> mappedData : mappedList)
                {
                    for(String container : mappedData.keySet())
                    {
                        JSONObject object = new JSONObject();
                        object.put("ctname", container);
                        object.put("con", mappedData.get(container));
                        keysToRemove.add(mappedData);
                        Log.d("bservice_object", object.toString());
                        ja.put(object);
                    }
                }

                Socket socket = new Socket(Constants.HOST, Constants.PORT);
                if(socket != null)
                {
                    if(socket.isConnected())
                    {
                        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                        JSONObject contentObj = new JSONObject();
                        contentObj.put("jsonArray", ja);
                        writer.println(contentObj.toString());

                        Log.d("bservice", contentObj.toString());
                        Log.d("bservice", contentObj.toString().length()+"");
                        Log.d("ClientThread", "sent");
                    }
                    else
                    {
                        Log.d("bservice","[&CubeThyme] TAS connection is closed\n");
                    }
                }
                socket.close();

                for(HashMap<String, String> object : keysToRemove)
                {
                    for(String container : object.keySet())
                    {
                        if( removeFromLocalDb(container, object.get(container)) )
                        {
                            mappedList.remove(object);
                        }
                    }
                }

                Integer totalNonSyncedRecords = mappedList.size();
                Integer totalSyncedRecords = totalRecords - totalNonSyncedRecords;

                responseMap.put("Synced: ", totalSyncedRecords + " of " + totalRecords + "\n" + "Missed: " + totalNonSyncedRecords);
            } else {
                responseMap.put("response: ", "there is no data recorded to send");
            }
        }
        catch (Exception e)
        {
            Log.d("ex", "Error occured while syncing: " + e.getMessage());
            responseMap.put("Error occured while syncing: ", e.getMessage());
        }
        return responseMap;
    }

    private boolean removeFromLocalDb(String sensorCode, String sensorValue)
    {
        try
        {
            dbHelper = new DBHelper(getBaseContext());
            String query = "delete from sys_sensor where sensorCode = '"+ sensorCode +"' and sensorValue = '"+ sensorValue +"'";
            dbHelper.putData(query);
            dbHelper.close();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        if(timer != null)
        {timer.cancel();}
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy()
    {
        Toast.makeText(getBaseContext(), "stopped", Toast.LENGTH_SHORT).show();
        if(timer != null)
        {timer.cancel();}
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
       return null;
    }
}

