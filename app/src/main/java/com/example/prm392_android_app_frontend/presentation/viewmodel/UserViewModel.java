package com.example.prm392_android_app_frontend.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.data.dto.UpdateUserRequest.UpdateUserRequest;
import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.data.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repo;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    private final MutableLiveData<UserDto>  _user    = new MutableLiveData<>();
    private final MutableLiveData<String>   _error   = new MutableLiveData<>();
    private final MutableLiveData<Boolean>  _updated = new MutableLiveData<>(false);

    public LiveData<Boolean> loading = _loading;
    public LiveData<UserDto> user    = _user;
    public LiveData<String>  error   = _error;
    public LiveData<Boolean> updated = _updated;

    public UserViewModel(@NonNull Application app) {
        super(app);
        repo = new UserRepository(app.getApplicationContext());
    }

    /** Load thông tin user theo id */
    public void loadUser(int userId) {
        _loading.setValue(true);
        repo.getUserById(userId, new UserRepository.CallbackResult<UserDto>() {
            @Override
            public void onSuccess(UserDto data) {
                _loading.postValue(false);
                _user.postValue(data);
            }

            @Override
            public void onError(String message, int code) {
                _loading.postValue(false);
                _error.postValue(formatError(message, code));
            }
        });
    }

    public void updateUser(int userId, UpdateUserRequest req) {
        _loading.setValue(true);
        repo.updateUser(userId, req, new UserRepository.CallbackResult<UserDto>() {
            @Override
            public void onSuccess(UserDto data) {
                _loading.postValue(false);
                _updated.postValue(true);
                _user.postValue(data);
            }

            @Override
            public void onError(String message, int code) {
                _loading.postValue(false);
                _error.postValue(formatError(message, code));
            }
        });
    }

    private String formatError(String msg, int code) {
        if (msg == null || msg.isEmpty()) msg = "Có lỗi xảy ra";
        return code > 0 ? msg + " (code " + code + ")" : msg;
    }
}
