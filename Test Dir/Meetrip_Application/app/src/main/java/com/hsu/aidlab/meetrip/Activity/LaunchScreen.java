package com.hsu.aidlab.meetrip.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.FirebaseApp;
import com.hsu.aidlab.meetrip.Util.CommonUtils;
import com.hsu.aidlab.meetrip.Util.Constants;


import static com.hsu.aidlab.meetrip.Util.CommonUtils.showAlertDlg;

public class LaunchScreen extends AppCompatActivity
{
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private ImageView imageView;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(com.hsu.aidlab.meetrip.R.layout.activity_launch_screen);

        context = LaunchScreen.this;
        
        imageView = (ImageView) findViewById(com.hsu.aidlab.meetrip.R.id.splashIcon);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), com.hsu.aidlab.meetrip.R.anim.zoom_in);
        imageView.setAnimation(animation);

//        new Handler().postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//              if(checkPermisson(CommonUtils.getPermissions()))
//              {
//                  startMainScreen();
//              }
//            }
//        }, 2000);
    }
//
//    // TODO: Check permissions
//    private boolean checkPermisson(String[] permissions)
//    {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
//        {
//            int result;
//            List<String> listPermissionsNeeded = new ArrayList<>();
//
//            for (String permisson : permissions)
//            {
//                result = ContextCompat.checkSelfPermission(LaunchScreen.this, permisson);
//                if (result != PackageManager.PERMISSION_GRANTED)
//                {
//                    listPermissionsNeeded.add(permisson);
//                }
//            }
//            if (!listPermissionsNeeded.isEmpty())
//            {
//                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
//                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS );
//                return false;
//            }
//            return true;
//        }
//        else
//        {
//          return true;
//        }
//    }
//
//    // TODO: Handle permission results
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
//    {
//        switch (requestCode)
//        {
//            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
//            {
//                if(grantResults.length > 0)
//                {
//                    Integer grantedPermissions = 0;
//                    for(int i = 0; i < grantResults.length; i++)
//                    {
//                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
//                        {
//                            ++grantedPermissions;
//                        }
//                    }
//
//                    if(grantedPermissions == grantResults.length)
//                    {
//                        startMainScreen();
//                    }
//                    else
//                    {
//                        checkMissedPermission();
//                    }
//                }
//                return;
//            }
//        }
//    }

    // TODO: Promt user for check Missed permission
//    private void checkMissedPermission()
//    {
//        final AlertDialog dialog = showAlertDlg(LaunchScreen.this, Constants.MISSED_PERMISSION, Constants.ASK_GRANT_MISSED_PERMISSIONS);
//        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, Constants.NO, new DialogInterface.OnClickListener()
//        {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i)
//            {
//                try
//                {
//                    Thread.sleep(500);
//                    finish();
//                } catch (InterruptedException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        });
//        dialog.setButton(DialogInterface.BUTTON_POSITIVE, Constants.YES, new DialogInterface.OnClickListener()
//        {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i)
//            {
//                checkPermisson(CommonUtils.getPermissions());
//            }
//        });
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.show();
//    }

    private void startLoginScreen()
    {
        startActivity(new Intent(LaunchScreen.this, LoginActivity.class));
        finish();
    }

    private void startMainScreen()
    {
        startActivity(new Intent(LaunchScreen.this, LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed()
    {
        //TODO: unable to close when splashing
    }
}
