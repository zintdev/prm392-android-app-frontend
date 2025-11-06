package com.example.prm392_android_app_frontend.presentation.viewmodel;

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

/**
 * ViewModel dùng cho các màn hình quản trị/staff với yêu cầu lọc, tìm kiếm
 * và cập nhật trạng thái đơn hàng.
 */
public class OrderManagementViewModel extends AndroidViewModel {

    private final OrderRepository orderRepository;

    private List<OrderDto> allOrders = new ArrayList<>();
    private final MediatorLiveData<Resource<List<OrderDto>>> orders = new MediatorLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> filterStatus = new MutableLiveData<>("ALL");

    private final MediatorLiveData<Resource<List<OrderDto>>> userOrders = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<OrderDto>> orderDetail = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<OrderDto>> updateOrderStatusResult = new MediatorLiveData<>();

    public OrderManagementViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application.getApplicationContext());

        orders.addSource(searchQuery, query -> applyFilters());
        orders.addSource(filterStatus, status -> fetchOrdersByStatus(status));
    }

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
        if (query == null) {
            query = "";
        }

        List<OrderDto> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        for (OrderDto order : allOrders) {
            String orderId = String.valueOf(order.getId());
            String customerName = order.getShippingFullName() != null
                    ? order.getShippingFullName().toLowerCase()
                    : "";
            if (orderId.contains(query) || customerName.contains(lowerCaseQuery)) {
                filteredList.add(order);
            }
        }
        orders.setValue(Resource.success(filteredList));
    }

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
        updateOrderStatus(orderId, newStatus, null);
    }

    public void updateOrderStatus(int orderId, String newStatus, Integer actorUserId) {
        updateOrderStatusResult.setValue(Resource.loading(null));
        final LiveData<Resource<OrderDto>> source = orderRepository.updateOrderStatus(orderId, newStatus, actorUserId);
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
