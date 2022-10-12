package com.hsuniv.aidlab.meetrip.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;

import com.hsuniv.aidlab.meetrip.R;
import com.hsuniv.aidlab.meetrip.Util.CommonUtils;
import com.hsuniv.aidlab.meetrip.Util.Constants;

public class LoginActivity extends Activity {

    private Context context;
    EditText username;
    private Button btnNumber;
    private final static int REQUEST_CODE = 999;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.context = LoginActivity.this;

        checkSession();

        username = (EditText) findViewById(R.id.phoneno);
        btnNumber = (Button) findViewById(R.id.btnPhone);

        btnNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (!username.getText().toString().isEmpty() && username.getText().length()>7)
                {
                    String userID = String.valueOf(username.getText());
                    saveLoginInfo(userID);
//                    startActivity(new Intent(LoginActivity.this, MSBand.class));
                    startActivity(new Intent(LoginActivity.this, MSBandActivity.class));
                    finish();

                    Toast.makeText(context, "전화번호: " + userID, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "전화번호를 입력해 주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

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
            startActivity(new Intent(LoginActivity.this, MSBandActivity.class));
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

    @Override
    protected void onResume() {super.onResume();}

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
