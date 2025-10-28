package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.CartItemDto;
import com.example.prm392_android_app_frontend.data.dto.CreateOrderRequestDto;
import com.example.prm392_android_app_frontend.data.dto.OrderDTO;
import com.example.prm392_android_app_frontend.presentation.adapter.AddressPagerAdapter;
import com.example.prm392_android_app_frontend.presentation.adapter.OrderItemAdapter;
import com.example.prm392_android_app_frontend.presentation.fragment.NewAddressFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.SavedAddressesFragment;
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderCreateActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewOrderItems;
    private OrderItemAdapter orderItemAdapter;
    private TextView textViewTotalPrice;
    private MaterialButton buttonPlaceOrder;
    private ProgressBar progressBar;

    // Tab components
    private TabLayout tabLayoutAddress;
    private ViewPager2 viewPagerAddress;
    private AddressPagerAdapter addressPagerAdapter;
    private RadioGroup radioGroupShipment;

    private OrderViewModel orderViewModel;
    private List<CartItemDto> selectedItems;
    private double totalAmount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_create);

        // Nhận dữ liệu từ Cart
        selectedItems = getIntent().getParcelableArrayListExtra("selected_items");
        totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);

        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào được chọn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupTabLayout();
        setupViewModel();
        updateTotalPrice();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_order_create);
        recyclerViewOrderItems = findViewById(R.id.recycler_view_order_items);
        textViewTotalPrice = findViewById(R.id.text_view_total_price);
        buttonPlaceOrder = findViewById(R.id.button_place_order);
        progressBar = findViewById(R.id.progress_bar_order);

        // Tab components
        tabLayoutAddress = findViewById(R.id.tab_layout_address);
        viewPagerAddress = findViewById(R.id.view_pager_address);
        radioGroupShipment = findViewById(R.id.radio_group_shipment);

        buttonPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        orderItemAdapter = new OrderItemAdapter(selectedItems);
        recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrderItems.setAdapter(orderItemAdapter);
    }

    private void setupTabLayout() {
        // Setup ViewPager2 với AddressPagerAdapter
        addressPagerAdapter = new AddressPagerAdapter(this);
        viewPagerAddress.setAdapter(addressPagerAdapter);

        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayoutAddress, viewPagerAddress,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Nhập địa chỉ mới");
                            break;
                        case 1:
                            tab.setText("Chọn địa chỉ có sẵn");
                            break;
                    }
                }
        ).attach();
    }

    private void setupViewModel() {
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        orderViewModel.getOrderLiveData().observe(this, order -> {
            hideLoading();
            if (order != null) {
                Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                // Có thể chuyển về trang chủ hoặc trang đơn hàng
                finish();
            }
        });

        orderViewModel.getErrorMessage().observe(this, error -> {
            hideLoading();
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void placeOrder() {
        if (!validateInputs()) {
            return;
        }

        // Kiểm tra đăng nhập
        if (!TokenStore.isLoggedIn(this)) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        showLoading();

        // Lấy userId từ session
        int userId = TokenStore.getUserId(this);
        if (userId == -1) {
            Toast.makeText(this, "Không thể xác định người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Lấy dữ liệu từ fragment đang active
        String fullName = "";
        String phone = "";
        String addressLine1 = "";
        String addressLine2 = "";
        String cityState = "";

        int currentTab = viewPagerAddress.getCurrentItem();
        if (currentTab == 0) {
            // Tab "Nhập địa chỉ mới"
            NewAddressFragment newAddressFragment = (NewAddressFragment) getSupportFragmentManager()
                    .findFragmentByTag("f" + 0);
            if (newAddressFragment != null) {
                fullName = newAddressFragment.getFullName();
                phone = newAddressFragment.getPhone();
                addressLine1 = newAddressFragment.getAddressLine1();
                addressLine2 = newAddressFragment.getAddressLine2();
                cityState = newAddressFragment.getCityState();
            }
        } else {
            // Tab "Chọn địa chỉ có sẵn"
            SavedAddressesFragment savedAddressesFragment = (SavedAddressesFragment) getSupportFragmentManager()
                    .findFragmentByTag("f" + 1);
            if (savedAddressesFragment != null) {
                fullName = savedAddressesFragment.getFullName();
                phone = savedAddressesFragment.getPhone();
                addressLine1 = savedAddressesFragment.getAddressLine1();
                addressLine2 = savedAddressesFragment.getAddressLine2();
                cityState = savedAddressesFragment.getCityState();
            }
        }

        // Tạo request object
        CreateOrderRequestDto request = new CreateOrderRequestDto(
                userId, // Lấy từ session thay vì hardcode
                getShipmentMethod(),
                fullName,
                phone,
                addressLine1,
                addressLine2,
                cityState
        );

        // Gọi API tạo order
        orderViewModel.placeOrder(request);
    }

    private boolean validateInputs() {
        int currentTab = viewPagerAddress.getCurrentItem();
        
        if (currentTab == 0) {
            // Tab "Nhập địa chỉ mới"
            NewAddressFragment newAddressFragment = (NewAddressFragment) getSupportFragmentManager()
                    .findFragmentByTag("f" + 0);
            if (newAddressFragment != null) {
                return newAddressFragment.validateInputs();
            }
        } else {
            // Tab "Chọn địa chỉ có sẵn"
            SavedAddressesFragment savedAddressesFragment = (SavedAddressesFragment) getSupportFragmentManager()
                    .findFragmentByTag("f" + 1);
            if (savedAddressesFragment != null) {
                return savedAddressesFragment.validateInputs();
            }
        }
        
        return false;
    }


    private String getShipmentMethod() {
        int selectedId = radioGroupShipment.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_delivery) {
            return "DELIVERY";
        } else {
            return "PICKUP";
        }
    }

    private void updateTotalPrice() {
        try {
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            String formattedPrice = formatter.format(totalAmount);
            textViewTotalPrice.setText(String.format("%sđ", formattedPrice));
        } catch (Exception e) {
            textViewTotalPrice.setText("0đ");
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        buttonPlaceOrder.setEnabled(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        buttonPlaceOrder.setEnabled(true);
    }
}