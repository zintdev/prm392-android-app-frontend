package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.CartItemDto;

import java.text.DecimalFormat;

public class CartAdapter extends ListAdapter<CartItemDto, CartAdapter.CartViewHolder> {

    private final OnCartItemActionListener listener;
    private boolean selectAll = false;
    private final java.util.Set<Integer> checkedItems = new java.util.HashSet<>();

    public interface OnCartItemActionListener {
        void onIncreaseQuantity(CartItemDto item);
        void onDecreaseQuantity(CartItemDto item);
        void onRemoveItem(CartItemDto item);
        void onItemCheckedChanged(CartItemDto item, boolean isChecked);
    }

    @Override
    public void submitList(java.util.List<CartItemDto> list) {
        if (list != null) {
            // Nếu đang ở trạng thái select all, tự động check tất cả items mới
            if (selectAll) {
                for (CartItemDto item : list) {
                    checkedItems.add(item.getCartItemId());
                    item.setSelected(true);
                }
            } else {
                // Nếu không, chỉ check những items đã được check trước đó
                for (CartItemDto item : list) {
                    if (checkedItems.contains(item.getCartItemId())) {
                        item.setSelected(true);
                    }
                }
            }
        }
        super.submitList(list);
        // Trigger lại việc tính toán tổng tiền
        if (listener != null) {
            listener.onItemCheckedChanged(null, true);
        }
    }

    public boolean isItemChecked(int cartItemId) {
        return checkedItems.contains(cartItemId);
    }

    public java.util.List<CartItemDto> getSelectedItems() {
        java.util.List<CartItemDto> selectedItems = new java.util.ArrayList<>();
        for (CartItemDto item : getCurrentList()) {
            if (checkedItems.contains(item.getCartItemId())) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }
    
    public double getCheckedItemsTotal() {
        double total = 0;
        for (CartItemDto item : getCurrentList()) {
            if (checkedItems.contains(item.getCartItemId())) {
                total += item.getUnitPrice() * item.getQuantity();
            }
        }
        return total;
    }

    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
        checkedItems.clear();
        if (selectAll) {
            for (CartItemDto item : getCurrentList()) {
                checkedItems.add(item.getCartItemId());
            }
        }
        notifyDataSetChanged();
        listener.onItemCheckedChanged(null, selectAll); // Trigger update total
    }

    public CartAdapter(@NonNull OnCartItemActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CartItemDto> DIFF_CALLBACK = new DiffUtil.ItemCallback<CartItemDto>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartItemDto oldItem, @NonNull CartItemDto newItem) {
            return oldItem.getProductId() == newItem.getProductId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartItemDto oldItem, @NonNull CartItemDto newItem) {
            return oldItem.getQuantity() == newItem.getQuantity() && oldItem.getUnitPrice() == newItem.getUnitPrice();
        }
    };

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItemDto currentItem = getItem(position);
        holder.bind(currentItem, listener);
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textViewName;
        private final TextView textViewPrice;
        private final TextView textViewQuantity;
        private final ImageButton buttonIncrease;
        private final ImageButton buttonDecrease;
        private final ImageButton buttonRemove;
        private final android.widget.CheckBox checkBox;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_cart_item);
            textViewName = itemView.findViewById(R.id.text_view_cart_item_name);
            textViewPrice = itemView.findViewById(R.id.text_view_cart_item_price);
            textViewQuantity = itemView.findViewById(R.id.text_view_cart_item_quantity);
            buttonIncrease = itemView.findViewById(R.id.button_increase_quantity);
            buttonDecrease = itemView.findViewById(R.id.button_decrease_quantity);
            buttonRemove = itemView.findViewById(R.id.button_remove_from_cart);
            checkBox = itemView.findViewById(R.id.checkbox_cart_item);
        }

        public void bind(final CartItemDto item, final OnCartItemActionListener listener) {
            textViewName.setText(item.getProductName());
            textViewQuantity.setText(String.valueOf(item.getQuantity()));

            DecimalFormat formatter = new DecimalFormat("###,###,###");
            // SỬA Ở ĐÂY: Dùng getUnitPrice() thay vì getPrice()
            textViewPrice.setText(formatter.format(item.getUnitPrice()) + "đ");

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(imageView);

            buttonIncrease.setOnClickListener(v -> {
                listener.onIncreaseQuantity(item);
                // Nếu item đang được chọn, cập nhật lại tổng tiền
                if (((CartAdapter) getBindingAdapter()).checkedItems.contains(item.getCartItemId())) {
                    listener.onItemCheckedChanged(item, true);
                }
            });

            buttonDecrease.setOnClickListener(v -> {
                listener.onDecreaseQuantity(item);
                // Nếu item đang được chọn, cập nhật lại tổng tiền
                if (((CartAdapter) getBindingAdapter()).checkedItems.contains(item.getCartItemId())) {
                    listener.onItemCheckedChanged(item, true);
                }
            });
            
            buttonRemove.setOnClickListener(v -> listener.onRemoveItem(item));
            
            checkBox.setOnCheckedChangeListener(null); // Remove old listener
            boolean isChecked = ((CartAdapter) getBindingAdapter()).checkedItems.contains(item.getCartItemId());
            checkBox.setChecked(isChecked);
            item.setSelected(isChecked); // Cập nhật trạng thái selected của item
            
            checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
                CartAdapter adapter = (CartAdapter) getBindingAdapter();
                if (checked) {
                    adapter.checkedItems.add(item.getCartItemId());
                } else {
                    adapter.checkedItems.remove(item.getCartItemId());
                }
                item.setSelected(checked); // Cập nhật trạng thái selected của item
                listener.onItemCheckedChanged(item, checked);
            });

            buttonDecrease.setEnabled(item.getQuantity() > 1);
        }
    }
}
