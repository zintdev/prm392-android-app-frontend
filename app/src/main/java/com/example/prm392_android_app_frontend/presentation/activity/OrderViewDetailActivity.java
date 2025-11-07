package com.example.prm392_android_app_frontend.presentation.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.OrderDTO;
import com.example.prm392_android_app_frontend.presentation.adapter.OrderItemAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.OrderViewModel;
import com.example.prm392_android_app_frontend.presentation.viewmodel.PaymentViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderViewDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private Chip chipStatus;
    private TextView textViewOrderDate;
    private TextView textViewShippingName;
    private TextView textViewShippingPhone;
    private TextView textViewShippingAddress;
    private TextView textViewShipmentMethod;
    private View pickupStoreDivider;
    private View pickupStoreSection;
    private TextView textViewPickupStoreName;
    private TextView textViewPickupStoreAddress;
    private MaterialButton btnPickupStoreDirections;
    private TextView textViewSubtotal;
    private TextView textViewShippingFee;
    private TextView textViewTotal;
    private RecyclerView recyclerViewItems;
    private MaterialButton btnPayment;

    private OrderViewModel orderViewModel;
    private PaymentViewModel paymentViewModel;
    private OrderItemAdapter itemAdapter;
    private int orderId;
    private OrderDTO currentOrder;
    private int currentPaymentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_view_detail);

        orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        loadOrderDetail();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        chipStatus = findViewById(R.id.chip_status);
        textViewOrderDate = findViewById(R.id.text_view_order_date);
        textViewShippingName = findViewById(R.id.text_view_shipping_name);
        textViewShippingPhone = findViewById(R.id.text_view_shipping_phone);
        textViewShippingAddress = findViewById(R.id.text_view_shipping_address);
        textViewShipmentMethod = findViewById(R.id.text_view_shipment_method);
    pickupStoreDivider = findViewById(R.id.divider_pickup_store);
    pickupStoreSection = findViewById(R.id.layout_pickup_store);
    textViewPickupStoreName = findViewById(R.id.text_view_pickup_store_name);
    textViewPickupStoreAddress = findViewById(R.id.text_view_pickup_store_address);
    btnPickupStoreDirections = findViewById(R.id.btn_pickup_store_directions);
        textViewSubtotal = findViewById(R.id.text_view_subtotal);
        textViewShippingFee = findViewById(R.id.text_view_shipping_fee);
        textViewTotal = findViewById(R.id.text_view_total);
        recyclerViewItems = findViewById(R.id.recycler_view_items);
        btnPayment = findViewById(R.id.btn_payment);

        btnPayment.setOnClickListener(v -> showPaymentConfirmationDialog());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViewModel() {
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        paymentViewModel = new ViewModelProvider(this).get(PaymentViewModel.class);

        // Since we don't have getOrderById API yet, we'll get all orders and filter
        // This is a workaround - ideally should have a dedicated API endpoint
        orderViewModel.getOrdersListLiveData().observe(this, orders -> {
            hideLoading();
            if (orders != null) {
                for (OrderDTO order : orders) {
                    if (order.getId() == orderId) {
                        displayOrderDetails(order);
                        return;
                    }
                }
                Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        orderViewModel.getErrorMessage().observe(this, error -> {
            hideLoading();
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        orderViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        // Observe payment creation
        paymentViewModel.getPaymentLiveData().observe(this, payment -> {
            if (payment != null && payment.getId() != 0) {
                // Lưu payment ID
                currentPaymentId = payment.getId();
                // Tạo VNPay URL với payment ID
                createVNPayUrl(payment.getId());
            }
        });

        // Observe VNPay URL
        paymentViewModel.getVnpayUrlLiveData().observe(this, url -> {
            if (url != null && !url.isEmpty()) {
                openVNPayUrl(url);
            }
        });

        // Observe payment errors
        paymentViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        itemAdapter = new OrderItemAdapter();
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(itemAdapter);
    }

    private void loadOrderDetail() {
        int userId = com.example.prm392_android_app_frontend.storage.TokenStore.getUserId(this);
        if (userId != -1) {
            orderViewModel.getOrdersByUserId(userId, null);
        }
    }

    private void displayOrderDetails(OrderDTO order) {
        currentOrder = order;
        
        // Trạng thái
        chipStatus.setText(getStatusText(order.getStatus()));
        chipStatus.setChipBackgroundColorResource(getStatusColor(order.getStatus()));

        // Hiển thị nút thanh toán nếu trạng thái là PENDING
        if ("PENDING".equalsIgnoreCase(order.getStatus())) {
            btnPayment.setVisibility(View.VISIBLE);
        } else {
            btnPayment.setVisibility(View.GONE);
        }

        // Ngày đặt hàng
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(order.getCreatedAt());
            if (date != null) {
                textViewOrderDate.setText("Đặt hàng lúc: " + outputFormat.format(date));
            }
        } catch (Exception e) {
            // Fallback: try another format without microseconds
            try {
                SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = inputFormat2.parse(order.getCreatedAt());
                if (date != null) {
                    textViewOrderDate.setText("Đặt hàng lúc: " + outputFormat.format(date));
                }
            } catch (Exception e2) {
                textViewOrderDate.setText("Đặt hàng lúc: " + order.getCreatedAt());
            }
        }

        // Thông tin giao hàng
        textViewShippingName.setText(order.getShippingFullName());
        textViewShippingPhone.setText(order.getShippingPhone());
        
        String fullAddress = order.getShippingAddressLine1();
        if (order.getShippingAddressLine2() != null && !order.getShippingAddressLine2().isEmpty()) {
            fullAddress += ", " + order.getShippingAddressLine2();
        }
        if (order.getShippingCityState() != null && !order.getShippingCityState().isEmpty()) {
            fullAddress += ", " + order.getShippingCityState();
        }
        textViewShippingAddress.setText(fullAddress);

        // Phương thức vận chuyển
        textViewShipmentMethod.setText(order.getShipmentMethod() != null ? order.getShipmentMethod() : "Giao hàng tiêu chuẩn");

        // Format tiền tệ
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        // Sử dụng các giá trị từ API
        textViewSubtotal.setText(formatter.format(order.getSubtotal()));
        textViewShippingFee.setText(order.getShippingFee() == 0 ? "Miễn phí" : formatter.format(order.getShippingFee()));
        textViewTotal.setText(formatter.format(order.getTotalAmount()));

        // Danh sách sản phẩm
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            itemAdapter.setItems(order.getItems());
        }

        updatePickupStoreSection(order);
    }

    private String getStatusText(String status) {
        if (status == null) return "Không xác định";
        switch (status.toUpperCase()) {
            case "PENDING":
                return "Chờ thanh toán";
            case "PAID":
                return "Đã thanh toán";
            case "PROCESSING":
                return "Đang xử lý";
            case "SHIPPING":
                return "Đang giao hàng";
            case "DELIVERED":
                return "Đã giao hàng";
            case "COMPLETED":
                return "Hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return R.color.text_secondary_light;
        switch (status.toUpperCase()) {
            case "PENDING":
                return R.color.warning_light;
            case "PAID":
                return R.color.success_light;
            case "PROCESSING":
                return R.color.info_light;
            case "SHIPPING":
                return R.color.info_light;
            case "DELIVERED":
            case "COMPLETED":
                return R.color.success_light;
            case "CANCELLED":
                return R.color.error_light;
            default:
                return R.color.text_secondary_light;
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showPaymentConfirmationDialog() {
        if (currentOrder == null) return;

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String totalAmount = formatter.format(currentOrder.getTotalAmount());

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thanh toán")
                .setMessage("Bạn có chắc chắn muốn thanh toán đơn hàng #" + currentOrder.getId() + 
                           " với số tiền " + totalAmount + " qua VNPay?")
                .setPositiveButton("Thanh toán", (dialog, which) -> {
                    // Tạo payment
                    paymentViewModel.createPayment(
                            currentOrder.getId(),
                            "VNPAY",
                            currentOrder.getTotalAmount()
                    );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createVNPayUrl(int paymentId) {
        if (currentOrder == null) return;

        String orderDescription = "Thanh toan don hang #" + currentOrder.getId();
        paymentViewModel.createVNPayUrl(
                paymentId,
                currentOrder.getTotalAmount(),
                orderDescription
        );
    }

    private void openVNPayUrl(String url) {
        try {
            Intent intent = new Intent(this, VNPayPaymentActivity.class);
            intent.putExtra("payment_url", url);
            intent.putExtra("order_id", currentOrder.getId());
            intent.putExtra("payment_id", currentPaymentId);
            intent.putExtra("amount", currentOrder.getTotalAmount());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở trang thanh toán", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePickupStoreSection(OrderDTO order) {
        if (pickupStoreSection == null) {
            return;
        }

        boolean isPickup = order.getShipmentMethod() != null
                && "PICKUP".equalsIgnoreCase(order.getShipmentMethod());

        if (!isPickup) {
            pickupStoreSection.setVisibility(View.GONE);
            if (pickupStoreDivider != null) {
                pickupStoreDivider.setVisibility(View.GONE);
            }
            if (btnPickupStoreDirections != null) {
                btnPickupStoreDirections.setVisibility(View.GONE);
                btnPickupStoreDirections.setOnClickListener(null);
            }
            return;
        }

        String storeName = order.getStoreName();
        if (storeName == null || storeName.trim().isEmpty()) {
            if (order.getStoreLocationId() != null) {
                storeName = getString(R.string.pickup_store_fallback_name, order.getStoreLocationId());
            } else {
                storeName = getString(R.string.pickup_store_title);
            }
        }

        String storeAddress = order.getStoreAddress();
        if (storeAddress == null || storeAddress.trim().isEmpty()) {
            storeAddress = textViewShippingAddress != null ? textViewShippingAddress.getText().toString() : "";
        }

        if (textViewPickupStoreName != null) {
            textViewPickupStoreName.setText(storeName);
        }
        if (textViewPickupStoreAddress != null) {
            textViewPickupStoreAddress.setText(storeAddress);
        }

        pickupStoreSection.setVisibility(View.VISIBLE);
        if (pickupStoreDivider != null) {
            pickupStoreDivider.setVisibility(View.VISIBLE);
        }

    boolean hasCoordinates = order.getStoreLatitude() != null && order.getStoreLongitude() != null;
    boolean hasAddress = storeAddress != null && !storeAddress.trim().isEmpty();
        boolean hasDestination = hasCoordinates || hasAddress;

        if (btnPickupStoreDirections != null) {
            btnPickupStoreDirections.setVisibility(hasDestination ? View.VISIBLE : View.GONE);
            btnPickupStoreDirections.setOnClickListener(v -> openStoreDirections(order));
        }
    }

    private void openStoreDirections(OrderDTO order) {
        String destination = null;
        if (order.getStoreLatitude() != null && order.getStoreLongitude() != null) {
            destination = order.getStoreLatitude() + "," + order.getStoreLongitude();
        } else if (order.getStoreAddress() != null && !order.getStoreAddress().trim().isEmpty()) {
            destination = order.getStoreAddress();
        } else if (order.getShippingAddressLine1() != null) {
            destination = order.getShippingAddressLine1();
        }

        if (destination == null) {
            Toast.makeText(this, R.string.pickup_store_missing_location, Toast.LENGTH_SHORT).show();
            return;
        }

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(destination) + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            String fallback = "https://www.google.com/maps/dir/?api=1&destination="
                    + Uri.encode(destination) + "&travelmode=driving";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallback)));
        }
    }
}
