package com.example.prm392_android_app_frontend.data.remote.api;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    public static String BASE_URL = "https://plowable-nonlevel-sharie.ngrok-free.dev/api/";

    private static Retrofit retrofit;

    public static Retrofit get() {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient ok = new OkHttpClient.Builder()
                .addInterceptor(log)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(ok)
                .build();
    }
    // Nếu cần client có Bearer token cho các API khác
    public static Retrofit getAuthClient(Context ctx) {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor auth = chain -> {
            String token = TokenStore.getToken(ctx);
            Request req = chain.request();
            if (token != null && !token.isEmpty()) {
                req = req.newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .build();
            }
            return chain.proceed(req);
        };

        OkHttpClient ok = new OkHttpClient.Builder()
                .addInterceptor(auth)
                .addInterceptor(log)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(ok)
                .build();
    }
}
