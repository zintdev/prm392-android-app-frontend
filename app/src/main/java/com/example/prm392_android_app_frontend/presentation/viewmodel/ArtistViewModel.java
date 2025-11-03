package com.example.prm392_android_app_frontend.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392_android_app_frontend.data.dto.ArtistDto;
import com.example.prm392_android_app_frontend.data.repository.ArtistRepository;

import java.util.List;

public class ArtistViewModel extends ViewModel {

    private final ArtistRepository artistRepository;

    private final MutableLiveData<List<ArtistDto>> artistList = new MutableLiveData<>();
    private final MutableLiveData<ArtistDto> selectedArtist = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public ArtistViewModel() {
        this.artistRepository = new ArtistRepository();
    }

    // Getters for LiveData
    public LiveData<List<ArtistDto>> getArtistList() {
        return artistList;
    }

    public LiveData<ArtistDto> getSelectedArtist() {
        return selectedArtist;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    // Fetch all artists
    public void fetchAllArtists() {
        isLoading.setValue(true);
        artistRepository.getAllArtists(new ArtistRepository.ArtistCallback<List<ArtistDto>>() {
            @Override
            public void onSuccess(List<ArtistDto> data) {
                isLoading.setValue(false);
                artistList.setValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    // Fetch artist by ID
    public void fetchArtistById(int artistId) {
        isLoading.setValue(true);
        artistRepository.getArtistById(artistId, new ArtistRepository.ArtistCallback<ArtistDto>() {
            @Override
            public void onSuccess(ArtistDto data) {
                isLoading.setValue(false);
                selectedArtist.setValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    // Create artist
    public void createArtist(ArtistDto artist) {
        isLoading.setValue(true);
        artistRepository.createArtist(artist, new ArtistRepository.ArtistCallback<ArtistDto>() {
            @Override
            public void onSuccess(ArtistDto data) {
                isLoading.setValue(false);
                successMessage.setValue("Thêm nghệ sĩ thành công!");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi thêm nghệ sĩ: " + error);
            }
        });
    }

    // Update artist
    public void updateArtist(int artistId, ArtistDto artist) {
        isLoading.setValue(true);
        artistRepository.updateArtist(artistId, artist, new ArtistRepository.ArtistCallback<ArtistDto>() {
            @Override
            public void onSuccess(ArtistDto data) {
                isLoading.setValue(false);
                successMessage.setValue("Cập nhật nghệ sĩ thành công!");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi cập nhật: " + error);
            }
        });
    }

    // Delete artist
    public void deleteArtist(int artistId) {
        isLoading.setValue(true);
        artistRepository.deleteArtist(artistId, new ArtistRepository.ArtistCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isLoading.setValue(false);
                successMessage.setValue("Xóa nghệ sĩ thành công!");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi xóa: " + error);
            }
        });
    }

    // Clear messages
    public void clearMessages() {
        errorMessage.setValue(null);
        successMessage.setValue(null);
    }
}