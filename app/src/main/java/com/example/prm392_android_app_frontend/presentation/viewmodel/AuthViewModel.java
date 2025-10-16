package com.example.prm392_android_app_frontend.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392_android_app_frontend.data.repository.AuthRepository;
import com.example.prm392_android_app_frontend.data.dto.login.LoginResponse;
import com.example.prm392_android_app_frontend.core.util.Resource;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repo = new AuthRepository();
    private final MediatorLiveData<Resource<LoginResponse>> loginState = new MediatorLiveData<>();

    public LiveData<Resource<LoginResponse>> getLoginState() {
        return loginState;
    }

    public void login(String usernameOrEmail, String password) {
        // thêm nguồn tạm thời và remove khi không còn LOADING để tránh leak
        LiveData<Resource<LoginResponse>> src = repo.login(usernameOrEmail, password);
        loginState.addSource(src, res -> {
            loginState.setValue(res);
            if (res.getStatus() != Resource.Status.LOADING) {
                loginState.removeSource(src);
            }
        });
    }
}
