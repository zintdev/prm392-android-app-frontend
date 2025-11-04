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
import com.example.prm392_android_app_frontend.data.dto.OrderItemDTO;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<CartItemDto> cartItems;
    private List<OrderItemDTO> orderItems;
    private boolean isCartMode = true;

    // Constructor for cart items
    public OrderItemAdapter(List<CartItemDto> cartItems) {
        this.cartItems = cartItems;
        this.isCartMode = true;
    }

    // Default constructor for order items
    public OrderItemAdapter() {
        this.orderItems = new ArrayList<>();
        this.isCartMode = false;
    }

    // Method to set order items
    public void setItems(List<OrderItemDTO> items) {
        this.orderItems = items != null ? items : new ArrayList<>();
        this.isCartMode = false;
        notifyDataSetChanged();
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
        if (isCartMode) {
            CartItemDto item = cartItems.get(position);
            holder.bindCartItem(item);
        } else {
            OrderItemDTO item = orderItems.get(position);
            holder.bindOrderItem(item);
        }
    }

    @Override
    public int getItemCount() {
        if (isCartMode) {
            return cartItems != null ? cartItems.size() : 0;
        } else {
            return orderItems != null ? orderItems.size() : 0;
        }
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

        public void bindCartItem(CartItemDto item) {
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

        public void bindOrderItem(OrderItemDTO item) {
            // Set product name
            textViewOrderItemName.setText(item.getProductName());

            // Set product image
            if (item.getProductImageUrl() != null && !item.getProductImageUrl().isEmpty()) {
                Glide.with(imageViewOrderItem.getContext())
                        .load(item.getProductImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(imageViewOrderItem);
            } else {
                imageViewOrderItem.setImageResource(R.drawable.ic_placeholder);
            }

            // Format prices
            DecimalFormat formatter = new DecimalFormat("###,###,###");

            // Set unit price - dùng unitPrice từ API
            String unitPriceText = formatter.format(item.getUnitPrice()) + "đ";
            textViewOrderItemPrice.setText(unitPriceText);

            // Set quantity
            textViewOrderItemQuantity.setText("Số lượng: " + item.getQuantity());

            // Set total price - tính từ unitPrice * quantity
            double total = item.getUnitPrice() * item.getQuantity();
            String totalPriceText = "Tổng: " + formatter.format(total) + "đ";
            textViewOrderItemTotal.setText(totalPriceText);
        }
    }
}