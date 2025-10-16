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

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.StoreApi;
import com.example.prm392_android_app_frontend.presentation.adapter.StoreAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;

import retrofit2.*;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQ_LOC = 101;
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private View stateOverlay, bottomSheet;
    private BottomSheetBehavior<View> bottomBehavior;

    private FusedLocationProviderClient fused;
    private Double myLat, myLng;

    private StoreAdapter storeAdapter;

    private StoreApi.StoreNearbyDto selectedStore;

    private final StoreApi storeApi = ApiClient.get().create(StoreApi.class);

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
        bottomSheet = findViewById(R.id.bottomSheet);
        if (bottomSheet != null) {
            bottomBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomBehavior.setPeekHeight((int) (160 * getResources().getDisplayMetrics().density));
            bottomBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            Toast.makeText(this, "Không tìm thấy MapFragment (id=map). Kiểm tra layout.", Toast.LENGTH_LONG).show();
            return;
        }
        mapFragment.getMapAsync(this);

        // Nút Áp dụng trong bottom sheet (nếu có)
        View btnApply = bottomSheet != null ? bottomSheet.findViewById(R.id.btnApply) : null;
        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                Double radius = 20.0;
                try {
                    var rView = bottomSheet.findViewById(R.id.edtRadius);
                    if (rView instanceof com.google.android.material.textfield.TextInputEditText) {
                        String r = String.valueOf(((com.google.android.material.textfield.TextInputEditText) rView).getText()).trim();
                        if (!r.isEmpty()) radius = Double.parseDouble(r);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Parse radius fail", e);
                }

                boolean isPickup = false;
                View btnPickup = bottomSheet.findViewById(R.id.btnPickup);
                if (btnPickup instanceof com.google.android.material.button.MaterialButton) {
                    isPickup = ((com.google.android.material.button.MaterialButton) btnPickup).isChecked();
                }

                Integer productId = null;
                if (isPickup) {
                    var pView = bottomSheet.findViewById(R.id.edtProductId);
                    if (pView instanceof com.google.android.material.textfield.TextInputEditText) {
                        String p = String.valueOf(((com.google.android.material.textfield.TextInputEditText) pView).getText()).trim();
                        try {
                            if (!p.isEmpty()) productId = Integer.parseInt(p);
                        } catch (Exception ignore) {
                        }
                    }
                }

                if (myLat != null && myLng != null) {
                    loadNearby(myLat, myLng, radius, 50, productId, isPickup ? Boolean.TRUE : null);
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
            if (myLat != null && myLng != null) loadNearby(myLat, myLng, 20.0, 50, null, null);
        });
        // ===== BottomSheet Behavior: bật kéo thả & callback log =====
        bottomBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomBehavior.setDraggable(true);
        bottomBehavior.setPeekHeight((int) (160 * getResources().getDisplayMetrics().density));
        bottomBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bs, int newState) {
                // Log.d(TAG, "BS state=" + newState);
            }

            @Override
            public void onSlide(@NonNull View bs, float offset) {
            }
        });

// ===== RecyclerView + Adapter =====
        androidx.recyclerview.widget.RecyclerView rv = bottomSheet.findViewById(R.id.rvStores);
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        storeAdapter = new StoreAdapter(item -> {
            // 1. Lưu cửa hàng được chọn
            selectedStore = item;

            // 2. Di chuyển camera tới vị trí cửa hàng
            LatLng p = new LatLng(item.latitude, item.longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(p, 16f));

            // 3. Hiện nút Go
            View fabGo = findViewById(R.id.fabGo);
            if (fabGo != null) fabGo.setVisibility(View.VISIBLE);

            // 4. (tuỳ chọn) bật info window của marker tương ứng nếu đã vẽ
            // sẽ gắn marker.setTag(...) ở bước renderMarkers() bên dưới
        });
        rv.setAdapter(storeAdapter);



