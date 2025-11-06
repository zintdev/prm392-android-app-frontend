package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.PublisherDto;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PublisherApi {

    @GET("publishers")
    Call<List<PublisherDto>> getPublishers();

    @GET("publishers/{id}")
    Call<PublisherDto> getPublisherById(@Path("id") Integer id);

    @POST("publishers")
    Call<PublisherDto> createPublisher(@Body PublisherDto publisherDto);

    @PUT("publishers/{id}")
    Call<PublisherDto> updatePublisher(@Path("id") Integer id, @Body PublisherDto publisherDto);

    @DELETE("publishers/{id}")
    Call<Void> deletePublisher(@Path("id") Integer id);
}
