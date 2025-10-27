package com.example.prm392_android_app_frontend.data.remote.api;

import com.example.prm392_android_app_frontend.data.dto.ArtistDto;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ArtistApi {
    @GET("artists")
    Call<List<ArtistDto>> getArtists();
}
