package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

public class StaffActivity extends AppCompatActivity implements OrderAdapter.OnUpdateClickListener, OrderAdapter.OnItemClickListener {

    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private Spinner statusSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        setupToolbar();
        initViews();
        setupViewModel();
        setupRecyclerView();
        setupFilterAndSearch();
        observeViewModel();

        swipeRefreshLayout.setOnRefreshListener(() -> orderViewModel.refreshOrders());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchView = findViewById(R.id.search_view);
        statusSpinner = findViewById(R.id.filter_status_spinner);
    }

    private void setupViewModel() {
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this, this);
        recyclerView.setAdapter(orderAdapter);
    }

    private void setupFilterAndSearch() {
        // Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.order_statuses_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = (String) parent.getItemAtPosition(position);
                orderViewModel.setFilterStatus(selectedStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                orderViewModel.setSearchQuery(newText);
                return true;
            }
        });
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
             // Logic to handle update status result
        });
    }

    @Override
    public void onUpdateClick(OrderDto order) {
        showUpdateStatusDialog(order);
    }

    @Override
    public void onItemClick(OrderDto order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getId());
        startActivity(intent);
    }

    private void showUpdateStatusDialog(final OrderDto order) {
        // Dialog logic remains the same
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_status, null);
        builder.setView(dialogView);

        final TextView dialogOrderIdTextView = dialogView.findViewById(R.id.dialog_order_id_textview);
        final Spinner statusSpinnerDialog = dialogView.findViewById(R.id.status_spinner);

        dialogOrderIdTextView.setText("Update status for Order #" + order.getId());

        String[] statuses = getResources().getStringArray(R.array.order_statuses_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinnerDialog.setAdapter(adapter);

        int currentStatusPosition = Arrays.asList(statuses).indexOf(order.getOrderStatus());
        if (currentStatusPosition >= 0) {
            statusSpinnerDialog.setSelection(currentStatusPosition);
        }

        builder.setTitle("Update Order Status")
                .setPositiveButton("Update", (dialog, id) -> {
                    String newStatus = (String) statusSpinnerDialog.getSelectedItem();
                    orderViewModel.updateOrderStatus(order.getId(), newStatus);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
