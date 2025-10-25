package com.example.prm392_android_app_frontend.presentation.adapter;

import android.app.AlertDialog;
import android.content.Context; // Import mới
import android.content.Intent;   // Import mới
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.ProductDto;
import com.example.prm392_android_app_frontend.presentation.activity.LoginActivity;
import com.example.prm392_android_app_frontend.presentation.activity.MainActivity;
import com.example.prm392_android_app_frontend.presentation.activity.ProductDetailActivity; // Import mới
import com.example.prm392_android_app_frontend.presentation.component.NavbarManager;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.example.prm392_android_app_frontend.utils.PriceFormatter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductDto> productList = new ArrayList<>();
    private static OnAddToCartClickListener onAddToCartClickListener;

    // Interface để xử lý sự kiện thêm vào giỏ hàng
    public interface OnAddToCartClickListener {
        void onAddToCart(int productId, int quantity);
    }

    // Setter cho listener
    public void setOnAddToCartClickListener(OnAddToCartClickListener listener) {
        this.onAddToCartClickListener = listener;
    }

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
        TextView textViewProductName, textViewArtistName, textViewProductPrice, textViewProductCategory, textViewStock;
        MaterialButton buttonViewDetails, buttonAddToCart;
        android.widget.ImageButton buttonFavorite;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageView_product);
            textViewProductName = itemView.findViewById(R.id.textView_productName);
            textViewArtistName = itemView.findViewById(R.id.textView_artistName);
            textViewProductPrice = itemView.findViewById(R.id.textView_productPrice);
            textViewProductCategory = itemView.findViewById(R.id.textView_productCategory);
            textViewStock = itemView.findViewById(R.id.textView_stock);
            
            // Buttons
            buttonViewDetails = itemView.findViewById(R.id.button_view_details);
            buttonAddToCart = itemView.findViewById(R.id.button_addToCart);
            buttonFavorite = itemView.findViewById(R.id.button_favorite);
        }

        void bind(ProductDto product) {
            textViewProductName.setText(product.getName());
            textViewArtistName.setText(product.getArtistName() != null ? product.getArtistName() : "Không có nghệ sĩ");
            textViewProductCategory.setText(product.getCategoryName() != null ? product.getCategoryName() : "Không có thể loại");
            textViewStock.setText(product.getQuantity() > 0 ? "Còn hàng" : "Hết hàng");
            
            // Sử dụng PriceFormatter
            textViewProductPrice.setText(PriceFormatter.formatPrice(product.getPrice()));

            Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher_round)
                    .into(imageViewProduct);

            // 3. Gán sự kiện click cho nút bấm chi tiết
            buttonViewDetails.setOnClickListener(v -> {
                Context context = itemView.getContext();
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
                context.startActivity(intent);
            });

            buttonAddToCart.setOnClickListener(v -> {
                Context context = itemView.getContext();
                
                // Kiểm tra đăng nhập trước khi thêm vào giỏ hàng
                if (!TokenStore.isLoggedIn(context)) {
                    // Hiển thị dialog yêu cầu đăng nhập
                    new AlertDialog.Builder(context)
                            .setTitle("Yêu cầu đăng nhập")
                            .setMessage("Bạn cần đăng nhập để sử dụng tính năng này.")
                            .setPositiveButton("ĐĂNG NHẬP", (dialog, which) -> {
                                Intent loginIntent = new Intent(context, LoginActivity.class);
                                context.startActivity(loginIntent);
                            })
                            .setNegativeButton("HỦY", null)
                            .setCancelable(false)
                            .show();
                    return;
                }
                
                // Kiểm tra số lượng sản phẩm còn hàng
                if (product.getQuantity() <= 0) {
                    android.widget.Toast.makeText(context, 
                        "Sản phẩm đã hết hàng", 
                        android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Nếu đã đăng nhập và còn hàng, gọi callback để thêm vào giỏ
                if (onAddToCartClickListener != null) {
                    onAddToCartClickListener.onAddToCart(product.getId(), 1);
                } else {
                    // Fallback: Chỉ hiện thông báo nếu không có listener
                    android.widget.Toast.makeText(context, 
                        "Đã thêm " + product.getName() + " vào giỏ hàng", 
                        android.widget.Toast.LENGTH_SHORT).show();
                }
            });

            buttonFavorite.setOnClickListener(v -> {
                // TODO: Implement favorite functionality
                android.widget.Toast.makeText(itemView.getContext(), 
                    "Đã thêm vào yêu thích", 
                    android.widget.Toast.LENGTH_SHORT).show();
            });
        }
    }
}
