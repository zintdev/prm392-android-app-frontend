package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.ArtistDto;
import com.example.prm392_android_app_frontend.data.remote.api.ArtistApi;

import java.util.List;
import retrofit2.Callback;

public class ArtistRepository {
    private final ArtistApi api;
    public ArtistRepository(ArtistApi api) { this.api = api; }

    public void getAll(Callback<List<ArtistDto>> cb) {
        api.getArtists().enqueue(cb);
    }
}
