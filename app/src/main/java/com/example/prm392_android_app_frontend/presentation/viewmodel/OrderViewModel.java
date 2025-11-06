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
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.core.util.Resource;
import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;
import com.example.prm392_android_app_frontend.data.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

public class OrderViewModel extends AndroidViewModel {

    private final OrderRepository orderRepository;

    // For admin/staff view
    private List<OrderDto> allOrders = new ArrayList<>();
    private final MediatorLiveData<Resource<List<OrderDto>>> orders = new MediatorLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> filterStatus = new MutableLiveData<>("ALL");

    // For user-specific orders
    private final MediatorLiveData<Resource<List<OrderDto>>> userOrders = new MediatorLiveData<>();

    private final MediatorLiveData<Resource<OrderDto>> orderDetail = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<OrderDto>> updateOrderStatusResult = new MediatorLiveData<>();

    public OrderViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application.getApplicationContext());

        // Admin/staff listeners
        orders.addSource(searchQuery, query -> applyFilters());
        orders.addSource(filterStatus, status -> fetchOrdersByStatus(status));
    }

    // --- Getters for UI --- //
    public LiveData<Resource<List<OrderDto>>> getOrders() {
        return orders;
    }

    public LiveData<Resource<List<OrderDto>>> getUserOrders() {
        return userOrders;
    }

    public LiveData<Resource<OrderDto>> getOrderDetail() {
        return orderDetail;
    }

    public LiveData<Resource<OrderDto>> getUpdateOrderStatusResult() {
        return updateOrderStatusResult;
    }

    // --- Admin/Staff Actions --- //
    public void setSearchQuery(String query) {
        this.searchQuery.setValue(query);
    }

    public void setFilterStatus(String status) {
        this.filterStatus.setValue(status);
    }

    public void refreshOrders() {
        fetchOrdersByStatus(filterStatus.getValue());
    }

    private void fetchOrdersByStatus(String status) {
        String apiStatus = "ALL".equals(status) ? null : status;
        orders.setValue(Resource.loading(null));
        final LiveData<Resource<List<OrderDto>>> source = orderRepository.getAllOrders(apiStatus);
        orders.addSource(source, resource -> {
            if (resource.getStatus() != Resource.Status.LOADING) {
                orders.removeSource(source);
            }
            if (resource.isSuccess() && resource.getData() != null) {
                allOrders = resource.getData();
                applyFilters();
            } else {
                orders.setValue(resource);
            }
        });
    }

    private void applyFilters() {
        String query = searchQuery.getValue();
        if (query == null) query = "";

        List<OrderDto> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        for (OrderDto order : allOrders) {
            String orderId = String.valueOf(order.getId());
            String customerName = order.getShippingFullName() != null ? order.getShippingFullName().toLowerCase() : "";
            if (orderId.contains(query) || customerName.contains(lowerCaseQuery)) {
                filteredList.add(order);
            }
        }
        orders.setValue(Resource.success(filteredList));
    }

    // --- User-specific Actions --- //
    public void fetchOrdersByUserId(int userId) {
        userOrders.setValue(Resource.loading(null));
        final LiveData<Resource<List<OrderDto>>> source = orderRepository.getOrdersByUserId(userId);
        userOrders.addSource(source, resource -> {
            userOrders.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                userOrders.removeSource(source);
            }
        });
    }

    // --- Common Actions --- //
    public void fetchOrderById(int orderId) {
        orderDetail.setValue(Resource.loading(null));
        final LiveData<Resource<OrderDto>> source = orderRepository.getOrderById(orderId);
        orderDetail.addSource(source, resource -> {
            orderDetail.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                orderDetail.removeSource(source);
            }
        });
    }

    public void updateOrderStatus(int orderId, String newStatus) {
        updateOrderStatusResult.setValue(Resource.loading(null));
        final LiveData<Resource<OrderDto>> source = orderRepository.updateOrderStatus(orderId, newStatus);
        updateOrderStatusResult.addSource(source, resource -> {
            updateOrderStatusResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                updateOrderStatusResult.removeSource(source);
                if (resource.isSuccess()) {
                    refreshOrders();
                }
            }
        });
    }
}
