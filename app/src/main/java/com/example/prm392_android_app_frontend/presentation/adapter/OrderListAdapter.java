package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.OrderDTO;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderViewHolder> {

    private List<OrderDTO> orders = new ArrayList<>();
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(OrderDTO order);
    }

    public OrderListAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderDTO order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<OrderDTO> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewOrderId;
        private TextView textViewDate;
        private TextView textViewItemCount;
        private TextView textViewShippingAddress;
        private TextView textViewTotalAmount;
        private Chip chipStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.text_view_order_id);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewItemCount = itemView.findViewById(R.id.text_view_item_count);
            textViewShippingAddress = itemView.findViewById(R.id.text_view_shipping_address);
            textViewTotalAmount = itemView.findViewById(R.id.text_view_total_amount);
            chipStatus = itemView.findViewById(R.id.chip_status);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onOrderClick(orders.get(position));
                }
            });
        }

        public void bind(OrderDTO order) {
            // Order ID
            textViewOrderId.setText("Đơn hàng #" + order.getId());

            // Ngày đặt hàng
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(order.getCreatedAt());
                if (date != null) {
                    textViewDate.setText(outputFormat.format(date));
                }
            } catch (Exception e) {
                // Fallback: try another format without microseconds
                try {
                    SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    Date date = inputFormat2.parse(order.getCreatedAt());
                    if (date != null) {
                        textViewDate.setText(outputFormat.format(date));
                    }
                } catch (Exception e2) {
                    textViewDate.setText(order.getCreatedAt());
                }
            }

            // Số lượng sản phẩm
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            textViewItemCount.setText(itemCount + " sản phẩm");

            // Địa chỉ giao hàng
            String address = order.getShippingAddressLine1();
            if (order.getShippingCityState() != null && !order.getShippingCityState().isEmpty()) {
                address += ", " + order.getShippingCityState();
            }
            textViewShippingAddress.setText(address);

            // Tổng tiền
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            textViewTotalAmount.setText(formatter.format(order.getTotalAmount()));

            // Trạng thái
            chipStatus.setText(getStatusText(order.getStatus()));
            chipStatus.setChipBackgroundColorResource(getStatusColor(order.getStatus()));
        }

        private String getStatusText(String status) {
            if (status == null) return "Không xác định";
            switch (status.toUpperCase()) {
                case "PENDING":
                    return "Chờ thanh toán";
                case "PAID":
                    return "Đã thanh toán";
                case "PROCESSING":
                    return "Đang xử lý";
                case "SHIPPED":
                    return "Đang giao";
                case "COMPLETED":
                    return "Hoàn thành";
                case "CANCELLED":
                    return "Đã hủy";
                default:
                    return status;
            }
        }

        private int getStatusColor(String status) {
            if (status == null) return R.color.text_secondary_light;
            switch (status.toUpperCase()) {
                case "PENDING":
                    return R.color.warning_light;
                case "PAID":
                    return R.color.success_light;
                case "PROCESSING":
                    return R.color.info_light;
                case "SHIPPED":
                    return R.color.info_light;
                case "COMPLETED":
                    return R.color.success_light;
                case "CANCELLED":
                    return R.color.error_light;
                default:
                    return R.color.text_secondary_light;
            }
        }
    }
}
