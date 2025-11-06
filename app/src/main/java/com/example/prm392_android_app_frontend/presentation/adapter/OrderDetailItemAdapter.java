package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.order.OrderItemDto;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách sản phẩm trong chi tiết đơn hàng.
 */
public class OrderDetailItemAdapter extends RecyclerView.Adapter<OrderDetailItemAdapter.ItemViewHolder> {

    private final List<OrderItemDto> items = new ArrayList<>();

    public void setItems(@NonNull List<OrderItemDto> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;
        private final TextView txtProductName;
        private final TextView txtQuantity;
        private final TextView txtUnitPrice;
        private final TextView txtLineTotal;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtUnitPrice = itemView.findViewById(R.id.txtUnitPrice);
            txtLineTotal = itemView.findViewById(R.id.txtLineTotal);
        }

        void bind(@NonNull OrderItemDto item) {
            txtProductName.setText(nonNull(item.getProductName()));
            txtQuantity.setText(itemView.getContext().getString(R.string.order_detail_quantity_format, safeQuantity(item.getQuantity())));

            double unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : 0d;
            double total = unitPrice * safeQuantity(item.getQuantity());

            txtUnitPrice.setText(itemView.getContext().getString(R.string.order_detail_unit_price_format, formatCurrency(unitPrice)));
            txtLineTotal.setText(itemView.getContext().getString(R.string.order_detail_total_line_format, formatCurrency(total)));

            String imageUrl = item.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.ic_placeholder);
            }
        }

        private int safeQuantity(Integer value) {
            return value != null ? value : 0;
        }

        private String nonNull(String value) {
            return value != null ? value : itemView.getContext().getString(R.string.staff_order_unknown_field);
        }

        private String formatCurrency(double amount) {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return format.format(amount);
        }
    }
}