// ===== Toggle All/Pickup: ẩn/hiện ProductId =====
        com.google.android.material.button.MaterialButton btnAll = bottomSheet.findViewById(R.id.btnAll);
        com.google.android.material.button.MaterialButton btnPickup = bottomSheet.findViewById(R.id.btnPickup);
        com.google.android.material.textfield.TextInputLayout tilProduct = bottomSheet.findViewById(R.id.tilProduct);

        View.OnClickListener toggleListener = v -> {
            boolean pickup = btnPickup.isChecked();
            tilProduct.setVisibility(pickup ? View.VISIBLE : View.GONE);
        };
        btnAll.setOnClickListener(toggleListener);
        btnPickup.setOnClickListener(toggleListener);

        toggleListener.onClick(btnAll);

        View fabGo = findViewById(R.id.fabGo);
        if (fabGo != null) {
            fabGo.setOnClickListener(v -> {
                if (selectedStore == null) {
                    Toast.makeText(this, "Hãy chọn một cửa hàng trước.", Toast.LENGTH_SHORT).show();
                    return;
                }
                openDirectionsInGoogleMaps(
                        selectedStore.latitude,
                        selectedStore.longitude,
                        selectedStore.name != null ? selectedStore.name : selectedStore.address
                );
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
            marker.showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16f));
            Object tag = marker.getTag();
            if (tag instanceof StoreApi.StoreNearbyDto) {
                selectedStore = (StoreApi.StoreNearbyDto) tag;
                View fabGo2 = findViewById(R.id.fabGo);
                if (fabGo2 != null) fabGo2.setVisibility(View.VISIBLE);
            }
            return true; // tự xử lý
        });

        // InfoWindow click: chỉ nhắc bấm GO
        mMap.setOnInfoWindowClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof StoreApi.StoreNearbyDto) {
                selectedStore = (StoreApi.StoreNearbyDto) tag;
                Toast.makeText(this, "Nhấn GO để mở chỉ đường.", Toast.LENGTH_SHORT).show();
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
                loadNearby(myLat, myLng, 20.0, 50, null, null);
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

    private void loadNearby(double lat, double lng, Double radiusKm, Integer limit,
                            Integer productId, Boolean inStockOnly) {
        showLoading(true, getString(R.string.loading));
        storeApi.getNearby(lat, lng, radiusKm, limit, productId, inStockOnly)
                .enqueue(new Callback<List<StoreApi.StoreNearbyDto>>() {
                    @Override public void onResponse(Call<List<StoreApi.StoreNearbyDto>> call,
                                                     Response<List<StoreApi.StoreNearbyDto>> resp) {
                        showLoading(false, null);
                        if (!resp.isSuccessful() || resp.body() == null) {
                            Toast.makeText(MapsActivity.this, "HTTP " + resp.code(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<StoreApi.StoreNearbyDto> list = new java.util.ArrayList<>(resp.body());

                        // 1) Lọc theo bán kính nếu người dùng nhập (km)
                        if (radiusKm != null) {
                            double r = radiusKm; // km
                            java.util.Iterator<StoreApi.StoreNearbyDto> it = list.iterator();
                            while (it.hasNext()) {
                                if (it.next().distanceKm > r) it.remove();
                            }
                        }

                        // 2) Sort theo khoảng cách tăng dần (phòng khi backend không sort)
                        java.util.Collections.sort(list, new java.util.Comparator<StoreApi.StoreNearbyDto>() {
                            @Override public int compare(StoreApi.StoreNearbyDto a, StoreApi.StoreNearbyDto b) {
                                return Double.compare(a.distanceKm, b.distanceKm);
                            }
                        });

                        // 3) Đổ vào danh sách + cập nhật map + đếm
                        storeAdapter.submit(list);
                        renderMarkers(list);
                        View tv = bottomSheet != null ? bottomSheet.findViewById(R.id.txtCount) : null;
                        if (tv instanceof android.widget.TextView)
                            ((android.widget.TextView) tv).setText(list.size() + " kết quả");

                        if (bottomBehavior != null)
                            bottomBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    }


                    @Override
                    public void onFailure(Call<List<StoreApi.StoreNearbyDto>> call, Throwable t) {
                        showLoading(false, null);
                        Log.e(TAG, "API nearby failed", t);
                        Toast.makeText(MapsActivity.this, "API lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderMarkers(List<StoreApi.StoreNearbyDto> stores) {
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

        for (StoreApi.StoreNearbyDto s : stores) {
            LatLng p = new LatLng(s.latitude, s.longitude);
            String title = (s.name != null && !s.name.isEmpty()) ? s.name : s.address;
            String snippet = String.format("~%.2f km%s",
                    s.distanceKm,
                    s.quantity != null ? (" • Còn: " + s.quantity) : "");
            mMap.addMarker(new MarkerOptions().position(p).title(title).snippet(snippet));
            bounds.include(p);
            hasAny = true;
        }

        if (hasAny) {
            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));
            } catch (Exception ignore) {
            }
        }

        Toast.makeText(this, "Chạm Info của marker để mở chỉ đường.", Toast.LENGTH_LONG).show();
        if (bottomBehavior != null)
            bottomBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
    }

    private void openDirectionsInGoogleMaps(double lat, double lng, String label) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng +
                "(" + Uri.encode(label == null ? "" : label) + ")" + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            String url = "https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng + "&travelmode=driving";
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
