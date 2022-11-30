package com.delivce.m_pesaintegration.interceptors;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    String mAuthToken;

    public AuthInterceptor(String mAuthToken) {
        this.mAuthToken = mAuthToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer "+mAuthToken)
                .build();
        return chain.proceed(request);
    }
}
