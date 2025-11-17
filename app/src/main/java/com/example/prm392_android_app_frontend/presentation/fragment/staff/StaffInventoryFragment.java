package com.example.prm392_android_app_frontend.presentation.fragment.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.data.dto.store.StoreInventoryChange;
import com.example.prm392_android_app_frontend.data.dto.store.StoreInventoryItemDto;
import com.example.prm392_android_app_frontend.presentation.adapter.StoreInventoryAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.StaffSharedViewModel;
import com.example.prm392_android_app_frontend.presentation.viewmodel.StoreInventoryViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.button.MaterialButton;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Shows store inventory for the logged-in staff member and allows
 * them to update product quantities for their assigned store only.
 */
public class StaffInventoryFragment extends Fragment {

    private StaffSharedViewModel sharedViewModel;
    private StoreInventoryViewModel inventoryViewModel;
    private TextView txtStoreName;
    private TextView txtStoreAddress;
    private TextView txtStoreHint;
    private SwipeRefreshLayout swipeInventory;
    private RecyclerView recyclerInventory;
    private ProgressBar progressInventory;
    private TextView txtEmpty;
    private MaterialButton btnSaveChanges;
    private View contentContainer;
    private StoreInventoryAdapter inventoryAdapter;

    private Integer storeId;
    private String storeName;
    private String storeAddress;
    private Integer lastLoadedStoreId;
    private boolean loadingState;
    private boolean updatingState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtStoreName = view.findViewById(R.id.txtInventoryStoreName);
        txtStoreAddress = view.findViewById(R.id.txtInventoryStoreAddress);
        txtStoreHint = view.findViewById(R.id.txtInventoryHint);
        swipeInventory = view.findViewById(R.id.swipeInventory);
        recyclerInventory = view.findViewById(R.id.recyclerInventory);
        progressInventory = view.findViewById(R.id.progressInventory);
        txtEmpty = view.findViewById(R.id.txtInventoryEmpty);
        btnSaveChanges = view.findViewById(R.id.btnSaveInventoryChanges);
        contentContainer = view.findViewById(R.id.inventoryContent);

        setupInventoryList();
        setupInventoryActions();

        sharedViewModel = new ViewModelProvider(requireActivity()).get(StaffSharedViewModel.class);
        inventoryViewModel = new ViewModelProvider(this).get(StoreInventoryViewModel.class);

        observeSharedViewModel();
        observeInventoryViewModel();

