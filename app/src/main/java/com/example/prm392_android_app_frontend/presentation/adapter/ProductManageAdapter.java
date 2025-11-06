package com.example.prm392_android_app_frontend.presentation.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.presentation.activity.AdminProductActivity;
import com.example.prm392_android_app_frontend.presentation.fragment.admin.AddAndUpdateProductFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProductManageAdapter extends RecyclerView.Adapter<ProductManageAdapter.ProductViewHolder> {

    private List<ProductDto> productList = new ArrayList<>();
    
    // Interface cho callback
    public interface OnEditClickListener {
        void onEditClick(ProductDto product);
    }
    
    public interface OnDeleteClickListener {
        void onDeleteClick(int productId, String productName);
    }
    
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public void setProducts(List<ProductDto> products) {
        this.productList = products;
        notifyDataSetChanged();
    }
    
    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }
    
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_product, parent, false);
        return new ProductViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(productList.get(position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName, textViewProductPrice, textViewProductQuantity;
        Button buttonViewDetails, buttonEditProduct, buttonDeleteProduct;
        ProductManageAdapter adapter;

        public ProductViewHolder(@NonNull View itemView, ProductManageAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            imageViewProduct = itemView.findViewById(R.id.imageView_product);
            textViewProductName = itemView.findViewById(R.id.textView_productName);
            textViewProductPrice = itemView.findViewById(R.id.textView_productPrice);
            textViewProductQuantity = itemView.findViewById(R.id.textView_productQuantity);
            // Khởi tạo các nút bấm
            buttonViewDetails = itemView.findViewById(R.id.button_view_details);
            buttonEditProduct = itemView.findViewById(R.id.button_edit_product);
            buttonDeleteProduct = itemView.findViewById(R.id.button_delete_product);
        }

        void bind(ProductDto product) {
            textViewProductName.setText(product.getName());
            textViewProductPrice.setText(String.format("$%.2f", product.getPrice()));
            textViewProductQuantity.setText(String.valueOf(product.getQuantity()));
            
            Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_upload_image)
                    .error(R.drawable.ic_upload_image)
                    .into(imageViewProduct);

            // Gán sự kiện click cho các nút bấm
            buttonViewDetails.setOnClickListener(v -> {
                Context context = itemView.getContext();
                Intent intent = new Intent(context, AdminProductActivity.class);
                intent.putExtra(AdminProductActivity.EXTRA_PRODUCT_ID, product.getId());

                context.startActivity(intent);
            });

            // Nút Edit - chỉ hiển thị trong admin mode
            if (buttonEditProduct != null) {
                buttonEditProduct.setOnClickListener(v -> {
                    Context context = itemView.getContext();

                    // Nếu adapter này nằm trong Fragment
                    if (context instanceof androidx.fragment.app.FragmentActivity) {
                        androidx.fragment.app.FragmentActivity activity =
                                (androidx.fragment.app.FragmentActivity) context;

                        AddAndUpdateProductFragment editDialog = new AddAndUpdateProductFragment();

                        Bundle args = new Bundle();
                        args.putSerializable("product_to_edit", (Serializable) product);
                        editDialog.setArguments(args);

                        editDialog.show(activity.getSupportFragmentManager(), "edit_product");
                    }

                });
            }

            // Nút Delete - chỉ hiển thị trong admin mode
            if (buttonDeleteProduct != null) {
                buttonDeleteProduct.setOnClickListener(v -> {
                    // Gọi callback để xác nhận xóa
                    if (adapter != null && adapter.onDeleteClickListener != null) {
                        adapter.onDeleteClickListener.onDeleteClick(product.getId(), product.getName());
                    }
                });
            }
        }
    }
}
