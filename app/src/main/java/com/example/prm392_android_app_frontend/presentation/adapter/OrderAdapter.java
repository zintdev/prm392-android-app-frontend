package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;
import com.google.android.material.button.MaterialButton;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<OrderDto> orders = new ArrayList<>();
    private final OnUpdateClickListener onUpdateClickListener;
    private final OnItemClickListener onItemClickListener;

    public interface OnUpdateClickListener {
        void onUpdateClick(OrderDto order);
    }

    public interface OnItemClickListener {
        void onItemClick(OrderDto order);
    }

    public OrderAdapter(OnUpdateClickListener updateListener, OnItemClickListener itemListener) {
        this.onUpdateClickListener = updateListener;
        this.onItemClickListener = itemListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderDto order = orders.get(position);
        holder.bind(order, onUpdateClickListener, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<OrderDto> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        private final TextView orderIdTextView;
        private final TextView orderStatusTextView;
        private final TextView customerNameTextView;
        private final TextView orderDateTextView;
        private final TextView shipmentMethodTextView;
        private final TextView orderTotalTextView;
        private final MaterialButton buttonUpdateStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            orderStatusTextView = itemView.findViewById(R.id.orderStatusTextView);
            customerNameTextView = itemView.findViewById(R.id.customerNameTextView);
            orderDateTextView = itemView.findViewById(R.id.orderDateTextView);
            shipmentMethodTextView = itemView.findViewById(R.id.shipmentMethodTextView);
            orderTotalTextView = itemView.findViewById(R.id.orderTotalTextView);
            buttonUpdateStatus = itemView.findViewById(R.id.button_update_status);
        }

        public void bind(final OrderDto order, final OnUpdateClickListener updateListener, final OnItemClickListener itemListener) {
            orderIdTextView.setText("Order ID: " + order.getId());
            orderStatusTextView.setText(order.getOrderStatus());
            customerNameTextView.setText("Customer: " + order.getShippingFullName());
            orderDateTextView.setText("Date: " + formatDate(order.getOrderDate()));
            shipmentMethodTextView.setText("Method: " + order.getShipmentMethod());
            orderTotalTextView.setText(String.format("%,.0f VND", order.getGrandTotal()));

            buttonUpdateStatus.setOnClickListener(v -> updateListener.onUpdateClick(order));
            itemView.setOnClickListener(v -> itemListener.onItemClick(order));
        }

        private String formatDate(String dateString) {
            try {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return zonedDateTime.format(formatter);
            } catch (Exception e) {
                return dateString; // Fallback to original string if parsing fails
            }
        }
    }
}
