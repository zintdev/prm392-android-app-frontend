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

    private static volatile Retrofit retrofit;
    private static volatile Retrofit retrofitAuth;
    private static volatile OkHttpClient baseClient;

    private ApiClient() {
    }

    private static OkHttpClient base() {
        if (baseClient == null) {
            synchronized (ApiClient.class) {
                if (baseClient == null) {
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(HttpLoggingInterceptor.Level.BODY);
                    baseClient = new OkHttpClient.Builder()
                            .addInterceptor(log)
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        return baseClient;
    }

    public static Retrofit get() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(base())
                            .build();
                }
            }
        }
        return retrofit;
    }

    // Nếu cần client có Bearer token cho các API khác
    public static Retrofit getAuthClient(Context ctx) {
        if (retrofitAuth == null) {
            synchronized (ApiClient.class) {
                if (retrofitAuth == null) {
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(HttpLoggingInterceptor.Level.BODY);

                    Interceptor auth = chain -> {
                        String token = com.example.prm392_android_app_frontend.storage.TokenStore.getToken(ctx);
                        Request req = chain.request();
                        if (token != null && !token.isEmpty()) {
                            req = req.newBuilder()
                                    .addHeader("Authorization", "Bearer " + token)
                                    .build();
                        }
                        return chain.proceed(req);
                    };

                    OkHttpClient ok = base().newBuilder()
                            .addInterceptor(auth)
                            .build();

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    retrofitAuth = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(ok)
                            .build();
                }
            }
        }
        return retrofitAuth;
    }
}
