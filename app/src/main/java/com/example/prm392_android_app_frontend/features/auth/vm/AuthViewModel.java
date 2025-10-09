package com.example.prm392_android_app_frontend.features.auth.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392_android_app_frontend.features.auth.data.AuthRepository;
import com.example.prm392_android_app_frontend.features.auth.data.dto.LoginResponse;
import com.example.prm392_android_app_frontend.util.Resource;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repo = new AuthRepository();
    private final MutableLiveData<Resource<LoginResponse>> loginState = new MutableLiveData<>();

    public LiveData<Resource<LoginResponse>> getLoginState() {
        return loginState;
    }

    public void login(String usernameOrEmail, String password) {
        repo.login(usernameOrEmail, password).observeForever(resource -> {
            loginState.setValue(resource);
        });
    }
}
