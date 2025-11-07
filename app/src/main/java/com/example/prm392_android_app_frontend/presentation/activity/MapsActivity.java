package com.example.prm392_android_app_frontend.presentation.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.core.util.Resource;
import com.example.prm392_android_app_frontend.data.dto.CartItemDto;
import com.example.prm392_android_app_frontend.data.dto.store.StoreNearbyDto;
import com.example.prm392_android_app_frontend.data.repository.StoreRepository;
import com.example.prm392_android_app_frontend.presentation.adapter.StoreAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.StoreViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQ_LOC = 101;
    private static final String TAG = "MapsActivity";
    public static final String EXTRA_CART_ITEMS = "extra_cart_items";
    public static final String EXTRA_SELECTED_STORE_ID = "extra_selected_store_id";
    public static final String RESULT_STORE_ID = "result_store_id";
    public static final String RESULT_STORE_NAME = "result_store_name";
    public static final String RESULT_STORE_ADDRESS = "result_store_address";
    public static final String RESULT_STORE_DISTANCE = "result_store_distance";
    private static final double DEFAULT_RADIUS_KM = 20.0;

    private GoogleMap mMap;
    private View stateOverlay;

    private FusedLocationProviderClient fused;
    private Double myLat, myLng;

    private StoreAdapter storeAdapter;

    private StoreNearbyDto selectedStore;

    private StoreViewModel viewModel;

    private MaterialToolbar toolbar;
    private TextView txtCount;
    private TextInputEditText edtSearch;
    private TextInputEditText edtRadius;
    private RecyclerView storeRecycler;

    private final Map<Integer, Marker> markerByStore = new HashMap<>();
    private final StoreRepository storeRepository = new StoreRepository();
    private final Map<Integer, Integer> cartRequirements = new HashMap<>();
    private Integer preselectedStoreId;
    private boolean markerHintShown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_map);

        toolbar = findViewById(R.id.toolbar_map);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        ArrayList<CartItemDto> cartItems = getIntent().getParcelableArrayListExtra(EXTRA_CART_ITEMS);
        if (cartItems != null) {
            for (CartItemDto item : cartItems) {
                if (item == null) {
                    continue;
                }
                int productId = item.getProductId();
                int quantity = Math.max(0, item.getQuantity());
                if (productId <= 0 || quantity <= 0) {
                    continue;
                }
                int current = cartRequirements.containsKey(productId) ? cartRequirements.get(productId) : 0;
                cartRequirements.put(productId, current + quantity);
            }
        }
        int initialStoreId = getIntent().getIntExtra(EXTRA_SELECTED_STORE_ID, -1);
        if (initialStoreId != -1) {
            preselectedStoreId = initialStoreId;
        }

        int playStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (playStatus != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(playStatus)) {
                android.app.Dialog dlg =
                        GoogleApiAvailability.getInstance().getErrorDialog(this, playStatus, 9000);
                if (dlg != null) {
                    dlg.show();
                }
            } else {
                Toast.makeText(this, "Thiết bị chưa có Google Play services cho Maps.", Toast.LENGTH_LONG).show();
            }
            return;
        }

        fused = LocationServices.getFusedLocationProviderClient(this);

        stateOverlay = findViewById(R.id.stateOverlay);
        txtCount = findViewById(R.id.txtCount);
        edtSearch = findViewById(R.id.edtSearch);
        edtRadius = findViewById(R.id.edtRadius);

        storeRecycler = findViewById(R.id.rvStores);
        if (storeRecycler != null) {
            storeRecycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
            storeRecycler.setItemAnimator(null);
        }

        storeAdapter = new StoreAdapter(new StoreAdapter.Callbacks() {
            @Override
            public void onPreview(StoreNearbyDto item) {
                selectStore(item, true);
            }

            @Override
            public void onConfirm(StoreNearbyDto item) {
                selectStore(item, true);
                returnSelection();
            }
        });
        if (storeRecycler != null) {
            storeRecycler.setAdapter(storeAdapter);
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            Toast.makeText(this, "Không tìm thấy MapFragment (id=map). Kiểm tra layout.", Toast.LENGTH_LONG).show();
            return;
        }
        mapFragment.getMapAsync(this);

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(StoreViewModel.class);
        viewModel.nearby.observe(this, res -> {
            if (res == null) {
                return;
            }
            if (res.isLoading()) {
                showLoading(true, getString(R.string.loading));
            } else if (res.isError()) {
                showLoading(false, null);
                Toast.makeText(this,
                        res.getMessage() != null ? res.getMessage() : getString(R.string.store_nearby_generic_error),
                        Toast.LENGTH_SHORT).show();
            } else if (res.isSuccess()) {
                showLoading(false, null);
                List<StoreNearbyDto> list = res.getData();
                if (list == null) {
                    list = Collections.emptyList();
                }
                storeAdapter.submit(list);
                reconcileSelectedStore(list);
                refreshMarkersFromAdapter();
                updateStoreCount(txtCount);
                if (storeRecycler != null) {
                    storeRecycler.scrollToPosition(0);
                }
            }
        });

        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    storeAdapter.filter(String.valueOf(s));
                    if (selectedStore != null && !storeAdapter.containsStore(selectedStore.storeId)) {
                        selectStore(null, false);
                    }
                    updateStoreCount(txtCount);
                    refreshMarkersFromAdapter();
                }

                @Override public void afterTextChanged(Editable s) { }
            });
        }

        View btnApply = findViewById(R.id.btnApply);
        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                Double radius = parseRadius(edtRadius);
                if (myLat != null && myLng != null) {
                    triggerLoadNearby(myLat, myLng, radius, 50, null, null);
                } else {
                    Toast.makeText(this, "Chưa có vị trí của bạn. Nhấn icon định vị trước.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View btnShowAll = findViewById(R.id.btnShowAll);
        if (btnShowAll != null) {
            btnShowAll.setOnClickListener(v -> {
                Double radius = parseRadius(edtRadius);
                if (edtSearch != null) {
                    edtSearch.setText("");
                }
                if (myLat != null && myLng != null) {
                    triggerLoadNearby(myLat, myLng, radius, 999, null, null);
                } else {
                    Toast.makeText(this, "Chưa có vị trí của bạn. Nhấn icon định vị trước.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View fabMy = findViewById(R.id.fabMyLocation);
        if (fabMy != null) {
            fabMy.setOnClickListener(v -> moveToMyLocation());
        }
        View fabRef = findViewById(R.id.fabRefresh);
        if (fabRef != null) {
            fabRef.setOnClickListener(v -> {
                if (myLat != null && myLng != null) {
                    triggerLoadNearby(myLat, myLng, DEFAULT_RADIUS_KM, 50, null, null);
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // TUYỆT ĐỐI không tạo biến cục bộ; gán vào field
        this.mMap = googleMap;

        if (this.mMap == null) {
            Log.e(TAG, "GoogleMap is null in onMapReady");
            return;
        }

        // Tùy chọn: UI
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMinZoomPreference(11.5f);

        // Marker click: chỉ chọn + zoom, KHÔNG mở Google Maps
        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof StoreNearbyDto) {
                selectStore((StoreNearbyDto) tag, true);
            } else {
                marker.showInfoWindow();
            }
            return true; // tự xử lý
        });

        // InfoWindow click: chỉ nhắc bấm GO
        mMap.setOnInfoWindowClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof StoreNearbyDto) {
                selectStore((StoreNearbyDto) tag, false);
                returnSelection();
            }
        });

        // Cuối cùng mới bật vị trí & load data
        enableMyLocation();
    }


    private void enableMyLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC);
                return;
            }
            if (mMap != null) mMap.setMyLocationEnabled(true);
        } catch (SecurityException se) {
            Log.e(TAG, "setMyLocationEnabled SecurityException", se);
        }

        fused.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) {
                myLat = loc.getLatitude();
                myLng = loc.getLongitude();
                moveCamera(new LatLng(myLat, myLng), 14f);
                triggerLoadNearby(myLat, myLng, 20.0, 50, null, null);
            } else {
                Toast.makeText(this, "Không lấy được vị trí. Bật GPS rồi thử.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "getLastLocation fail", e);
            Toast.makeText(this, "Lỗi vị trí: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void moveToMyLocation() {
        if (myLat != null && myLng != null) moveCamera(new LatLng(myLat, myLng), 15f);
        else enableMyLocation();
    }

    private void moveCamera(LatLng target, float zoom) {
        if (mMap != null) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, zoom));
    }

    private void showLoading(boolean show, String msg) {
        if (stateOverlay != null) stateOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        if (msg != null) {
            View tv = findViewById(R.id.txtStatus);
            if (tv instanceof android.widget.TextView)
                ((android.widget.TextView) tv).setText(msg);
        }
    }

    private void updateStoreCount(TextView txtCount) {
        if (txtCount != null && storeAdapter != null) {
            int filtered = storeAdapter.getFilteredCount();
            int total = storeAdapter.getTotalCount();
            String text = filtered == total
                    ? filtered + " kết quả"
                    : filtered + "/" + total + " kết quả";
            txtCount.setText(text);
        }
    }

    private double parseRadius(com.google.android.material.textfield.TextInputEditText edtRadius) {
        double radius = 20.0;
        if (edtRadius != null) {
            try {
                String value = String.valueOf(edtRadius.getText()).trim();
                if (!value.isEmpty()) {
                    radius = Double.parseDouble(value);
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Parse radius fail", e);
            }
        }
        return radius;
    }

    private void triggerLoadNearby(double lat, double lng, Double radiusKm, Integer limit,
                                   Integer productId, Boolean inStockOnly) {
        if (!cartRequirements.isEmpty()) {
            loadStoresForCart(lat, lng, radiusKm, limit);
        } else {
            viewModel.loadNearby(lat, lng, radiusKm, limit, productId, inStockOnly);
        }
    }

    private void loadStoresForCart(double lat, double lng, Double radiusKm, Integer limit) {
        if (cartRequirements.isEmpty()) {
            viewModel.loadNearby(lat, lng, radiusKm, limit, null, true);
            return;
        }

        viewModel.nearby.postValue(Resource.loading());
        ConcurrentHashMap<Integer, StoreMatch> matches = new ConcurrentHashMap<>();
        AtomicInteger remaining = new AtomicInteger(cartRequirements.size());
        AtomicBoolean hasError = new AtomicBoolean(false);
        int fetchLimit = limit != null ? limit : 100;

        for (Map.Entry<Integer, Integer> entry : cartRequirements.entrySet()) {
            int productId = entry.getKey();
            int requiredQty = entry.getValue();
            storeRepository.getNearby(lat, lng, radiusKm, fetchLimit, productId, true,
                    new StoreRepository.Result<List<StoreNearbyDto>>() {
                        @Override
                        public void onSuccess(List<StoreNearbyDto> data) {
                            if (hasError.get()) {
                                return;
                            }

                            List<StoreNearbyDto> filtered = new ArrayList<>();
                            if (data != null) {
                                for (StoreNearbyDto dto : data) {
                                    int available = dto.quantity != null ? dto.quantity : 0;
                                    if (available >= requiredQty) {
                                        if (radiusKm == null || dto.distanceKm <= radiusKm + 1e-6) {
                                            filtered.add(dto);
                                        }
                                    }
                                }
                            }

                            for (StoreNearbyDto dto : filtered) {
                                matches.compute(dto.storeId, (id, match) -> {
                                    StoreMatch storeMatch = match != null ? match : new StoreMatch();
                                    storeMatch.merge(dto);
                                    return storeMatch;
                                });
                            }

                            if (remaining.decrementAndGet() == 0 && !hasError.get()) {
                                publishMatches(matches, cartRequirements.size(), fetchLimit);
                            }
                        }

                        @Override
                        public void onError(String message) {
                            if (hasError.compareAndSet(false, true)) {
                                viewModel.nearby.postValue(Resource.error(
                                        message != null ? message : getString(R.string.store_nearby_generic_error)));
                            }
                        }
                    });
        }
    }

    private void publishMatches(Map<Integer, StoreMatch> matches, int requiredCount, int limit) {
        List<StoreNearbyDto> results = new ArrayList<>();
        for (StoreMatch match : matches.values()) {
            if (match.matchedProducts >= requiredCount && match.store != null) {
                results.add(match.store);
            }
        }
        results.sort(Comparator.comparingDouble(s -> s.distanceKm));
        if (limit > 0 && results.size() > limit) {
            results = new ArrayList<>(results.subList(0, limit));
        }
        viewModel.nearby.postValue(Resource.success(results));
    }

    private void refreshMarkersFromAdapter() {
        if (storeAdapter != null) {
            renderMarkers(storeAdapter.getVisibleItems());
        }
    }

    private void renderMarkers(List<StoreNearbyDto> stores) {
        if (mMap == null || stores == null) return;
        mMap.clear();
        markerByStore.clear();

        if (myLat != null && myLng != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(myLat, myLng))
                    .title("Tôi")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        boolean hasAny = false;
        if (myLat != null) {
            bounds.include(new LatLng(myLat, myLng));
            hasAny = true;
        }

        for (StoreNearbyDto s : stores) {
            LatLng p = new LatLng(s.latitude, s.longitude);
            String title = (s.name != null && !s.name.isEmpty()) ? s.name : s.address;
            String snippet = String.format("~%.2f km%s",
                    s.distanceKm,
                    s.quantity != null ? (" • Còn: " + s.quantity) : "");
            Marker marker = mMap.addMarker(new MarkerOptions().position(p).title(title).snippet(snippet));
            if (marker != null) {
                marker.setTag(s);
                markerByStore.put(s.storeId, marker);
            }
            bounds.include(p);
            hasAny = true;
        }

        if (hasAny) {
            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));
            } catch (Exception ignore) {
            }
        }

        if (!markerHintShown && !stores.isEmpty()) {
            markerHintShown = true;
            Toast.makeText(this, "Chạm marker hoặc dùng nút Go trên thẻ để mở chỉ đường.", Toast.LENGTH_LONG).show();
        }

        if (selectedStore != null) {
            highlightSelectedMarker(true);
        }
    }

    private void reconcileSelectedStore(List<StoreNearbyDto> stores) {
        if (stores == null || stores.isEmpty()) {
            selectStore(null, false);
            return;
        }

        StoreNearbyDto candidate = null;
        if (selectedStore != null) {
            for (StoreNearbyDto dto : stores) {
                if (dto.storeId == selectedStore.storeId) {
                    candidate = dto;
                    break;
                }
            }
        }

        if (candidate == null && preselectedStoreId != null) {
            for (StoreNearbyDto dto : stores) {
                if (dto.storeId == preselectedStoreId) {
                    candidate = dto;
                    break;
                }
            }
            preselectedStoreId = null;
        }

        selectStore(candidate, false);
    }

    private void selectStore(StoreNearbyDto store, boolean centerOnMap) {
        selectedStore = store;
        if (store == null) {
            return;
        }

        highlightSelectedMarker(true);
        if (centerOnMap && mMap != null) {
            LatLng target = new LatLng(store.latitude, store.longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 15.5f));
        }
    }

    private void highlightSelectedMarker(boolean showInfoWindow) {
        if (selectedStore == null) {
            return;
        }
        Marker marker = markerByStore.get(selectedStore.storeId);
        if (marker != null && showInfoWindow) {
            marker.showInfoWindow();
        }
    }

    private void returnSelection() {
        if (selectedStore == null) {
            Toast.makeText(this, R.string.pickup_store_select_prompt, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data = new Intent();
        data.putExtra(RESULT_STORE_ID, selectedStore.storeId);
        String storeName = selectedStore.name != null && !selectedStore.name.trim().isEmpty()
                ? selectedStore.name.trim()
                : getString(R.string.pickup_store_fallback_name, selectedStore.storeId);
        data.putExtra(RESULT_STORE_NAME, storeName);
        data.putExtra(RESULT_STORE_ADDRESS, selectedStore.address);
        data.putExtra(RESULT_STORE_DISTANCE, selectedStore.distanceKm);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Cần quyền vị trí để hiển thị cửa hàng gần bạn.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static class StoreMatch {
        StoreNearbyDto store;
        int matchedProducts;
        Integer minAvailable;

        void merge(StoreNearbyDto dto) {
            matchedProducts++;
            if (store == null) {
                store = copy(dto);
            } else {
                // distance dùng giá trị nhỏ nhất để ưu tiên cửa hàng gần hơn khi đồng bộ multi-product
                if (dto.distanceKm < store.distanceKm) {
                    store.distanceKm = dto.distanceKm;
                }
            }

            if (dto.quantity != null) {
                minAvailable = (minAvailable == null) ? dto.quantity : Math.min(minAvailable, dto.quantity);
            }
            if (store != null) {
                store.quantity = minAvailable;
            }
        }

        private static StoreNearbyDto copy(StoreNearbyDto from) {
            StoreNearbyDto clone = new StoreNearbyDto();
            clone.storeId = from.storeId;
            clone.name = from.name;
            clone.address = from.address;
            clone.latitude = from.latitude;
            clone.longitude = from.longitude;
            clone.distanceKm = from.distanceKm;
            clone.quantity = from.quantity;
            return clone;
        }
    }
}
