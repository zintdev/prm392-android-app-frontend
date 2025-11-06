package com.example.prm392_android_app_frontend.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.data.repository.UserRepository;
import com.example.prm392_android_app_frontend.storage.TokenStore;

/**
 * Shared ViewModel used across staff screens to expose the logged-in staff information
 * such as assigned store and personal profile details.
 */
public class StaffSharedViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<UserDto> staffInfo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public StaffSharedViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application.getApplicationContext());
    }

    public LiveData<UserDto> getStaffInfo() {
        return staffInfo;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadCurrentStaff() {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        if (staffInfo.getValue() != null) {
            return;
        }
        int userId = TokenStore.getUserId(getApplication());
        if (userId <= 0) {
            error.setValue("Không tìm thấy thông tin nhân viên");
            return;
        }
        loading.setValue(true);
        userRepository.getUserById(userId, new UserRepository.CallbackResult<UserDto>() {
            @Override
            public void onSuccess(UserDto data) {
                loading.postValue(false);
                staffInfo.postValue(data);
            }

            @Override
            public void onError(String message, int code) {
                loading.postValue(false);
                String msg = message != null && !message.isEmpty() ? message : "Không thể tải thông tin nhân viên";
                if (code > 0) {
                    msg = msg + " (" + code + ")";
                }
                error.postValue(msg);
            }
        });
    }
}
