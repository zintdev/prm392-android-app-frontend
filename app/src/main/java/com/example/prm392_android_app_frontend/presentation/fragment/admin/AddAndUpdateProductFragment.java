package com.example.prm392_android_app_frontend.presentation.fragment.admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.core.util.FirebaseStorageUploader;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.databinding.DialogAddProductBinding;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;

import java.util.stream.Collectors;

public class AddAndUpdateProductFragment extends DialogFragment {

    public interface OnProductCreatedListener {
        void onProductCreated(ProductDto product);
    }

    private DialogAddProductBinding binding;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private ProductViewModel productViewModel;
    private boolean isUploading = false;
    private OnProductCreatedListener listener;

    private Integer selectedCategoryId;
    private Integer selectedArtistId;
    private Integer selectedPublisherId;

    private ProductDto editingProduct = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .centerCrop()
                                    .into(binding.imagePreview);
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void setOnProductCreatedListener(OnProductCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().containsKey("product_to_edit")) {
            editingProduct = (ProductDto) getArguments().getSerializable("product_to_edit");
        }

        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        binding.btnSelectImage.setOnClickListener(v -> {
            if (!isUploading) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            }
        });

        productViewModel.fetchAllCategories();
        productViewModel.fetchAllArtists();
        productViewModel.fetchAllPublishers();

        // ===== CATEGORIES =====
        productViewModel.getCategoriesState().observe(getViewLifecycleOwner(), categories -> {
            if (categories == null) return;

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categories.stream().map(c -> c.getName()).collect(Collectors.toList())
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerCategory.setAdapter(adapter);

            // Prefill nếu đang sửa
            if (editingProduct != null) {
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getId().equals(editingProduct.getCategoryId())) {
                        binding.spinnerCategory.setText(categories.get(i).getName(), false);
                        selectedCategoryId = categories.get(i).getId();
                        break;
                    }
                }
            } else if (!categories.isEmpty()) {
                binding.spinnerCategory.setText(categories.get(0).getName(), false);
                selectedCategoryId = categories.get(0).getId();
            }

            binding.spinnerCategory.setOnItemClickListener((parent, view1, position, id) ->
                    selectedCategoryId = categories.get(position).getId());
        });

        // ===== ARTISTS =====
        productViewModel.getArtistsState().observe(getViewLifecycleOwner(), artists -> {
            if (artists == null) return;

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    artists.stream().map(a -> a.getArtistName()).collect(Collectors.toList())
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerArtist.setAdapter(adapter);

            if (editingProduct != null) {
                for (int i = 0; i < artists.size(); i++) {
                    if (artists.get(i).getId().equals(editingProduct.getArtistId())) {
                        binding.spinnerArtist.setText(artists.get(i).getArtistName(), false);
                        selectedArtistId = artists.get(i).getId();
                        break;
                    }
                }
            } else if (!artists.isEmpty()) {
                binding.spinnerArtist.setText(artists.get(0).getArtistName(), false);
                selectedArtistId = artists.get(0).getId();
            }

            binding.spinnerArtist.setOnItemClickListener((parent, view12, position, id) ->
                    selectedArtistId = artists.get(position).getId());
        });

        // ===== PUBLISHERS =====
        productViewModel.getPublishersState().observe(getViewLifecycleOwner(), publishers -> {
            if (publishers == null) return;

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    publishers.stream().map(p -> p.getName()).collect(Collectors.toList())
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerPublisher.setAdapter(adapter);

            if (editingProduct != null) {
                for (int i = 0; i < publishers.size(); i++) {
                    if (publishers.get(i).getId().equals(editingProduct.getPublisherId())) {
                        binding.spinnerPublisher.setText(publishers.get(i).getName(), false);
                        selectedPublisherId = publishers.get(i).getId();
                        break;
                    }
                }
            } else if (!publishers.isEmpty()) {
                binding.spinnerPublisher.setText(publishers.get(0).getName(), false);
                selectedPublisherId = publishers.get(0).getId();
            }

            binding.spinnerPublisher.setOnItemClickListener((parent, view13, position, id) ->
                    selectedPublisherId = publishers.get(position).getId());
        });

        // ===== Prefill các trường khi sửa =====
        if (editingProduct != null) {
            binding.etProductName.setText(editingProduct.getName());
            binding.etProductPrice.setText(String.valueOf(editingProduct.getPrice()));
            binding.etProductQuantity.setText(String.valueOf(editingProduct.getQuantity()));
            binding.etProductDescription.setText(editingProduct.getDescription());

            Glide.with(this)
                    .load(editingProduct.getImageUrl())
                    .centerCrop()
                    .into(binding.imagePreview);

            binding.btnSave.setText("Cập nhật sản phẩm");
        }

        // ===== Button Cancel =====
        binding.btnCancel.setOnClickListener(v -> {
            if (!isUploading) dismiss();
        });

        // ===== Button Save =====
        binding.btnSave.setOnClickListener(v -> {
            if (isUploading) return;

            String productName = binding.etProductName.getText().toString().trim();
            String priceStr = binding.etProductPrice.getText().toString().trim();
            String quantityStr = binding.etProductQuantity.getText().toString().trim();
            String description = binding.etProductDescription.getText().toString().trim();

            if (productName.isEmpty() || priceStr.isEmpty() || selectedCategoryId == null
                    || selectedArtistId == null || selectedPublisherId == null) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);

            if (editingProduct != null) {
                updateProduct(editingProduct.getId(), productName, price, quantity, description);
            } else {
                uploadImageAndCreateProduct(productName, price, quantity, description);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ===== CREATE PRODUCT =====
    private void uploadImageAndCreateProduct(String productName, double price, int quantity, String description) {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        isUploading = true;
        updateUIForUploading(true);

        FirebaseStorageUploader.uploadImage(selectedImageUri, new FirebaseStorageUploader.Callback() {
            @Override
            public void onSuccess(String downloadUrl) {
                ProductDto newProduct = new ProductDto();
                newProduct.setName(productName);
                newProduct.setPrice(price);
                newProduct.setQuantity(quantity);
                newProduct.setDescription(description);
                newProduct.setImageUrl(downloadUrl);
                newProduct.setCategoryId(selectedCategoryId);
                newProduct.setArtistId(selectedArtistId);
                newProduct.setPublisherId(selectedPublisherId);

                productViewModel.createProduct(newProduct);
                if (listener != null) listener.onProductCreated(newProduct);

                Toast.makeText(getContext(), "Đã tạo sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onError(String message) {
                isUploading = false;
                updateUIForUploading(false);
                Toast.makeText(getContext(), "Lỗi upload ảnh: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ===== UPDATE PRODUCT =====
    private void updateProduct(int productId, String name, double price, int quantity, String description) {
        isUploading = true;
        updateUIForUploading(true);

        if (selectedImageUri != null) {
            // Có chọn ảnh mới
            FirebaseStorageUploader.uploadImage(selectedImageUri, new FirebaseStorageUploader.Callback() {
                @Override
                public void onSuccess(String newImageUrl) {
                    saveUpdatedProduct(productId, name, price, quantity, description, newImageUrl);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), "Lỗi upload ảnh: " + message, Toast.LENGTH_SHORT).show();
                    updateUIForUploading(false);
                }
            });
        } else {
            // Không đổi ảnh
            saveUpdatedProduct(productId, name, price, quantity, description, editingProduct.getImageUrl());
        }
    }

    private void saveUpdatedProduct(int productId, String name, double price, int quantity, String description, String imageUrl) {
        ProductDto updated = new ProductDto();
        updated.setId(productId);
        updated.setName(name);
        updated.setPrice(price);
        updated.setQuantity(quantity);
        updated.setDescription(description);
        updated.setImageUrl(imageUrl);
        updated.setCategoryId(selectedCategoryId);
        updated.setArtistId(selectedArtistId);
        updated.setPublisherId(selectedPublisherId);

        productViewModel.updateProduct(updated.getId(), updated);

        Toast.makeText(getContext(), "Đã cập nhật sản phẩm thành công!", Toast.LENGTH_SHORT).show();
        isUploading = false;
        updateUIForUploading(false);
        dismiss();
    }

    private void updateUIForUploading(boolean uploading) {
        binding.btnSave.setEnabled(!uploading);
        binding.btnCancel.setEnabled(!uploading);
        binding.btnSelectImage.setEnabled(!uploading);
        binding.btnSave.setText(uploading ? "Đang xử lý..." : (editingProduct != null ? "Cập nhật" : "Lưu"));
    }
}
