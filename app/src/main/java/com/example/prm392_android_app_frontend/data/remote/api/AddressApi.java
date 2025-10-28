package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.address.AddressDto;
import com.example.prm392_android_app_frontend.data.dto.address.CreateAddressRequest;
import com.example.prm392_android_app_frontend.data.dto.address.UpdateAddressRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AddressApi {
    @GET("addresses/user/{userId}")
    Call<List<AddressDto>> getAddressesByUserId(@Path("userId") int userId);
    @POST("addresses")
    Call<AddressDto> createAddress(@Body CreateAddressRequest body);
    @PUT("addresses/{id}")
    Call<AddressDto> updateAddress(@Path("id") int id, @Body UpdateAddressRequest body);

    @DELETE("addresses/{id}")
    Call<Void> deleteAddress(@Path("id") int id);
}
