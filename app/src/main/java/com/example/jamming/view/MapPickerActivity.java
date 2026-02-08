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

    // Stores the currently selected coordinates and address
    private double selectedLat = Double.NaN;
    private double selectedLng = Double.NaN;
    private String selectedAddress = "";

    // UI elements for searching a location
    private EditText etSearchLocation;
    private ImageButton btnSearch;


    /**
     * Initializes the UI, sets up the map fragment, and configures
     * the search and confirmation buttons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_user);

        etSearchLocation = findViewById(R.id.etSearchLocation);
        btnSearch = findViewById(R.id.btnSearch);

        // Obtain the map fragment and request the map asynchronously
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Confirm button returns the selected location to the caller
        Button confirmBtn = findViewById(R.id.confirmLocationBtn);
        confirmBtn.setOnClickListener(v -> confirmLocation());

        // Search button triggers geocoding of the entered query
        btnSearch.setOnClickListener(v -> {
            String query = etSearchLocation.getText().toString().trim();
            if (query.isEmpty()) {
                etSearchLocation.setError(getString(R.string.error_enter_location_search));
                etSearchLocation.requestFocus();
                return;
            }

            searchLocationOnMap(query);
        });

    }

    /**
     * Called when the Google Map instance is ready to be used.
     *
     * Configures map settings, moves the camera to the user's last known location
     * (or a default location), and registers a click listener for manual selection.
     */
    @Override
    protected void onMapReadyCustom() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        enableMyLocationSafe();

        // Try to center the camera on the user's last known location
        fetchLastLocation(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                );
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17f));
            } else {
                // Fallback to a default location (Israel) if location is unavailable
                LatLng israel = new LatLng(31.0461, 34.8516);
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(israel, 15f)
                );
            }
        });

        // Handle user taps on the map to select a location manually
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();

            mMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.title_selected_location))
            );

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

            // Store the selected coordinates
            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;

            try {
                // Reverse-geocode the coordinates into an address
                Address address = AddressUtils.getAddressFromLatLng(
                        this, selectedLat, selectedLng
                );

                // Validate that the address contains at least street and city
                if (!AddressUtils.hasStreetAndCity(address)) {
                    selectedAddress = "";
                    Toast.makeText(
                            this,
                            getString(R.string.error_address_not_identified),
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                // Store the formatted address
                selectedAddress = AddressUtils.formatAddress(address);

            } catch (IOException e) {
                // Handle geocoding failure
                selectedAddress = "";
                Toast.makeText(this, getString(R.string.error_address_recognition_failed), Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**
     * Validates the selected location and returns it to the calling activity.
     *
     * If no valid coordinates or address were selected, an error message is shown.
     */
    private void confirmLocation() {
        if (Double.isNaN(selectedLat) || Double.isNaN(selectedLng)) {
            Toast.makeText(this, getString(R.string.error_select_location_on_map), Toast.LENGTH_SHORT).show();

            return;
        }

        if (selectedAddress == null || selectedAddress.trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.error_select_location_with_address), Toast.LENGTH_SHORT).show();

            return;
        }

        // Return the selected location data to the caller
        Intent result = new Intent();
        result.putExtra("lat", selectedLat);
        result.putExtra("lng", selectedLng);
        result.putExtra("address", selectedAddress);

        setResult(RESULT_OK, result);
        finish();
    }

    /**
     * Searches for a location by a textual query using geocoding,
     * moves the map to the found location, and updates the selected values.
     *
     * @param query The user-entered address or place name.
     */
    private void searchLocationOnMap(String query) {
        try {
            Address address =
                    AddressUtils.getAddressFromQuery(this, query);
            if (address == null) {
                Toast.makeText(this, getString(R.string.error_search_failed), Toast.LENGTH_SHORT).show();
                return;
            }
            // Ensure the found address is sufficiently precise
            if (!AddressUtils.hasStreetAndCity(address)) {
                Toast.makeText(this, getString(R.string.error_address_not_accurate), Toast.LENGTH_LONG).show();
                return;
            }

            LatLng latLng = new LatLng(
                    address.getLatitude(),
                    address.getLongitude()
            );

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

            // Update the selected location state
            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;
            selectedAddress = AddressUtils.formatAddress(address);

        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_search_failed), Toast.LENGTH_SHORT).show();
        }
    }

}
