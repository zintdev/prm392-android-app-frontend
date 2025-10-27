package com.example.prm392_android_app_frontend.presentation.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prm392_android_app_frontend.core.util.FirebaseStorageUploader;
import com.example.prm392_android_app_frontend.core.util.Resource;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.data.dto.ProductFilter;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.ProductApi;
import com.example.prm392_android_app_frontend.data.repository.ProductRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;

    private final MutableLiveData<Resource<List<ProductDto>>> productsState = new MutableLiveData<>();
    public LiveData<Resource<List<ProductDto>>> getProductsState() { return productsState; }

    private final MutableLiveData<List<ProductDto>> productList = new MutableLiveData<>();
    private final MutableLiveData<ProductDto> selectedProduct = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> imageUploadStatus = new MutableLiveData<>();

    public ProductViewModel(@NonNull Application application) {
        super(application);
        ProductApi productApi = ApiClient.get().create(ProductApi.class);
        this.productRepository = new ProductRepository(productApi);
    }

    public LiveData<List<ProductDto>> getProductList() { return productList; }
    public LiveData<ProductDto> getSelectedProduct() { return selectedProduct; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getImageUploadStatus() { return imageUploadStatus; }

    // --- Danh sách tất cả sản phẩm ---
    public void fetchAllProducts() {
        productRepository.getAllProducts(new Callback<List<ProductDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductDto>> call, @NonNull Response<List<ProductDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.postValue(response.body());
                } else {
                    errorMessage.postValue(getErrorMessageForStatusCode(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductDto>> call, @NonNull Throwable t) {
                errorMessage.postValue(getNetworkErrorMessage(t));
            }
        });
    }

    // --- Lấy chi tiết theo ID ---
    public void fetchProductById(int productId) {
        productRepository.getProductById(productId, new Callback<ProductDto>() {
            @Override
            public void onResponse(@NonNull Call<ProductDto> call, @NonNull Response<ProductDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    selectedProduct.postValue(response.body());
                } else {
                    errorMessage.postValue(getErrorMessageForStatusCode(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductDto> call, @NonNull Throwable t) {
                errorMessage.postValue(getNetworkErrorMessage(t));
            }
        });
    }

    // --- Upload ảnh sản phẩm ---
    public void uploadProductImage(int productId, Uri imageUri) {
        imageUploadStatus.postValue(true);
        FirebaseStorageUploader.uploadImage(imageUri, new FirebaseStorageUploader.Callback() {
            @Override
            public void onSuccess(String downloadUrl) {
                ProductDto current = selectedProduct.getValue();
                if (current == null) current = new ProductDto();

                current.setId(productId);
                current.setImageUrl(downloadUrl);
                selectedProduct.postValue(current);

                productRepository.updateProduct(productId, current, new Callback<ProductDto>() {
                    @Override
                    public void onResponse(@NonNull Call<ProductDto> call, @NonNull Response<ProductDto> response) {
                        imageUploadStatus.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            selectedProduct.postValue(response.body());
                        } else {
                            errorMessage.postValue(getErrorMessageForStatusCode(response.code()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ProductDto> call, @NonNull Throwable t) {
                        imageUploadStatus.postValue(false);
                        errorMessage.postValue(getNetworkErrorMessage(t));
                    }
                });
            }

            @Override
            public void onError(String message) {
                imageUploadStatus.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    // --- Tạo sản phẩm ---
    public void createProduct(ProductDto product) {
        productRepository.createProduct(product, new Callback<ProductDto>() {
            @Override
            public void onResponse(@NonNull Call<ProductDto> call, @NonNull Response<ProductDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fetchAllProducts();
                } else {
                    errorMessage.postValue(getErrorMessageForStatusCode(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductDto> call, @NonNull Throwable t) {
                errorMessage.postValue(getNetworkErrorMessage(t));
            }
        });
    }

    // --- Cập nhật sản phẩm ---
    public void updateProduct(int productId, ProductDto product) {
        productRepository.updateProduct(productId, product, new Callback<ProductDto>() {
            @Override
            public void onResponse(@NonNull Call<ProductDto> call, @NonNull Response<ProductDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fetchAllProducts();
                } else {
                    errorMessage.postValue(getErrorMessageForStatusCode(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductDto> call, @NonNull Throwable t) {
                errorMessage.postValue(getNetworkErrorMessage(t));
            }
        });
    }

    // --- Xóa sản phẩm ---
    public void deleteProduct(int productId) {
        productRepository.deleteProduct(productId, new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchAllProducts();
                } else {
                    errorMessage.postValue(getErrorMessageForStatusCode(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                errorMessage.postValue(getNetworkErrorMessage(t));
            }
        });
    }

    public void search(String name, ProductFilter filter) {
    productsState.postValue(Resource.loading());

    // Nếu name rỗng hoặc null → chỉ tìm theo filter
    if (name == null || name.trim().isEmpty()) {
        productRepository.filterProducts(filter, new Callback<List<ProductDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductDto>> call, @NonNull Response<List<ProductDto>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    List<ProductDto> products = res.body();
                    if (products.isEmpty()) {
                        productsState.postValue(Resource.error("Không có sản phẩm phù hợp với bộ lọc"));
                    } else {
                        productsState.postValue(Resource.success(products));
                    }
                } else {
                    productsState.postValue(Resource.error(getErrorMessageForStatusCode(res.code())));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductDto>> call, @NonNull Throwable t) {
                productsState.postValue(Resource.error(getNetworkErrorMessage(t)));
            }
        });
    } else {
        // Có name → tìm theo name + filter
        productRepository.search(name, filter, new Callback<List<ProductDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductDto>> call, @NonNull Response<List<ProductDto>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    List<ProductDto> products = res.body();
                    if (products.isEmpty()) {
                        productsState.postValue(Resource.error("Không có sản phẩm phù hợp với từ khóa"));
                    } else {
                        productsState.postValue(Resource.success(products));
                    }
                } else {
                    productsState.postValue(Resource.error(getErrorMessageForStatusCode(res.code())));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductDto>> call, @NonNull Throwable t) {
                productsState.postValue(Resource.error(getNetworkErrorMessage(t)));
            }
        });
    }
}
    public void searchByName(String name) {
        search(name, null);
    }

    // --- Xử lý lỗi chung ---
    private String getErrorMessageForStatusCode(int statusCode) {
        switch (statusCode) {
            case 400: return "Yêu cầu không hợp lệ";
            case 401: return "Không có quyền truy cập";
            case 403: return "Truy cập bị từ chối";
            case 404: return "Không tìm thấy dữ liệu";
            case 500: return "Lỗi máy chủ";
            case 503: return "Dịch vụ tạm thời không khả dụng";
            default: return "Lỗi tải dữ liệu (HTTP " + statusCode + ")";
        }
    }

    private String getNetworkErrorMessage(Throwable t) {
        return "Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : "Không xác định");
    }
}
