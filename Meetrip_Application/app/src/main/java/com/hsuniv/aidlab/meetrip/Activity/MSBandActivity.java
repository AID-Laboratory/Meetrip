package com.hsuniv.aidlab.meetrip.Activity;


import static com.hsuniv.aidlab.meetrip.Util.CommonUtils.controlBackgroundServices;
import static com.hsuniv.aidlab.meetrip.Util.CommonUtils.isMyServiceRunning;
import static com.hsuniv.aidlab.meetrip.Util.CommonUtils.startServiceOnce;
import static com.hsuniv.aidlab.meetrip.Util.CommonUtils.stopServiceOnce;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.lang.ref.WeakReference;

import com.hsuniv.aidlab.meetrip.R;
import com.hsuniv.aidlab.meetrip.Service.MsBandService;
import com.hsuniv.aidlab.meetrip.Util.Constants;

public class MSBandActivity extends Activity {

    private Button btnStart, btnConsent;
    public TextView txtStatus, tvBarometerPressure, tvUV0, tvUV1,tvAltimeterFlightAscended, tvAltimeterRate, tvAltimeterSteppingGain, tvAltimeterTotalLoss, tvAltimeterTotalGain, tvAltimeterSteppingLoss, tvAltimeterStepsDescended, tvAltimeterStepsAscended, tvBarometerTemperature, tvDistanceMotionType, tvAltimeterFlightDescended, tvDistanceTotalDistance, tvDistancePace, tvDistanceSpeed, tvPedometer, tvAccelerometerX, tvAccelerometerY, tvAccelerometerZ, tvGyroscopeX, tvGyroscopeY, tvGyroscopeZ, tvHeartRate, tvHeartRateQuality, tvSkinTemperature, tvGsr, tvContact, tvAmbientLight, tvRRInterval, tvCalories;
    //    public DBHelper dbHelper;
    private BandClient client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msband);
