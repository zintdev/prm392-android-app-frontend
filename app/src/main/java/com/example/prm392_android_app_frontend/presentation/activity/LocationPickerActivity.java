package com.example.prm392_android_app_frontend.presentation.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.store.NominatimPlace;
import com.example.prm392_android_app_frontend.data.remote.api.NominatimApiService;
import com.example.prm392_android_app_frontend.presentation.adapter.LocationSuggestionAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "LocationPickerActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 101;
    private static final int AUTOCOMPLETE_DELAY_MS = 500; // Debounce delay

    // Views
    private TextInputLayout tilAddress;
    private TextInputEditText etAddress;
    private TextInputEditText etLatitude;
    private TextInputEditText etLongitude;
    private MaterialButton btnConfirm;
    private RecyclerView rvSuggestions;
    private View loadingOverlay;
    private View rootView;

    // Map
    private GoogleMap googleMap;
    private Marker locationMarker;
    private FusedLocationProviderClient fusedLocationClient;

    // Services
    private NominatimApiService nominatimService;
    private LocationSuggestionAdapter suggestionAdapter;

    // State
    private boolean isMapDragging = false;
    private boolean isUpdatingFromSuggestion = false;
    private android.os.Handler debounceHandler = new android.os.Handler();
    private Runnable debounceRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        rootView = findViewById(R.id.root);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupNominatimService();
        setupAutocomplete();
        setupMap();
    }

    private void initViews() {
        tilAddress = findViewById(R.id.tilAddress);
        etAddress = findViewById(R.id.etAddress);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        btnConfirm = findViewById(R.id.btnConfirm);
        rvSuggestions = findViewById(R.id.rvSuggestions);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Chọn địa chỉ");
            }
        }

        // Setup RecyclerView for suggestions
        suggestionAdapter = new LocationSuggestionAdapter(this::onSuggestionSelected);
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        rvSuggestions.setAdapter(suggestionAdapter);

        // Confirm button
        btnConfirm.setOnClickListener(v -> confirmLocation());

        // Hide suggestions initially
        rvSuggestions.setVisibility(View.GONE);

        // Hide suggestions when address field loses focus
        etAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // Delay hiding to allow click on suggestions
                etAddress.postDelayed(() -> hideSuggestions(), 200);
            }
        });
    }

    private void setupNominatimService() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

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
    }

    private void setupAutocomplete() {
        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous debounce
                if (debounceRunnable != null) {
                    debounceHandler.removeCallbacks(debounceRunnable);
                }

                String query = s.toString().trim();
                if (query.isEmpty()) {
                    hideSuggestions();
                    return;
                }

                // Debounce autocomplete
                debounceRunnable = () -> performAutocomplete(query);
                debounceHandler.postDelayed(debounceRunnable, AUTOCOMPLETE_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performAutocomplete(String query) {
        if (query.trim().isEmpty()) {
            hideSuggestions();
            return;
        }

        showLoading(true, "Đang tìm kiếm...");

        nominatimService.autocomplete(query, "json", 10).enqueue(new Callback<List<NominatimPlace>>() {
            @Override
            public void onResponse(Call<List<NominatimPlace>> call, Response<List<NominatimPlace>> response) {
                showLoading(false, null);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    suggestionAdapter.updateSuggestions(response.body());
                    showSuggestions();
                } else {
                    hideSuggestions();
                    if (response.code() == 429) {
                        showError("Quá nhiều yêu cầu. Vui lòng đợi một chút.");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<NominatimPlace>> call, Throwable t) {
                showLoading(false, null);
                Log.e(TAG, "Autocomplete failure", t);
                hideSuggestions();
                showError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void onSuggestionSelected(NominatimPlace place) {
        isUpdatingFromSuggestion = true;
        
        // Hide suggestions immediately
        hideSuggestions();
        etAddress.clearFocus();

        // Update address field
        etAddress.setText(place.getDisplayName());

        // Parse coordinates
        try {
            double lat = Double.parseDouble(place.getLat());
            double lon = Double.parseDouble(place.getLon());

            // Update Lat/Long fields
            etLatitude.setText(String.valueOf(lat));
            etLongitude.setText(String.valueOf(lon));

            // Enable confirm button
            btnConfirm.setEnabled(true);

            // Move map and add marker
            LatLng position = new LatLng(lat, lon);
            moveMapToPosition(position, true);
            updateMarker(position);

            isUpdatingFromSuggestion = false;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse coordinates", e);
            showError("Tọa độ không hợp lệ");
            isUpdatingFromSuggestion = false;
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
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
        googleMap.setMinZoomPreference(5f);
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

        // Handle map click to place/update marker (with delay to reduce sensitivity)
        googleMap.setOnMapClickListener(latLng -> {
            if (!isUpdatingFromSuggestion && !isMapDragging) {
                // Delay để tránh click nhạy quá - chỉ chọn khi camera đã dừng
                new android.os.Handler().postDelayed(() -> {
                    if (!isMapDragging) {
                        updateLocationFromMap(latLng);
                    }
                }, 300); // Delay 300ms
            }
        });

        // Handle marker click to show info window
        googleMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return true; // Consume the event
        });

        // Handle marker drag
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                isMapDragging = true;
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // Optionally update during drag
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                isMapDragging = false;
                updateLocationFromMap(marker.getPosition());
            }
        });

        // Handle camera move end (when user drags map)
        googleMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                isMapDragging = true;
            }
        });

        googleMap.setOnCameraIdleListener(() -> {
            // Không tự động update khi camera idle để tránh quá nhạy
            // Chỉ update khi user drag marker thực sự
            isMapDragging = false;
        });
    }

    private void updateLocationFromMap(LatLng position) {
        isUpdatingFromSuggestion = true;

        // Update Lat/Long fields
        etLatitude.setText(String.valueOf(position.latitude));
        etLongitude.setText(String.valueOf(position.longitude));

        // Enable confirm button
        btnConfirm.setEnabled(true);

        // Update marker
        updateMarker(position);

        // Reverse geocoding to get address
        performReverseGeocoding(position.latitude, position.longitude);

        isUpdatingFromSuggestion = false;
    }

    private void updateMarker(LatLng position) {
        if (googleMap == null) return;

        // Create marker with info window
        // Note: For a marker with arrow/rotation pointing direction, we would need
        // location updates with bearing, which is more complex. Using a standard marker
        // with info window as fallback per requirements.
        String address = etAddress.getText().toString().trim();
        String snippet = address.isEmpty() ? 
                String.format("%.6f, %.6f", position.latitude, position.longitude) : address;
        
        if (locationMarker == null) {
            locationMarker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("Vị trí đã chọn")
                    .snippet(snippet)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        } else {
            locationMarker.setPosition(position);
            locationMarker.setSnippet(snippet);
        }
        
        // Show info window when marker is updated
        locationMarker.showInfoWindow();
    }

    private void moveMapToPosition(LatLng position, boolean animate) {
        if (googleMap == null) return;

        if (animate) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16f));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16f));
        }
    }

    private void performReverseGeocoding(double lat, double lon) {
        showLoading(true, "Đang lấy địa chỉ...");

        nominatimService.reverseGeocode(lat, lon, "json").enqueue(new Callback<NominatimPlace>() {
            @Override
            public void onResponse(Call<NominatimPlace> call, Response<NominatimPlace> response) {
                showLoading(false, null);
                if (response.isSuccessful() && response.body() != null) {
                    NominatimPlace place = response.body();
                    if (!isUpdatingFromSuggestion || etAddress.getText().toString().isEmpty()) {
                        etAddress.setText(place.getDisplayName());
                    }
                    // Update marker snippet
                    if (locationMarker != null) {
                        locationMarker.setSnippet(place.getDisplayName());
                        locationMarker.showInfoWindow();
                    }
                }
            }

            @Override
            public void onFailure(Call<NominatimPlace> call, Throwable t) {
                showLoading(false, null);
                Log.e(TAG, "Reverse geocoding failure", t);
                // Don't show error, just keep coordinates
            }
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && googleMap != null) {
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                moveMapToPosition(myLocation, false);
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(14f));
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get location", e);
        });
    }

    private void confirmLocation() {
        String latStr = etLatitude.getText().toString();
        String lonStr = etLongitude.getText().toString();
        String address = etAddress.getText().toString();

        if (latStr.isEmpty() || lonStr.isEmpty()) {
            showError("Vui lòng chọn một vị trí trên bản đồ");
            return;
        }

        try {
            double lat = Double.parseDouble(latStr);
            double lon = Double.parseDouble(lonStr);

            // Return result to calling activity
            android.content.Intent resultIntent = new android.content.Intent();
            resultIntent.putExtra("latitude", lat);
            resultIntent.putExtra("longitude", lon);
            resultIntent.putExtra("address", address);
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (NumberFormatException e) {
            showError("Tọa độ không hợp lệ");
        }
    }

    private void showSuggestions() {
        rvSuggestions.setVisibility(View.VISIBLE);
    }

    private void hideSuggestions() {
        rvSuggestions.setVisibility(View.GONE);
    }

    private void showLoading(boolean show, String message) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show && message != null) {
            android.widget.TextView tvMessage = findViewById(R.id.tvLoadingMessage);
            if (tvMessage != null) {
                tvMessage.setText(message);
            }
        }
    }

    private void showError(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
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
                showError("Cần quyền vị trí để hiển thị bản đồ");
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
