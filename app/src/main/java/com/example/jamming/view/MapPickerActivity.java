package com.example.jamming.view;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.utils.AddressUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends BaseMapActivity {

    private double selectedLat = Double.NaN;
    private double selectedLng = Double.NaN;
    private String selectedAddress = "";
    private EditText etSearchLocation;
    private ImageButton btnSearch;
    private boolean locationSelected = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        etSearchLocation = findViewById(R.id.etSearchLocation);
        btnSearch = findViewById(R.id.btnSearch);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button confirmBtn = findViewById(R.id.confirmLocationBtn);
        confirmBtn.setOnClickListener(v -> confirmLocation());

        btnSearch.setOnClickListener(v -> {
            String query = etSearchLocation.getText().toString().trim();
            if (query.isEmpty()) {
                etSearchLocation.setError("נא להזין מיקום לחיפוש");
                etSearchLocation.requestFocus();

                return;
            }

            searchLocationOnMap(query);
        });

    }

    @Override
    protected void onMapReadyCustom() {
        // מיקום התחלתי – ישראל
        LatLng israel = new LatLng(31.0461, 34.8516);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(israel, 7f));

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();

            mMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title("מיקום נבחר")
            );

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses =
                        geocoder.getFromLocation(selectedLat, selectedLng, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    selectedAddress = AddressUtils.formatAddress(addresses.get(0));
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

    private void searchLocationOnMap(String query) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> results = geocoder.getFromLocationName(query, 1);

            if (results != null && !results.isEmpty()) {
                Address address = results.get(0);

                String street = address.getThoroughfare();
                String house = address.getSubThoroughfare();
                boolean missingStreet = (street == null || street.trim().isEmpty());
                boolean missingHouse  = (house == null  || house.trim().isEmpty());

                if (missingStreet || missingHouse) {
                    Toast.makeText(
                            this,
                            "הכתובת לא נמצאה במדויק. נא לבחור נקודה ידנית במפה",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                LatLng latLng = new LatLng(
                        address.getLatitude(),
                        address.getLongitude()
                );

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

                selectedLat = latLng.latitude;
                selectedLng = latLng.longitude;
                selectedAddress = AddressUtils.formatAddress(address);
                locationSelected = true;
            } else {
                Toast.makeText(this, "לא נמצאה תוצאה", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בחיפוש", Toast.LENGTH_SHORT).show();
        }
    }

}
