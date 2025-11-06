package com.example.prm392_android_app_frontend.presentation.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.store.StoreInventoryChange;
import com.example.prm392_android_app_frontend.data.dto.store.StoreInventoryItemDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StoreInventoryAdapter extends RecyclerView.Adapter<StoreInventoryAdapter.InventoryViewHolder> {

    public interface OnQuantityUpdateListener {
        void onUpdate(StoreInventoryItemDto item, int newQuantity);
    }

    private final List<Row> items = new ArrayList<>();
    private final OnQuantityUpdateListener listener;
    private boolean updating;

    public StoreInventoryAdapter(OnQuantityUpdateListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store_inventory_product, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submit(List<StoreInventoryItemDto> data) {
        Map<Integer, Row> existing = new HashMap<>();
        for (Row row : items) {
            if (row.dto != null && row.dto.getProductId() != null) {
                existing.put(row.dto.getProductId(), row);
            }
        }
        items.clear();
        if (data != null) {
            for (StoreInventoryItemDto dto : data) {
                Integer productId = dto != null ? dto.getProductId() : null;
                Row row = productId != null ? existing.get(productId) : null;
                if (row == null) {
                    row = new Row();
                    row.dto = dto;
                    row.originalQuantity = safeQuantity(dto);
                    row.maxAllowed = safeAvailable(dto, row.originalQuantity);
                    row.quantity = row.originalQuantity;
                    row.dirty = false;
                } else {
                    row.dto = dto;
                    row.originalQuantity = safeQuantity(dto);
                    row.maxAllowed = safeAvailable(dto, row.originalQuantity);
                    if (!row.dirty) {
                        row.quantity = row.originalQuantity;
                    }
                    if (row.quantity > row.maxAllowed) {
                        row.quantity = row.maxAllowed;
                    }
                    if (row.quantity == row.originalQuantity) {
                        row.dirty = false;
                    }
                }
                items.add(row);
            }
        }
        notifyDataSetChanged();
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
        notifyDataSetChanged();
    }

    public void markSynced(StoreInventoryItemDto updatedDto) {
        if (updatedDto == null || updatedDto.getProductId() == null) {
            return;
        }
        int productId = updatedDto.getProductId();
        for (int i = 0; i < items.size(); i++) {
            Row row = items.get(i);
            if (row.dto != null && row.dto.getProductId() != null
                    && Objects.equals(row.dto.getProductId(), productId)) {
                row.dto = updatedDto;
                row.originalQuantity = safeQuantity(updatedDto);
                row.maxAllowed = safeAvailable(updatedDto, row.originalQuantity);
                row.quantity = row.originalQuantity;
                row.dirty = false;
                notifyItemChanged(i);
                return;
            }
        }
    }

    public List<StoreInventoryChange> getDirtyItems() {
        List<StoreInventoryChange> dirty = new ArrayList<>();
        for (Row row : items) {
            if (row.dto != null && row.dto.getProductId() != null && row.dirty) {
                dirty.add(new StoreInventoryChange(
                        row.dto.getProductId(),
                        row.quantity,
                        row.dto.getProductName()));
            }
        }
        return dirty;
    }

    private int safeQuantity(StoreInventoryItemDto dto) {
        Integer q = dto != null ? dto.getQuantity() : null;
        return q != null && q >= 0 ? q : 0;
    }

    private int safeAvailable(StoreInventoryItemDto dto, int currentQuantity) {
        Integer max = dto != null ? dto.getAvailableForStore() : null;
        if (max == null) {
            return Integer.MAX_VALUE;
        }
        if (max < currentQuantity) {
            return currentQuantity;
        }
        return max;
    }

    class InventoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgProduct;
        private final TextView txtName;
        private final EditText txtQuantity;
        private final TextView txtLabel;
        private final ImageButton btnDecrease;
        private final ImageButton btnIncrease;
        private final com.google.android.material.button.MaterialButton btnUpdate;
        private TextWatcher quantityWatcher;
        private Toast infoToast;

        InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtProductName);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtLabel = itemView.findViewById(R.id.txtQuantityLabel);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
        }

        void bind(Row row) {
            if (infoToast != null) {
                infoToast.cancel();
                infoToast = null;
            }
            String imageUrl = row.dto != null ? row.dto.getProductImageUrl() : null;
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(imgProduct);

            String name = row.dto != null ? row.dto.getProductName() : "";
            txtName.setText(name != null ? name : "");
            // show the latest typed value if dirty, otherwise fall back to the stored quantity
            int displayQuantity = row.dirty ? row.quantity : row.originalQuantity;
            setQuantityText(String.valueOf(displayQuantity), false);

            StringBuilder labelBuilder = new StringBuilder(itemView.getContext().getString(
                    R.string.inventory_current_quantity,
                    row.originalQuantity));
            if (row.maxAllowed != Integer.MAX_VALUE) {
                labelBuilder.append(itemView.getContext().getString(
                        R.string.inventory_limit_suffix,
                        row.maxAllowed));
            }
            txtLabel.setText(labelBuilder.toString());

            boolean canInteract = !updating;
            btnDecrease.setEnabled(canInteract);
            btnIncrease.setEnabled(canInteract);
            updateButtonState(row, canInteract);

            // enable/disable input while updating
            txtQuantity.setEnabled(!updating);

            btnDecrease.setOnClickListener(v -> {
                if (updating) {
                    return;
                }
                if (row.quantity > 0) {
                    row.quantity -= 1;
                }
                // ensure we never go below zero or over the limit
                enforceLimit(row, false);
                updateDirtyState(row);
                setQuantityText(String.valueOf(row.quantity), true);
                updateButtonState(row, !updating);
            });

            btnIncrease.setOnClickListener(v -> {
                if (updating) {
                    return;
                }
                if (row.maxAllowed != Integer.MAX_VALUE && row.quantity >= row.maxAllowed) {
                    showLimitMessage(row);
                    setQuantityText(String.valueOf(row.quantity), true);
                    updateButtonState(row, !updating);
                    return;
                }
                row.quantity += 1;
                enforceLimit(row, true);
                setQuantityText(String.valueOf(row.quantity), true);
                updateDirtyState(row);
                updateButtonState(row, !updating);
            });

            // handle manual input changes
            if (quantityWatcher != null) {
                txtQuantity.removeTextChangedListener(quantityWatcher);
            }
            quantityWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (updating) return;
                    String val = s != null ? s.toString().trim() : "";
                    int q = 0;
                    try {
                        q = val.isEmpty() ? 0 : Integer.parseInt(val);
                    } catch (NumberFormatException ex) {
                        q = 0;
                    }
                    if (q < 0) q = 0;
                    row.quantity = q;
                    if (enforceLimit(row, true)) {
                        setQuantityText(String.valueOf(row.quantity), true);
                    }
                    updateDirtyState(row);
                    updateButtonState(row, !updating);
                }

                @Override
                public void afterTextChanged(Editable s) { }
            };
            txtQuantity.addTextChangedListener(quantityWatcher);

            txtQuantity.setOnEditorActionListener((v, actionId, event) -> {
                boolean isDone = actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_SEND
                        || (actionId == EditorInfo.IME_NULL
                        && event != null
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
                if (!isDone) {
                    return false;
                }
                triggerUpdateIfNeeded(row);
                return true;
            });

            btnUpdate.setOnClickListener(v -> {
                if (updating) {
                    return;
                }
                if (enforceLimit(row, true)) {
                    setQuantityText(String.valueOf(row.quantity), true);
                    updateDirtyState(row);
                }
                triggerUpdateIfNeeded(row);
                updateButtonState(row, !updating);
            });
        }

        private void triggerUpdateIfNeeded(Row row) {
            if (listener == null || row.dto == null || row.dto.getProductId() == null) {
                return;
            }
            if (!row.dirty) {
                Toast.makeText(itemView.getContext(), "Chưa có thay đổi số lượng", Toast.LENGTH_SHORT).show();
                return;
            }
            listener.onUpdate(row.dto, row.quantity);
        }

        private void setQuantityText(String text, boolean reattachWatcher) {
            // avoid triggering watcher when programmatically setting text
            if (quantityWatcher != null) {
                txtQuantity.removeTextChangedListener(quantityWatcher);
            }
            txtQuantity.setText(text != null ? text : "0");
            // move cursor to end
            try {
                txtQuantity.setSelection(txtQuantity.getText().length());
            } catch (Exception ignored) { }
            if (reattachWatcher && quantityWatcher != null) {
                txtQuantity.addTextChangedListener(quantityWatcher);
            }
        }

        private void updateDirtyState(Row row) {
            if (row.quantity < 0) {
                row.quantity = 0;
            }
            row.dirty = row.quantity != row.originalQuantity;
        }

        private void updateButtonState(Row row, boolean canInteract) {
            boolean enabled = canInteract && row.dirty && !updating;
            btnUpdate.setEnabled(enabled);
            btnUpdate.setAlpha(enabled ? 1f : 0.5f);
        }

        private boolean enforceLimit(Row row, boolean notify) {
            if (row.maxAllowed == Integer.MAX_VALUE) {
                return false;
            }
            if (row.quantity <= row.maxAllowed) {
                return false;
            }
            row.quantity = row.maxAllowed;
            if (notify) {
                showLimitMessage(row);
            }
            return true;
        }

        private void showLimitMessage(Row row) {
            if (row.maxAllowed == Integer.MAX_VALUE) {
                return;
            }
            String message;
            if (row.originalQuantity >= row.maxAllowed) {
                message = itemView.getContext().getString(
                        R.string.inventory_limit_cap,
                        row.maxAllowed);
            } else {
                message = itemView.getContext().getString(
                        R.string.inventory_not_enough,
                        row.maxAllowed);
            }
            showInfoToast(message);
        }

        private void showInfoToast(String message) {
            if (message == null || message.isEmpty()) {
                return;
            }
            if (infoToast != null) {
                infoToast.cancel();
            }
            infoToast = Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT);
            infoToast.show();
        }
    }

    private static class Row {
        StoreInventoryItemDto dto;
        int originalQuantity;
        int quantity;
        boolean dirty;
        int maxAllowed;
    }
}
