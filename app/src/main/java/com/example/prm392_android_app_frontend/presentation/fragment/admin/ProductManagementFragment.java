package com.example.prm392_android_app_frontend.presentation.fragment.admin;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392_android_app_frontend.databinding.FragmentProductManagementBinding;
import com.example.prm392_android_app_frontend.presentation.adapter.ProductManageAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;

public class ProductManagementFragment extends Fragment {

    private FragmentProductManagementBinding binding;
    private ProductViewModel productViewModel;
    private ProductManageAdapter productManageAdapter;
    private Uri pickedImageUri; // lưu tạm ảnh đã chọn
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pickedImageUri = uri;
                    Toast.makeText(getContext(), "Đã chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo RecyclerView và Adapter
        productManageAdapter = new ProductManageAdapter();

        // Thiết lập callback cho adapter
        productManageAdapter.setOnEditClickListener(product -> {
            showEditProductDialog(product);
        });

        productManageAdapter.setOnDeleteClickListener((productId, productName) -> {
            showDeleteConfirmDialog(productId, productName);
        });

        binding.recyclerViewProducts.setAdapter(productManageAdapter);

        // TODO: Gọi API để lấy danh sách sản phẩm (getAllProduct) và cập nhật RecyclerView
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        // Bắt đầu quan sát dữ liệu
        observeViewModel();
        productViewModel.fetchAllProducts();

        binding.fabAddProduct.setOnClickListener(v -> {
            // Mở DialogFragment để thêm sản phẩm
            AddAndUpdateProductFragment dialog = new AddAndUpdateProductFragment();
            dialog.show(getParentFragmentManager(), "AddProductDialog");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void observeViewModel() {
        // Lắng nghe danh sách sản phẩm
        productViewModel.getProductList().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                productManageAdapter.setProducts(products);
            }
        });

        // Lắng nghe thông báo lỗi
        productViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Hiển thị dialog xác nhận xóa sản phẩm
     */
    public void showDeleteConfirmDialog(int productId, String productName) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm \"" + productName + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    productViewModel.deleteProduct(productId);
                    Toast.makeText(getContext(), "Đang xóa sản phẩm...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Hiển thị dialog chỉnh sửa sản phẩm
     */
    public void showEditProductDialog(ProductDto product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chỉnh sửa sản phẩm");

        // Tạo layout cho dialog
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);

        EditText editName = new EditText(getContext());
        editName.setText(product.getName());
        editName.setHint("Tên sản phẩm");

        EditText editPrice = new EditText(getContext());
        editPrice.setText(String.valueOf(product.getPrice()));
        editPrice.setHint("Giá sản phẩm");

        EditText editQuantity = new EditText(getContext());
        editQuantity.setText(String.valueOf(product.getQuantity()));
        editQuantity.setHint("Số lượng sản phẩm");

        EditText editDescription = new EditText(getContext());
        editDescription.setText(product.getDescription());
        editDescription.setHint("Mô tả");

        layout.addView(editName);
        layout.addView(editPrice);
        layout.addView(editDescription);

        builder.setView(layout);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String name = editName.getText().toString().trim();
            String priceStr = editPrice.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            Integer quantity = Integer.parseInt(editQuantity.getText().toString().trim());

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                product.setName(name);
                product.setPrice(price);
                product.setDescription(description);
                product.setQuantity(quantity);

                productViewModel.updateProduct(product.getId(), product);
                Toast.makeText(getContext(), "Đang cập nhật sản phẩm...", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Giá sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
