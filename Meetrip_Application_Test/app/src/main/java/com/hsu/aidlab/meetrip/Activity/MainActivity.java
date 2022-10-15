package com.hsu.aidlab.meetrip.Activity;

import static com.hsu.aidlab.meetrip.Util.CommonUtils.controlBackgroundServices;
import static com.hsu.aidlab.meetrip.Util.CommonUtils.isMyServiceRunning;
import static com.hsu.aidlab.meetrip.Util.CommonUtils.startServiceOnce;
import static com.hsu.aidlab.meetrip.Util.CommonUtils.stopServiceOnce;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.storage.UploadTask;
import com.hsu.aidlab.meetrip.R;
import com.hsu.aidlab.meetrip.Service.MsBandService;
import com.hsu.aidlab.meetrip.Util.Constants;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.lang.ref.WeakReference;

public class MainActivity extends Activity implements View.OnClickListener {

    Button msband_monit_btn, heartRateConsent, img_upload_btn, startBandService;
    private BandClient mBandClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controlBackgroundServices(this, Constants.FLAG_START);

        msband_monit_btn = findViewById(R.id.msband_monit_btn);
        img_upload_btn = findViewById(R.id.img_upload_btn);
        heartRateConsent = findViewById(R.id.heartRateConsent);
        startBandService = findViewById(R.id.startBandService);

        msband_monit_btn.setOnClickListener(this);
        img_upload_btn.setOnClickListener(this);
        startBandService.setOnClickListener(this);


        final WeakReference<Activity> reference = new WeakReference<Activity>(this);
        heartRateConsent.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                new HeartRateConsentTask().execute(reference);
            }
        });

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.msband_monit_btn:
                startActivity(new Intent(MainActivity.this, MSBandActivity.class));
                break;

            case R.id.img_upload_btn:
                uploadImageToFirebaseStorage();
                break;

            case R.id.startBandService:
                if (!isMyServiceRunning(MsBandService.class, getBaseContext())) {
                    if (startServiceOnce(MsBandService.class, getBaseContext())) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Start",Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    if (stopServiceOnce(MsBandService.class, getBaseContext())) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Stop",Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                break;

            default:
                break;
        }
    }

    private void uploadImageToFirebaseStorage() {

        Toast toast = Toast.makeText(getApplicationContext(), "Start Upload",Toast.LENGTH_SHORT);
        toast.show();

        String file_name = "SampleImage_001.jpg";
        String storageLocationTag = "Sample Images";

        FirebaseApp.initializeApp(this);
        FirebaseStorage reference = FirebaseStorage.getInstance();
        StorageReference storageRef = reference.getReference();

        Uri img_file_uri = Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/DCIM/Meetrip/" + file_name);
        StorageReference fileRef = storageRef.child(storageLocationTag + "/"+img_file_uri.getLastPathSegment());
        UploadTask uploadTask = fileRef.putFile(img_file_uri);

        // 파일 업로드의 성공/실패에 대한 콜백 받아 핸들링 하기 위해 아래와 같이 작성한다
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.wtf("uploadTask Exception", String.valueOf(exception));
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast toast = Toast.makeText(getApplicationContext(), "Image Upload Success",Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
        @Override
        protected Void doInBackground(WeakReference<Activity>... params) {
            try {
                if (getConnectedBandClient()) {

                    if (params[0].get() != null) {
                        mBandClient.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean consentGiven) {
                            }
                        });
                    }
                } else {
                    Log.wtf("MainActivity","Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
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
                Log.wtf("MainActivity",exceptionMessage);

            } catch (Exception e) {
                Log.wtf("MainActivity",e.getMessage());
            }
            return null;
        }
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (mBandClient == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                Log.wtf("MainActivity","Band isn't paired with your phone.\n");
                return false;
            }
            mBandClient = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == mBandClient.getConnectionState()) {
            return true;
        }

        Log.wtf("MainActivity","Band is connecting...\n");
        return ConnectionState.CONNECTED == mBandClient.connect().await();
    }
}