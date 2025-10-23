package com.example.prm392_android_app_frontend.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392_android_app_frontend.core.util.Resource;
import com.example.prm392_android_app_frontend.data.dto.BlogDto;
import com.example.prm392_android_app_frontend.data.repository.BlogRepository;

import java.util.List;

public class BlogViewModel extends ViewModel {

    private final MutableLiveData<Resource<List<BlogDto>>> blogsState = new MutableLiveData<>();
    private final BlogRepository repo = new BlogRepository();

    public LiveData<Resource<List<BlogDto>>> getBlogsState() {
        return blogsState;
    }

    public void fetchBlogs() {
        blogsState.setValue(Resource.loading(null));
        repo.fetchData(new BlogRepository.BlogDataListener() {
            @Override
            public void onBlogDataFetched(List<BlogDto> blogDtos) {
                blogsState.setValue(Resource.success(blogDtos));
            }

            @Override
            public void onError(String errorMessage) {
                blogsState.setValue(Resource.error(errorMessage, null));
            }
        });
    }
}
