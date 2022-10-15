package com.hsu.aidlab.meetrip.Activity;

import static com.hsu.aidlab.meetrip.Util.CommonUtils.controlBackgroundServices;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import com.hsu.aidlab.meetrip.Util.Constants;

public class MainActivity extends Activity implements View.OnClickListener {

    Button msband_monit_btn;
    Button img_upload_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        controlBackgroundServices(this, Constants.FLAG_START);

//        controlBackgroundServices(this, Constants.FLAG_START);

        msband_monit_btn = (Button) findViewById(R.id.msband_monit_btn);
        img_upload_btn = (Button) findViewById(R.id.img_upload_btn);

        msband_monit_btn.setOnClickListener(this);
        img_upload_btn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.msband_monit_btn:
                startActivity(new Intent(MainActivity.this, MSBandActivity.class));
                break;

            case R.id.img_upload_btn:
                uploadImageToFirebaseStorage();
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
                Log.w("uploadTask Exception", String.valueOf(exception));
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast toast = Toast.makeText(getApplicationContext(), "Image Upload Success",Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }
}