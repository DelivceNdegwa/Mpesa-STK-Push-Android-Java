package com.delivce.m_pesaintegration.services;

import static com.delivce.m_pesaintegration.Constants.BASE_URL;

import com.delivce.m_pesaintegration.models.AccessToken;
import com.delivce.m_pesaintegration.models.STKPush;

import java.util.StringJoiner;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface STKPushService {
    @GET("oauth/v1/generate?grant_type=client_credentials")
    Call<AccessToken> getAccessToken();

    @POST("mpesa/stkpush/v1/processrequest")
    Call<STKPush> sendPush(@Body STKPush body);
}
