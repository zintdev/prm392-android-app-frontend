package com.example.prm392_android_app_frontend.presentation.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import com.example.prm392_android_app_frontend.data.remote.api.StoreApi;
import com.example.prm392_android_app_frontend.data.repository.StoreRepository;

public class StoreViewModel extends ViewModel {
    private final StoreRepository repo = new StoreRepository();

    public enum Status { LOADING, SUCCESS, ERROR }
    public static class Resource<T> {
        public final Status status; public final T data; public final String message;
        private Resource(Status s, T d, String m){status=s;data=d;message=m;}
        public static <T> Resource<T> loading(){ return new Resource<>(Status.LOADING, null, null); }
        public static <T> Resource<T> success(T d){ return new Resource<>(Status.SUCCESS, d, null); }
        public static <T> Resource<T> error(String m){ return new Resource<>(Status.ERROR, null, m); }
    }

    public final MutableLiveData<Resource<List<StoreApi.StoreNearbyDto>>> nearby = new MutableLiveData<>();

    public void loadNearby(double lat,double lng, Double radiusKm,Integer limit,
                           Integer productId, Boolean inStockOnly){
        nearby.postValue(Resource.loading());
        repo.getNearby(lat,lng,radiusKm,limit,productId,inStockOnly,new StoreRepository.Result<>() {
            @Override public void onSuccess(List<StoreApi.StoreNearbyDto> data) {
                nearby.postValue(Resource.success(data));
            }
            @Override public void onError(String m) {
                nearby.postValue(Resource.error(m));
            }
        });
    }
}
