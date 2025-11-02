package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.address.AddressDto;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    public interface OnAddressActionListener {
        void onEdit(AddressDto address);
        void onDelete(AddressDto address);
    }

    private final List<AddressDto> list = new ArrayList<>();
    private final OnAddressActionListener listener;

    public AddressAdapter(OnAddressActionListener listener) {
        this.listener = listener;
    }

    public void setAddresses(List<AddressDto> addresses) {
        list.clear();
        if (addresses != null) list.addAll(addresses);
        notifyDataSetChanged();
    }

    public void updateItem(AddressDto updated) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == updated.id) {
                list.set(i, updated);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void removeItemById(int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == id) {
                list.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
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

        String recipient = (a.fullName != null && !a.fullName.isEmpty())
                ? a.fullName : "Người nhận";
        String phone = (a.phoneNumber != null && !a.phoneNumber.isEmpty())
                ? " - " + a.phoneNumber : "";
        h.tvRecipient.setText(recipient + phone);//Này chat nó để thế chứ chả biết recipent là j , mạ định ng nhận đi

        String line1 = a.shippingAddressLine1 != null ? a.shippingAddressLine1 : "";
        String line2 = a.shippingAddressLine2 != null && !a.shippingAddressLine2.isEmpty()
                ? (", " + a.shippingAddressLine2) : "";
        h.tvAddressLine.setText(line1 + line2);

        h.tvWardDistrictCity.setText(a.shippingCityState != null ? a.shippingCityState : "");

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(a);
        });
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(a);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
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
