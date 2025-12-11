package com.example.jamming.view;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.repository.UserRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class ExploreEventsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explore_events);

        // Hello <username>
        title = findViewById(R.id.exploreTitle);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        new UserRepository().getUserById(uid)
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getString("username");
                    if (name == null) name = "";
                    title.setText("Hello " + name);
                });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();   // גם מרכז על המשתמש וגם קורא לטעינת אירועים
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE
            );
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        moveCameraToLocation(location);
                        loadEventsMarkers(location);   // ← כאן אנחנו מוסיפים את האירועים
                    } else {
                        // אם אין מיקום – עדיין נטען אירועים למפה
                        loadEventsMarkers(null);
                    }
                });
    }

    private void moveCameraToLocation(Location location) {
        LatLng here = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(here).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 13f));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    // ⚪️ טעינת אירועים מפיירסטור והצגת מרקרים
    private void loadEventsMarkers(Location userLocation) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;
                        if (!event.isActive()) continue;

                        double lat = event.getLatitude();
                        double lng = event.getLongitude();

                        // אם אין קואורדינטות – מדלגים
                        if (lat == 0 && lng == 0) continue;

                        // אופציונלי: סינון לפי רדיוס מהמיקום של המשתמש (למשל 10 ק"מ)
                        if (userLocation != null) {
                            float[] results = new float[1];
                            Location.distanceBetween(
                                    userLocation.getLatitude(), userLocation.getLongitude(),
                                    lat, lng,
                                    results
                            );
                            float distanceMeters = results[0];
                            if (distanceMeters > 10000) { // 10km
                                continue;
                            }
                        }

                        LatLng pos = new LatLng(lat, lng);
                        mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(event.getName()));
                    }
                });
    }
}
