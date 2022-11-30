package com.delivce.m_pesaintegration.interceptors;

import java.io.IOException;
import android.util.Base64;

import com.delivce.m_pesaintegration.BuildConfig;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AccessTokenInterceptor implements Interceptor {
    public AccessTokenInterceptor() {

    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String keys = new StringBuilder().append(BuildConfig.CONSUMER_KEY).append(":").append(BuildConfig.CONSUMER_KEY).toString();

        Request request = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Basic "+ Base64.encodeToString(keys.getBytes(), Base64.NO_WRAP)).
                build();

        return chain.proceed(request);
    }
}
