package com.example.prm392_android_app_frontend.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392_android_app_frontend.data.dto.address.AddressDto;
import com.example.prm392_android_app_frontend.data.dto.address.DistrictDto;
import com.example.prm392_android_app_frontend.data.dto.address.ProvinceDto;
import com.example.prm392_android_app_frontend.data.dto.address.WardDto;
import com.example.prm392_android_app_frontend.data.remote.api.AddressApi;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.ProvincesApi;
import com.example.prm392_android_app_frontend.data.remote.api.ProvincesServices;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressViewModel extends ViewModel {

    private final MutableLiveData<List<AddressDto>> addressesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ProvinceDto>> provincesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<DistrictDto>> districtsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<WardDto>> wardsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private final AddressApi addressApi;
    private final ProvincesApi provincesApi;

    public AddressViewModel() {
        addressApi = ApiClient.get().create(AddressApi.class);
        provincesApi = ProvincesServices.api();
    }

    public LiveData<List<AddressDto>> getAddressesLiveData() {
        return addressesLiveData;
    }

    public LiveData<List<ProvinceDto>> getProvincesLiveData() {
        return provincesLiveData;
    }

    public LiveData<List<DistrictDto>> getDistrictsLiveData() {
        return districtsLiveData;
    }

    public LiveData<List<WardDto>> getWardsLiveData() {
        return wardsLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void getAddressesByUserId(int userId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        Call<List<AddressDto>> call = addressApi.getAddressesByUserId(userId);
        call.enqueue(new Callback<List<AddressDto>>() {
            @Override
            public void onResponse(Call<List<AddressDto>> call, Response<List<AddressDto>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    addressesLiveData.setValue(response.body());
                } else {
                    errorMessage.setValue("Không thể tải danh sách địa chỉ");
                }
            }

            @Override
            public void onFailure(Call<List<AddressDto>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // Load provinces
    public void loadProvinces() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        Call<List<ProvinceDto>> call = provincesApi.getProvinces();
        call.enqueue(new Callback<List<ProvinceDto>>() {
            @Override
            public void onResponse(Call<List<ProvinceDto>> call, Response<List<ProvinceDto>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("AddressViewModel", "Loaded " + response.body().size() + " provinces");
                    provincesLiveData.setValue(response.body());
                } else {
                    android.util.Log.e("AddressViewModel", "Failed to load provinces: " + response.code());
                    errorMessage.setValue("Không thể tải danh sách tỉnh/thành phố");
                }
            }

            @Override
            public void onFailure(Call<List<ProvinceDto>> call, Throwable t) {
                isLoading.setValue(false);
                android.util.Log.e("AddressViewModel", "Error loading provinces", t);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // Load districts by province code
    public void loadDistricts(int provinceCode) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        android.util.Log.d("AddressViewModel", "Loading districts for province: " + provinceCode);
        Call<ProvinceDto> call = provincesApi.getProvinceDetail(provinceCode);
        call.enqueue(new Callback<ProvinceDto>() {
            @Override
            public void onResponse(Call<ProvinceDto> call, Response<ProvinceDto> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getDistricts() != null) {
                    android.util.Log.d("AddressViewModel", "Loaded " + response.body().getDistricts().size() + " districts");
                    districtsLiveData.setValue(response.body().getDistricts());
                } else {
                    android.util.Log.e("AddressViewModel", "Failed to load districts: " + response.code());
                    errorMessage.setValue("Không thể tải danh sách quận/huyện");
                }
            }

            @Override
            public void onFailure(Call<ProvinceDto> call, Throwable t) {
                isLoading.setValue(false);
                android.util.Log.e("AddressViewModel", "Error loading districts", t);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // Load wards by district code
    public void loadWards(int districtCode) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        android.util.Log.d("AddressViewModel", "Loading wards for district: " + districtCode);
        Call<DistrictDto> call = provincesApi.getDistrictDetail(districtCode);
        call.enqueue(new Callback<DistrictDto>() {
            @Override
            public void onResponse(Call<DistrictDto> call, Response<DistrictDto> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getWards() != null) {
                    android.util.Log.d("AddressViewModel", "Loaded " + response.body().getWards().size() + " wards");
                    wardsLiveData.setValue(response.body().getWards());
                } else {
                    android.util.Log.e("AddressViewModel", "Failed to load wards: " + response.code());
                    errorMessage.setValue("Không thể tải danh sách phường/xã");
                }
            }

            @Override
            public void onFailure(Call<DistrictDto> call, Throwable t) {
                isLoading.setValue(false);
                android.util.Log.e("AddressViewModel", "Error loading wards", t);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}