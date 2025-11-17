package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.store.StoreInventoryItemDto;
import com.example.prm392_android_app_frontend.data.dto.store.StoreInventoryUpdateRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface StoreInventoryApi {

    @GET("store-locations/{storeId}/inventory")
    Call<List<StoreInventoryItemDto>> listInventory(@Path("storeId") int storeId);

    @PUT("store-locations/{storeId}/inventory")
    Call<StoreInventoryItemDto> updateInventory(@Path("storeId") int storeId,
                                               @Body StoreInventoryUpdateRequest request);
}
