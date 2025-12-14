package com.example.jamming.view;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private double selectedLat = Double.NaN;
    private double selectedLng = Double.NaN;
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button confirmBtn = findViewById(R.id.confirmLocationBtn);
        confirmBtn.setOnClickListener(v -> confirmLocation());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;

        // מיקום התחלתי – ישראל (אפשר לשנות)
        LatLng israel = new LatLng(31.0461, 34.8516);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(israel, 7f));

        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng));

            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses =
                        geocoder.getFromLocation(selectedLat, selectedLng, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    selectedAddress = address.getAddressLine(0);
                } else {
                    selectedAddress = "מיקום נבחר";
                }

            } catch (IOException e) {
                selectedAddress = "מיקום נבחר";
            }
        });

    }

    private void confirmLocation() {
        if (Double.isNaN(selectedLat) || Double.isNaN(selectedLng)) {
            Toast.makeText(this, "נא לבחור מיקום על המפה", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent result = new Intent();
        result.putExtra("lat", selectedLat);
        result.putExtra("lng", selectedLng);
        result.putExtra("address", selectedAddress);

        setResult(RESULT_OK, result);
        finish();
    }
}
