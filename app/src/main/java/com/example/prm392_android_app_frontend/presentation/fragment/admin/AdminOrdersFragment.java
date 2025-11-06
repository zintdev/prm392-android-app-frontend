package com.example.prm392_android_app_frontend.presentation.fragment.admin;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;
import com.example.prm392_android_app_frontend.presentation.activity.OrderDetailActivity;
import com.example.prm392_android_app_frontend.presentation.adapter.StaffOrderAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderManagementViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;

import java.util.Locale;

public class AdminOrdersFragment extends Fragment implements StaffOrderAdapter.Listener {

    private static final String[] MANAGEABLE_STATUSES = new String[]{
            "PENDING",
            "KEEPING",
            "PROCESSING",
            "SHIPPED",
            "COMPLETED",
            "CANCELLED"
    };

    private OrderManagementViewModel orderViewModel;
    private RecyclerView ordersRecyclerView;
    private StaffOrderAdapter orderAdapter;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_order, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupRecyclerView();
        observeViewModel();
    }

    private void initViews(View view) {
        ordersRecyclerView = view.findViewById(R.id.ordersRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupViewModel() {
    orderViewModel = new ViewModelProvider(this).get(OrderManagementViewModel.class);
    }

    private void setupRecyclerView() {
    orderAdapter = new StaffOrderAdapter(this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void observeViewModel() {
        orderViewModel.getOrders().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        progressBar.setVisibility(View.GONE);
                        orderAdapter.submit(resource.getData());
                        break;
                    case ERROR:
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        orderViewModel.getUpdateOrderStatusResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), getString(R.string.staff_order_update_success), Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        progressBar.setVisibility(View.GONE);
                        String message = resource.getMessage() != null ? resource.getMessage() : "";
                        Toast.makeText(getContext(), getString(R.string.staff_order_update_failed, message), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    @Override
    public void onUpdateStatus(@NonNull OrderDto order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_status, null);
        builder.setView(dialogView);

        final TextView dialogOrderIdTextView = dialogView.findViewById(R.id.dialog_order_id_textview);
        final Spinner statusSpinner = dialogView.findViewById(R.id.status_spinner);

        dialogOrderIdTextView.setText(getString(R.string.staff_order_id_format, order.getId()));

        String[] displayValues = new String[MANAGEABLE_STATUSES.length];
        for (int i = 0; i < MANAGEABLE_STATUSES.length; i++) {
            displayValues[i] = mapStatusLabel(MANAGEABLE_STATUSES[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, displayValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        int currentStatusPosition = indexOfStatus(order.getOrderStatus());
        if (currentStatusPosition >= 0) {
            statusSpinner.setSelection(currentStatusPosition);
        }

    builder.setTitle(R.string.staff_order_update_status)
        .setPositiveButton(R.string.staff_order_update_status, (dialog, id) -> {
                    String selectedStatus = MANAGEABLE_STATUSES[statusSpinner.getSelectedItemPosition()];
                    int rawUserId = TokenStore.getUserId(requireContext());
                    Integer actorId = rawUserId > 0 ? rawUserId : null;
                    orderViewModel.updateOrderStatus(order.getId(), selectedStatus, actorId);
        })
        .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onViewDetail(@NonNull OrderDto order) {
        Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getId());
        startActivity(intent);
    }

    private int indexOfStatus(String status) {
        if (status == null) {
            return -1;
        }
        for (int i = 0; i < MANAGEABLE_STATUSES.length; i++) {
            if (MANAGEABLE_STATUSES[i].equalsIgnoreCase(status)) {
                return i;
            }
        }
        return -1;
    }

    private String mapStatusLabel(String status) {
        switch (status.toUpperCase(Locale.ROOT)) {
            case "KEEPING":
                return getString(R.string.staff_order_status_keeping);
            case "PENDING":
                return getString(R.string.staff_order_status_pending);
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
}
