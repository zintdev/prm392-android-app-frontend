package com.example.prm392_android_app_frontend.data.repository;

import com.example.prm392_android_app_frontend.data.dto.PublisherDto;
import com.example.prm392_android_app_frontend.data.remote.api.PublisherApi;

import java.util.List;
import retrofit2.Callback;

public class PublisherRepository {
    private final PublisherApi publisherApi;

    public PublisherRepository(PublisherApi publisherApi) {
        this.publisherApi = publisherApi;
    }

    public void getAll(Callback<List<PublisherDto>> callback) {
        publisherApi.getPublishers().enqueue(callback);
    }
}
