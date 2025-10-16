package com.example.prm392_android_app_frontend.data.remote;

import com.example.prm392_android_app_frontend.data.dto.ApiError;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ErrorUtils {

    public static ApiError parseError(Retrofit retrofit, Response<?> response) {
        Converter<ResponseBody, ApiError> converter =
                retrofit.responseBodyConverter(ApiError.class, new java.lang.annotation.Annotation[0]);

        ApiError error;
        try {
            if (response.errorBody() != null) {
                error = converter.convert(response.errorBody());
                if (error != null) return error;
            }
        } catch (IOException e) {
            // Ignore and fallback
        }
        // Fallback: cố đọc message text
        ApiError fallback = new ApiError();
        try {
            String raw = response.errorBody() != null ? response.errorBody().string() : "";
            // cố parse "message" nếu server trả text/json lạ
            ApiError tmp = new Gson().fromJson(raw, ApiError.class);
            if (tmp != null) return tmp;
        } catch (Exception ignored) {}
        return fallback;
    }
}
