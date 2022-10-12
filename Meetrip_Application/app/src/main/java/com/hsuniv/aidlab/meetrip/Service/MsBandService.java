package com.hsuniv.aidlab.meetrip.Service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.InvalidBandVersionException;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandAltimeterEvent;
import com.microsoft.band.sensors.BandAltimeterEventListener;
import com.microsoft.band.sensors.BandAmbientLightEvent;
import com.microsoft.band.sensors.BandAmbientLightEventListener;
import com.microsoft.band.sensors.BandBarometerEvent;
import com.microsoft.band.sensors.BandBarometerEventListener;
import com.microsoft.band.sensors.BandCaloriesEvent;
import com.microsoft.band.sensors.BandCaloriesEventListener;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGsrEvent;
import com.microsoft.band.sensors.BandGsrEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.SampleRate;

import java.util.Timer;
import java.util.TimerTask;

import com.hsuniv.aidlab.meetrip.Util.CommonUtils;
import com.hsuniv.aidlab.meetrip.Util.Constants;
import com.hsuniv.aidlab.meetrip.Util.DBHelper;

public class MsBandService extends Service {

    private BandClient client = null;
    private DBHelper dbHelper;
    private String TAG = "MSBandService";
    private String heartrate, quality , serverStatus;
    private String skt, gsr;
    private String pressure, temperature;
    private String motiontype, totaldistance, pace, speed;
    private String totalstep;
    private String x, y, z, x1, y1, z1;
    private String indexlevel, indexlevel1, RR, bandcontact, light, calories;
    private String flightascended, flightdescended, rate, steppinggain, steppingloss, stepsascended, stepsdescended, totalgain, totalloss;

    private boolean flag = false;
    private Timer timer;
    private BandTask bandTask;

    Intent mIntent;

    Bundle bundle = new Bundle();
    ResultReceiver receiver;

