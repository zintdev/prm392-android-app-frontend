package com.example.prm392_android_app_frontend.presentation.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationResponse;
import com.example.prm392_android_app_frontend.data.dto.store.StoreNearbyDto;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.NominatimApiService;
import com.example.prm392_android_app_frontend.data.remote.api.StoreApi;
import com.example.prm392_android_app_frontend.data.dto.store.NominatimPlace;
import com.example.prm392_android_app_frontend.data.dto.store.StoreLocationRequest;
import com.example.prm392_android_app_frontend.data.remote.api.StoreApiService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddStoreActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "AddStoreActivity";
    private static final int REQUEST_CODE_LOCATION_PICKER = 1001;
    private static final int REQUEST_LOCATION_PERMISSION = 102;

    private EditText etAddressInput, etStoreName;
    private Button btnSearchAddress, btnSaveStore;
    private com.google.android.material.button.MaterialButton btnPickOnMap;
    private TextView tvSelectedAddressInfo;

    private NominatimApiService nominatimService;
    private StoreApiService storeApiService; // Service backend của bạn
    private StoreApi storeApi; // API để lấy danh sách cửa hàng

    private NominatimPlace foundPlace = null;

    // Map variables
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Double myLat, myLng;
    private Marker selectedLocationMarker;
    private List<Marker> storeMarkers = new ArrayList<>();
    private List<StoreNearbyDto> nearbyStores = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_store);

        etStoreName = findViewById(R.id.et_store_name);
        etAddressInput = findViewById(R.id.et_address_input);
        btnSearchAddress = findViewById(R.id.btn_search_address);
        btnPickOnMap = findViewById(R.id.btn_pick_on_map);
        btnSaveStore = findViewById(R.id.btn_save_store);
        tvSelectedAddressInfo = findViewById(R.id.tv_selected_address_info);

        // --- HTTP CLIENT CHO NOMINATIM (GIỮ NGUYÊN) ---
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        nominatimService = ApiClient.get().create(NominatimApiService.class);

        final String userAgent = getApplicationContext().getPackageName();
        Interceptor userAgentInterceptor = chain -> {
            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                    .header("User-Agent", userAgent)
                    .build();
            return chain.proceed(requestWithUserAgent);
        };
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(userAgentInterceptor)
                .addInterceptor(loggingInterceptor)
                .build();
        Retrofit retrofitNominatim = new Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        nominatimService = retrofitNominatim.create(NominatimApiService.class);
        // --- KẾT THÚC HTTP CLIENT ---

        // TODO: Khởi tạo service cho backend của bạn
        // (Bạn phải tự cung cấp Retrofit client cho API backend)
        // Ví dụ:
        // storeApiService = ApiClient.getClient().create(StoreApiService.class);


        btnSearchAddress.setOnClickListener(v -> searchAddress());
        btnPickOnMap.setOnClickListener(v -> openLocationPicker());
        btnSaveStore.setOnClickListener(v -> callCreateStoreApi());

        // Initialize map
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        storeApi = ApiClient.get().create(StoreApi.class);
        
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Configure map
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setMinZoomPreference(10f);
        googleMap.setMaxZoomPreference(18f);

        // Enable my location if permission granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        // Update map when location is found or when address is selected
        updateMapMarkers();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && googleMap != null) {
                myLat = location.getLatitude();
                myLng = location.getLongitude();
                
                // Move camera to current location
                LatLng myLocation = new LatLng(myLat, myLng);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14f));
                
                // Load nearby stores
                loadNearbyStores();
                
                // Update markers
                updateMapMarkers();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get location", e);
        });
    }

    private void loadNearbyStores() {
        if (myLat == null || myLng == null || storeApi == null) return;

        storeApi.getNearby(myLat, myLng, 50.0, 100, null, null)
                .enqueue(new Callback<List<StoreNearbyDto>>() {
                    @Override
                    public void onResponse(Call<List<StoreNearbyDto>> call,
                                         Response<List<StoreNearbyDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            nearbyStores = response.body();
                            updateMapMarkers();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<StoreNearbyDto>> call, Throwable t) {
                        Log.e(TAG, "Failed to load nearby stores", t);
                    }
                });
    }

    private void updateMapMarkers() {
        if (googleMap == null) return;

        // Clear existing store markers
        for (Marker marker : storeMarkers) {
            marker.remove();
        }
        storeMarkers.clear();

        // Add markers for nearby stores
        for (StoreNearbyDto store : nearbyStores) {
            LatLng storeLocation = new LatLng(store.latitude, store.longitude);
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(storeLocation)
                    .title(store.name != null ? store.name : store.address)
                    .snippet(String.format("~%.2f km", store.distanceKm))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            storeMarkers.add(marker);
        }

        // Add marker for selected location if exists
        if (foundPlace != null) {
            try {
                double lat = Double.parseDouble(foundPlace.getLat());
                double lon = Double.parseDouble(foundPlace.getLon());
                LatLng selectedLocation = new LatLng(lat, lon);

                if (selectedLocationMarker != null) {
                    selectedLocationMarker.remove();
                }

                selectedLocationMarker = googleMap.addMarker(new MarkerOptions()
                        .position(selectedLocation)
                        .title("Địa điểm đã chọn")
                        .snippet(foundPlace.getDisplayName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                // Move camera to show all markers
                moveCameraToShowAllMarkers();
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid coordinates", e);
            }
        } else if (myLat != null && myLng != null) {
            // If no selected location, just show current location and stores
            moveCameraToShowAllMarkers();
        }
    }

    private void moveCameraToShowAllMarkers() {
        if (googleMap == null) return;

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();

        // Add current location
        if (myLat != null && myLng != null) {
            bounds.include(new LatLng(myLat, myLng));
        }

        // Add selected location
        if (foundPlace != null) {
            try {
                double lat = Double.parseDouble(foundPlace.getLat());
                double lon = Double.parseDouble(foundPlace.getLon());
                bounds.include(new LatLng(lat, lon));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        // Add all store locations
        for (StoreNearbyDto store : nearbyStores) {
            bounds.include(new LatLng(store.latitude, store.longitude));
        }

        try {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 150));
        } catch (Exception e) {
            // If bounds is empty or invalid, just move to current location
            if (myLat != null && myLng != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(myLat, myLng), 14f));
            }
        }
    }

    private void searchAddress() {
        String query = etAddressInput.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSearchAddress.setEnabled(false);
        btnSaveStore.setEnabled(false);
        tvSelectedAddressInfo.setVisibility(View.GONE);

        nominatimService.search(query, "json", 1).enqueue(new Callback<List<NominatimPlace>>() {
            @Override
            public void onResponse(Call<List<NominatimPlace>> call, Response<List<NominatimPlace>> response) {
                btnSearchAddress.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    foundPlace = response.body().get(0);

                    String info = "Địa chỉ tìm thấy: " + foundPlace.getDisplayName() + "\n" +
                            "Tọa độ: " + foundPlace.getLat() + ", " + foundPlace.getLon();
                    tvSelectedAddressInfo.setText(info);
                    tvSelectedAddressInfo.setVisibility(View.VISIBLE);
                    btnSaveStore.setEnabled(true);
                    
                    // Update map markers
                    updateMapMarkers();
                } else {
                    Toast.makeText(AddStoreActivity.this, "Không tìm thấy địa chỉ", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<List<NominatimPlace>> call, Throwable t) {
                btnSearchAddress.setEnabled(true);
                Log.e(TAG, "Nominatim API Call Failure", t);
                Toast.makeText(AddStoreActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void callCreateStoreApi() {
        String storeName = etStoreName.getText().toString().trim();

        if (storeName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên cửa hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (foundPlace == null) {
            Toast.makeText(this, "Vui lòng tìm kiếm địa chỉ trước", Toast.LENGTH_SHORT).show();
            return;
        }

        storeApiService = ApiClient.get().create(StoreApiService.class);

        // Lấy dữ liệu từ kết quả tìm kiếm (OpenStreetMap)
        BigDecimal latitude = new BigDecimal(foundPlace.getLat());
        BigDecimal longitude = new BigDecimal(foundPlace.getLon());
        String fullAddress = foundPlace.getDisplayName(); // Địa chỉ đầy đủ

        // Tạo request với 4 trường
        StoreLocationRequest requestToSend = new StoreLocationRequest(latitude, longitude, storeName, fullAddress);

        btnSaveStore.setEnabled(false);
        btnSaveStore.setText("Đang lưu...");

        // TODO: Đảm bảo 'storeApiService' đã được khởi tạo
        if (storeApiService == null) {
            Toast.makeText(this, "Lỗi: storeApiService chưa được khởi tạo", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "storeApiService is null. Bạn đã khởi tạo nó trong onCreate() chưa?");
            btnSaveStore.setEnabled(true);
            btnSaveStore.setText("Lưu cửa hàng");
            return;
        }

        // --- BẮT ĐẦU GỌI API BACKEND THẬT ---
        storeApiService.create(requestToSend).enqueue(new Callback<StoreLocationResponse>() {
            @Override
            public void onResponse(Call<StoreLocationResponse> call, Response<StoreLocationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AddStoreActivity.this, "Tạo cửa hàng thành công!", Toast.LENGTH_LONG).show();
                    // Đóng Activity sau khi thành công
                    finish();
                } else {
                    // Xử lý lỗi từ server (ví dụ: 400 Bad Request)
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Lỗi tạo cửa hàng: " + response.code() + " - " + errorBody);
                        Toast.makeText(AddStoreActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Lỗi đọc error body", e);
                    }
                    btnSaveStore.setEnabled(true);
                    btnSaveStore.setText("Lưu cửa hàng");
                }
            }

            @Override
            public void onFailure(Call<StoreLocationResponse> call, Throwable t) {
                // Xử lý lỗi mạng (không có internet, không kết nối được server)
                Log.e(TAG, "Lỗi mạng khi gọi API backend", t);
                Toast.makeText(AddStoreActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                btnSaveStore.setEnabled(true);
                btnSaveStore.setText("Lưu cửa hàng");
            }
        });
        // --- KẾT THÚC GỌI API ---
    }

    /**
     * Mở LocationPickerActivity để chọn vị trí trên bản đồ
     */
    private void openLocationPicker() {
        Intent intent = new Intent(this, LocationPickerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOCATION_PICKER);
    }

    /**
     * Nhận kết quả từ LocationPickerActivity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOCATION_PICKER) {
            if (resultCode == RESULT_OK && data != null) {
                // Lấy dữ liệu từ LocationPicker
                double lat = data.getDoubleExtra("latitude", 0.0);
                double lon = data.getDoubleExtra("longitude", 0.0);
                String address = data.getStringExtra("address");

                if (lat != 0.0 && lon != 0.0) {
                    // Tạo NominatimPlace từ kết quả
                    foundPlace = new NominatimPlace();
                    foundPlace.setLat(String.valueOf(lat));
                    foundPlace.setLon(String.valueOf(lon));
                    foundPlace.setDisplayName(address != null ? address : "");
                    
                    // Cập nhật UI
                    etAddressInput.setText(address);
                    
                    String info = "Địa chỉ đã chọn: " + address + "\n" +
                            "Tọa độ: " + lat + ", " + lon;
                    tvSelectedAddressInfo.setText(info);
                    tvSelectedAddressInfo.setVisibility(View.VISIBLE);
                    btnSaveStore.setEnabled(true);
                    
                    // Update map markers
                    updateMapMarkers();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (googleMap != null) {
                    googleMap.setMyLocationEnabled(true);
                    getCurrentLocation();
                }
            } else {
                Toast.makeText(this, "Cần quyền vị trí để hiển thị bản đồ", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
