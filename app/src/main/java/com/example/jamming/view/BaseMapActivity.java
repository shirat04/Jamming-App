package com.example.jamming.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import java.util.function.Consumer;


/**
 * Abstract base activity for screens that use Google Maps.
 *
 * This class centralizes common map-related behavior such as:
 * - Initializing the map instance
 * - Configuring map UI settings
 * - Handling location permission requests
 * - Fetching the user's last known location
 *
 * Subclasses are expected to implement {@link #onMapReadyCustom()} in order
 * to add screen-specific map logic (markers, listeners, overlays, etc.).
 */
public abstract class BaseMapActivity extends BaseActivity
        implements OnMapReadyCallback {

    // Reference to the GoogleMap instance once it is ready
    protected GoogleMap mMap;

    // Client used to retrieve the device's last known location
    private FusedLocationProviderClient fusedLocationClient;

    // Request code used for runtime location permission requests
    protected static final int LOCATION_REQUEST_CODE = 1001;

    /**
     * Initializes shared resources for map-based activities.
     * In particular, prepares the FusedLocationProviderClient for later use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Called when the GoogleMap instance is ready to be used.
     * Stores the map reference, applies common UI configuration,
     * and delegates screen-specific setup to subclasses.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        configureMapUi();
        onMapReadyCustom();
    }

    /**
     * Configures common UI and behavior settings for the map.
     * This method is shared across all map-based screens.
     */
    protected void configureMapUi() {
        if (mMap == null) return;
        mMap.getUiSettings().setZoomControlsEnabled(true); //  + -
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMinZoomPreference(5f);
        mMap.setMaxZoomPreference(20f);
    }

    /**
     * Enables the "My Location" layer on the map in a safe way.
     * If the required permission is not granted, a runtime permission
     * request is issued instead of accessing the location directly.
     */
    protected void enableMyLocationSafe() {
        if (mMap == null) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE
            );
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Fetches the device's last known location and returns it via the given callback.
     * If the location permission is missing or an error occurs, the callback receives null.
     *
     * @param onResult callback invoked with the last known location or null
     */
    protected void fetchLastLocation(Consumer<Location> onResult) {
        if (fusedLocationClient == null) {
            onResult.accept(null);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            onResult.accept(null);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(onResult::accept)
                .addOnFailureListener(e -> onResult.accept(null));
    }

    /**
     * Handles the result of runtime permission requests.
     * When location permission is granted, enables the location layer
     * and moves the camera to the user's last known position (if available).
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                enableMyLocationSafe();

                fetchLastLocation(location -> {
                    if (location != null && mMap != null) {
                        LatLng here = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );
                        mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(here, 13f)
                        );
                    }
                });
            }
        }
    }


    /**
     * Called after the map is ready and the common configuration is applied.
     * Subclasses should implement this method to add their own map-specific logic.
     */
    protected abstract void onMapReadyCustom();
}
