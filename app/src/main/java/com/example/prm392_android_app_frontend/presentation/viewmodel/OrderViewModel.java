package com.example.prm392_android_app_frontend.presentation.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.core.util.Resource;
import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;
import com.example.prm392_android_app_frontend.data.repository.OrderRepository;

import java.util.List;

public class OrderViewModel extends AndroidViewModel {

    private static final String TAG = "OrderViewModel_DEBUG";
    private final OrderRepository orderRepository;
    private MutableLiveData<Resource<List<OrderDto>>> orders = new MutableLiveData<>();
    private MutableLiveData<Resource<OrderDto>> updateOrderStatusResult = new MutableLiveData<>();

    public OrderViewModel(@NonNull Application application) {
        super(application);
        Log.d(TAG, "Initializing OrderRepository.");
        orderRepository = new OrderRepository(application.getApplicationContext());
        Log.d(TAG, "OrderRepository initialized.");
        fetchOrders();
    }

    public LiveData<Resource<List<OrderDto>>> getOrders() {
        return orders;
    }

    public LiveData<Resource<OrderDto>> getUpdateOrderStatusResult() {
        return updateOrderStatusResult;
    }

    public void refreshOrders() {
        fetchOrders();
    }

    private void fetchOrders() {
        Log.d(TAG, "fetchOrders: Fetching from repository.");
        orders.postValue(Resource.loading(null));
        orderRepository.getAllOrders().observeForever(newOrders -> orders.postValue(newOrders));
    }

    public void updateOrderStatus(int orderId, String newStatus) {
        Log.d(TAG, "updateOrderStatus: Updating status for orderId " + orderId);
        updateOrderStatusResult.postValue(Resource.loading(null));
        orderRepository.updateOrderStatus(orderId, newStatus).observeForever(result -> {
            updateOrderStatusResult.postValue(result);
            // If the update was successful, refresh the list of orders
            if (result.isSuccess()) {
                Log.d(TAG, "updateOrderStatus: Successfully updated, refreshing orders list.");
                fetchOrders();
            }
        });
    }
}
