package com.delivce.m_pesaintegration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class PdfActivity extends AppCompatActivity {
    Button btnGeneratePdf;
    TextView tvInvokedTime, tvPhoneNumber, tvAmount;
    String phoneNumber, message, amount;

    int pageHeight = 1120;
    int pagewidth = 792;

    // creating a bitmap variable
    // for storing our images
    Bitmap bmp, scaledbmp;
    // constant code for runtime permissions
    private static final int PERMISSION_REQUEST_CODE = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        btnGeneratePdf = findViewById(R.id.btnGeneratePDF);
        tvInvokedTime = findViewById(R.id.tvInvokedTime);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvAmount = findViewById(R.id.tvAmount);

        Intent intent = getIntent();
        message = intent.getStringExtra("MESSAGE");
        phoneNumber = intent.getStringExtra("PHONE_NUMBER");
        amount = intent.getStringExtra("AMOUNT");
    }
}