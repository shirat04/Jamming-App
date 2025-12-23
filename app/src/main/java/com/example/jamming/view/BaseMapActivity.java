package com.example.jamming.view;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public abstract class BaseMapActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    protected GoogleMap mMap;

    protected static final int LOCATION_REQUEST_CODE = 1001;

    @Override
    public final void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        configureMapUi();      // הגדרות UI אחידות
        onMapReadyCustom();    // לוגיקה ייחודית למסך
    }

    /**
     * הגדרות UI משותפות לכל המפות באפליקציה
     */
    protected void configureMapUi() {
        mMap.getUiSettings().setZoomControlsEnabled(false); // בלי + -
        mMap.getUiSettings().setMapToolbarEnabled(false);  // בלי "Navigate"
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
    }

    /**
     * הפעלה בטוחה של מיקום משתמש (אופציונלי למסכים)
     */
    protected void enableMyLocationSafe() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

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
     * כל Activity שמרחיבה את המחלקה הזו
     * חייבת לממש את המתודה הזו
     */
    protected abstract void onMapReadyCustom();
}
