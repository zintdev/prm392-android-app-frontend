package com.example.prm392_android_app_frontend.presentation.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.prm392_android_app_frontend.core.util.Resource;
import com.example.prm392_android_app_frontend.data.dto.store.StoreNearbyDto;
import com.example.prm392_android_app_frontend.data.repository.StoreRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StoreViewModel extends ViewModel {
    private final StoreRepository repo;

    public StoreViewModel() {
        this.repo = new StoreRepository(); // TODO: có thể thay bằng Factory để DI/mocking
    }

    public final MutableLiveData<Resource<List<StoreNearbyDto>>> nearby = new MutableLiveData<>();

    public void loadNearby(double lat, double lng, Double radiusKm, Integer limit,
                           Integer productId, Boolean inStockOnly) {

        nearby.postValue(Resource.loading());
        repo.getNearby(lat, lng, radiusKm, limit, productId, inStockOnly,
                new StoreRepository.Result<>() {
                    @Override public void onSuccess(List<StoreNearbyDto> data) {
                        List<StoreNearbyDto> list = new ArrayList<>(data);
                        if (radiusKm != null) {
                            double r = radiusKm;
                            list.removeIf(x -> x.distanceKm > r);
                        }
                        Collections.sort(list, Comparator.comparingDouble(x -> x.distanceKm));
                        nearby.postValue(Resource.success(list));
                    }

                    @Override public void onError(String m) {
                        nearby.postValue(Resource.error(m));
                    }
                });
    }
}
