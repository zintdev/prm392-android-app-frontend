package com.example.prm392_android_app_frontend.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392_android_app_frontend.data.dto.CreateOrderRequestDto;
import com.example.prm392_android_app_frontend.data.dto.OrderDTO;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.OrderApi;
import com.example.prm392_android_app_frontend.data.repository.OrderRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderViewModel extends ViewModel {

    private final MutableLiveData<OrderDTO> orderLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<OrderDTO>> ordersListLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private final OrderRepository orderRepository;

    public OrderViewModel() {
        OrderApi orderApi = ApiClient.get().create(OrderApi.class);
        orderRepository = new OrderRepository(orderApi);
    }

    public LiveData<OrderDTO> getOrderLiveData() {
        return orderLiveData;
    }

    public LiveData<List<OrderDTO>> getOrdersListLiveData() {
        return ordersListLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void placeOrder(CreateOrderRequestDto request) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        orderRepository.placeOrder(request, new Callback<OrderDTO>() {
            @Override
            public void onResponse(Call<OrderDTO> call, Response<OrderDTO> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    orderLiveData.setValue(response.body());
                }
//                else {
//                    errorMessage.setValue("Không thể tạo đơn hàng. Vui lòng thử lại.");
//                }
            }

            @Override
            public void onFailure(Call<OrderDTO> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void getOrdersByUserId(int userId, String status) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        orderRepository.getOrdersByUserId(userId, status, new Callback<List<OrderDTO>>() {
            @Override
            public void onResponse(Call<List<OrderDTO>> call, Response<List<OrderDTO>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ordersListLiveData.setValue(response.body());
                }
//                else {
//                    errorMessage.setValue("Không thể tải danh sách đơn hàng. Vui lòng thử lại.");
//                }
            }

            @Override
            public void onFailure(Call<List<OrderDTO>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}