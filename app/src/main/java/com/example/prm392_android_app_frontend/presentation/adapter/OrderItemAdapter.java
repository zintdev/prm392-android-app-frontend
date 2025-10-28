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
import com.example.prm392_android_app_frontend.data.dto.CartItemDto;

import java.text.DecimalFormat;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<CartItemDto> orderItems;

    public OrderItemAdapter(List<CartItemDto> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        CartItemDto item = orderItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewOrderItem;
        private TextView textViewOrderItemName;
        private TextView textViewOrderItemPrice;
        private TextView textViewOrderItemQuantity;
        private TextView textViewOrderItemTotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewOrderItem = itemView.findViewById(R.id.image_view_order_item);
            textViewOrderItemName = itemView.findViewById(R.id.text_view_order_item_name);
            textViewOrderItemPrice = itemView.findViewById(R.id.text_view_order_item_price);
            textViewOrderItemQuantity = itemView.findViewById(R.id.text_view_order_item_quantity);
            textViewOrderItemTotal = itemView.findViewById(R.id.text_view_order_item_total);
        }

        public void bind(CartItemDto item) {
            // Set product name
            textViewOrderItemName.setText(item.getProductName());

            // Set product image
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(imageViewOrderItem);
            } else {
                imageViewOrderItem.setImageResource(R.drawable.ic_placeholder);
            }

            // Format prices
            DecimalFormat formatter = new DecimalFormat("###,###,###");

            // Set unit price
            String unitPriceText = formatter.format(item.getUnitPrice()) + "đ";
            textViewOrderItemPrice.setText(unitPriceText);

            // Set quantity
            textViewOrderItemQuantity.setText("Số lượng: " + item.getQuantity());

            // Calculate and set total price for this item
            double totalPrice = item.getUnitPrice() * item.getQuantity();
            String totalPriceText = "Tổng: " + formatter.format(totalPrice) + "đ";
            textViewOrderItemTotal.setText(totalPriceText);
        }
    }
}