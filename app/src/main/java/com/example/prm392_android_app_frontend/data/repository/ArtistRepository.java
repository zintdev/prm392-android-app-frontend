package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.ArtistDto;
import com.example.prm392_android_app_frontend.data.remote.api.ArtistApi;

import java.util.List;
import retrofit2.Callback;

import retrofit2.Response;
import retrofit2.Call;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
public class ArtistRepository {
    private final ArtistApi api;
    public ArtistRepository(ArtistApi api) { this.api = api; }

    public void getAll(Callback<List<ArtistDto>> cb) {
        api.getArtists().enqueue(cb);
    }

    public ArtistRepository() {
        this.api = ApiClient.get().create(ArtistApi.class);
    }

    public interface ArtistCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    // Get all artists
    public void getAllArtists(ArtistCallback<List<ArtistDto>> callback) {
        api.getArtists().enqueue(new Callback<List<ArtistDto>>() {
            @Override
            public void onResponse(Call<List<ArtistDto>> call, Response<List<ArtistDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải danh sách nghệ sĩ");
                }
            }

            @Override
            public void onFailure(Call<List<ArtistDto>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // Get artist by ID
    public void getArtistById(int artistId, ArtistCallback<ArtistDto> callback) {
        api.getArtistById(artistId).enqueue(new Callback<ArtistDto>() {
            @Override
            public void onResponse(Call<ArtistDto> call, Response<ArtistDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không tìm thấy nghệ sĩ");
                }
            }

            @Override
            public void onFailure(Call<ArtistDto> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // Create artist
    public void createArtist(ArtistDto artist, ArtistCallback<ArtistDto> callback) {
        api.createArtist(artist).enqueue(new Callback<ArtistDto>() {
            @Override
            public void onResponse(Call<ArtistDto> call, Response<ArtistDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể thêm nghệ sĩ");
                }
            }

            @Override
            public void onFailure(Call<ArtistDto> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // Update artist
    public void updateArtist(int artistId, ArtistDto artist, ArtistCallback<ArtistDto> callback) {
        api.updateArtist(artistId, artist).enqueue(new Callback<ArtistDto>() {
            @Override
            public void onResponse(Call<ArtistDto> call, Response<ArtistDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể cập nhật nghệ sĩ");
                }
            }

            @Override
            public void onFailure(Call<ArtistDto> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // Delete artist
    public void deleteArtist(int artistId, ArtistCallback<Void> callback) {
        api.deleteArtist(artistId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Không thể xóa nghệ sĩ");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
