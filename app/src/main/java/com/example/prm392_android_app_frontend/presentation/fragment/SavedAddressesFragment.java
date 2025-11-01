package com.example.prm392_android_app_frontend.presentation.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.address.AddressDto;
import com.example.prm392_android_app_frontend.presentation.adapter.SavedAddressAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.AddressViewModel;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class SavedAddressesFragment extends Fragment implements SavedAddressAdapter.OnAddressSelectedListener {

    private RecyclerView recyclerViewSavedAddresses;
    private LinearLayout layoutNoAddresses;
    private ProgressBar progressBarAddresses;
    private SavedAddressAdapter addressAdapter;
    private AddressViewModel addressViewModel;

    // Form fields for personal info
    private TextInputLayout inputLayoutFullName;
    private TextInputLayout inputLayoutPhone;
    private TextInputEditText editTextFullName;
    private TextInputEditText editTextPhone;

    private AddressDto selectedAddress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_order_saved_addresses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        loadAddresses();
    }

    private void initViews(View view) {
        recyclerViewSavedAddresses = view.findViewById(R.id.recycler_view_saved_addresses);
        layoutNoAddresses = view.findViewById(R.id.layout_no_addresses);
        progressBarAddresses = view.findViewById(R.id.progress_bar_addresses);

        inputLayoutFullName = view.findViewById(R.id.input_layout_full_name);
        inputLayoutPhone = view.findViewById(R.id.input_layout_phone);
        editTextFullName = view.findViewById(R.id.edit_text_full_name);
        editTextPhone = view.findViewById(R.id.edit_text_phone);
    }

    private void setupViewModel() {
        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);

        addressViewModel.getAddressesLiveData().observe(getViewLifecycleOwner(), addresses -> {
            hideLoading();
            if (addresses != null && !addresses.isEmpty()) {
                showAddressesList(addresses);
            } else {
                showNoAddresses();
            }
        });

        addressViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            hideLoading();
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                showNoAddresses();
            }
        });
    }

    private void setupRecyclerView() {
        addressAdapter = new SavedAddressAdapter(null, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewSavedAddresses.setLayoutManager(layoutManager);
        recyclerViewSavedAddresses.setAdapter(addressAdapter);
        
        // Cho phép RecyclerView xử lý touch events khi scroll ngang
        recyclerViewSavedAddresses.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {
                int action = e.getAction();
                if (action == android.view.MotionEvent.ACTION_MOVE) {
                    rv.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
    }

    private void loadAddresses() {
        if (!TokenStore.isLoggedIn(getContext())) {
            showNoAddresses();
            return;
        }

        showLoading();
        int userId = TokenStore.getUserId(getContext());
        if (userId != -1) {
            addressViewModel.getAddressesByUserId(userId);
        } else {
            hideLoading();
            showNoAddresses();
        }
    }

    private void showLoading() {
        progressBarAddresses.setVisibility(View.VISIBLE);
        recyclerViewSavedAddresses.setVisibility(View.GONE);
        layoutNoAddresses.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBarAddresses.setVisibility(View.GONE);
    }

    private void showAddressesList(List<AddressDto> addresses) {
        recyclerViewSavedAddresses.setVisibility(View.VISIBLE);
        layoutNoAddresses.setVisibility(View.GONE);
        addressAdapter.updateAddresses(addresses);
    }

    private void showNoAddresses() {
        recyclerViewSavedAddresses.setVisibility(View.GONE);
        layoutNoAddresses.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAddressSelected(AddressDto address, int position) {
        selectedAddress = address;
        
        // Tự động điền thông tin từ địa chỉ đã chọn
        if (address != null) {
            if (address.fullName != null && !address.fullName.isEmpty()) {
                editTextFullName.setText(address.fullName);
            }
            if (address.phoneNumber != null && !address.phoneNumber.isEmpty()) {
                editTextPhone.setText(address.phoneNumber);
            }
        }
        
        Toast.makeText(getContext(), "Đã chọn địa chỉ", Toast.LENGTH_SHORT).show();
    }

    // Public methods để OrderCreateActivity có thể lấy dữ liệu
    public String getFullName() {
        return editTextFullName != null ? editTextFullName.getText().toString().trim() : "";
    }

    public String getPhone() {
        return editTextPhone != null ? editTextPhone.getText().toString().trim() : "";
    }

    public String getAddressLine1() {
        return selectedAddress != null ? selectedAddress.shippingAddressLine1 : "";
    }

    public String getAddressLine2() {
        return selectedAddress != null ? selectedAddress.shippingAddressLine2 : "";
    }

    public String getCityState() {
        return selectedAddress != null ? selectedAddress.shippingCityState : "";
    }

    public AddressDto getSelectedAddress() {
        return selectedAddress;
    }

    public boolean validateInputs() {
        boolean isValid = true;

        // Validate tên
        if (getFullName().isEmpty()) {
            inputLayoutFullName.setError("Vui lòng nhập họ tên");
            isValid = false;
        } else {
            inputLayoutFullName.setError(null);
        }

        // Validate số điện thoại
        String phone = getPhone();
        if (phone.isEmpty()) {
            inputLayoutPhone.setError("Vui lòng nhập số điện thoại");
            isValid = false;
        } else if (!phone.matches("^[0-9]{10,11}$")) {
            inputLayoutPhone.setError("Số điện thoại không hợp lệ");
            isValid = false;
        } else {
            inputLayoutPhone.setError(null);
        }

        // Validate address selection
        if (selectedAddress == null) {
            Toast.makeText(getContext(), "Vui lòng chọn một địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }
}