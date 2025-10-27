package com.example.prm392_android_app_frontend.presentation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.address.AddressDto;


import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<AddressDto> list;
    private Context context;

    public AddressAdapter(List<AddressDto> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setAddresses(List<AddressDto> addresses) {
        this.list = addresses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder h, int pos) {
        AddressDto a = list.get(pos);
        h.tvRecipient.setText("Người nhận ");
        h.tvAddressLine.setText(a.shippingAddressLine1 + ", " + a.shippingAddressLine2);
        h.tvWardDistrictCity.setText(a.shippingCityState);

        h.btnEdit.setOnClickListener(v ->
                Toast.makeText(context, "Sửa địa chỉ #" + a.id, Toast.LENGTH_SHORT).show());

        h.btnDelete.setOnClickListener(v ->
                Toast.makeText(context, "Xoá địa chỉ #" + a.id, Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecipient, tvAddressLine, tvWardDistrictCity;
        ImageButton btnEdit, btnDelete;

        public AddressViewHolder(@NonNull View v) {
            super(v);
            tvRecipient = v.findViewById(R.id.tvRecipient);
            tvAddressLine = v.findViewById(R.id.tvAddressLine);
            tvWardDistrictCity = v.findViewById(R.id.tvWardDistrictCity);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
