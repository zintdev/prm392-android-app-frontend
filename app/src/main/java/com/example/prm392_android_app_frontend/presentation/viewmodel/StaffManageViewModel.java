package com.example.prm392_android_app_frontend.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationResponse;
import com.example.prm392_android_app_frontend.data.dto.user.CreateStaffRequest;
import com.example.prm392_android_app_frontend.data.repository.StaffRepository;

import java.util.List;

public class StaffManageViewModel extends AndroidViewModel {

    private final StaffRepository repository;
    private final MutableLiveData<List<UserDto>> staffList = new MutableLiveData<>();
    private final MutableLiveData<List<StoreLocationResponse>> stores = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> success = new MutableLiveData<>();

    public StaffManageViewModel(@NonNull Application application) {
        super(application);
        repository = new StaffRepository(application.getApplicationContext());
    }

    public LiveData<List<UserDto>> getStaffList() {
        return staffList;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<List<StoreLocationResponse>> getStores() {
        return stores;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSuccess() {
        return success;
    }

    public void loadStaff() {
        loading.setValue(true);
        repository.getStaffList(new StaffRepository.Result<List<UserDto>>() {
            @Override
            public void onSuccess(List<UserDto> data) {
                loading.postValue(false);
                staffList.postValue(data);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void loadStores() {
        loading.setValue(true);
        repository.listStores(new StaffRepository.Result<List<StoreLocationResponse>>() {
            @Override
            public void onSuccess(List<StoreLocationResponse> data) {
                loading.postValue(false);
                stores.postValue(data);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void createStaff(String username, String email, String password, String phone) {
        loading.setValue(true);
        CreateStaffRequest request = new CreateStaffRequest();
        request.username = username;
        request.email = email;
        request.password = password;
        request.phoneNumber = phone;
        repository.createStaff(request, new StaffRepository.ResultWithCode<UserDto>() {
            @Override
            public void onSuccess(UserDto data) {
                loading.postValue(false);
                success.postValue("Đã tạo nhân viên mới");
                loadStaff();
            }

            @Override
            public void onError(String message, int code) {
                loading.postValue(false);
                error.postValue(message != null ? message : "Không thể tạo nhân viên (" + code + ")");
            }
        });
    }

    public void assignStaffToStore(int staffId, int storeId, int actorId) {
        loading.setValue(true);
        repository.assignStaffToStore(storeId, staffId, actorId,
                new StaffRepository.ResultWithCode<UserDto>() {
                    @Override
                    public void onSuccess(UserDto data) {
                        loading.postValue(false);
                        String storeName = data.storeName != null ? data.storeName : "cửa hàng";
                        success.postValue("Đã gán nhân viên vào " + storeName);
                        loadStaff();
                    }

                    @Override
                    public void onError(String message, int code) {
                        loading.postValue(false);
                        error.postValue(message != null ? message : "Không thể gán nhân viên (" + code + ")");
                    }
                });
    }
}
