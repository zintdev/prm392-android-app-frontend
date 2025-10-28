package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.order.OrderDto;
import com.example.prm392_android_app_frontend.presentation.adapter.OrderAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderViewModel;

import java.util.Arrays;

public class StaffActivity extends AppCompatActivity implements OrderAdapter.OnUpdateClickListener {

    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView recyclerView = findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderAdapter = new OrderAdapter(this);
        recyclerView.setAdapter(orderAdapter);

        orderViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(OrderViewModel.class);

        observeViewModel();

        swipeRefreshLayout.setOnRefreshListener(() -> orderViewModel.refreshOrders());
    }

    private void observeViewModel() {
        orderViewModel.getOrders().observe(this, resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        if (!swipeRefreshLayout.isRefreshing()) {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                        break;
                    case SUCCESS:
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        if (resource.getData() != null) {
                            orderAdapter.setOrders(resource.getData());
                        }
                        break;
                    case ERROR:
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        orderViewModel.getUpdateOrderStatusResult().observe(this, resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Order status updated successfully!", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to update order status: " + resource.getMessage(), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    @Override
    public void onUpdateClick(OrderDto order) {
        showUpdateStatusDialog(order);
    }

    private void showUpdateStatusDialog(final OrderDto order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_status, null);
        builder.setView(dialogView);

        final TextView dialogOrderIdTextView = dialogView.findViewById(R.id.dialog_order_id_textview);
        final Spinner statusSpinner = dialogView.findViewById(R.id.status_spinner);

        dialogOrderIdTextView.setText("Update status for Order #" + order.getId());

        // Setup Spinner
        String[] statuses = getResources().getStringArray(R.array.order_statuses_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
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
