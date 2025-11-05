package com.example.prm392_android_app_frontend.presentation.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.store.StoreNearbyDto;
import com.example.prm392_android_app_frontend.data.remote.api.StoreApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.VH> {

    public interface OnItemClick { void onClick(StoreNearbyDto item); }

    private final List<StoreNearbyDto> items = new ArrayList<>();
    private final OnItemClick listener;

    public StoreAdapter(OnItemClick listener) { this.listener = listener; }

    public void submit(List<StoreNearbyDto> data){
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_row, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        StoreNearbyDto it = items.get(position);
        // Chuẩn hoá và trim các trường trước khi so sánh
        String name = it.name != null ? it.name.trim() : "";
        String address = it.address != null ? it.address.trim() : "";

        // Nếu name rỗng thì dùng fallback; nếu name trùng chính xác với address thì cũng fallback
        String displayName;
        if (name.isEmpty()) {
            displayName = "Cửa hàng " + it.storeId;
        } else if (!address.isEmpty() && name.equals(address)) {
            // Trường hợp backend trả name trùng với address -> tránh lặp
            displayName = "Cửa hàng " + it.storeId;
        } else {
            displayName = name;
        }

        h.txtName.setText(displayName);
        // Luôn hiển thị địa chỉ thật (có thể rỗng)
        h.txtAddress.setText(address);
        h.txtDistance.setText(String.format(Locale.getDefault(), "%.2f km", it.distanceKm));
        if (it.quantity != null) {
            h.txtQuantity.setVisibility(View.VISIBLE);
            h.txtQuantity.setText("Còn: " + it.quantity);
        } else {
            h.txtQuantity.setVisibility(View.GONE);
        }
        h.itemView.setOnClickListener(v -> listener.onClick(it));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtName, txtAddress, txtDistance, txtQuantity;
        VH(@NonNull View v) {
            super(v);
            txtName = v.findViewById(R.id.txtName);
            txtAddress = v.findViewById(R.id.txtAddress);
            txtDistance = v.findViewById(R.id.txtDistance);
            txtQuantity = v.findViewById(R.id.txtQuantity);
        }
    }
}
