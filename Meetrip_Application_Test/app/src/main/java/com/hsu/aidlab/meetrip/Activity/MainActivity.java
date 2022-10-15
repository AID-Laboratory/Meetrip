package com.hsu.aidlab.meetrip.Activity;

import static com.hsu.aidlab.meetrip.Util.CommonUtils.controlBackgroundServices;
import static com.hsu.aidlab.meetrip.Util.CommonUtils.isMyServiceRunning;
import static com.hsu.aidlab.meetrip.Util.CommonUtils.startServiceOnce;
import static com.hsu.aidlab.meetrip.Util.CommonUtils.stopServiceOnce;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.storage.UploadTask;
import com.hsu.aidlab.meetrip.R;
import com.hsu.aidlab.meetrip.Service.MsBandService;
import com.hsu.aidlab.meetrip.Util.CommonUtils;
import com.hsu.aidlab.meetrip.Util.Constants;
import com.hsu.aidlab.meetrip.Util.DBHelper;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.lang.ref.WeakReference;

public class MainActivity extends Activity implements View.OnClickListener {

    Button img_upload_btn;
    TextView tasStatusReceiver;
    public static Context thisContext;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(getBaseContext());
        controlBackgroundServices(this, Constants.FLAG_START);

        img_upload_btn = findViewById(R.id.img_upload_btn);
//        tasStatusReceiver = findViewById(R.id.tasStatusReceiver);

        img_upload_btn.setOnClickListener(this);

        thisContext = this;


//        Intent intent = new Intent(this, MsBandService.class);
//        intent.putExtra("tasStatusReceiver", resultReceiver); //(2)

    }

//    private Handler handler = new Handler();
//    private ResultReceiver resultReceiver = new ResultReceiver(handler) { // (3)
//
//        @Override
//        protected void onReceiveResult(int resultCode, Bundle resultData) {
//            super.onReceiveResult(resultCode, resultData);
//
//            // SYNC_COMPLETED CODE = 1
//            if (resultCode == 1) {  // (4)
//                tasStatusReceiver.setText(resultData.getString("tasStatus"));
//            }
//        }
//    };


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.img_upload_btn:
                uploadImageToFirebaseStorage();
                break;

            default:
                break;
        }
    }

    private void uploadImageToFirebaseStorage() {

        Toast toast = Toast.makeText(getApplicationContext(), "Start Upload", Toast.LENGTH_SHORT);
        toast.show();

        String file_name = "SampleImage_001.jpg";
        String storageLocationTag = "Sample Images";

        FirebaseApp.initializeApp(this);
        FirebaseStorage reference = FirebaseStorage.getInstance();
        StorageReference storageRef = reference.getReference();

        Uri img_file_uri = Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/DCIM/Meetrip/" + file_name);
        StorageReference fileRef = storageRef.child(storageLocationTag + "/" + img_file_uri.getLastPathSegment());
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
                uploadImagePathToTAS(storageLocationTag, file_name);
                Toast toast = Toast.makeText(getApplicationContext(), "Image Upload Success", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void uploadImagePathToTAS(String storageLocationTag, String file_name) {

        String sensorData = "{\"TAG\" :\"" + storageLocationTag + "\",\"ImgName\" :\"" + file_name + "\"}";
        String query = "insert into sys_sensor (sensorCode, sensorValue) values ('Image', '" + sensorData + "')";
        dbHelper.putData(query);

    }
}