package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffOrderAdapter extends RecyclerView.Adapter<StaffOrderAdapter.StaffOrderViewHolder> {

    public interface Listener {
        void onViewDetail(@NonNull OrderDto order);
        void onUpdateStatus(@NonNull OrderDto order);
    }

    private final List<OrderDto> orders = new ArrayList<>();
    private final Listener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat inputDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
    private final SimpleDateFormat fallbackInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat displayDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public StaffOrderAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void submit(List<OrderDto> data) {
        orders.clear();
        if (data != null) {
            orders.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StaffOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_order, parent, false);
        return new StaffOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffOrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class StaffOrderViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtOrderId;
        private final TextView txtOrderDate;
        private final TextView txtCustomer;
        private final TextView txtStore;
        private final TextView txtTotal;
        private final Chip chipStatus;
        private final MaterialButton btnViewDetail;
        private final MaterialButton btnUpdateStatus;

        StaffOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtOrderDate = itemView.findViewById(R.id.txtOrderDate);
            txtCustomer = itemView.findViewById(R.id.txtCustomer);
            txtStore = itemView.findViewById(R.id.txtStore);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            chipStatus = itemView.findViewById(R.id.chipOrderStatus);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }

        void bind(OrderDto order) {
            txtOrderId.setText(itemView.getContext().getString(R.string.staff_order_id_format, order.getId()));
            txtOrderDate.setText(itemView.getContext().getString(R.string.staff_order_date_format, formatDate(order.getOrderDate())));
            txtCustomer.setText(itemView.getContext().getString(R.string.staff_order_customer_format, safe(order.getShippingFullName())));
            String storeLabel = order.getStoreName() != null ? order.getStoreName() : itemView.getContext().getString(R.string.staff_order_store_unknown);
            txtStore.setText(itemView.getContext().getString(R.string.staff_order_store_format, storeLabel));
            double total = order.getGrandTotal() != null ? order.getGrandTotal() : 0d;
            txtTotal.setText(itemView.getContext().getString(R.string.staff_order_total_format, currencyFormat.format(total)));

            String status = order.getOrderStatus() != null ? order.getOrderStatus() : "UNKNOWN";
            chipStatus.setText(mapStatusLabel(status));

            btnViewDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetail(order);
                }
            });
            btnUpdateStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpdateStatus(order);
                }
            });
        }

        private String safe(String value) {
            return value != null ? value : itemView.getContext().getString(R.string.staff_order_unknown_field);
        }

        private String formatDate(String value) {
            if (value == null || value.isEmpty()) {
                return itemView.getContext().getString(R.string.staff_order_unknown_field);
            }
            try {
                Date parsed = inputDateTime.parse(value);
                if (parsed != null) {
                    return displayDateTime.format(parsed);
                }
            } catch (ParseException ignore) {
                try {
                    Date fallback = fallbackInput.parse(value);
                    if (fallback != null) {
                        return displayDateTime.format(fallback);
                    }
                } catch (ParseException ignored) {
                    // ignore
                }
            }
            return value;
        }

        private String mapStatusLabel(String status) {
            switch (status.toUpperCase(Locale.ROOT)) {
                case "KEEPING":
                    return itemView.getContext().getString(R.string.staff_order_status_keeping);
                case "PENDING":
                    return itemView.getContext().getString(R.string.staff_order_status_pending);
                case "PROCESSING":
                    return itemView.getContext().getString(R.string.staff_order_status_processing);
                case "SHIPPED":
                    return itemView.getContext().getString(R.string.staff_order_status_shipped);
                case "COMPLETED":
                    return itemView.getContext().getString(R.string.staff_order_status_completed);
                case "CANCELLED":
                    return itemView.getContext().getString(R.string.staff_order_status_cancelled);
                default:
                    return status;
            }
        }
    }
}
