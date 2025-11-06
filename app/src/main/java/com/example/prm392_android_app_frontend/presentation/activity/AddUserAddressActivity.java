package com.example.prm392_android_app_frontend.presentation.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.address.AddressDto;
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

    private TextInputEditText edtFullName, edtPhoneNumber, edtAddress;
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

        edtFullName = findViewById(R.id.edtFullName);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
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
                if (data == null || data.isEmpty()) {
                    enableManualMode("Không lấy được danh sách Tỉnh/Thành. Vui lòng nhập tay.");
                    return;
                }
                provinces = data;
                manualMode = false;
                bindProvinceAdapter();
                useDropdownMode(tilProvince, ddProvince, true);
                useDropdownMode(tilDistrict, ddDistrict, true);
                useDropdownMode(tilWard, ddWard, true);
                clearPlaceholders();
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
                useDropdownMode(tilDistrict, ddDistrict, true);
                tilDistrict.setPlaceholderText(null);
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
        String fullName = edtFullName.getText() == null ? "" : edtFullName.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText() == null ? "" : edtPhoneNumber.getText().toString().trim();
        String address = edtAddress.getText() == null ? "" : edtAddress.getText().toString().trim();
        String ward    = selWard != null ? selWard.name : "";
        String district= selDistrict != null ? selDistrict.name : "";
        String province= selProvince != null ? selProvince.name : "";

        if (fullName.isEmpty()) { toast("Vui lòng nhập họ tên người nhận"); return; }
        if (phoneNumber.isEmpty()) { toast("Vui lòng nhập số điện thoại"); return; }
        if (address.isEmpty()) { toast("Vui lòng nhập số nhà, tên đường"); return; }
        if (province.isEmpty()) { toast("Vui lòng chọn Tỉnh/Thành phố"); return; }
        if (ward.isEmpty())     { toast("Vui lòng chọn Phường/Xã"); return; }
        if (district.isEmpty()) { toast("Vui lòng chọn Quận/Huyện"); return; }

        String line1 = address + ", " + ward;
        String line2 = district;
        String cityState = province;

        int userId = TokenStore.getUserId(this);

        showLoading(true);
        addressRepo.createAddress(userId, fullName, phoneNumber, line1, line2, cityState,
                new AddressRepository.CallbackResult<AddressDto>() {
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

    private void enableManualMode(String reason) {
        manualMode = true;
        toast(reason);

        // Gỡ adapter để không hiện menu
        ddProvince.setAdapter(null);
        ddDistrict.setAdapter(null);
        ddWard.setAdapter(null);

        // Ẩn icon dropdown + bật bàn phím
        useDropdownMode(tilProvince, ddProvince, false);
        useDropdownMode(tilDistrict, ddDistrict, false);
        useDropdownMode(tilWard, ddWard, false);

        // Placeholder gợi ý (đặt ở parent để không chồng label)
        setPlaceholdersForManual(true, true, true);

        // Xóa lựa chọn object
        selProvince = null; selDistrict = null; selWard = null;
    }

    private void useDropdownMode(TextInputLayout til, AutoCompleteTextView view, boolean dropdown) {
        til.setEndIconMode(dropdown
                ? TextInputLayout.END_ICON_DROPDOWN_MENU
                : TextInputLayout.END_ICON_NONE);

        if (dropdown) {
            // Khóa gõ tay cho dropdown (đúng UX của ExposedDropdownMenu)
            view.setInputType(InputType.TYPE_NULL);
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
            view.setClickable(true);
        } else {
            // Cho phép gõ tay
            view.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setClickable(true);
        }
        // Không đặt hint ở con để tránh đè label
        view.setHint(null);
    }

    private void clearPlaceholders() {
        tilProvince.setPlaceholderText(null);
        tilDistrict.setPlaceholderText(null);
        tilWard.setPlaceholderText(null);
    }

    private void setPlaceholdersForManual(boolean p, boolean d, boolean w) {
        if (p) tilProvince.setPlaceholderText("Nhập Tỉnh/Thành phố (gõ tay)");
        if (d) tilDistrict.setPlaceholderText("Nhập Quận/Huyện (gõ tay)");
        if (w) tilWard.setPlaceholderText("Nhập Phường/Xã (gõ tay)");
    }

    // =================== Utils ===================

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
