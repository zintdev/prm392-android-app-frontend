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
import com.example.prm392_android_app_frontend.presentation.component.LocalNotificationManager;
import com.example.prm392_android_app_frontend.presentation.fragment.NewAddressFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.SavedAddressesFragment;
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderViewModel;
import com.example.prm392_android_app_frontend.presentation.viewmodel.PaymentViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class    OrderCreateActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewOrderItems;
    private OrderItemAdapter orderItemAdapter;
    private TextView textViewTotalPrice;
    private MaterialButton buttonPlaceOrder;
    private ProgressBar progressBar;

    private TabLayout tabLayoutAddress;
    private ViewPager2 viewPagerAddress;
    private AddressPagerAdapter addressPagerAdapter;
    private RadioGroup radioGroupShipment;
    private RadioGroup radioGroupPayment;
    private MaterialCardView pickupStoreCard;
    private TextView textSelectedStoreName;
    private TextView textSelectedStoreAddress;
    private TextView textSelectedStoreDistance;
    private MaterialButton buttonSelectStore;

    private OrderViewModel orderViewModel;
    private PaymentViewModel paymentViewModel;
    private LocalNotificationManager localNotificationManager;
    private List<CartItemDto> selectedItems;
    private double totalAmount;
    private Integer selectedStoreId;
    private String selectedStoreName;
    private String selectedStoreAddress;
    private Double selectedStoreDistance;

    private int currentOrderId;
    private int currentPaymentId;

    private static final int VNPAY_REQUEST_CODE = 1001;
    private static final int STORE_PICKER_REQUEST_CODE = 2002;
    private static final String STATE_HAS_STORE = "state_has_store";
    private static final String STATE_STORE_ID = "state_store_id";
    private static final String STATE_STORE_NAME = "state_store_name";
    private static final String STATE_STORE_ADDRESS = "state_store_address";
    private static final String STATE_STORE_DISTANCE = "state_store_distance";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_create);

        selectedItems = getIntent().getParcelableArrayListExtra("selected_items");
        totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);

        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào được chọn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_HAS_STORE, false)) {
            selectedStoreId = savedInstanceState.getInt(STATE_STORE_ID);
            selectedStoreName = savedInstanceState.getString(STATE_STORE_NAME);
            selectedStoreAddress = savedInstanceState.getString(STATE_STORE_ADDRESS);
            if (savedInstanceState.containsKey(STATE_STORE_DISTANCE)) {
                selectedStoreDistance = savedInstanceState.getDouble(STATE_STORE_DISTANCE);
            }
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

        tabLayoutAddress = findViewById(R.id.tab_layout_address);
        viewPagerAddress = findViewById(R.id.view_pager_address);
        radioGroupShipment = findViewById(R.id.radio_group_shipment);
        radioGroupPayment = findViewById(R.id.radio_group_payment);
        pickupStoreCard = findViewById(R.id.pickup_store_card);
        textSelectedStoreName = findViewById(R.id.text_selected_store_name);
        textSelectedStoreAddress = findViewById(R.id.text_selected_store_address);
        textSelectedStoreDistance = findViewById(R.id.text_selected_store_distance);
        buttonSelectStore = findViewById(R.id.button_select_store);

        if (buttonSelectStore != null) {
            buttonSelectStore.setOnClickListener(v -> openStorePicker());
        }

        boolean initialPickup = radioGroupShipment != null
                && radioGroupShipment.getCheckedRadioButtonId() == R.id.radio_pickup;
        togglePickupStoreSection(initialPickup);
        updateSelectedStoreViews();

        if (radioGroupShipment != null) {
            radioGroupShipment.setOnCheckedChangeListener((group, checkedId) -> {
                boolean isPickup = checkedId == R.id.radio_pickup;
                togglePickupStoreSection(isPickup);
                if (isPickup && selectedStoreId == null) {
                    Toast.makeText(this, R.string.pickup_store_select_prompt, Toast.LENGTH_SHORT).show();
                }
            });
        }

        localNotificationManager = new LocalNotificationManager(this);

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
        addressPagerAdapter = new AddressPagerAdapter(this);
        viewPagerAddress.setAdapter(addressPagerAdapter);

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

    private void togglePickupStoreSection(boolean show) {
        if (pickupStoreCard != null) {
            pickupStoreCard.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updateSelectedStoreViews() {
        if (textSelectedStoreName == null || textSelectedStoreAddress == null) {
            return;
        }

        if (selectedStoreId != null) {
            String displayName;
            if (selectedStoreName != null && !selectedStoreName.trim().isEmpty()) {
                displayName = selectedStoreName;
            } else {
                displayName = getString(R.string.pickup_store_fallback_name, selectedStoreId);
            }
            textSelectedStoreName.setText(displayName);

            if (selectedStoreAddress != null && !selectedStoreAddress.trim().isEmpty()) {
                textSelectedStoreAddress.setText(selectedStoreAddress);
            } else {
                textSelectedStoreAddress.setText(R.string.pickup_store_address_placeholder);
            }

            if (textSelectedStoreDistance != null) {
                if (selectedStoreDistance != null) {
                    textSelectedStoreDistance.setVisibility(View.VISIBLE);
                    textSelectedStoreDistance.setText(getString(R.string.pickup_store_distance_format, selectedStoreDistance));
                } else {
                    textSelectedStoreDistance.setVisibility(View.GONE);
                }
            }

            if (buttonSelectStore != null) {
                buttonSelectStore.setText(R.string.pickup_store_change_button);
            }
        } else {
            textSelectedStoreName.setText(R.string.pickup_store_placeholder);
            textSelectedStoreAddress.setText(R.string.pickup_store_address_placeholder);
            if (textSelectedStoreDistance != null) {
                textSelectedStoreDistance.setVisibility(View.GONE);
            }
            if (buttonSelectStore != null) {
                buttonSelectStore.setText(R.string.pickup_store_select_button);
            }
        }
    }

    private void openStorePicker() {
        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, R.string.pickup_store_no_items, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, MapsActivity.class);
        intent.putParcelableArrayListExtra(MapsActivity.EXTRA_CART_ITEMS, new ArrayList<>(selectedItems));
        if (selectedStoreId != null) {
            intent.putExtra(MapsActivity.EXTRA_SELECTED_STORE_ID, selectedStoreId);
        }
        startActivityForResult(intent, STORE_PICKER_REQUEST_CODE);
    }

    private void setupViewModel() {
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        paymentViewModel = new ViewModelProvider(this).get(PaymentViewModel.class);

        orderViewModel.getOrderLiveData().observe(this, order -> {
            if (order != null) {
                currentOrderId = order.getId();
                String paymentMethod = getSelectedPaymentMethod();

                if ("COD".equals(paymentMethod)) {
                    //chuyển sang màn hình đặt thành công
                    hideLoading();
                    // THÔNG BÁO: Đặt hàng thành công (COD)
                    localNotificationManager.showOrderPlacedNotification(String.valueOf(currentOrderId));
                    navigateToOrderSuccess(order.getId());
                } else {
                    createPaymentForOrder(order);
                }
            }
        });

        orderViewModel.getErrorMessage().observe(this, error -> {
            hideLoading();
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi tạo đơn hàng: " + error, Toast.LENGTH_LONG).show();
            }
        });

        paymentViewModel.getPaymentLiveData().observe(this, payment -> {
            if (payment != null) {
                currentPaymentId = payment.getId();
                android.util.Log.d("OrderCreateActivity", "Payment created successfully with ID: " + currentPaymentId);
                
                // Sau khi có paymentId, gọi API để lấy VNPay URL
                String orderDescription = "Thanh toán đơn hàng #" + payment.getOrderId();
                paymentViewModel.createVNPayUrl(currentPaymentId, totalAmount, orderDescription);
            }
        });

        paymentViewModel.getErrorMessage().observe(this, error -> {
            hideLoading();
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi tạo thanh toán: " + error, Toast.LENGTH_LONG).show();
            }
        });

        paymentViewModel.getVnpayUrlLiveData().observe(this, vnpayUrl -> {
            hideLoading();
            if (vnpayUrl != null && !vnpayUrl.isEmpty()) {
                android.util.Log.d("OrderCreateActivity", "VNPay URL: " + vnpayUrl);
                // Mở WebView để thanh toán
                openVNPayWebView(vnpayUrl);
            }
        });
    }

    private void openVNPayWebView(String paymentUrl) {
        Intent intent = new Intent(this, VNPayPaymentActivity.class);
        intent.putExtra("payment_url", paymentUrl);
        intent.putExtra("order_id", currentOrderId);
        intent.putExtra("payment_id", currentPaymentId);
        intent.putExtra("amount", totalAmount);
        startActivityForResult(intent, VNPAY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VNPAY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // THÔNG BÁO: Thanh toán thành công (VNPay)
                localNotificationManager.showPaymentSuccessNotification(String.valueOf(currentOrderId));
                Toast.makeText(this, "Đặt hàng và thanh toán thành công!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Thanh toán chưa hoàn tất. Vui lòng kiểm tra đơn hàng của bạn.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == STORE_PICKER_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                int storeId = data.getIntExtra(MapsActivity.RESULT_STORE_ID, -1);
                if (storeId != -1) {
                    selectedStoreId = storeId;
                    selectedStoreName = data.getStringExtra(MapsActivity.RESULT_STORE_NAME);
                    selectedStoreAddress = data.getStringExtra(MapsActivity.RESULT_STORE_ADDRESS);
                    Double distance = null;
                    if (data.hasExtra(MapsActivity.RESULT_STORE_DISTANCE)) {
                        double raw = data.getDoubleExtra(MapsActivity.RESULT_STORE_DISTANCE, Double.NaN);
                        if (!Double.isNaN(raw) && raw >= 0) {
                            distance = raw;
                        }
                    }
                    selectedStoreDistance = distance;
                    updateSelectedStoreViews();
                    if (radioGroupShipment != null && radioGroupShipment.getCheckedRadioButtonId() == R.id.radio_pickup) {
                        togglePickupStoreSection(true);
                    }
                    return;
                }
            }

            if (selectedStoreId == null) {
                if (radioGroupShipment != null) {
                    radioGroupShipment.check(R.id.radio_delivery);
                }
                Toast.makeText(this, R.string.pickup_store_cancelled_message, Toast.LENGTH_SHORT).show();
            } else {
                updateSelectedStoreViews();
            }
        }
    }

    private void createPaymentForOrder(OrderDTO order) {
        String paymentMethod = "VNPAY";
        double amount = totalAmount;
        paymentViewModel.createPayment(order.getId(), paymentMethod, amount);
    }

    private void placeOrder() {
        if (!validateInputs()) {
            return;
        }
        if (!TokenStore.isLoggedIn(this)) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int userId = TokenStore.getUserId(this);
        if (userId == -1) {
            Toast.makeText(this, "Không thể xác định người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String shipmentMethod = getShipmentMethod();
        String paymentMethod = getSelectedPaymentMethod();
        boolean isPickup = "PICKUP".equals(shipmentMethod);
        if (isPickup && selectedStoreId == null) {
            Toast.makeText(this, R.string.pickup_store_select_required, Toast.LENGTH_LONG).show();
            return;
        }

        showLoading();

        String fullName = "";
        String phone = "";
        String addressLine1 = "";
        String addressLine2 = "";
        String cityState = "";

        int currentTab = viewPagerAddress.getCurrentItem();
        if (currentTab == 0) {
            NewAddressFragment newAddressFragment = (NewAddressFragment) getSupportFragmentManager().findFragmentByTag("f" + 0);
            if (newAddressFragment != null) {
                fullName = newAddressFragment.getFullName();
                phone = newAddressFragment.getPhone();
                addressLine1 = newAddressFragment.getAddressLine1();
                addressLine2 = newAddressFragment.getAddressLine2();
                cityState = newAddressFragment.getCityState();
            }
        } else {
            SavedAddressesFragment savedAddressesFragment = (SavedAddressesFragment) getSupportFragmentManager().findFragmentByTag("f" + 1);
            if (savedAddressesFragment != null) {
                fullName = savedAddressesFragment.getFullName();
                phone = savedAddressesFragment.getPhone();
                addressLine1 = savedAddressesFragment.getAddressLine1();
                addressLine2 = savedAddressesFragment.getAddressLine2();
                cityState = savedAddressesFragment.getCityState();
            }
        }

    CreateOrderRequestDto request = new CreateOrderRequestDto(
        userId,
        shipmentMethod,
        paymentMethod,
        fullName,
        phone,
        addressLine1,
        addressLine2,
        cityState,
        isPickup ? selectedStoreId : null
    );

        orderViewModel.placeOrder(request);
    }

    private boolean validateInputs() {
        int currentTab = viewPagerAddress.getCurrentItem();
        if (currentTab == 0) {
            NewAddressFragment newAddressFragment = (NewAddressFragment) getSupportFragmentManager().findFragmentByTag("f" + 0);
            if (newAddressFragment != null) {
                return newAddressFragment.validateInputs();
            }
        } else {
            SavedAddressesFragment savedAddressesFragment = (SavedAddressesFragment) getSupportFragmentManager().findFragmentByTag("f" + 1);
            if (savedAddressesFragment != null) {
                // Cần implement validation trong SavedAddressesFragment
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

    private String getSelectedPaymentMethod() {
        int selectedId = radioGroupPayment.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_payment_cod) {
            return "COD";
        } else {
            return "VNPAY";
        }
    }

    private void navigateToOrderSuccess(int orderId) {
        // Chuyển sang màn hình thành công chung
        Intent intent = new Intent(this, OrderSuccessActivity.class);
        intent.putExtra("order_id", String.valueOf(orderId));
        intent.putExtra("total_amount", totalAmount);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        boolean hasStore = selectedStoreId != null;
        outState.putBoolean(STATE_HAS_STORE, hasStore);
        if (hasStore) {
            outState.putInt(STATE_STORE_ID, selectedStoreId);
            if (selectedStoreName != null) {
                outState.putString(STATE_STORE_NAME, selectedStoreName);
            }
            if (selectedStoreAddress != null) {
                outState.putString(STATE_STORE_ADDRESS, selectedStoreAddress);
            }
            if (selectedStoreDistance != null) {
                outState.putDouble(STATE_STORE_DISTANCE, selectedStoreDistance);
            }
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
