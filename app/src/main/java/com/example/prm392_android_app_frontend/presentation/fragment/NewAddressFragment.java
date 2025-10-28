package com.example.prm392_android_app_frontend.presentation.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.address.DistrictDto;
import com.example.prm392_android_app_frontend.data.dto.address.ProvinceDto;
import com.example.prm392_android_app_frontend.data.dto.address.WardDto;
import com.example.prm392_android_app_frontend.presentation.viewmodel.AddressViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class NewAddressFragment extends Fragment {

    private TextInputLayout inputLayoutFullName;
    private TextInputLayout inputLayoutPhone;
    private TextInputLayout inputLayoutProvince;
    private TextInputLayout inputLayoutDistrict;
    private TextInputLayout inputLayoutWard;
    private TextInputLayout inputLayoutAddressLine1;

    private TextInputEditText editTextFullName;
    private TextInputEditText editTextPhone;
    private TextInputEditText editTextCountry;
    private AutoCompleteTextView dropdownProvince;
    private AutoCompleteTextView dropdownDistrict;
    private AutoCompleteTextView dropdownWard;
    private TextInputEditText editTextAddressLine1;

    private AddressViewModel addressViewModel;
    private List<ProvinceDto> provinces = new ArrayList<>();
    private List<DistrictDto> districts = new ArrayList<>();
    private List<WardDto> wards = new ArrayList<>();

    private ProvinceDto selProvince;
    private DistrictDto selDistrict;
    private WardDto selWard;

    private ArrayAdapter<String> provinceAdapter;
    private ArrayAdapter<String> districtAdapter;
    private ArrayAdapter<String> wardAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_order_new_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupViewModel();
        setupAdapters();
        setupListeners();
        loadProvinces();
    }

    private void initViews(View view) {
        inputLayoutFullName = view.findViewById(R.id.input_layout_full_name);
        inputLayoutPhone = view.findViewById(R.id.input_layout_phone);
        inputLayoutProvince = view.findViewById(R.id.input_layout_province);
        inputLayoutDistrict = view.findViewById(R.id.input_layout_district);
        inputLayoutWard = view.findViewById(R.id.input_layout_ward);
        inputLayoutAddressLine1 = view.findViewById(R.id.input_layout_address_line1);

        editTextFullName = view.findViewById(R.id.edit_text_full_name);
        editTextPhone = view.findViewById(R.id.edit_text_phone);
        editTextCountry = view.findViewById(R.id.edit_text_country);
        dropdownProvince = view.findViewById(R.id.dropdown_province);
        dropdownDistrict = view.findViewById(R.id.dropdown_district);
        dropdownWard = view.findViewById(R.id.dropdown_ward);
        editTextAddressLine1 = view.findViewById(R.id.edit_text_address_line1);
    }

    private void setupViewModel() {
        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);
        
        // Observe provinces
        addressViewModel.getProvincesLiveData().observe(getViewLifecycleOwner(), provinceList -> {
            if (provinceList != null) {
                android.util.Log.d("NewAddressFragment", "Received " + provinceList.size() + " provinces");
                provinces.clear();
                provinces.addAll(provinceList);
                updateProvinceAdapter();
            } else {
                android.util.Log.d("NewAddressFragment", "Received null province list");
            }
        });

        // Observe error messages
        addressViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                android.util.Log.e("NewAddressFragment", "Error: " + error);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe districts
        addressViewModel.getDistrictsLiveData().observe(getViewLifecycleOwner(), districtList -> {
            if (districtList != null) {
                android.util.Log.d("NewAddressFragment", "Received " + districtList.size() + " districts");
                districts.clear();
                districts.addAll(districtList);
                updateDistrictAdapter();
                // Clear district and ward selections only after districts are loaded
                dropdownDistrict.setText("");
                dropdownWard.setText("");
                selDistrict = null;
                selWard = null;
            }
        });

        // Observe wards
        addressViewModel.getWardsLiveData().observe(getViewLifecycleOwner(), wardList -> {
            if (wardList != null) {
                android.util.Log.d("NewAddressFragment", "Received " + wardList.size() + " wards");
                wards.clear();
                wards.addAll(wardList);
                updateWardAdapter();
                // Clear ward selection only after wards are loaded
                dropdownWard.setText("");
                selWard = null;
            }
        });
    }

    private void setupAdapters() {
        provinceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        districtAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        wardAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());

        dropdownProvince.setAdapter(provinceAdapter);
        dropdownDistrict.setAdapter(districtAdapter);
        dropdownWard.setAdapter(wardAdapter);
    }

    private void setupListeners() {
        // Province selection listener
        dropdownProvince.setOnItemClickListener((parent, view, position, id) -> {
            selProvince = provinces.get(position);
            android.util.Log.d("NewAddressFragment", "Selected province: " + selProvince.getName() + " (code: " + selProvince.getCode() + ")");
            // Load districts for selected province (don't clear yet, will be cleared when data arrives)
            addressViewModel.loadDistricts(selProvince.getCode());
        });

        // District selection listener
        dropdownDistrict.setOnItemClickListener((parent, view, position, id) -> {
            selDistrict = districts.get(position);
            android.util.Log.d("NewAddressFragment", "Selected district: " + selDistrict.getName() + " (code: " + selDistrict.getCode() + ")");
            // Load wards for selected district (don't clear yet, will be cleared when data arrives)
            addressViewModel.loadWards(selDistrict.getCode());
        });

        // Ward selection listener
        dropdownWard.setOnItemClickListener((parent, view, position, id) -> {
            selWard = wards.get(position);
            android.util.Log.d("NewAddressFragment", "Selected ward: " + selWard.getName());
        });
    }

    private void loadProvinces() {
        android.util.Log.d("NewAddressFragment", "Loading provinces...");
        addressViewModel.loadProvinces();
    }

    private void updateProvinceAdapter() {
        List<String> provinceNames = new ArrayList<>();
        for (ProvinceDto province : provinces) {
            provinceNames.add(province.getName());
        }
        provinceAdapter.clear();
        provinceAdapter.addAll(provinceNames);
        provinceAdapter.notifyDataSetChanged();
    }

    private void updateDistrictAdapter() {
        List<String> districtNames = new ArrayList<>();
        for (DistrictDto district : districts) {
            districtNames.add(district.getName());
        }
        districtAdapter.clear();
        districtAdapter.addAll(districtNames);
        districtAdapter.notifyDataSetChanged();
    }

    private void updateWardAdapter() {
        List<String> wardNames = new ArrayList<>();
        for (WardDto ward : wards) {
            wardNames.add(ward.getName());
        }
        wardAdapter.clear();
        wardAdapter.addAll(wardNames);
        wardAdapter.notifyDataSetChanged();
        android.util.Log.d("NewAddressFragment", "Updated ward adapter with " + wardNames.size() + " wards");
    }

    public String getFullName() {
        return editTextFullName != null ? editTextFullName.getText().toString().trim() : "";
    }

    public String getPhone() {
        return editTextPhone != null ? editTextPhone.getText().toString().trim() : "";
    }

    public String getAddressLine1() {

        return editTextAddressLine1 != null ? editTextAddressLine1.getText().toString().trim() : "";
    }

    public String getAddressLine2() {

        String ward = selWard != null ? selWard.getName() : "";
        String district = selDistrict != null ? selDistrict.getName() : "";
        
        StringBuilder addressLine2 = new StringBuilder();
        if (!ward.isEmpty()) addressLine2.append(ward);
        if (!district.isEmpty()) {
            if (addressLine2.length() > 0) addressLine2.append(", ");
            addressLine2.append(district);
        }
        
        return addressLine2.toString();
    }

    public String getCityState() {
 
        return selProvince != null ? selProvince.getName() : "";
    }

    public String getFullAddress() {
 
        String address = editTextAddressLine1 != null ? editTextAddressLine1.getText().toString().trim() : "";
        String ward = selWard != null ? selWard.getName() : "";
        String district = selDistrict != null ? selDistrict.getName() : "";
        String province = selProvince != null ? selProvince.getName() : "";
        
        StringBuilder fullAddress = new StringBuilder();
        if (!address.isEmpty()) fullAddress.append(address);
        if (!ward.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(ward);
        }
        if (!district.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(district);
        }
        if (!province.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(province);
        }
        
        return fullAddress.toString();
    }

    // Validation methods
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

        if (selProvince == null) {
            inputLayoutProvince.setError("Vui lòng chọn tỉnh/thành phố");
            isValid = false;
        } else {
            inputLayoutProvince.setError(null);
        }

        if (selDistrict == null) {
            inputLayoutDistrict.setError("Vui lòng chọn quận/huyện");
            isValid = false;
        } else {
            inputLayoutDistrict.setError(null);
        }

        if (selWard == null) {
            inputLayoutWard.setError("Vui lòng chọn phường/xã");
            isValid = false;
        } else {
            inputLayoutWard.setError(null);
        }

        if (getAddressLine1().isEmpty()) {
            inputLayoutAddressLine1.setError("Vui lòng nhập địa chỉ nhận hàng");
            isValid = false;
        } else {
            inputLayoutAddressLine1.setError(null);
        }

        return isValid;
    }
}