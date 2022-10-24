package com.hsu.aidlab.meetrip.Activity;

import static com.hsu.aidlab.meetrip.Util.CommonUtils.controlBackgroundServices;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.hsu.aidlab.meetrip.R;
import com.hsu.aidlab.meetrip.Util.Constants;
import com.hsu.aidlab.meetrip.Util.DBHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener {

    Button img_upload_btn, img_shoot_btn;
    TextView tasStatusReceiver;
    ImageView imageView;
    public static Context thisContext;
    private DBHelper dbHelper;

    final private static String TAG = "태그명";

    final static int TAKE_PICTURE = 1;

    String currentPhotoPath;
    final static int REQUEST_TAKE_PHOTO = 1;
    private View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(getBaseContext());
        controlBackgroundServices(this, Constants.FLAG_START);

//        img_upload_btn = findViewById(R.id.img_upload_btn);
        img_shoot_btn = findViewById(R.id.img_shoot_btn);

        img_shoot_btn = (Button) findViewById(R.id.img_shoot_btn);
//        imageView = (ImageView) findViewById(R.id.imageView);

//        img_upload_btn.setOnClickListener(this);
        img_shoot_btn.setOnClickListener(this);

        thisContext = this;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

//            case R.id.img_upload_btn:
//                uploadImageToFirebaseStorage("SampleImage_001.jpg", "Sample Images");
//                break;

            case R.id.img_shoot_btn:
                dispatchTakePictureIntent();

            default:
                break;
        }
    }

    // String file_name = "SampleImage_001.jpg";
    // String storageLocationTag = "Sample Images";
    private void uploadImageToFirebaseStorage(String file_name, String storageLocationTag) {

        Toast toast = Toast.makeText(getApplicationContext(), "Start Upload", Toast.LENGTH_SHORT);
        toast.show();


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
                String fNameToast = file_name.split("_")[0] + "_" + file_name.split("_")[1];
                Toast toast = Toast.makeText(getApplicationContext(), "Uploaded\n" + fNameToast, Toast.LENGTH_SHORT);
                Log.wtf("업로드 됐나?","ㅇㅇㅇㅇ 모비우스로도 보냄 ㄹㅇㅋㅋ");
                toast.show();
            }
        });
    }

    private void uploadImagePathToTAS(String storageLocationTag, String file_name) {

        String sensorData = "{\"TAG\" :\"" + storageLocationTag + "\",\"ImgName\" :\"" + file_name + "\"}";
        String query = "insert into sys_sensor (sensorCode, sensorValue) values ('Image', '" + sensorData + "')";
        dbHelper.putData(query);

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/DCIM/Meetrip/");
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.hsu.aidlab.meetrip.provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                while (true){
//                    Log.wtf("File_Size", String.valueOf(photoFile.length()));
                    if (photoFile.length() != 0){
                        uploadImageToFirebaseStorage(currentPhotoPath.replace("/storage/emulated/0/DCIM/Meetrip/",""), "Sample Images");
                        break;
                    }
                }
            }
        }
    }
}

