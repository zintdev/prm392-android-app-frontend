package com.example.prm392_android_app_frontend.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.core.util.Resource;
import com.example.prm392_android_app_frontend.data.dto.login.LoginRequest;
import com.example.prm392_android_app_frontend.data.dto.login.LoginResponse;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.AuthApi;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApi api;
    private final Gson gson = new Gson();

    public AuthRepository() {
        this.api = ApiClient.get().create(AuthApi.class);
    }

    public LiveData<Resource<LoginResponse>> login(String usernameOrEmail, String password) {
        MutableLiveData<Resource<LoginResponse>> live = new MutableLiveData<>();
        live.postValue(Resource.loading());

        LoginRequest req = new LoginRequest(usernameOrEmail, password);

        api.login(req).enqueue(new Callback<LoginResponse>() {
            @Override public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    live.postValue(Resource.success(resp.body()));
                    return;
                }

                int code = resp.code();
                String serverMsg = parseServerMessage(resp);
                String mappedMsg = mapHttpCodeToMessage(code, serverMsg);
                String finalMsg = (mappedMsg == null || mappedMsg.isEmpty())
                        ? "Đăng nhập thất bại"
                        : mappedMsg;

                live.postValue(Resource.error(finalMsg, null));
            }

            @Override public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                // --- Lỗi mạng / ngoại lệ client
                String finalMsg = mapNetworkThrowable(t);
                live.postValue(Resource.error(finalMsg, null));
            }
        });

        return live;
    }
    private String parseServerMessage(Response<?> resp) {
        try {
            if (resp.errorBody() == null) return null;
            String raw = resp.errorBody().string();
            if (raw == null || raw.isEmpty()) return null;

            JsonObject obj = gson.fromJson(raw, JsonObject.class);
            if (obj == null) return null;

            if (obj.has("message") && !obj.get("message").isJsonNull()) {
                return obj.get("message").getAsString();
            }
            if (obj.has("error") && !obj.get("error").isJsonNull()) {
                return obj.get("error").getAsString();
            }
            if (obj.has("detail") && !obj.get("detail").isJsonNull()) {
                return obj.get("detail").getAsString();
            }
            return raw;
        } catch (Exception ignore) {
            return null;
        }
    }

    private String mapHttpCodeToMessage(int code, String serverMsg) {
        switch (code) {
            case 400:
                return "Sai tài khoản hoặc mật khẩu";
//            case 401:
//                return "Sai tài khoản hoặc mật khẩu";
//            case 403:
//                return "Tài khoản không có quyền truy cập";
            case 404:
                return "Mất kết nối với máy chủ , vui lòng thử lại sau";
//            case 409:
//                return "Xung đột dữ liệu";
//            case 422:
//                return "Dữ liệu không hợp lệ";
//            case 429:
//                return "Bạn thao tác quá nhanh, vui lòng thử lại sau";
//            case 500:
//                return "Máy chủ gặp sự cố, vui lòng thử lại sau";
//            case 502:
//            case 503:
//            case 504:
//                return "Dịch vụ tạm thời gián đoạn, vui lòng thử lại";
            default:
                return "Đăng nhập thất bại";
        }
    }

    private String mapNetworkThrowable(Throwable t) {
        if (t instanceof UnknownHostException) {
            return "Không có kết nối mạng hoặc không phân giải được máy chủ";
        }
        if (t instanceof ConnectException) {
            return "Không thể kết nối tới máy chủ";
        }
        if (t instanceof SocketTimeoutException) {
            return "Quá thời gian chờ phản hồi từ máy chủ";
        }
        if (t instanceof SSLException) {
            return "Lỗi bảo mật kết nối (SSL). Vui lòng thử lại";
        }
        String msg = t.getMessage();
        return (msg == null || msg.isEmpty()) ? "Đã xảy ra lỗi không xác định" : msg;
    }
}
