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
        void onPreview(StoreNearbyDto item);
        void onConfirm(StoreNearbyDto item);
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

    public boolean containsStore(int storeId) {
        for (StoreNearbyDto dto : items) {
            if (dto.storeId == storeId) {
                return true;
            }
        }
        return false;
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
        View itemView = h.itemView;
        String name = it.name != null ? it.name.trim() : "";
        String address = it.address != null ? it.address.trim() : "";

        boolean duplicateName = !name.isEmpty() && !address.isEmpty() && name.equals(address);
        String displayName = (!name.isEmpty() && !duplicateName)
                ? name
                : itemView.getContext().getString(R.string.pickup_store_fallback_name, it.storeId);

        h.txtName.setText(displayName);
        h.txtAddress.setText(address);
        h.txtDistance.setText(itemView.getContext().getString(R.string.pickup_store_distance_format, it.distanceKm));
        if (it.quantity != null) {
            h.txtQuantity.setVisibility(View.VISIBLE);
            h.txtQuantity.setText(itemView.getContext().getString(R.string.store_card_quantity_format, it.quantity));
        } else {
            h.txtQuantity.setVisibility(View.GONE);
        }
        h.itemView.setOnClickListener(v -> callbacks.onPreview(it));
        h.btnSelect.setOnClickListener(v -> callbacks.onConfirm(it));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtName, txtAddress, txtDistance, txtQuantity;
        com.google.android.material.button.MaterialButton btnSelect;
        VH(@NonNull View v) {
            super(v);
            txtName = v.findViewById(R.id.txtName);
            txtAddress = v.findViewById(R.id.txtAddress);
            txtDistance = v.findViewById(R.id.txtDistance);
            txtQuantity = v.findViewById(R.id.txtQuantity);
            btnSelect = v.findViewById(R.id.btnSelectStore);
        }
    }
}
