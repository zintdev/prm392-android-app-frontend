package com.example.prm392_android_app_frontend.presentation.fragment.staff;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.example.prm392_android_app_frontend.core.util.Resource;
import com.example.prm392_android_app_frontend.data.dto.UserDto;
import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;
import com.example.prm392_android_app_frontend.presentation.activity.OrderDetailActivity;
import com.example.prm392_android_app_frontend.presentation.adapter.StaffOrderAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderManagementViewModel;
import com.example.prm392_android_app_frontend.presentation.viewmodel.StaffSharedViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Fragment hiển thị danh sách đơn hàng dành cho nhân viên cửa hàng.
 * Đơn hàng được lọc theo cửa hàng mà nhân viên được gán.
 */
public class StaffOrdersFragment extends Fragment implements StaffOrderAdapter.Listener {

    private static final String[] FILTER_STATUSES = new String[]{
        "ALL",
        "PENDING",
        "PAID",
        "KEEPING",
        "PROCESSING",
        "SHIPPED",
        "COMPLETED",
        "CANCELLED"
    };

    private TextView txtStoreInfo;
    private TextView txtStoreHint;
    private TabLayout tabOrderStatus;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerOrders;
    private ProgressBar progressBar;
    private TextView txtEmpty;

    private StaffOrderAdapter adapter;
    private OrderManagementViewModel orderViewModel;
    private StaffSharedViewModel staffSharedViewModel;

