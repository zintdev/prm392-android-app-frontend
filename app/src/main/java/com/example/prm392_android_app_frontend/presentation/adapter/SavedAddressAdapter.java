package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.address.AddressDto;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.List;

public class SavedAddressAdapter extends RecyclerView.Adapter<SavedAddressAdapter.AddressViewHolder> {

    private List<AddressDto> addresses;
    private int selectedPosition = -1;
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onAddressSelected(AddressDto address, int position);
    }

    public SavedAddressAdapter(List<AddressDto> addresses, OnAddressSelectedListener listener) {
        this.addresses = addresses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressDto address = addresses.get(position);
        holder.bind(address, position);
    }

    @Override
    public int getItemCount() {
        return addresses != null ? addresses.size() : 0;
    }

    public AddressDto getSelectedAddress() {
        if (selectedPosition >= 0 && selectedPosition < addresses.size()) {
            return addresses.get(selectedPosition);
        }
        return null;
    }

    public void updateAddresses(List<AddressDto> newAddresses) {
        this.addresses = newAddresses;
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {
        private MaterialRadioButton radioSelectAddress;
        private TextView textViewAddressLine1;
        private TextView textViewAddressLine2;
        private TextView textViewCityState;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            radioSelectAddress = itemView.findViewById(R.id.radio_select_address);
            textViewAddressLine1 = itemView.findViewById(R.id.text_view_address_line1);
            textViewAddressLine2 = itemView.findViewById(R.id.text_view_address_line2);
            textViewCityState = itemView.findViewById(R.id.text_view_city_state);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    selectAddress(position);
                }
            });

            radioSelectAddress.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    selectAddress(position);
                }
            });
        }

        private void selectAddress(int position) {
            int previousSelected = selectedPosition;
            selectedPosition = position;
            
            // Update UI
            if (previousSelected != -1) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);

            // Notify listener
            if (listener != null) {
                listener.onAddressSelected(addresses.get(position), position);
            }
        }

        public void bind(AddressDto address, int position) {
            // Set address data
            textViewAddressLine1.setText(address.shippingAddressLine1);
            
            if (address.shippingAddressLine2 != null && !address.shippingAddressLine2.trim().isEmpty()) {
                textViewAddressLine2.setText(address.shippingAddressLine2);
                textViewAddressLine2.setVisibility(View.VISIBLE);
            } else {
                textViewAddressLine2.setVisibility(View.GONE);
            }
            
            textViewCityState.setText(address.shippingCityState);

            // Set radio button state
            radioSelectAddress.setChecked(position == selectedPosition);
        }
    }
}