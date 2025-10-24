package com.example.prm392_android_app_frontend.presentation.fragment.admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.databinding.DialogAddProductBinding;
import com.example.prm392_android_app_frontend.core.util.FirebaseStorageUploader;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;
import androidx.lifecycle.ViewModelProvider;

public class AddProductFragment extends DialogFragment {

    public interface OnProductCreatedListener {
        void onProductCreated(ProductDto product);
    }

    private DialogAddProductBinding binding;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private ProductViewModel productViewModel;
    private boolean isUploading = false;
    private OnProductCreatedListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo ActivityResultLauncher để xử lý kết quả chọn ảnh
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Sử dụng Glide để hiển thị ảnh preview
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

        // Khởi tạo ViewModel
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        // Sự kiện khi nhấn nút "Chọn ảnh"
        binding.btnSelectImage.setOnClickListener(v -> {
            if (!isUploading) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            }
        });

        // Sự kiện cho nút Hủy
        binding.btnCancel.setOnClickListener(v -> {
            if (!isUploading) {
                dismiss();
            }
        });

        // Sự kiện cho nút Lưu
        binding.btnSave.setOnClickListener(v -> {
            if (isUploading) return;

            String productName = binding.etProductName.getText().toString().trim();
            String priceStr = binding.etProductPrice.getText().toString().trim();
            String quantityStr = binding.etProductQuantity.getText().toString().trim();
            String description = binding.etProductDescription.getText().toString().trim();

            // Validation
            if (productName.isEmpty()) {
                binding.etProductName.setError("Tên sản phẩm không được để trống");
                return;
            }
            if (priceStr.isEmpty()) {
                binding.etProductPrice.setError("Giá sản phẩm không được để trống");
                return;
            }
            if (selectedImageUri == null) {
                Toast.makeText(getContext(), "Vui lòng chọn một ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);

                // Bắt đầu upload ảnh lên Firebase Storage
                uploadImageAndCreateProduct(productName, price, quantity, description);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Giá hoặc số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Thiết lập kích thước cho dialog
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Upload ảnh lên Firebase Storage, sau đó tạo sản phẩm với URL ảnh
     */
    private void uploadImageAndCreateProduct(String productName, double price, int quantity, String description) {
        isUploading = true;
        updateUIForUploading(true);

        FirebaseStorageUploader.uploadImage(selectedImageUri, new FirebaseStorageUploader.Callback() {
            @Override
            public void onSuccess(String downloadUrl) {
                // Tạo ProductDto với URL ảnh từ Firebase
                ProductDto newProduct = new ProductDto();
                newProduct.setName(productName);
                newProduct.setPrice(price);
                newProduct.setQuantity(quantity);
                newProduct.setDescription(description);
                newProduct.setImageUrl(downloadUrl); // URL từ Firebase Storage

                // Gọi ViewModel để tạo sản phẩm
                productViewModel.createProduct(newProduct);
                
                // Thông báo cho listener
                if (listener != null) {
                    listener.onProductCreated(newProduct);
                }
                
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

    /**
     * Cập nhật UI khi đang upload
     */
    private void updateUIForUploading(boolean uploading) {
        binding.btnSave.setEnabled(!uploading);
        binding.btnCancel.setEnabled(!uploading);
        binding.btnSelectImage.setEnabled(!uploading);
        
        if (uploading) {
            binding.btnSave.setText("Đang tải ảnh...");
        } else {
            binding.btnSave.setText("Lưu");
        }
    }
}