//        dbHelper = new DBHelper(this);

        controlBackgroundServices(MSBandActivity.this, Constants.FLAG_START);

        txtStatus = (TextView) findViewById(R.id.txtStatus);

        tvHeartRate = (TextView) findViewById(R.id.tvHeartRate);
        tvHeartRateQuality = (TextView) findViewById(R.id.tvHeartRateQuality);

        tvSkinTemperature = (TextView) findViewById(R.id.tvSkinTemperature);
        tvGsr = (TextView) findViewById(R.id.tvGsr);

        tvBarometerPressure = (TextView) findViewById(R.id.tvBarometerPressure);
        tvBarometerTemperature = (TextView) findViewById(R.id.tvBarometerTemperature);

        tvDistanceMotionType = (TextView) findViewById(R.id.tvDistanceMotionType);
        tvDistanceTotalDistance = (TextView) findViewById(R.id.tvDistanceTotalDistance);
        tvDistancePace = (TextView) findViewById(R.id.tvDistancePace);
        tvDistanceSpeed = (TextView) findViewById(R.id.tvDistanceSpeed);

        tvPedometer = (TextView) findViewById(R.id.tvPedometer);

        tvAccelerometerX = (TextView) findViewById(R.id.tvAccelerometerX);
        tvAccelerometerY = (TextView) findViewById(R.id.tvAccelerometerY);
        tvAccelerometerZ = (TextView) findViewById(R.id.tvAccelerometerZ);

        tvGyroscopeX = (TextView) findViewById(R.id.tvGyroscopeX);
        tvGyroscopeY = (TextView) findViewById(R.id.tvGyroscopeY);
        tvGyroscopeZ = (TextView) findViewById(R.id.tvGyroscopeZ);

        tvUV0 = (TextView) findViewById(R.id.tvUV0);
        tvUV1 = (TextView) findViewById(R.id.tvUV1);

        tvContact = (TextView) findViewById(R.id.tvContact);

        tvAmbientLight = (TextView) findViewById(R.id.tvAmbientLight);

        tvAltimeterFlightAscended = (TextView) findViewById(R.id.tvAltimeterFlightAscended);
        tvAltimeterFlightDescended = (TextView) findViewById(R.id.tvAltimeterFlightDescended);
        tvAltimeterRate = (TextView) findViewById(R.id.tvAltimeterRate);
        tvAltimeterSteppingGain = (TextView) findViewById(R.id.tvAltimeterSteppingGain);
        tvAltimeterSteppingLoss = (TextView) findViewById(R.id.tvAltimeterSteppingLoss);
        tvAltimeterStepsAscended = (TextView) findViewById(R.id.tvAltimeterStepsAscended);
        tvAltimeterStepsDescended = (TextView) findViewById(R.id.tvAltimeterStepsDescended);
        tvAltimeterTotalGain = (TextView) findViewById(R.id.tvAltimeterTotalGain);
        tvAltimeterTotalLoss = (TextView) findViewById(R.id.tvAltimeterTotalLoss);

        tvRRInterval = (TextView) findViewById(R.id.tvRRInterval);
        tvCalories = (TextView) findViewById(R.id.tvCalories);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                txtStatus.setText("");
                if (!isMyServiceRunning(MsBandService.class, getBaseContext())) {
                    if (startServiceOnce(MsBandService.class, getBaseContext())) {
                        btnStart.setText("Stop");
                        appendToUI(String.format("Band started\n"));
                    }
                } else {
                    if (stopServiceOnce(MsBandService.class, getBaseContext())) {
                        btnStart.setText("Start");
                        appendToUI(String.format("Band stopped\n"));
                    }
                }
            }
        });

        final WeakReference<Activity> reference = new WeakReference<Activity>(this);
        btnConsent = (Button) findViewById(R.id.btnConsent);
        btnConsent.setOnClickListener(new OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                new HeartRateConsentTask().execute(reference);
            }
        });

        Intent intent = new Intent(this, MsBandService.class);
        intent.putExtra("bandHeartRateReceiver", resultReceiver); //(2)
        intent.putExtra("bandHeartQualityReceiver", resultReceiver);
        intent.putExtra("bandSkinTemperatureReceiver", resultReceiver);
        intent.putExtra("bandGsrReceiver", resultReceiver);
        intent.putExtra("bandBarometerPressureReceiver", resultReceiver);
        intent.putExtra("bandBarometerTemperatureReceiver", resultReceiver);
        intent.putExtra("bandDistanceMotionTypeReceiver", resultReceiver);
        intent.putExtra("bandDistanceTotalDistanceReceiver", resultReceiver);
        intent.putExtra("bandDistancePaceReceiver", resultReceiver);
        intent.putExtra("bandDistanceSpeedReceiver", resultReceiver);
        intent.putExtra("bandPedometerReceiver", resultReceiver);
        intent.putExtra("bandAccelerometerXReceiver", resultReceiver);
        intent.putExtra("bandAccelerometerYReceiver", resultReceiver);
        intent.putExtra("bandAccelerometerZReceiver", resultReceiver);
        intent.putExtra("bandGyroscopeXReceiver", resultReceiver);
        intent.putExtra("bandGyroscopeYReceiver", resultReceiver);
        intent.putExtra("bandGyroscopeZReceiver", resultReceiver);
        intent.putExtra("bandUvLevel0Receiver", resultReceiver);
        intent.putExtra("bandUvLevel1Receiver", resultReceiver);
        intent.putExtra("bandContactReceiver", resultReceiver);
        intent.putExtra("bandRRIntervalReceiver", resultReceiver);
        intent.putExtra("bandCaloriesReceiver", resultReceiver);
        intent.putExtra("bandAmbientLightReceiver", resultReceiver);
        intent.putExtra("bandAltimeterFlightAscendedReceiver", resultReceiver);
        intent.putExtra("bandAltimeterFlightDescendedReceiver", resultReceiver);
        intent.putExtra("bandAltimeterRateReceiver", resultReceiver);
        intent.putExtra("bandAltimeterSteppingGainReceiver", resultReceiver);
        intent.putExtra("bandAltimeterSteppingLossReceiver", resultReceiver);
        intent.putExtra("bandAltimeterStepsAscendedReceiver", resultReceiver);
        intent.putExtra("bandAltimeterStepsDescendedReceiver", resultReceiver);
        intent.putExtra("bandAltimeterTotalGainReceiver", resultReceiver);
        intent.putExtra("bandAltimeterTotalLossReceiver", resultReceiver);
        intent.putExtra("bandStatusReceiver", resultReceiver);
        startService(intent);

    }

    private Handler handler = new Handler();

    private ResultReceiver resultReceiver = new ResultReceiver(handler) { // (3)

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            // SYNC_COMPLETED CODE = 1
            if (resultCode == 1) {  // (4)
                tvHeartRate.setText(resultData.getString("bandHeartRate"));
                tvHeartRateQuality.setText(resultData.getString("bandHeartQuality"));
                tvSkinTemperature.setText(resultData.getString("bandSkinTemperature"));

                tvGsr.setText(resultData.getString("bandGsr"));

                tvBarometerPressure.setText(resultData.getString("bandBarometerPressure"));
                tvBarometerTemperature.setText(resultData.getString("bandBarometerTemperature"));

                tvDistanceMotionType.setText(resultData.getString("bandDistanceMotionType"));
                tvDistanceTotalDistance.setText(resultData.getString("bandDistanceTotalDistance"));
                tvDistancePace.setText(resultData.getString("bandDistancePace"));
                tvDistanceSpeed.setText(resultData.getString("bandDistanceSpeed"));

                tvPedometer.setText(resultData.getString("bandPedometer"));

                tvAccelerometerX.setText(resultData.getString("bandAccelerometerX"));
                tvAccelerometerY.setText(resultData.getString("bandAccelerometerY"));
                tvAccelerometerZ.setText(resultData.getString("bandAccelerometerZ"));

                tvGyroscopeX.setText(resultData.getString("bandGyroscopeX"));
                tvGyroscopeY.setText(resultData.getString("bandGyroscopeY"));
                tvGyroscopeZ.setText(resultData.getString("bandGyroscopeZ"));

                tvUV0.setText(resultData.getString("bandUvLevel0"));
                tvUV1.setText(resultData.getString("bandUvLevel1"));

                tvContact.setText(resultData.getString("bandContact"));

                tvRRInterval.setText(resultData.getString("bandRRInterval"));

                tvCalories.setText(resultData.getString("bandCalories"));

                tvAmbientLight.setText(resultData.getString("bandAmbientLight"));

                tvAltimeterFlightAscended.setText(resultData.getString("bandAltimeterFlightAscended"));
                tvAltimeterFlightDescended.setText(resultData.getString("bandAltimeterFlightDescended"));
                tvAltimeterRate.setText(resultData.getString("bandAltimeterRate"));
                tvAltimeterSteppingGain.setText(resultData.getString("bandAltimeterSteppingGain"));
                tvAltimeterSteppingLoss.setText(resultData.getString("bandAltimeterSteppingLoss"));
                tvAltimeterStepsAscended.setText(resultData.getString("bandAltimeterStepsAscended"));
                tvAltimeterStepsDescended.setText(resultData.getString("bandAltimeterStepsDescended"));
                tvAltimeterTotalGain.setText(resultData.getString("bandAltimeterTotalGain"));
                tvAltimeterTotalLoss.setText(resultData.getString("bandAltimeterTotalLoss"));

                txtStatus.setText(resultData.getString("bandStatus"));

            }

        }
    };

    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
        @Override
        protected Void doInBackground(WeakReference<Activity>... params) {
            try {
                if (getConnectedBandClient()) {

                    if (params[0].get() != null) {
                        client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean consentGiven) {
                            }
                        });
                    }
                } else {
                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
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
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }

    @Override
    protected void onResume() {
        super.onResume();

        txtStatus.setText("");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void appendToUI(final String string) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtStatus.setText(string);

            }
        });
    }
}


