package com.delivce.m_pesaintegration;

import static com.delivce.m_pesaintegration.Constants.BUSINESS_SHORT_CODE;
import static com.delivce.m_pesaintegration.Constants.CALLBACKURL;
import static com.delivce.m_pesaintegration.Constants.PARTYB;
import static com.delivce.m_pesaintegration.Constants.PASSKEY;
import static com.delivce.m_pesaintegration.Constants.TRANSACTION_TYPE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.delivce.m_pesaintegration.models.AccessToken;
import com.delivce.m_pesaintegration.models.STKPush;
import com.delivce.m_pesaintegration.services.DarajaApiClient;
import com.google.android.material.textfield.TextInputEditText;

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
            }
        });
    }

    private void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                if(response.isSuccessful()){
                    mApiClient.setAuthToken(response.body().getAccessToken());
                    isReady = true;
                }
                else{
                    Log.d("E_STK_RES", String.valueOf(response.errorBody()));
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
                "0701766206", // PARTY_B,
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

}