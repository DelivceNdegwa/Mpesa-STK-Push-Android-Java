package com.delivce.m_pesaintegration;

import static com.delivce.m_pesaintegration.Constants.BUSINESS_SHORT_CODE;
import static com.delivce.m_pesaintegration.Constants.CALLBACKURL;
import static com.delivce.m_pesaintegration.Constants.PARTYB;
import static com.delivce.m_pesaintegration.Constants.PASSKEY;
import static com.delivce.m_pesaintegration.Constants.SMS_PERMISSION_REQUEST_CODE;
import static com.delivce.m_pesaintegration.Constants.TRANSACTION_TYPE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.delivce.m_pesaintegration.models.AccessToken;
import com.delivce.m_pesaintegration.models.STKPush;
import com.delivce.m_pesaintegration.services.DarajaApiClient;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    TextInputEditText etPhoneNumber, etAmount;
    Button btnSendAmount;
    private DarajaApiClient mApiClient;
    private ProgressDialog mProgressDialog;

    public boolean isReady = false;

    String phoneNumber;
    String invokeMessage = "STKPush invoked";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApiClient = new DarajaApiClient();
        mApiClient.setIsDebug(true);
        mProgressDialog = new ProgressDialog(this);

        etPhoneNumber = findViewById(R.id.et_phone);
        etAmount = findViewById(R.id.et_amount);
        btnSendAmount = findViewById(R.id.btn_send_amount);


        getAccessToken();


        btnSendAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptConfirmation();
            }
        });
    }


    private void requestSmsPermission(String message, String phoneNumber) {

        // check permission is given
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        } else {
            // permission already granted run sms send
            sendSMS(message, phoneNumber);
        }
    }

    private void promptConfirmation() {
        phoneNumber = etPhoneNumber.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Send amount "+etAmount.getText().toString()+" from number "+etPhoneNumber.getText().toString()+"?");
        builder.setTitle("Confirmation");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            if(!isReady){
                getAccessToken();
                performSTKPush(
                        etPhoneNumber.getText().toString(),
                        etAmount.getText().toString()
                );
            }
            else{
                performSTKPush(
                        etPhoneNumber.getText().toString(),
                        etAmount.getText().toString()
                );
            }

        });

        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            Toast.makeText(this, "You have cancelled this order", Toast.LENGTH_SHORT).show();
            dialog.cancel();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                try {
                    if (response.isSuccessful()) {
                        mApiClient.setAuthToken(response.body().getAccessToken());
                        isReady = true;
                    } else {
                        Timber.e("Response %s", response.errorBody().string());
                        Toast.makeText(MainActivity.this, BuildConfig.CONSUMER_KEY, Toast.LENGTH_SHORT).show();
                        Log.d("CONSUMER_KEY", BuildConfig.CONSUMER_KEY);
                        Log.d("CONSUMER_SECRET", BuildConfig.CONSUMER_SECRET);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                Timber.tag("E_STK_FAILURE").d(t.getMessage());
            }
        });
    }


    public void performSTKPush(String phone_number,String amount) {
        mProgressDialog.setMessage("Processing your request");
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        String timestamp = Utils.getTimestamp();
        STKPush stkPush = new STKPush(
                BUSINESS_SHORT_CODE,
                Utils.getPassword(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
                timestamp,
                TRANSACTION_TYPE,
                String.valueOf(amount),
                Utils.sanitizePhoneNumber(phone_number),
                PARTYB,
                Utils.sanitizePhoneNumber(phone_number),
                CALLBACKURL,
                "Test Supplier", //Account reference
                "Mpesa STK Push test"  //Transaction description
        );

        mApiClient.setGetAccessToken(false);

        //Sending the data to the Mpesa API, remember to remove the logging when in production.
        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(@NonNull Call<STKPush> call, @NonNull Response<STKPush> response) {
                mProgressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        Timber.d("post submitted to API. %s", response.body());
                        requestSmsPermission("STK Push invoked", phone_number);
                    } else {
                        Timber.e("Response %s", response.errorBody().string());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
                mProgressDialog.dismiss();
                Timber.e(t);
            }
        });
    }

    private void sendSMS(String message, String phoneNumber) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(getApplicationContext(), "Message Sent",
                        Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                Log.d("SEND_SMS_ERROR", ex.getMessage().toString());
                ex.printStackTrace();
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case(SMS_PERMISSION_REQUEST_CODE): {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    sendSMS(invokeMessage, phoneNumber);
                } else {
                    // permission denied
                    Toast.makeText(this, "Sorry, you have to grant this app sms permissions", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}