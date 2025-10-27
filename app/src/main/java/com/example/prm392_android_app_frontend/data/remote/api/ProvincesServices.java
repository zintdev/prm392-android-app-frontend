package com.example.prm392_android_app_frontend.data.remote.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public final class ProvincesServices {
    private static ProvincesApi api;

    public static ProvincesApi api() {
        if (api == null) {
            OkHttpClient ok = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();

            Retrofit r = new Retrofit.Builder()
                    .baseUrl("https://provinces.open-api.vn/")
                    .client(ok)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            api = r.create(ProvincesApi.class);
        }
        return api;
    }
}
