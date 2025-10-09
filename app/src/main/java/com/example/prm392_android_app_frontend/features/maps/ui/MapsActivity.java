package com.example.prm392_android_app_frontend.features.maps.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_android_app_frontend.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.view.View;
import android.widget.Button;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final double STORE_LAT = 10.776530;   // ví dụ: Q1, HCM
    private static final double STORE_LNG = 106.700981;
    private static final String STORE_NAME = "TEST";

    private GoogleMap mMap;
    private Button btnDirections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btnDirections = findViewById(R.id.btnDirections);
        btnDirections.setOnClickListener(v -> openDirectionsInGoogleMaps(STORE_LAT, STORE_LNG, STORE_NAME));

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng store = new LatLng(STORE_LAT, STORE_LNG);
        mMap.addMarker(new MarkerOptions().position(store).title(STORE_NAME));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(store, 15f));
    }

    /** dùng Google Maps URLs – tự lấy vị trí hiện tại của user */
    private void openDirectionsInGoogleMaps(double lat, double lng, String label) {
        // URL chuẩn từ Google (tự lấy current location làm origin)
        String url = "https://www.google.com/maps/dir/?api=1"
                + "&destination=" + lat + "," + lng
                + "&destination_place_id="
                + "&travelmode=driving"; // driving | walking | bicycling | transit

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng + "(" + Uri.encode(label) + ")" + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapIntent); // mở app Google Maps
        } catch (ActivityNotFoundException e) {
            // fallback: mở trình duyệt với Google Maps URL
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }
    }
}