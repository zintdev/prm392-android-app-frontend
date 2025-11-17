package com.example.prm392_android_app_frontend.data.remote;

import android.content.Context;

import com.example.prm392_android_app_frontend.storage.TokenStore;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context appContext;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = TokenStore.getToken(appContext);
        if (token != null && !token.isEmpty()) {
            Request authed = original.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(authed);
        }
        return chain.proceed(original);
    }
}
