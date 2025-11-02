package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.ArtistDto;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ArtistApi {
    @GET("artists")
    Call<List<ArtistDto>> getArtists();

    @POST("artists")
    Call<ArtistDto> createArtist(@Body ArtistDto artist);

    @PUT("artists/{id}")
    Call<ArtistDto> updateArtist(@Path("id") int id, @Body ArtistDto artist);

    @DELETE("artists/{id}")
    Call<Void> deleteArtist(@Path("id") int id);

    @GET("artists/{id}")
    Call<ArtistDto> getArtistById(@Path("id") int id);
}
