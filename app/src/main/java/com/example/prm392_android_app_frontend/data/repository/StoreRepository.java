package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.store.StoreNearbyDto;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.StoreApi;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreRepository {
    private final StoreApi api = ApiClient.get().create(StoreApi.class);

    public interface Result<T> { void onSuccess(T d); void onError(String m); }

    public void getNearby(double lat, double lng, Double radiusKm, Integer limit,
                          Integer productId, Boolean inStockOnly,
                          Result<List<StoreNearbyDto>> cb) {
        api.getNearby(lat, lng, radiusKm, limit, productId, inStockOnly)
                .enqueue(new Callback<List<StoreNearbyDto>>() {
                    @Override public void onResponse(Call<List<StoreNearbyDto>> call,
                                                     Response<List<StoreNearbyDto>> resp) {
                        if (resp.isSuccessful() && resp.body()!=null) {
                            cb.onSuccess(resp.body());
                        } else {
                            cb.onError("Không thể tải danh sách cửa hàng. Vui lòng thử lại.");
                        }
                    }
                    @Override public void onFailure(Call<List<StoreNearbyDto>> call, Throwable t) {
                        cb.onError(t.getMessage());
                    }
                });
    }
}
