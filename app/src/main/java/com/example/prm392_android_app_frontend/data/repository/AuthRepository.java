package com.example.prm392_android_app_frontend.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.data.dto.LoginRequest;
import com.example.prm392_android_app_frontend.data.dto.LoginResponse;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.AuthApi;
import com.example.prm392_android_app_frontend.core.util.Resource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApi api;

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
                } else {
                    String msg = "Login failed (" + resp.code() + ")";
                    live.postValue(Resource.error(msg, null));
                }
            }
            @Override public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                live.postValue(Resource.error(t.getMessage(), null));
            }
        });

        return live;
    }
}