    private Integer currentStoreId;
    private String currentStoreName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupViewModels();
        setupStatusTabs();
        observeStaffInfo();
        observeOrders();
        observeUpdateResult();
        observeStaffLoading();
    }

    private void initViews(@NonNull View view) {
        txtStoreInfo = view.findViewById(R.id.txtStoreInfo);
        txtStoreHint = view.findViewById(R.id.txtStoreHint);
        tabOrderStatus = view.findViewById(R.id.tabOrderStatus);
        swipeRefreshLayout = view.findViewById(R.id.swipeOrders);
        recyclerOrders = view.findViewById(R.id.recyclerOrders);
        progressBar = view.findViewById(R.id.progressOrders);
        txtEmpty = view.findViewById(R.id.txtEmpty);
    }

    private void setupRecyclerView() {
        adapter = new StaffOrderAdapter(this);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerOrders.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (currentStoreId != null) {
                orderViewModel.refreshOrders();
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setupViewModels() {
        orderViewModel = new ViewModelProvider(this).get(OrderManagementViewModel.class);
        staffSharedViewModel = new ViewModelProvider(requireActivity()).get(StaffSharedViewModel.class);
        if (staffSharedViewModel.getStaffInfo().getValue() == null) {
            staffSharedViewModel.loadCurrentStaff();
        }
    }

    private void setupStatusTabs() {
        if (tabOrderStatus == null) {
            return;
        }
        tabOrderStatus.removeAllTabs();
        for (String statusKey : FILTER_STATUSES) {
            TabLayout.Tab tab = tabOrderStatus.newTab();
            tab.setTag(statusKey);
            tab.setText(mapTabLabel(statusKey));
            tabOrderStatus.addTab(tab);
        }

        tabOrderStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                Object tag = tab.getTag();
                if (tag instanceof String) {
                    orderViewModel.setFilterStatus((String) tag);
                }
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) {
                Object tag = tab.getTag();
                if (tag instanceof String) {
                    orderViewModel.setFilterStatus((String) tag);
                }
            }
        });

        TabLayout.Tab first = tabOrderStatus.getTabAt(0);
        if (first != null) {
            first.select();
        } else {
            orderViewModel.refreshOrders();
        }
        orderViewModel.setFilterStatus(FILTER_STATUSES[0]);
    }

    private void observeStaffInfo() {
        staffSharedViewModel.getStaffInfo().observe(getViewLifecycleOwner(), this::onStaffInfoLoaded);
        staffSharedViewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                showToast(message);
                setEmptyState(message);
            }
        });
    }

    private void observeStaffLoading() {
        staffSharedViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                setLoading(true);
            } else {
                setLoading(false);
            }
        });
    }

    private void observeOrders() {
        orderViewModel.getOrders().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) {
                return;
            }
            switch (resource.getStatus()) {
                case LOADING:
                    if (!swipeRefreshLayout.isRefreshing()) {
                        setLoading(true);
                    }
                    break;
                case SUCCESS:
                    setLoading(false);
                    swipeRefreshLayout.setRefreshing(false);
                    List<OrderDto> filtered = filterOrdersForStore(resource.getData());
                    adapter.submit(filtered);
                    updateEmptyState(filtered);
                    break;
                case ERROR:
                    setLoading(false);
                    swipeRefreshLayout.setRefreshing(false);
                    String message = resource.getMessage() != null ? resource.getMessage() : getString(R.string.staff_orders_empty);
                    String friendly = getString(R.string.staff_orders_load_error, message);
                    showToast(friendly);
                    setEmptyState(friendly);
                    break;
            }
        });
    }

    private void observeUpdateResult() {
        orderViewModel.getUpdateOrderStatusResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) {
                return;
            }
            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    showToast(getString(R.string.staff_order_update_success));
                    break;
                case ERROR:
                    setLoading(false);
                    String message = resource.getMessage() != null ? resource.getMessage() : "";
                    showToast(getString(R.string.staff_order_update_failed, message));
                    break;
            }
        });
    }

    private void onStaffInfoLoaded(@Nullable UserDto userDto) {
        if (userDto == null) {
            setEmptyState(getString(R.string.staff_orders_empty));
            return;
        }
        Integer newStoreId = userDto.storeLocationId;
        boolean storeChanged = !Objects.equals(currentStoreId, newStoreId);
        currentStoreId = newStoreId;
        currentStoreName = userDto.storeName;

        if (currentStoreName != null && !currentStoreName.trim().isEmpty()) {
            txtStoreInfo.setText(getString(R.string.staff_order_store_format, currentStoreName));
        } else {
            txtStoreInfo.setText(getString(R.string.staff_order_store_format, getString(R.string.staff_order_store_unknown)));
        }

        if (currentStoreId == null) {
            orderViewModel.setStoreFilter(null);
            txtStoreHint.setVisibility(View.GONE);
            adapter.submit(Collections.emptyList());
            setEmptyState(getString(R.string.staff_orders_no_store));
            return;
        }

        txtStoreHint.setVisibility(View.VISIBLE);
        if (storeChanged) {
            adapter.submit(Collections.emptyList());
        }
        txtEmpty.setVisibility(View.GONE);
        if (storeChanged || adapter.getItemCount() == 0) {
            setLoading(true);
        }
        if (storeChanged) {
            orderViewModel.setStoreFilter(currentStoreId);
        } else {
            orderViewModel.refreshOrders();
        }
    }

    private List<OrderDto> filterOrdersForStore(@Nullable List<OrderDto> data) {
        if (currentStoreId == null || data == null) {
            return Collections.emptyList();
        }
        List<OrderDto> filtered = new ArrayList<>();
        for (OrderDto order : data) {
            if (order.getStoreLocationId() != null && Objects.equals(order.getStoreLocationId(), currentStoreId)) {
                filtered.add(order);
            }
        }
        filtered.sort((o1, o2) -> {
            String d1 = o1.getOrderDate();
            String d2 = o2.getOrderDate();
            if (d1 == null && d2 == null) {
                return 0;
            }
            if (d1 == null) {
                return 1;
            }
            if (d2 == null) {
                return -1;
            }
            return d2.compareTo(d1);
        });
        return filtered;
    }

    private void updateEmptyState(@NonNull List<OrderDto> orders) {
        if (orders.isEmpty()) {
            setEmptyState(getString(R.string.staff_orders_empty));
        } else {
            txtEmpty.setVisibility(View.GONE);
        }
    }

    private void setEmptyState(@NonNull String message) {
        txtEmpty.setText(message);
        txtEmpty.setVisibility(View.VISIBLE);
        recyclerOrders.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showToast(@NonNull String message) {
        Toast.makeText(requireContext().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewDetail(@NonNull OrderDto order) {
        Context context = requireContext();
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getId());
        startActivity(intent);
    }

    @Override
    public void onUpdateStatus(@NonNull OrderDto order) {
        if (currentStoreId == null) {
            showToast(getString(R.string.staff_orders_no_store));
            return;
        }
        showStatusDialog(order);
    }

    private void showStatusDialog(@NonNull OrderDto order) {
        if (getContext() == null) {
            return;
        }
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_status, null);
        TextView dialogOrderIdTextView = dialogView.findViewById(R.id.dialog_order_id_textview);
        Spinner statusSpinner = dialogView.findViewById(R.id.status_spinner);

        dialogOrderIdTextView.setText(getString(R.string.staff_order_id_format, order.getId()));

        String[] availableStatuses = getTransitionTargets(order);
        if (availableStatuses.length == 0) {
            showToast(getString(R.string.staff_orders_status_update_unavailable));
            return;
        }

        String[] displayValues = new String[availableStatuses.length];
        for (int i = 0; i < availableStatuses.length; i++) {
            displayValues[i] = mapStatusLabel(availableStatuses[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, displayValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.staff_order_update_status)
                .setView(dialogView)
                .setPositiveButton(R.string.staff_order_update_status, (dialog, which) -> {
                    String selectedStatus = availableStatuses[statusSpinner.getSelectedItemPosition()];
                    int rawUserId = TokenStore.getUserId(requireContext());
                    Integer actorId = rawUserId > 0 ? rawUserId : null;
                    orderViewModel.updateOrderStatus(order.getId(), selectedStatus, actorId);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String mapStatusLabel(@NonNull String status) {
        switch (status.toUpperCase(Locale.ROOT)) {
            case "KEEPING":
                return getString(R.string.staff_order_status_keeping);
            case "PENDING":
                return getString(R.string.staff_order_status_pending);
            case "PAID":
                return getString(R.string.staff_order_status_paid);
            case "PROCESSING":
                return getString(R.string.staff_order_status_processing);
            case "SHIPPED":
                return getString(R.string.staff_order_status_shipped);
            case "COMPLETED":
                return getString(R.string.staff_order_status_completed);
            case "CANCELLED":
                return getString(R.string.staff_order_status_cancelled);
            default:
                return status;
        }
    }

    private String mapTabLabel(@NonNull String status) {
        if ("ALL".equalsIgnoreCase(status)) {
            return getString(R.string.all);
        }
        return mapStatusLabel(status);
    }

    private String[] getTransitionTargets(@NonNull OrderDto order) {
        String currentStatus = order.getOrderStatus();
        if (currentStatus == null) {
            return new String[0];
        }
        String normalized = currentStatus.toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "PENDING":
                if ("PICKUP".equalsIgnoreCase(order.getShipmentMethod())) {
                    return new String[]{"PAID", "CANCELLED"};
                }
                return new String[]{"PROCESSING", "CANCELLED"};
            case "PAID":
                if ("PICKUP".equalsIgnoreCase(order.getShipmentMethod())) {
                    return new String[]{"KEEPING", "CANCELLED"};
                }
                return new String[]{"PROCESSING", "CANCELLED"};
            case "KEEPING":
                return new String[]{"COMPLETED", "CANCELLED"};
            case "PROCESSING":
                if ("DELIVERY".equalsIgnoreCase(order.getShipmentMethod())) {
                    return new String[]{"SHIPPED", "CANCELLED"};
                }
                return new String[]{"CANCELLED"};
            case "SHIPPED":
                return new String[]{"COMPLETED", "CANCELLED"};
            default:
                return new String[0];
        }
    }
}
