package com.hsu.aidlab.meetrip.Activity;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.hsu.aidlab.meetrip.R;
import com.hsu.aidlab.meetrip.Service.MsBandService;
import com.hsu.aidlab.meetrip.Util.Constants;
import com.hsu.aidlab.meetrip.Util.DBHelper;

import static com.hsu.aidlab.meetrip.Util.CommonUtils.controlBackgroundServices;
import static com.hsu.aidlab.meetrip.Util.CommonUtils.isMyServiceRunning;
import static com.hsu.aidlab.meetrip.Util.CommonUtils.startServiceOnce;
import static com.hsu.aidlab.meetrip.Util.CommonUtils.stopServiceOnce;

public class MSBand extends Activity {

    private Button btnStart,btnConsent;
    private TextView txtStatus;
    public DBHelper dbHelper;
    private BandClient client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msband);
        dbHelper = new DBHelper(this);

        controlBackgroundServices(MSBand.this, Constants.FLAG_START);

        txtStatus = (TextView) findViewById(R.id.txtStatus);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                txtStatus.setText("");
                if ( !isMyServiceRunning(MsBandService.class, getBaseContext())){
                    if( startServiceOnce(MsBandService.class, getBaseContext()) ){
                        btnStart.setText("Stop");
                        appendToUI(String.format("Band started\n"));
                    }
                } else {
                    if(stopServiceOnce(MsBandService.class, getBaseContext())){
                        btnStart.setText("Start");
                        appendToUI(String.format("Band stopped\n"));
                    }
                }
            }
        });

        final WeakReference<Activity> reference = new WeakReference<Activity>(this);
        btnConsent = (Button) findViewById(R.id.btnConsent);
        btnConsent.setOnClickListener(new OnClickListener()
        {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v)
            {
                new HeartRateConsentTask().execute(reference);
            }
        });
    }

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


