package com.hsu.aidlab.meetrip.Activity;
import static com.hsu.aidlab.meetrip.Util.CommonUtils.showAlertDlg;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;

import com.hsu.aidlab.meetrip.R;
import com.hsu.aidlab.meetrip.Util.CommonUtils;
import com.hsu.aidlab.meetrip.Util.Constants;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {

    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private Context context;
    EditText username;
    private Button btnNumber;
    private final static int REQUEST_CODE = 999;

    private BandClient mBandClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(com.hsu.aidlab.meetrip.R.layout.activity_login);

        VideoView video_background = findViewById(R.id.video_background);
        video_background.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.korea_bg_video_mid));
        video_background.start();
        video_background.setOnPreparedListener(onPreparedListener);

        this.context = LoginActivity.this;

        checkSession();

        username = (EditText) findViewById(R.id.phoneno);
        btnNumber = (Button) findViewById(R.id.btnPhone);

        final WeakReference<Activity> reference = new WeakReference<Activity>(this);
        new HeartRateConsentTask().execute(reference);

        btnNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (!username.getText().toString().isEmpty() && username.getText().length()>7)
                {
                    String userID = String.valueOf(username.getText());
                    saveLoginInfo(userID);
//                    startActivity(new Intent(LoginActivity.this, MSBand.class));
//                    startActivity(new Intent(LoginActivity.this, MSBandActivity.class));
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();

                    Toast.makeText(context, "User ID: " + userID, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "User ID를 입력해 주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
              checkPermisson(CommonUtils.getPermissions());
            }
        }, 500);
    }

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.setLooping(true);
        }
    };

    private void startLoginPage(LoginType loginType)
    {
        if(CommonUtils.checkInternetConnection(LoginActivity.this))
        {
            btnNumber.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(LoginActivity.this, AccountKitActivity.class);
            AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder
                    (loginType, AccountKitActivity.ResponseType.TOKEN);
            intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
            startActivityForResult(intent, REQUEST_CODE);
        }
        else 
        {
            Toast.makeText(context, "Please check your internet connection and try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE)
        {
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if(result != null)
            {
                if(result.getAccessToken() != null)
                {
                    /**
                     * if phone number is verified and access token returned from Account kit
                     * get user information from Account Kit using token in getAccount() method
                     */
                    getAccount();
                }
                else if(result.getAuthorizationCode() != null)
                {
                    String userID = result.getAuthorizationCode().substring(0, 10);
                    saveLoginInfo(userID);
                }

                return;
            }
            else if(result.wasCancelled())
            {
                Toast.makeText(context, "Request canceled", Toast.LENGTH_SHORT).show();
                return;
            }
            else 
            {
                Toast.makeText(context, result.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Save user phone number and server ip address and port number into shared preferences after phone number is verified by Account Kit service
     * @param userNumber
     */
    private void saveLoginInfo(String userNumber)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.USER_ID, userNumber);
        editor.apply();
        checkSession();
    }

    /**
     * Check user registered in this application
     */
    private void checkSession()
    {
        /**
         * Create shared preferences object
         */
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE);
        /**
         * Get user phone number from shared preferences
         */
        String userID = sharedPreferences.getString(Constants.USER_ID, null);

        /**
         * open Main activity if user registered
         */
        if( userID != null)
        {
//            startActivity(new Intent(LoginActivity.this, MSBand.class));
//            startActivity(new Intent(LoginActivity.this, MSBandActivity.class));
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    /**
     * Gets current account from Facebook Account Kit which include user's phone number.
     */
    private void getAccount()
    {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>()
        {
            @Override
            public void onSuccess(final Account account)
            {
                // Get Account Kit ID
                String accountKitId = account.getId();

                // Get phone number
                PhoneNumber phoneNumber = account.getPhoneNumber();
                String phoneNumberString = phoneNumber.toString();

                saveLoginInfo(phoneNumberString);
            }

            @Override
            public void onError(final AccountKitError error)
            {
                Log.e("AccountKit",error.toString());
                Toast.makeText(context, "Something went error", Toast.LENGTH_SHORT).show();
                System.exit(1);
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
                    Log.wtf("MainActivity", "Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
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
                Log.wtf("MainActivity", exceptionMessage);

            } catch (Exception e) {
                Log.wtf("MainActivity", e.getMessage());
            }
            return null;
        }
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (mBandClient == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                Log.wtf("MainActivity", "Band isn't paired with your phone.\n");
                return false;
            }
            mBandClient = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == mBandClient.getConnectionState()) {
            return true;
        }

        Log.wtf("MainActivity", "Band is connecting...\n");
        return ConnectionState.CONNECTED == mBandClient.connect().await();
    }

    // TODO: Check permissions
    private boolean checkPermisson(String[] permissions)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            int result;
            List<String> listPermissionsNeeded = new ArrayList<>();

            for (String permisson : permissions)
            {
                result = ContextCompat.checkSelfPermission(LoginActivity.this, permisson);
                if (result != PackageManager.PERMISSION_GRANTED)
                {
                    listPermissionsNeeded.add(permisson);
                }
            }
            if (!listPermissionsNeeded.isEmpty())
            {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS );
                return false;
            }
            return true;
        }
        else
        {
          return true;
        }
    }

    // TODO: Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                if(grantResults.length > 0)
                {
                    Integer grantedPermissions = 0;
                    for(int i = 0; i < grantResults.length; i++)
                    {
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        {
                            ++grantedPermissions;
                        }
                    }

                    if(grantedPermissions != grantResults.length)
                    {
                        checkMissedPermission();
                    }
                }
                return;
            }
        }
    }

    //TODO: Promt user for check Missed permission
    private void checkMissedPermission()
    {
        final AlertDialog dialog = showAlertDlg(LoginActivity.this, Constants.MISSED_PERMISSION, Constants.ASK_GRANT_MISSED_PERMISSIONS);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, Constants.NO, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                try
                {
                    Thread.sleep(500);
                    finish();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, Constants.YES, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                checkPermisson(CommonUtils.getPermissions());
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onResume() {super.onResume();}

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}

