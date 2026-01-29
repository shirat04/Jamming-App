package com.example.jamming.view;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.jamming.R;
import com.example.jamming.utils.AddressUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;

/**
 * Activity that allows the user to pick a location on a Google Map.

 * The user can search for a place or select a point on the map.
 * A validated address and coordinates are returned to the caller.
 */
public class MapPickerActivity extends BaseMapActivity {

    private double selectedLat = Double.NaN;
    private double selectedLng = Double.NaN;
    private String selectedAddress = "";
    private EditText etSearchLocation;
    private ImageButton btnSearch;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_user);

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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        enableMyLocationSafe();

        fetchLastLocation(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                );
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17f));
            } else {
                LatLng israel = new LatLng(31.0461, 34.8516);
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(israel, 15f)
                );
            }
        });


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

            try {
                Address addr = AddressUtils.getAddressFromLatLng(
                        this, selectedLat, selectedLng
                );

                if (!AddressUtils.hasStreetAndCity(addr)) {
                    selectedAddress = "";
                    Toast.makeText(
                            this,
                            "לא ניתן לזהות כתובת. נסי לבחור נקודה מדויקת יותר",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                selectedAddress = AddressUtils.formatAddress(addr);

            } catch (IOException e) {
                selectedAddress = "";
                Toast.makeText(
                        this,
                        "שגיאה בזיהוי כתובת. נסי שוב",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }


    private void confirmLocation() {
        if (Double.isNaN(selectedLat) || Double.isNaN(selectedLng)) {
            Toast.makeText(this, "נא לבחור מיקום על המפה", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedAddress == null || selectedAddress.trim().isEmpty()) {
            Toast.makeText(this, "נא לבחור מיקום עם כתובת מזוהה", Toast.LENGTH_SHORT).show();
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
        try {
            Address address =
                    AddressUtils.getAddressFromQuery(this, query);

            if (!AddressUtils.hasStreetAndCity(address)) {
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

        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בחיפוש", Toast.LENGTH_SHORT).show();
        }
    }

}
