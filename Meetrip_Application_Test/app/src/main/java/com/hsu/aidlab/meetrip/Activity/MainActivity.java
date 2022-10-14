package com.hsu.aidlab.meetrip.Activity;

import static com.hsu.aidlab.meetrip.Util.CommonUtils.controlBackgroundServices;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.hsu.aidlab.meetrip.Model;
import com.hsu.aidlab.meetrip.R;
import com.hsu.aidlab.meetrip.Util.Constants;

import java.io.File;

public class MainActivity extends Activity implements View.OnClickListener {

    Button msband_monit_btn;
    Button img_upload_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        String file_name = "SampleImage_001.jpg";

//        FirebaseStorage storage = FirebaseStorage.getInstance("gs://my-custom-bucket");
//        StorageReference storageRef = storage.getReference();
//        StorageReference spaceRef = storageRef.child("images/space.jpg");

        FirebaseApp.initializeApp(this);

        FirebaseStorage reference = FirebaseStorage.getInstance();
        StorageReference storageRef = reference.getReference();
//        StorageReference fileRef = storageRef.child("Sample_Images/" + file_name);

        Uri img_file_uri = Uri.parse(Environment.getExternalStorageDirectory() + "/DCIM/Meetrip/" + file_name);
        StorageReference fileRef = storageRef.child("Sample_Images/"+img_file_uri.getLastPathSegment());

        UploadTask uploadTask = fileRef.putFile(img_file_uri);

        // 파일 업로드의 성공/실패에 대한 콜백 받아 핸들링 하기 위해 아래와 같이 작성한다
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });

    }
}