    public MsBandService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mIntent = intent;
        dbHelper = new DBHelper(getBaseContext());
        /*final WeakReference<Activity> reference = new WeakReference<Activity>(this);*/
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                flag = true;
            }
        }, 0 , 5 * 1000);

        new HeartRateConsentTask().execute();
        return START_STICKY;
    }

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {
                //appendToUI(String.format("Heart Rate = %s beats per minute\n", heartrate));
                heartrate = String.valueOf(event.getHeartRate());
                quality = String.valueOf(event.getQuality());
                String sensorData = "{\"heartrate\" :" + heartrate + ",\"quality\" :\"" + quality + "\",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                bandTask = new BandTask();
                bandTask.execute(Constants.TAG_HeartRate, sensorData);

                receiver = mIntent.getParcelableExtra("bandHeartRateReceiver"); // (1)
                bundle.putString("bandHeartRate", heartrate);
                receiver.send(1,bundle); // (2)

                receiver = mIntent.getParcelableExtra("bandHeartQualityReceiver");
                bundle.putString("bandHeartQuality", quality);
                receiver.send(1,bundle);

            }
        }
    };

    private BandSkinTemperatureEventListener bandSkinTemperatureEventListener = new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(final BandSkinTemperatureEvent event) {
            if (event != null) {
                skt = String.valueOf(event.getTemperature());
                String sensorData = "{\"skt\" :" + skt  + ",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                bandTask = new BandTask();
                bandTask.execute(Constants.TAG_SkinTemperature, sensorData);

                receiver = mIntent.getParcelableExtra("bandSkinTemperatureReceiver");
                bundle.putString("bandSkinTemperature", skt);
                receiver.send(1,bundle);
            }
        }
    };

    private BandGsrEventListener bandGsrEventListener = new BandGsrEventListener() {
        @Override
        public void onBandGsrChanged(final BandGsrEvent event) {
            if (event != null) {
                gsr = String.valueOf(event.getResistance());
                String sensorData = "{\"gsr\" :" + gsr  + ",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                bandTask = new BandTask();
                bandTask.execute(Constants.TAG_Gsr, sensorData);

                receiver = mIntent.getParcelableExtra("bandGsrReceiver");
                bundle.putString("bandGsr", gsr);
                receiver.send(1,bundle);
            }
        }
    };
    private BandBarometerEventListener bandBarometerEventListener = new BandBarometerEventListener() {
        @Override
        public void onBandBarometerChanged(BandBarometerEvent bandBarometerEvent) {
            if (bandBarometerEvent != null) {
                pressure = String.valueOf(bandBarometerEvent.getAirPressure());
                temperature = String.valueOf(bandBarometerEvent.getTemperature());
                String sensorData = "{\"pressure\" :" + pressure + ",\"temperature\" :" + temperature  + ",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                bandTask = new BandTask();
                bandTask.execute(Constants.TAG_Barometer, sensorData);

                receiver = mIntent.getParcelableExtra("bandBarometerPressureReceiver");
                bundle.putString("bandBarometerPressure", pressure);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandBarometerTemperatureReceiver");
                bundle.putString("bandBarometerTemperature", temperature);
                receiver.send(1,bundle);
            }
        }
    };

    private BandDistanceEventListener bandDistanceEventListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(BandDistanceEvent bandDistanceEvent) {
            if (bandDistanceEvent != null) {
                motiontype = String.valueOf(bandDistanceEvent.getMotionType());
                totaldistance = String.valueOf(bandDistanceEvent.getTotalDistance());
                pace = String.valueOf(bandDistanceEvent.getPace());
                speed = String.valueOf(bandDistanceEvent.getSpeed());
                String sensorData = "{\"motiontype\" :\"" + motiontype + "\",\"totaldistance\" :" + totaldistance  + ",\"pace\" :" + pace  + ",\"speed\" :" + speed  + ",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                bandTask = new BandTask();
                bandTask.execute(Constants.TAG_Distance, sensorData);

                receiver = mIntent.getParcelableExtra("bandDistanceMotionTypeReceiver");
                bundle.putString("bandDistanceMotionType", motiontype);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandDistanceTotalDistanceReceiver");
                bundle.putString("bandDistanceTotalDistance", totaldistance);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandDistancePaceReceiver");
                bundle.putString("bandDistancePace", pace);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandDistanceSpeedReceiver");
                bundle.putString("bandDistanceSpeed", speed);
                receiver.send(1,bundle);
            }
        }
    };
    private BandPedometerEventListener bandPedometerEventListener = new BandPedometerEventListener() {
        @Override
        public void onBandPedometerChanged(BandPedometerEvent bandPedometerEvent) {
            if (bandPedometerEvent != null) {
                totalstep = String.valueOf(bandPedometerEvent.getTotalSteps());
                String sensorData = "{\"totalstep\" :" + totalstep  + ",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                bandTask = new BandTask();
                bandTask.execute(Constants.TAG_Pedometer, sensorData);

                receiver = mIntent.getParcelableExtra("bandPedometerReceiver");
                bundle.putString("bandPedometer", totalstep);
                receiver.send(1,bundle);
            }
        }
    };

    private BandAccelerometerEventListener bandAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(BandAccelerometerEvent bandAccelerometerEvent) {
            if (bandAccelerometerEvent != null) {
                x = String.valueOf(bandAccelerometerEvent.getAccelerationX());
                y = String.valueOf(bandAccelerometerEvent.getAccelerationY());
                z = String.valueOf(bandAccelerometerEvent.getAccelerationZ());
                String sensorData = "{\"x\" :" + x + ",\"y\" :" + y  + ",\"z\" :" + z  + ",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                executeAsync(Constants.TAG_Accelerometer, sensorData);

                receiver = mIntent.getParcelableExtra("bandAccelerometerXReceiver");
                bundle.putString("bandAccelerometerX", x);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandAccelerometerYReceiver");
                bundle.putString("bandAccelerometerY", y);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandAccelerometerZReceiver");
                bundle.putString("bandAccelerometerZ", z);
                receiver.send(1,bundle);
            }
        }
    };

    private BandGyroscopeEventListener bandGyroscopeEventListener = new BandGyroscopeEventListener() {
        @Override
        public void onBandGyroscopeChanged(BandGyroscopeEvent bandGyroscopeEvent) {
            if (bandGyroscopeEvent != null) {
                x1 = String.valueOf(bandGyroscopeEvent.getAngularVelocityX());
                y1 = String.valueOf(bandGyroscopeEvent.getAngularVelocityY());
                z1 = String.valueOf(bandGyroscopeEvent.getAngularVelocityZ());
                String sensorData = "{\"x1\" :" + x1 + ",\"y1\" :" + y1  + ",\"z1\" :" + z1  + ",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                executeAsync(Constants.TAG_Gyroscope, sensorData);

                receiver = mIntent.getParcelableExtra("bandGyroscopeXReceiver");
                bundle.putString("bandGyroscopeX", x1);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandGyroscopeYReceiver");
                bundle.putString("bandGyroscopeY", y1);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandGyroscopeZReceiver");
                bundle.putString("bandGyroscopeZ", z1);
                receiver.send(1,bundle);
            }
        }
    };

    private BandUVEventListener bandUVEventListener = new BandUVEventListener() {
        @Override
        public void onBandUVChanged(BandUVEvent bandUVEvent) {
            if (bandUVEvent != null) {
                indexlevel = String.valueOf(bandUVEvent.getUVIndexLevel());
                try {
                    indexlevel1 = String.valueOf(bandUVEvent.getUVExposureToday());
                    String sensorData = "{\"UV\" :\"" + indexlevel + "\",\"UV today\" :" + indexlevel1  + ",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                    bandTask = new BandTask();
                    bandTask.execute(Constants.TAG_UV, sensorData);

                    receiver = mIntent.getParcelableExtra("bandUvLevel0Receiver");
                    bundle.putString("bandUvLevel0", indexlevel);
                    receiver.send(1,bundle);

                    receiver = mIntent.getParcelableExtra("bandUvLevel1Receiver");
                    bundle.putString("bandUvLevel1", indexlevel1);
                    receiver.send(1,bundle);

                } catch (InvalidBandVersionException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private BandContactEventListener bandContactEventListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(BandContactEvent bandContactEvent) {
            if (bandContactEvent != null) {
                bandcontact = String.valueOf(bandContactEvent.getContactState());
                String sensorData = "{\"bandcontact\" :\"" + bandcontact  + "\",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                bandTask = new BandTask();
                bandTask.execute(Constants.TAG_Contact, sensorData);

                receiver = mIntent.getParcelableExtra("bandContactReceiver");
                bundle.putString("bandContact", bandcontact);
                receiver.send(1,bundle);
            }
        }
    };

    private BandRRIntervalEventListener bandRRIntervalEventListener = new BandRRIntervalEventListener() {
        @Override
        public void onBandRRIntervalChanged(BandRRIntervalEvent bandRRIntervalEvent) {
            if (bandRRIntervalEvent != null) {
                RR = String.valueOf(bandRRIntervalEvent.getInterval());
                String sensorData = "{\"RR\" :" + RR  + ",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                bandTask = new BandTask();
                bandTask.execute(Constants.TAG_RRInterval, sensorData);

                receiver = mIntent.getParcelableExtra("bandRRIntervalReceiver");
                bundle.putString("bandRRInterval", RR);
                receiver.send(1,bundle);
            }
        }
    };

    private BandCaloriesEventListener bandCaloriesEventListener = new BandCaloriesEventListener() {
        @Override
        public void onBandCaloriesChanged(BandCaloriesEvent bandCaloriesEvent) {
            if (bandCaloriesEvent != null) {
                calories = String.valueOf(bandCaloriesEvent.getCalories());
                String sensorData = "{\"calories\" :" + calories  + ",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                bandTask = new BandTask();
                bandTask.execute(Constants.TAG_Calories, sensorData);

                receiver = mIntent.getParcelableExtra("bandCaloriesReceiver");
                bundle.putString("bandCalories", calories);
                receiver.send(1,bundle);
            }
        }
    };

    private BandAmbientLightEventListener bandAmbientLightEventListener = new BandAmbientLightEventListener() {
        @Override
        public void onBandAmbientLightChanged(BandAmbientLightEvent bandAmbientLightEvent) {
            if (bandAmbientLightEvent != null) {
                light = String.valueOf(bandAmbientLightEvent.getBrightness());
                String sensorData = "{\"light\" :" + light  + ",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                bandTask = new BandTask();
                bandTask.execute(Constants.TAG_AmbientLight, sensorData);

                receiver = mIntent.getParcelableExtra("bandAmbientLightReceiver");
                bundle.putString("bandAmbientLight", light);
                receiver.send(1,bundle);
            }
        }
    };

    private BandAltimeterEventListener bandAltimeterEventListener = new BandAltimeterEventListener() {
        @Override
        public void onBandAltimeterChanged(BandAltimeterEvent bandAltimeterEvent) {
            if (bandAltimeterEvent != null) {
                flightascended = String.valueOf(bandAltimeterEvent.getFlightsAscended());
                flightdescended = String.valueOf(bandAltimeterEvent.getFlightsDescended());
                rate = String.valueOf(bandAltimeterEvent.getRate());
                steppinggain = String.valueOf(bandAltimeterEvent.getSteppingGain());
                steppingloss = String.valueOf(bandAltimeterEvent.getSteppingLoss());
                stepsascended = String.valueOf(bandAltimeterEvent.getStepsAscended());
                stepsdescended = String.valueOf(bandAltimeterEvent.getStepsDescended());
                totalgain = String.valueOf(bandAltimeterEvent.getTotalGain());
                totalloss = String.valueOf(bandAltimeterEvent.getTotalLoss());
                String sensorData = "{\"Rate\" :" + rate + ",\"FlightAscended\" :" + flightascended + ",\"FlightDescended\" :" + flightdescended + ",\"Total gain\" :" + totalgain + ",\"Totalloss\" :" + totalloss + ",\"Stepping gain\" :" + steppinggain + ",\"Stepping loss\" :" + steppingloss + ",\"Date\":\"" + CommonUtils.getCurDateStr() + "\"}";
                bandTask = new BandTask();
                bandTask.execute(Constants.TAG_Altimeter, sensorData);

                receiver = mIntent.getParcelableExtra("bandAltimeterFlightAscendedReceiver");
                bundle.putString("bandAltimeterFlightAscended", flightascended);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandAltimeterFlightDescendedReceiver");
                bundle.putString("bandAltimeterFlightDescended", flightdescended);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandAltimeterRateReceiver");
                bundle.putString("bandAltimeterRate", rate);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandAltimeterSteppingGainReceiver");
                bundle.putString("bandAltimeterSteppingGain", steppinggain);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandAltimeterSteppingLossReceiver");
                bundle.putString("bandAltimeterSteppingLoss", steppingloss);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandAltimeterStepsAscendedReceiver");
                bundle.putString("bandAltimeterStepsAscended", stepsascended);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandAltimeterStepsDescendedReceiver");
                bundle.putString("bandAltimeterStepsDescended", stepsdescended);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandAltimeterTotalGainReceiver");
                bundle.putString("bandAltimeterTotalGain", totalgain);
                receiver.send(1,bundle);

                receiver = mIntent.getParcelableExtra("bandAltimeterTotalLossReceiver");
                bundle.putString("bandAltimeterTotalLoss", totalloss);
                receiver.send(1,bundle);
            }
        }
    };

    public class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        public Void doInBackground(Void... params) {
            try {
                int hardwareVersion = Integer.parseInt(client.getHardwareVersion().await());

                if (getConnectedBandClient()) {
                    if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED && hardwareVersion >= 20) {
                        client.getSensorManager().registerGsrEventListener(bandGsrEventListener);
                        client.getSensorManager().registerSkinTemperatureEventListener(bandSkinTemperatureEventListener);
//                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                        client.getSensorManager().registerBarometerEventListener(bandBarometerEventListener);
                        client.getSensorManager().registerDistanceEventListener(bandDistanceEventListener);
                        client.getSensorManager().registerAltimeterEventListener(bandAltimeterEventListener);
                        client.getSensorManager().registerPedometerEventListener(bandPedometerEventListener);
                        client.getSensorManager().registerAccelerometerEventListener(bandAccelerometerEventListener, SampleRate.MS16);
                        client.getSensorManager().registerGyroscopeEventListener(bandGyroscopeEventListener, SampleRate.MS16);
                        client.getSensorManager().registerUVEventListener(bandUVEventListener);
                        client.getSensorManager().registerContactEventListener(bandContactEventListener);
                        client.getSensorManager().registerAmbientLightEventListener(bandAmbientLightEventListener);
                        client.getSensorManager().registerRRIntervalEventListener(bandRRIntervalEventListener);
                        client.getSensorManager().registerCaloriesEventListener(bandCaloriesEventListener);
//                        client.getSensorManager().registerRRIntervalEventListener(bandRRIntervalEventListener);
//                        client.getSensorManager().registerCaloriesEventListener(bandCaloriesEventListener);

//                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                        if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                            client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                        }
                    }
                } else {
                    //appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                    Log.d(TAG, "Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");

                    receiver = mIntent.getParcelableExtra("bandStatusReceiver");
                    bundle.putString("bandStatus", "Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                    receiver.send(1,bundle);
                }

            } catch (BandException e) {
                String exceptionMessage = "";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                //appendToUI(exceptionMessage);
                Log.d(TAG, exceptionMessage);

                receiver = mIntent.getParcelableExtra("bandStatusReceiver");
                bundle.putString("bandStatus", exceptionMessage);
                receiver.send(1,bundle);

            } catch (Exception e) {
                //appendToUI(e.getMessage());
                Log.d(TAG, e.getMessage());

                receiver = mIntent.getParcelableExtra("bandStatusReceiver");
                bundle.putString("bandStatus", e.getMessage());
                receiver.send(1,bundle);
            }
            return null;
        }
    }

    private class HeartRateConsentTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                if (getConnectedBandClient()) {
                    return true;
                    /*if (params[0].get() != null) {
                        client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean consentGiven) {
                            }
                        });
                    }*/
                } else {
                    //appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                    Log.d(TAG, "Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");

                    receiver = mIntent.getParcelableExtra("bandStatusReceiver");
                    bundle.putString("bandStatus", "Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                    receiver.send(1,bundle);
                }
            } catch (BandException e) {
                String exceptionMessage = "";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                //appendToUI(exceptionMessage);
                Log.d(TAG, exceptionMessage);

                receiver = mIntent.getParcelableExtra("bandStatusReceiver");
                bundle.putString("bandStatus", e.getMessage());
                receiver.send(1,bundle);

            } catch (Exception e) {
                //appendToUI(e.getMessage());
                Log.d(TAG, e.getMessage());

                receiver = mIntent.getParcelableExtra("bandStatusReceiver");
                bundle.putString("bandStatus", e.getMessage());
                receiver.send(1,bundle);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean){ new HeartRateSubscriptionTask().execute();};
            super.onPostExecute(aBoolean);
        }
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                //appendToUI("Band isn't paired with your phone.\n");
                Log.d(TAG, "Band isn't paired with your phone.\n");

                receiver = mIntent.getParcelableExtra("bandStatusReceiver");
                bundle.putString("bandStatus", "Band isn't paired with your phone.");
                receiver.send(1,bundle);

                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        //appendToUI("Band is connecting...\n");
        Log.d(TAG, "Band is connecting...\\n");

        receiver = mIntent.getParcelableExtra("bandStatusReceiver");
        bundle.putString("bandStatus", "Band is connecting...");
        receiver.send(1,bundle);

        return ConnectionState.CONNECTED == client.connect().await();

    }

    private void executeAsync(String sensorCode, String sensorData){
        if (flag){
            bandTask = new BandTask();
            bandTask.execute(sensorCode, sensorData);
            flag = false;
        }
    }

    private class BandTask extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... data)
        {
            String sensorCode = data[0];
            String sensorValues = data[1];
            String query = "insert into sys_sensor (sensorCode, sensorValue) values ('" + sensorCode + "', '" + sensorValues + "')";
            dbHelper.putData(query);
            return query;
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            Log.d("MSBand logging", string);

//            receiver = mIntent.getParcelableExtra("bandStatusReceiver");
//            bundle.putString("bandStatus", string);
//            receiver.send(1,bundle);
        }
    }

    @Override
    public void onDestroy() {
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }
}
