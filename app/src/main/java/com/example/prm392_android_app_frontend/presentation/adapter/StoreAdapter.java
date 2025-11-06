package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.store.StoreNearbyDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.VH> {

    public interface Callbacks {
        void onSelect(StoreNearbyDto item);
        void onNavigate(StoreNearbyDto item);
    }

    private final List<StoreNearbyDto> items = new ArrayList<>();
    private final List<StoreNearbyDto> original = new ArrayList<>();
    private final Callbacks callbacks;
    private String currentQuery = "";

    public StoreAdapter(Callbacks callbacks) { this.callbacks = callbacks; }

    public void submit(List<StoreNearbyDto> data){
        original.clear();
        if (data != null) {
            original.addAll(data);
        }
        applyFilter(currentQuery);
    }

    public void filter(String query) {
        applyFilter(query != null ? query : "");
    }

    public int getFilteredCount() {
        return items.size();
    }

    public int getTotalCount() {
        return original.size();
    }

    public List<StoreNearbyDto> getVisibleItems() {
        return new ArrayList<>(items);
    }

    private void applyFilter(String query) {
        currentQuery = query.trim().toLowerCase(Locale.getDefault());
        items.clear();
        if (currentQuery.isEmpty()) {
            items.addAll(original);
        } else {
            for (StoreNearbyDto dto : original) {
                String name = dto.name != null ? dto.name.toLowerCase(Locale.getDefault()) : "";
                String address = dto.address != null ? dto.address.toLowerCase(Locale.getDefault()) : "";
                if (name.contains(currentQuery) || address.contains(currentQuery)) {
                    items.add(dto);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_card, parent, false);
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
        h.itemView.setOnClickListener(v -> callbacks.onSelect(it));
        h.btnGo.setOnClickListener(v -> callbacks.onNavigate(it));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtName, txtAddress, txtDistance, txtQuantity;
        com.google.android.material.button.MaterialButton btnGo;
        VH(@NonNull View v) {
            super(v);
            txtName = v.findViewById(R.id.txtName);
            txtAddress = v.findViewById(R.id.txtAddress);
            txtDistance = v.findViewById(R.id.txtDistance);
            txtQuantity = v.findViewById(R.id.txtQuantity);
            btnGo = v.findViewById(R.id.btnGo);
        }
    }
}
