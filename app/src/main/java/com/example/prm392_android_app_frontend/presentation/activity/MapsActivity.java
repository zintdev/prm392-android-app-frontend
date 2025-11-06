package com.example.prm392_android_app_frontend.presentation.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.store.StoreNearbyDto;
import com.example.prm392_android_app_frontend.presentation.adapter.StoreAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.StoreViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;


import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQ_LOC = 101;
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private View stateOverlay;

    private FusedLocationProviderClient fused;
    private Double myLat, myLng;

    private StoreAdapter storeAdapter;

    private StoreNearbyDto selectedStore;

    private StoreViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_map);

        // 0) Google Play Services check (tránh crash trên emulator không có Google APIs)
        int playStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (playStatus != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(playStatus)) {
                android.app.Dialog dlg =
                        GoogleApiAvailability.getInstance().getErrorDialog(this, playStatus, 9000);
                if (dlg != null) dlg.show();
            } else {
                Toast.makeText(this, "Thiết bị chưa có Google Play services cho Maps.", Toast.LENGTH_LONG).show();
            }
            return; // nhớ return để không chạy tiếp khi chưa OK
        }

        fused = LocationServices.getFusedLocationProviderClient(this);

        stateOverlay = findViewById(R.id.stateOverlay);
        TextView txtCount = findViewById(R.id.txtCount);
        com.google.android.material.textfield.TextInputEditText edtSearch = findViewById(R.id.edtSearch);
        com.google.android.material.textfield.TextInputEditText edtRadius = findViewById(R.id.edtRadius);

        androidx.recyclerview.widget.RecyclerView rv = findViewById(R.id.rvStores);
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        rv.setItemAnimator(null);
        storeAdapter = new StoreAdapter(new StoreAdapter.Callbacks() {
            @Override
            public void onSelect(StoreNearbyDto item) {
                selectedStore = item;
                LatLng target = new LatLng(item.latitude, item.longitude);
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 16f));
                }
            }

            @Override
            public void onNavigate(StoreNearbyDto item) {
                selectedStore = item;
                openDirectionsInGoogleMaps(item);
            }
        });
        rv.setAdapter(storeAdapter);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            Toast.makeText(this, "Không tìm thấy MapFragment (id=map). Kiểm tra layout.", Toast.LENGTH_LONG).show();
            return;
        }
        mapFragment.getMapAsync(this);

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(StoreViewModel.class);
        viewModel.nearby.observe(this, res -> {
            if (res == null) return;
            if (res.isLoading()) {
                showLoading(true, getString(R.string.loading));
            } else if (res.isError()) {
                showLoading(false, null);
                android.widget.Toast.makeText(this,
                        res.getMessage() != null ? res.getMessage() : "Có lỗi xảy ra",
                        android.widget.Toast.LENGTH_SHORT).show();
            } else if (res.isSuccess()) {
                showLoading(false, null);
                List<StoreNearbyDto> list = res.getData();
                storeAdapter.submit(list);
                refreshMarkersFromAdapter();
                updateStoreCount(txtCount);
            }
        });

        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    storeAdapter.filter(String.valueOf(s));
                    updateStoreCount(txtCount);
                    refreshMarkersFromAdapter();
                }

                @Override
                public void afterTextChanged(Editable s) { }
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
                if (edtSearch != null) edtSearch.setText("");
                if (myLat != null && myLng != null) {
                    triggerLoadNearby(myLat, myLng, radius, 999, null, null);
                } else {
                    Toast.makeText(this, "Chưa có vị trí của bạn. Nhấn icon định vị trước.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // FABs (có thể bạn chưa thêm trong layout)
        View fabMy = findViewById(R.id.fabMyLocation);
        if (fabMy != null) fabMy.setOnClickListener(v -> moveToMyLocation());
        View fabRef = findViewById(R.id.fabRefresh);
        if (fabRef != null) fabRef.setOnClickListener(v -> {
            if (myLat != null && myLng != null) triggerLoadNearby(myLat, myLng, 20.0, 50, null, null);
        });
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
            marker.showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16f));
            Object tag = marker.getTag();
            if (tag instanceof StoreNearbyDto) {
                selectedStore = (StoreNearbyDto) tag;
            }
            return true; // tự xử lý
        });

        // InfoWindow click: chỉ nhắc bấm GO
        mMap.setOnInfoWindowClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof StoreNearbyDto) {
                selectedStore = (StoreNearbyDto) tag;
                Toast.makeText(this, "Dùng nút Go trong danh sách để mở chỉ đường.", Toast.LENGTH_SHORT).show();
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
        viewModel.loadNearby(lat, lng, radiusKm, limit, productId, inStockOnly);
    }

    private void refreshMarkersFromAdapter() {
        if (storeAdapter != null) {
            renderMarkers(storeAdapter.getVisibleItems());
        }
    }

    private void renderMarkers(List<StoreNearbyDto> stores) {
        if (mMap == null || stores == null) return;
        mMap.clear();

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

    Toast.makeText(this, "Chạm marker hoặc dùng nút Go trên thẻ để mở chỉ đường.", Toast.LENGTH_LONG).show();
    }

    private void openDirectionsInGoogleMaps(StoreNearbyDto store) {
        if (store == null) return;

        String address = store.address != null ? store.address.trim() : "";
        String destinationQuery = address.isEmpty()
                ? store.latitude + "," + store.longitude
                : address;

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(destinationQuery) + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            String url = "https://www.google.com/maps/dir/?api=1&destination="
                    + Uri.encode(destinationQuery) + "&travelmode=driving";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
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
}
