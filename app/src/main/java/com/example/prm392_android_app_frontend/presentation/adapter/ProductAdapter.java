package com.example.prm392_android_app_frontend.presentation.adapter;

import android.content.Context; // Import mới
import android.content.Intent;   // Import mới
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;    // Import mới
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.presentation.activity.ProductDetailActivity; // Import mới
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductDto> productList = new ArrayList<>();

    public void setProducts(List<ProductDto> products) {
        this.productList = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
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
        TextView textViewProductName, textViewProductPrice;
        Button buttonViewDetails; // 1. Khai báo nút bấm

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageView_product);
            textViewProductName = itemView.findViewById(R.id.textView_productName);
            textViewProductPrice = itemView.findViewById(R.id.textView_productPrice);

            // 2. Tìm nút bấm bằng ID từ layout item_product.xml
            buttonViewDetails = itemView.findViewById(R.id.button_view_details);
        }

        void bind(ProductDto product) {
            textViewProductName.setText(product.getName());
            textViewProductPrice.setText(String.format("$%.2f", product.getPrice()));

            Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher_round)
                    .into(imageViewProduct);

            // 3. Gán sự kiện click cho nút bấm
            buttonViewDetails.setOnClickListener(v -> {
                Context context = itemView.getContext();
                Intent intent = new Intent(context, ProductDetailActivity.class);

                // Gửi ID của sản phẩm được click qua cho ProductDetailActivity
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());

                context.startActivity(intent);
            });
        }
    }
}
