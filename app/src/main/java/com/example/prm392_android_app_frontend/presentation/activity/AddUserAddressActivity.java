package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.address.DistrictDto;
import com.example.prm392_android_app_frontend.data.dto.address.ProvinceDto;
import com.example.prm392_android_app_frontend.data.dto.address.WardDto;
import com.example.prm392_android_app_frontend.data.repository.AddressRepository;
import com.example.prm392_android_app_frontend.data.repository.ProvincesRepository;
import com.example.prm392_android_app_frontend.storage.TokenStore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddUserAddressActivity extends AppCompatActivity {

    private TextInputEditText edtAddress;
    private AutoCompleteTextView ddProvince, ddDistrict, ddWard;
    private MaterialButton btnConfirm;
    private View progress;

    private final ProvincesRepository provinceRepo = new ProvincesRepository();
    private AddressRepository addressRepo;

    private List<ProvinceDto> provinces = new ArrayList<>();
    private List<DistrictDto> districts = new ArrayList<>();
    private List<WardDto> wards = new ArrayList<>();

    private ProvinceDto selProvince;
    private DistrictDto selDistrict;
    private WardDto selWard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        addressRepo = new AddressRepository(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        edtAddress  = findViewById(R.id.edtAddress);
        ddProvince  = findViewById(R.id.ddProvince);
        ddDistrict  = findViewById(R.id.ddDistrict);
        ddWard      = findViewById(R.id.ddWard);
        btnConfirm  = findViewById(R.id.btnConfirm);
        progress    = findViewById(R.id.progress);

        // Load Provinces
        loadProvinces();

        ddProvince.setOnItemClickListener((parent, view, position, id) -> {
            selProvince = provinces.get(position);
            ddDistrict.setText(""); ddWard.setText("");
            districts.clear(); wards.clear();
            loadDistricts(selProvince.code);
        });

        ddDistrict.setOnItemClickListener((parent, view, position, id) -> {
            selDistrict = districts.get(position);
            ddWard.setText("");
            wards.clear();
            loadWards(selDistrict.code);
        });

        ddWard.setOnItemClickListener((parent, view, position, id) -> selWard = wards.get(position));

        // Gửi địa chỉ lên backend
        btnConfirm.setOnClickListener(v -> submitAddress());
    }

    private void loadProvinces() {
        showLoading(true);
        provinceRepo.getProvinces(new ProvincesRepository.SimpleCallback<List<ProvinceDto>>() {
            @Override public void onSuccess(List<ProvinceDto> data) {
                showLoading(false);
                provinces = data != null ? data : new ArrayList<>();
                ddProvince.setAdapter(new ArrayAdapter<>(
                        AddUserAddressActivity.this,
                        android.R.layout.simple_list_item_1,
                        mapProvinceNames(provinces)
                ));
            }
            @Override public void onError(String message) {
                showLoading(false); toast(message);
            }
        });
    }

    private void loadDistricts(int provinceCode) {
        showLoading(true);
        provinceRepo.getDistrictsByProvince(provinceCode, new ProvincesRepository.SimpleCallback<List<DistrictDto>>() {
            @Override public void onSuccess(List<DistrictDto> data) {
                showLoading(false);
                districts = data != null ? data : new ArrayList<>();
                ddDistrict.setAdapter(new ArrayAdapter<>(
                        AddUserAddressActivity.this,
                        android.R.layout.simple_list_item_1,
                        mapDistrictNames(districts)
                ));
            }
            @Override public void onError(String message) {
                showLoading(false); toast(message);
            }
        });
    }

    private void loadWards(int districtCode) {
        showLoading(true);
        provinceRepo.getWardsByDistrict(districtCode, new ProvincesRepository.SimpleCallback<List<WardDto>>() {
            @Override public void onSuccess(List<WardDto> data) {
                showLoading(false);
                wards = data != null ? data : new ArrayList<>();
                ddWard.setAdapter(new ArrayAdapter<>(
                        AddUserAddressActivity.this,
                        android.R.layout.simple_list_item_1,
                        mapWardNames(wards)
                ));
            }
            @Override public void onError(String message) {
                showLoading(false); toast(message);
            }
        });
    }

    private void submitAddress() {
        String address = edtAddress.getText() == null ? "" : edtAddress.getText().toString().trim();
        String ward    = selWard != null ? selWard.name : "";
        String district= selDistrict != null ? selDistrict.name : "";
        String province= selProvince != null ? selProvince.name : "";

        if (address.isEmpty()) { toast("Vui lòng nhập số nhà, tên đường"); return; }
        if (province.isEmpty()) { toast("Vui lòng chọn Tỉnh/Thành phố"); return; }
        if (ward.isEmpty())     { toast("Vui lòng chọn Phường/Xã"); return; }
        if (district.isEmpty()) { toast("Vui lòng chọn Quận/Huyện"); return; }

        String line1 = address + ", " + ward;
        String line2 = district;
        String cityState = province;

        int userId = TokenStore.getUserId(this);

        showLoading(true);
        addressRepo.createAddress(userId, line1, line2, cityState,
                new AddressRepository.CallbackResult<com.example.prm392_android_app_frontend.data.dto.address.AddressDto>() {
                    @Override public void onSuccess(com.example.prm392_android_app_frontend.data.dto.address.AddressDto data) {
                        showLoading(false);
                        toast("Đã thêm địa chỉ thành công");
                        finish();
                    }

                    @Override public void onError(String message, int code) {
                        showLoading(false);
                        toast("Không thể thêm địa chỉ: " + message + (code>0?(" ("+code+")"):""));
                    }
                });
    }

    // Helpers
    private List<String> mapProvinceNames(List<ProvinceDto> list){
        List<String> r = new ArrayList<>(); for (ProvinceDto p : list) r.add(p.name); return r;
    }
    private List<String> mapDistrictNames(List<DistrictDto> list){
        List<String> r = new ArrayList<>(); for (DistrictDto d : list) r.add(d.name); return r;
    }
    private List<String> mapWardNames(List<WardDto> list){
        List<String> r = new ArrayList<>(); for (WardDto w : list) r.add(w.name); return r;
    }

    private void showLoading(boolean b){ if (progress!=null) progress.setVisibility(b? View.VISIBLE : View.GONE); }
    private void toast(String m){ Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}
