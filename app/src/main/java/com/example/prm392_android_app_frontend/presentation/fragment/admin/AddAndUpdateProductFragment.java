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
import com.example.prm392_android_app_frontend.databinding.DialogUpdateProductBinding;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;

import java.util.List;
import java.util.stream.Collectors;

public class AddAndUpdateProductFragment extends DialogFragment {

    public interface OnProductCreatedListener {
        void onProductCreated(ProductDto product);
    }

    private DialogAddProductBinding binding;
    private DialogUpdateProductBinding updateBinding;
//    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private ProductViewModel productViewModel;
    private boolean isUploading = false;
    private boolean isUpdateMode = false;
    private OnProductCreatedListener listener;

    private Integer selectedCategoryId;
    private Integer selectedArtistId;
    private Integer selectedPublisherId;

    private ProductDto editingProduct = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    selectedImageUri = imageUri; // Lưu URI đã chọn

                    // ✅ Chỉ dùng binding khi nó KHÔNG null
                    if (isUpdateMode && updateBinding != null) {
                        Glide.with(requireContext())
                                .load(imageUri)
                                .centerCrop()
                                .into(updateBinding.imagePreview);
                        checkIfChanged(); // ✅ gọi lại khi ảnh thay đổi
                    } else if (binding != null) {
                        Glide.with(requireContext())
                                .load(imageUri)
                                .centerCrop()
                                .into(binding.imagePreview);
                    }
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null && getArguments().containsKey("product_to_edit")) {
            updateBinding = DialogUpdateProductBinding.inflate(inflater, container, false);
            return updateBinding.getRoot();
        } else {
            binding = DialogAddProductBinding.inflate(inflater, container, false);
            return binding.getRoot();
        }
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

        // =========================
        // PHÂN NHÁNH THEO LOẠI DIALOG
        // =========================
        if (editingProduct != null) {
            // Ban đầu, disable nút lưu vì chưa có thay đổi
            updateBinding.btnSave.setEnabled(false);
            // ======================= UPDATE MODE =======================
            isUpdateMode = true;
            updateBinding.btnSelectImage.setOnClickListener(v -> {
                if (!isUploading) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    imagePickerLauncher.launch(intent);
                }
            });

            productViewModel.fetchAllCategories();
            productViewModel.fetchAllArtists();
            productViewModel.fetchAllPublishers();

            // ====== CATEGORIES ======
            productViewModel.getCategoriesState().observe(getViewLifecycleOwner(), categories -> {
                if (categories == null || categories.isEmpty() || editingProduct == null) return;

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categories.stream().map(c -> c.getName()).collect(Collectors.toList())
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                updateBinding.spinnerCategory.setAdapter(adapter);

                updateBinding.spinnerCategory.post(() -> {
                    boolean found = false;

                    for (int i = 0; i < categories.size(); i++) {
                        String categoryName = categories.get(i).getName();
                        if (categoryName.equalsIgnoreCase(editingProduct.getCategoryName())) {
                            updateBinding.spinnerCategory.setText(categoryName, false);
                            selectedCategoryId = categories.get(i).getId();
                            found = true;
                            break;
                        }
                    }

                    // Nếu không tìm thấy publisher trùng khớp, hiển thị mặc định
                    if (!found) {
                        updateBinding.spinnerCategory.setText("Chưa chọn danh mục", false);
                        selectedCategoryId = null;
                    }
                });

                updateBinding.spinnerCategory.setOnItemClickListener((parent, view1, position, id) ->
                        selectedCategoryId = categories.get(position).getId());
            });

            // ====== ARTISTS ======
            productViewModel.getArtistsState().observe(getViewLifecycleOwner(), artists -> {
                if (artists == null || artists.isEmpty() || editingProduct == null) return;

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        artists.stream().map(a -> a.getArtistName()).collect(Collectors.toList())
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                updateBinding.spinnerArtist.setAdapter(adapter);

                updateBinding.spinnerArtist.post(() -> {
                    boolean found = false;

                    for (int i = 0; i < artists.size(); i++) {
                        String artistName = artists.get(i).getArtistName();
                        if (artistName.equalsIgnoreCase(editingProduct.getArtistName())) {
                            updateBinding.spinnerArtist.setText(artistName, false);
                            selectedArtistId = artists.get(i).getId();
                            found = true;
                            break;
                        }
                    }

                    // Nếu không tìm thấy publisher trùng khớp, hiển thị mặc định
                    if (!found) {
                        updateBinding.spinnerArtist.setText("Chưa chọn nghệ sĩ", false);
                        selectedCategoryId = null;
                    }
                });

                updateBinding.spinnerArtist.setOnItemClickListener((parent, view12, position, id) ->
                        selectedArtistId = artists.get(position).getId());
            });

            // ====== PUBLISHERS ======
            productViewModel.getPublishersState().observe(getViewLifecycleOwner(), publishers -> {
                if (publishers == null || publishers.isEmpty() || editingProduct == null) return;

                List<String> publisherNames = publishers.stream()
                        .map(p -> p.getName())
                        .collect(Collectors.toList());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        publisherNames
                );
                updateBinding.spinnerPublisher.setAdapter(adapter);

                // Prefill publisher name theo editingProduct
                updateBinding.spinnerPublisher.post(() -> {
                    boolean found = false;

                    for (int i = 0; i < publishers.size(); i++) {
                        String pubName = publishers.get(i).getName();
                        if (pubName.equalsIgnoreCase(editingProduct.getPublisherName())) {
                            updateBinding.spinnerPublisher.setText(pubName, false);
                            selectedPublisherId = publishers.get(i).getId();
                            found = true;
                            break;
                        }
                    }

                    // Nếu không tìm thấy publisher trùng khớp, hiển thị mặc định
                    if (!found) {
                        updateBinding.spinnerPublisher.setText("Chưa chọn nhà xuất bản", false);
                        selectedPublisherId = null;
                    }
                });


                // Khi user chọn publisher mới
                updateBinding.spinnerPublisher.setOnItemClickListener((parent, view3, position, id) -> {
                    selectedPublisherId = publishers.get(position).getId();
                });
            });



            // ===== Prefill dữ liệu =====
            updateBinding.etProductName.setText(editingProduct.getName());
            updateBinding.etProductPrice.setText(String.valueOf(editingProduct.getPrice()));
            updateBinding.etProductQuantity.setText(String.valueOf(editingProduct.getQuantity()));
            updateBinding.etProductDescription.setText(editingProduct.getDescription());
            updateBinding.etProductReleaseDate.setText(editingProduct.getReleaseDate());

            Glide.with(this)
                    .load(editingProduct.getImageUrl())
                    .centerCrop()
                    .into(updateBinding.imagePreview);
            //
            // Theo dõi thay đổi để bật/tắt nút Lưu
            updateBinding.etProductName.addTextChangedListener(new SimpleTextWatcher() {
                @Override public void afterTextChanged(android.text.Editable s) { checkIfChanged(); }
            });
            updateBinding.etProductPrice.addTextChangedListener(new SimpleTextWatcher() {
                @Override public void afterTextChanged(android.text.Editable s) { checkIfChanged(); }
            });
            updateBinding.etProductQuantity.addTextChangedListener(new SimpleTextWatcher() {
                @Override public void afterTextChanged(android.text.Editable s) { checkIfChanged(); }
            });
            updateBinding.etProductDescription.addTextChangedListener(new SimpleTextWatcher() {
                @Override public void afterTextChanged(android.text.Editable s) { checkIfChanged(); }
            });
            updateBinding.etProductReleaseDate.addTextChangedListener(new SimpleTextWatcher() {
                @Override public void afterTextChanged(android.text.Editable s) { checkIfChanged(); }
            });


            // Spinner thay đổi
            updateBinding.spinnerCategory.setOnItemClickListener((parent, v1, pos, id) -> {
                selectedCategoryId = productViewModel.getCategoriesState().getValue().get(pos).getId();
                checkIfChanged();
            });

            updateBinding.spinnerArtist.setOnItemClickListener((parent, v1, pos, id) -> {
                selectedArtistId = productViewModel.getArtistsState().getValue().get(pos).getId();
                checkIfChanged();
            });

            updateBinding.spinnerPublisher.setOnItemClickListener((parent, v1, pos, id) -> {
                selectedPublisherId = productViewModel.getPublishersState().getValue().get(pos).getId();
                checkIfChanged();
            });

            //
            updateBinding.btnCancel.setOnClickListener(v -> {
                if (!isUploading) dismiss();
            });

            updateBinding.btnSave.setOnClickListener(v -> {
                if (isUploading) return;

                String productName = updateBinding.etProductName.getText().toString().trim();
                String priceStr = updateBinding.etProductPrice.getText().toString().trim();
                String quantityStr = updateBinding.etProductQuantity.getText().toString().trim();
                String description = updateBinding.etProductDescription.getText().toString().trim();
                String releaseDate = updateBinding.etProductReleaseDate.getText().toString().trim();

                double price = Double.parseDouble(priceStr);
                int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);

                updateProduct(editingProduct.getId(), productName, price, quantity, description, releaseDate);
            });

        } else {
            // ======================= ADD MODE =======================
            binding.btnSelectImage.setOnClickListener(v -> {
                if (!isUploading) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    imagePickerLauncher.launch(intent);
                }
            });

            productViewModel.fetchAllCategories();
            productViewModel.fetchAllArtists();
            productViewModel.fetchAllPublishers();

            // ====== CATEGORIES ======
            productViewModel.getCategoriesState().observe(getViewLifecycleOwner(), categories -> {
                if (categories == null) return;

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categories.stream().map(c -> c.getName()).collect(Collectors.toList())
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerCategory.setAdapter(adapter);

                binding.spinnerCategory.setOnItemClickListener((parent, view1, position, id) ->
                        selectedCategoryId = categories.get(position).getId());
            });

            // ====== ARTISTS ======
            productViewModel.getArtistsState().observe(getViewLifecycleOwner(), artists -> {
                if (artists == null) return;

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        artists.stream().map(a -> a.getArtistName()).collect(Collectors.toList())
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerArtist.setAdapter(adapter);

                binding.spinnerArtist.setOnItemClickListener((parent, view12, position, id) ->
                        selectedArtistId = artists.get(position).getId());
            });

            // ====== PUBLISHERS ======
            productViewModel.getPublishersState().observe(getViewLifecycleOwner(), publishers -> {
                if (publishers == null) return;

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        publishers.stream().map(p -> p.getName()).collect(Collectors.toList())
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerPublisher.setAdapter(adapter);

                binding.spinnerPublisher.setOnItemClickListener((parent, view13, position, id) ->
                        selectedPublisherId = publishers.get(position).getId());
            });

            binding.btnCancel.setOnClickListener(v -> {
                if (!isUploading) dismiss();
            });

            binding.btnSave.setOnClickListener(v -> {
                if (isUploading) return;

                String productName = binding.etProductName.getText().toString().trim();
                String priceStr = binding.etProductPrice.getText().toString().trim();
                String quantityStr = binding.etProductQuantity.getText().toString().trim();
                String description = binding.etProductDescription.getText().toString().trim();
                String releaseDate = binding.etProductReleaseDate.getText().toString().trim();

                if (productName.isEmpty() || priceStr.isEmpty()) {
                    Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                double price = Double.parseDouble(priceStr);
                int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);

                uploadImageAndCreateProduct(productName, price, quantity, description, releaseDate);
            });
        }
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
        updateBinding = null;
    }

    // ===== CREATE PRODUCT =====
    private void uploadImageAndCreateProduct(String productName, double price, int quantity, String description, String releaseDate) {
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
                newProduct.setReleaseDate(releaseDate);

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
    private void updateProduct(int productId, String name, double price, int quantity, String description, String releaseDate) {
        isUploading = true;
        updateUIForUploading(true);

        if (selectedImageUri != null) {
            // Có chọn ảnh mới
            FirebaseStorageUploader.uploadImage(selectedImageUri, new FirebaseStorageUploader.Callback() {
                @Override
                public void onSuccess(String newImageUrl) {
                    saveUpdatedProduct(productId, name, price, quantity, description, newImageUrl, releaseDate);
                }

                @Override
                public void onError(String message) {
                    isUploading = false;
                    updateUIForUploading(false);
                    Toast.makeText(getContext(), "Lỗi upload ảnh: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Không đổi ảnh
            saveUpdatedProduct(productId, name, price, quantity, description, editingProduct.getImageUrl(), releaseDate);
        }
    }

    private void saveUpdatedProduct(int productId, String name, double price, int quantity, String description, String imageUrl, String releaseDate) {
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
        updated.setReleaseDate(releaseDate);

        productViewModel.updateProduct(updated.getId(), updated);

        Toast.makeText(getContext(), "Đã cập nhật sản phẩm thành công!", Toast.LENGTH_SHORT).show();
        isUploading = false;
        updateUIForUploading(false);
        dismiss();
    }

    private void updateUIForUploading(boolean uploading) {
        if (isUpdateMode && updateBinding != null) {
            updateBinding.btnSave.setEnabled(!uploading);
            updateBinding.btnCancel.setEnabled(!uploading);
            updateBinding.btnSelectImage.setEnabled(!uploading);
            updateBinding.btnSave.setText(uploading ? "Đang xử lý..." : "Cập nhật");
        } else if (binding != null) {
            binding.btnSave.setEnabled(!uploading);
            binding.btnCancel.setEnabled(!uploading);
            binding.btnSelectImage.setEnabled(!uploading);
            binding.btnSave.setText(uploading ? "Đang xử lý..." : "Lưu");
        }
    }

    private void checkIfChanged() {
        if (updateBinding == null || editingProduct == null) return;

        boolean changed =
                !updateBinding.etProductName.getText().toString().equals(editingProduct.getName()) ||
                        !updateBinding.etProductPrice.getText().toString().equals(String.valueOf(editingProduct.getPrice())) ||
                        !updateBinding.etProductQuantity.getText().toString().equals(String.valueOf(editingProduct.getQuantity())) ||
                        !updateBinding.etProductDescription.getText().toString().equals(editingProduct.getDescription()) ||
                        !updateBinding.etProductReleaseDate.getText().toString().equals(editingProduct.getReleaseDate()) ||
                        (selectedCategoryId != null && !selectedCategoryId.equals(editingProduct.getCategoryId())) ||
                        (selectedArtistId != null && !selectedArtistId.equals(editingProduct.getArtistId())) ||
                        (selectedPublisherId != null && !selectedPublisherId.equals(editingProduct.getPublisherId())) ||
                        selectedImageUri != null; // nếu đổi ảnh

        updateBinding.btnSave.setEnabled(changed);
    }

    private abstract static class SimpleTextWatcher implements android.text.TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
