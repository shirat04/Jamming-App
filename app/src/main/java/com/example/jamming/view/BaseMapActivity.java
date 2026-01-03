package com.example.jamming.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import java.util.function.Consumer;

public abstract class BaseMapActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    protected GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    protected static final int LOCATION_REQUEST_CODE = 1001;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        configureMapUi();      // הגדרות UI אחידות
        onMapReadyCustom();    // לוגיקה ייחודית למסך
    }

//הגדרות מפה
    protected void configureMapUi() {
        mMap.getUiSettings().setZoomControlsEnabled(true); //  + -
        mMap.getUiSettings().setMapToolbarEnabled(false);  // בלי "Navigate"
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMinZoomPreference(5f);
        mMap.setMaxZoomPreference(20f);
    }


     // הפעלה בטוחה של מיקום משתמש
    protected void enableMyLocationSafe() {
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
    protected void fetchLastLocation(Consumer<Location> onResult) {
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
     * כל Activity שמרחיבה את המחלקה הזו
     * חייבת לממש את המתודה הזו
     */
    protected abstract void onMapReadyCustom();
}
