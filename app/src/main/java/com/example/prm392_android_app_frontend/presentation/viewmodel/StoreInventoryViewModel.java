package com.example.prm392_android_app_frontend.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.data.dto.store.StoreInventoryChange;
import com.example.prm392_android_app_frontend.data.dto.store.StoreInventoryItemDto;
import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationResponse;
import com.example.prm392_android_app_frontend.data.repository.StoreInventoryRepository;

import java.util.ArrayList;
import java.util.List;

public class StoreInventoryViewModel extends AndroidViewModel {

    private final StoreInventoryRepository repository;
    private final MutableLiveData<List<StoreLocationResponse>> stores = new MutableLiveData<>();
    private final MutableLiveData<List<StoreInventoryItemDto>> inventory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> updating = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> success = new MutableLiveData<>();
    private final MutableLiveData<StoreInventoryItemDto> updatedItem = new MutableLiveData<>();

    private final List<StoreInventoryItemDto> inventoryCache = new ArrayList<>();

    public StoreInventoryViewModel(@NonNull Application application) {
        super(application);
        repository = new StoreInventoryRepository(application.getApplicationContext());
    }

    public LiveData<List<StoreLocationResponse>> getStores() {
        return stores;
    }

    public LiveData<List<StoreInventoryItemDto>> getInventory() {
        return inventory;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getUpdating() {
        return updating;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSuccess() {
        return success;
    }

    public LiveData<StoreInventoryItemDto> getUpdatedItem() {
        return updatedItem;
    }

    public void loadStores() {
        loading.setValue(true);
        repository.listStores(new StoreInventoryRepository.Result<List<StoreLocationResponse>>() {
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

    public void loadInventory(int storeId) {
        loading.setValue(true);
        repository.listInventory(storeId, new StoreInventoryRepository.Result<List<StoreInventoryItemDto>>() {
            @Override
            public void onSuccess(List<StoreInventoryItemDto> data) {
                loading.postValue(false);
                inventoryCache.clear();
                if (data != null) {
                    inventoryCache.addAll(data);
                }
                inventory.postValue(new ArrayList<>(inventoryCache));
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void updateInventory(int storeId, StoreInventoryItemDto item, int quantity, int actorUserId) {
        if (item == null || item.getProductId() == null) {
            error.setValue("Không xác định được sản phẩm");
            return;
        }
        List<StoreInventoryChange> changes = new ArrayList<>();
        changes.add(new StoreInventoryChange(item.getProductId(), quantity, item.getProductName()));
        runUpdateQueue(storeId, changes, actorUserId, false);
    }

    public void updateInventoryBatch(int storeId, List<StoreInventoryChange> changes, int actorUserId) {
        runUpdateQueue(storeId, changes, actorUserId, true);
    }

    private void runUpdateQueue(int storeId, List<StoreInventoryChange> changes, int actorUserId, boolean bulk) {
        if (changes == null || changes.isEmpty()) {
            success.setValue("Không có thay đổi cần lưu");
            return;
        }
        updating.setValue(true);
        processNext(storeId, changes, actorUserId, 0, bulk, 0);
    }

    private void processNext(int storeId,
                             List<StoreInventoryChange> changes,
                             int actorUserId,
                             int index,
                             boolean bulk,
                             int successCount) {
        if (index >= changes.size()) {
            updating.postValue(false);
            if (bulk) {
                success.postValue("Đã lưu " + successCount + " sản phẩm");
            } else if (successCount > 0) {
                success.postValue("Đã cập nhật số lượng sản phẩm");
            }
            return;
        }

        StoreInventoryChange change = changes.get(index);
        repository.updateInventory(storeId, change.getProductId(), change.getQuantity(), actorUserId,
                new StoreInventoryRepository.ResultWithCode<StoreInventoryItemDto>() {
                    @Override
                    public void onSuccess(StoreInventoryItemDto data) {
                        handleUpdateSuccess(data);
                        processNext(storeId, changes, actorUserId, index + 1, bulk, successCount + 1);
                    }

                    @Override
                    public void onError(String message, int code) {
                        updating.postValue(false);
                        error.postValue(buildErrorMessage(message, code, change.getProductName()));
                    }
                });
    }

    private void handleUpdateSuccess(StoreInventoryItemDto data) {
        if (data == null || data.getProductId() == null) {
            return;
        }
        boolean replaced = false;
        for (int i = 0; i < inventoryCache.size(); i++) {
            StoreInventoryItemDto item = inventoryCache.get(i);
            if (item != null && item.getProductId() != null
                    && item.getProductId().equals(data.getProductId())) {
                inventoryCache.set(i, data);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            inventoryCache.add(data);
        }
        inventory.postValue(new ArrayList<>(inventoryCache));
        updatedItem.postValue(data);
    }

    private String buildErrorMessage(String message, int code, String productName) {
        String label = productName != null && !productName.isEmpty() ? productName : "sản phẩm";
        if (message != null && !message.isEmpty()) {
            return message;
        }
        if (code > 0) {
            return "Không thể cập nhật " + label + " (" + code + ")";
        }
        return "Không thể cập nhật " + label;
    }
}
