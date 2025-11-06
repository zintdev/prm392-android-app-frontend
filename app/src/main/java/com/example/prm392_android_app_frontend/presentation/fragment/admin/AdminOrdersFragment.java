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
import com.example.prm392_android_app_frontend.presentation.adapter.OrderAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderViewModel;

import java.util.Arrays;

public class AdminOrdersFragment extends Fragment implements OrderAdapter.OnUpdateClickListener, OrderAdapter.OnItemClickListener {

    private OrderViewModel orderViewModel;
    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
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
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, this);
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
                        if (resource.getData() != null) {
                            orderAdapter.setOrders(resource.getData());
                        }
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
                        Toast.makeText(getContext(), "Order status updated successfully!", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to update order status: " + resource.getMessage(), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    @Override
    public void onUpdateClick(OrderDto order) {
        showUpdateStatusDialog(order);
    }

    @Override
    public void onItemClick(OrderDto order) {
        Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getId());
        startActivity(intent);
    }

    private void showUpdateStatusDialog(final OrderDto order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_status, null);
        builder.setView(dialogView);

        final TextView dialogOrderIdTextView = dialogView.findViewById(R.id.dialog_order_id_textview);
        final Spinner statusSpinner = dialogView.findViewById(R.id.status_spinner);

        dialogOrderIdTextView.setText("Update status for Order #" + order.getId());

        // Setup Spinner
        String[] statuses = getResources().getStringArray(R.array.order_statuses_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        // Set current status as selected
        int currentStatusPosition = Arrays.asList(statuses).indexOf(order.getOrderStatus());
        if (currentStatusPosition >= 0) {
            statusSpinner.setSelection(currentStatusPosition);
        }

        builder.setTitle("Update Order Status")
                .setPositiveButton("Update", (dialog, id) -> {
                    String newStatus = (String) statusSpinner.getSelectedItem();
                    orderViewModel.updateOrderStatus(order.getId(), newStatus);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