        if (sharedViewModel.getStaffInfo().getValue() == null) {
            sharedViewModel.loadCurrentStaff();
        }
    }

    private void setupInventoryList() {
        inventoryAdapter = new StoreInventoryAdapter(this::handleSingleUpdate);
        recyclerInventory.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerInventory.setHasFixedSize(true);
        recyclerInventory.setAdapter(inventoryAdapter);
    }

    private void setupInventoryActions() {
        swipeInventory.setOnRefreshListener(() -> {
            if (storeId == null || storeId <= 0) {
                swipeInventory.setRefreshing(false);
                return;
            }
            inventoryViewModel.loadInventory(storeId);
        });

        btnSaveChanges.setOnClickListener(v -> handleSaveAll());
    }

    private void observeSharedViewModel() {
        sharedViewModel.getStaffInfo().observe(getViewLifecycleOwner(), this::renderStaffInfo);
        sharedViewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeInventoryViewModel() {
        inventoryViewModel.getInventory().observe(getViewLifecycleOwner(), this::renderInventory);
        inventoryViewModel.getLoading().observe(getViewLifecycleOwner(), this::onLoadingChanged);
        inventoryViewModel.getUpdating().observe(getViewLifecycleOwner(), this::onUpdatingChanged);
        inventoryViewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        inventoryViewModel.getSuccess().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        inventoryViewModel.getUpdatedItem().observe(getViewLifecycleOwner(), updated -> {
            if (updated != null) {
                inventoryAdapter.markSynced(updated);
                updateSaveButtonState();
            }
        });
    }

    private void renderStaffInfo(@Nullable UserDto userDto) {
        if (userDto == null) {
            txtStoreName.setText(R.string.staff_inventory_store_unknown);
            txtStoreAddress.setText(R.string.staff_inventory_address_unknown);
            storeId = null;
            storeName = null;
            storeAddress = null;
            showNoStoreState();
            return;
        }
        storeId = userDto.storeLocationId;
        storeName = userDto.storeName;
        storeAddress = userDto.storeAddress;
        if (storeAddress == null || storeAddress.isEmpty()) {
            storeAddress = userDto.address;
        }

        if (storeName != null && !storeName.isEmpty()) {
            txtStoreName.setText(storeName);
        } else {
            txtStoreName.setText(R.string.staff_inventory_store_unknown);
        }
        if (storeAddress != null && !storeAddress.isEmpty()) {
            txtStoreAddress.setText(storeAddress);
        } else {
            txtStoreAddress.setText(R.string.staff_inventory_address_unknown);
        }

        boolean hasStore = storeId != null && storeId > 0;
        if (!hasStore) {
            showNoStoreState();
            return;
        }

        showStoreContent();
        if (!Objects.equals(lastLoadedStoreId, storeId)) {
            lastLoadedStoreId = storeId;
            inventoryViewModel.loadInventory(storeId);
        } else {
            updateSaveButtonState();
        }
    }

    private void showNoStoreState() {
        txtStoreHint.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.GONE);
        btnSaveChanges.setVisibility(View.GONE);
        swipeInventory.setEnabled(false);
        swipeInventory.setRefreshing(false);
        progressInventory.setVisibility(View.GONE);
        loadingState = false;
        updatingState = false;
        inventoryAdapter.submit(Collections.emptyList());
        lastLoadedStoreId = null;
        updateSaveButtonState();
    }

    private void showStoreContent() {
        txtStoreHint.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);
        btnSaveChanges.setVisibility(View.VISIBLE);
        swipeInventory.setEnabled(true);
        updateSaveButtonState();
    }

    private void renderInventory(@Nullable List<StoreInventoryItemDto> items) {
        inventoryAdapter.submit(items);
        boolean empty = items == null || items.isEmpty();
        txtEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        updateSaveButtonState();
    }

    private void onLoadingChanged(@Nullable Boolean loading) {
        loadingState = loading != null && loading;
        if (!loadingState) {
            swipeInventory.setRefreshing(false);
        }
        updateProgressState();
    }

    private void onUpdatingChanged(@Nullable Boolean updating) {
        updatingState = updating != null && updating;
        inventoryAdapter.setUpdating(updatingState);
        updateProgressState();
        updateSaveButtonState();
    }

    private void updateProgressState() {
        boolean show = loadingState || updatingState;
        progressInventory.setVisibility(show ? View.VISIBLE : View.GONE);
        if (updatingState) {
            swipeInventory.setRefreshing(false);
        }
        swipeInventory.setEnabled(storeId != null && storeId > 0 && !updatingState);
    }

    private void updateSaveButtonState() {
        if (btnSaveChanges.getVisibility() != View.VISIBLE) {
            return;
        }
        boolean enabled = storeId != null && storeId > 0 && !updatingState;
        btnSaveChanges.setEnabled(enabled);
        btnSaveChanges.setAlpha(enabled ? 1f : 0.6f);
    }

    private void handleSingleUpdate(@Nullable StoreInventoryItemDto item, int quantity) {
        if (storeId == null || storeId <= 0) {
            Toast.makeText(requireContext(), R.string.staff_inventory_no_store, Toast.LENGTH_SHORT).show();
            return;
        }
        int actorId = TokenStore.getUserId(requireContext());
        if (actorId <= 0) {
            Toast.makeText(requireContext(), R.string.inventory_actor_missing, Toast.LENGTH_SHORT).show();
            return;
        }
        inventoryViewModel.updateInventory(storeId, item, quantity, actorId);
    }

    private void handleSaveAll() {
        if (storeId == null || storeId <= 0) {
            Toast.makeText(requireContext(), R.string.staff_inventory_no_store, Toast.LENGTH_SHORT).show();
            return;
        }
        List<StoreInventoryChange> changes = inventoryAdapter.getDirtyItems();
        if (changes.isEmpty()) {
            Toast.makeText(requireContext(), R.string.inventory_no_changes, Toast.LENGTH_SHORT).show();
            return;
        }
        int actorId = TokenStore.getUserId(requireContext());
        if (actorId <= 0) {
            Toast.makeText(requireContext(), R.string.inventory_actor_missing, Toast.LENGTH_SHORT).show();
            return;
        }
        inventoryViewModel.updateInventoryBatch(storeId, changes, actorId);
    }
}
