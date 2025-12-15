package com.example.jamming.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.repository.UserRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.view.View;
import android.widget.Button;

import com.google.firebase.firestore.FieldPath;

import java.util.List;
import java.util.ArrayList;

public class ExploreEventsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView title;
    private ImageButton btnMenu;

    private TextView radiusLabel;          // תצוגת רדיוס
    private int eventRadiusKm = 10;        // רדיוס התחלתי בק״מ
    private Location lastKnownLocation;    // נשמור את מיקום המשתמש האחרון
    private FirebaseFirestore db;
    private TextView emptyText;
    private Button btnMyEvents;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explore_events);


        btnMenu = findViewById(R.id.btnMore);
        btnMyEvents = findViewById(R.id.btnMyEvents);
        btnMyEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, MyEventUserActivity.class));
        });



        // --- כותרת Hello <username> ---
        title = findViewById(R.id.exploreTitle);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        new UserRepository().getUserById(uid)
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getString("fullName");
                    if (name == null) name = "";
                    title.setText("Hello " + name);
                });


        // --- מיקום + מפה + Firestore ---
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // ודא שאת מייבאת את Activity היעד בתחילת הקובץ:
// import com.example.jamming.view.EventDetailActivity;

    private void handleMarkerClick(Marker marker) {

        // 1. קוראים את ה-ID מתוך תגית המרקר
        Object tag = marker.getTag();
        if (tag == null || !(tag instanceof String)) {
            // המרקר הזה לא מכיל ID של אירוע (אולי זה מרקר "You are here")
            return;
        }

        String eventId = (String) tag;

        // 2. יוצרים Intent למסך פרטי האירוע
        Intent intent = new Intent(this, eventDetailActivity.class);

        // 3. **חובה:** מעבירים את מזהה האירוע למסך הבא
        // המסך הבא ישתמש ב-ID זה כדי לטעון את פרטי האירוע מ-Firestore
        intent.putExtra("EVENT_ID", eventId);

        // 4. מבצעים את המעבר
        startActivity(intent);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        enableMyLocation();   // גם מרכז על המשתמש וגם טוען אירועים
        // **הוספת המאזין ללחיצות על מרקרים**
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                // נקרא לשלב הבא
                handleMarkerClick(marker);

                // מחזירים false כדי שהמפה תבצע גם את פעולת ברירת המחדל (הצגת חלון מידע)
                return false;
            }
        });
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
                        lastKnownLocation = location;
                        moveCameraToLocation(location);
                        loadEventsMarkers(location);
                    } else {
                        // אם אין מיקום – עדיין נטען אירועים (ללא סינון רדיוס)
                        loadEventsMarkers(null);
                    }
                });
    }

    private void updateRadiusAndReload() {
        radiusLabel.setText(eventRadiusKm + " km");

        if (mMap == null) return;

        mMap.clear(); // מוחקים מרקרים ישנים

        if (lastKnownLocation != null) {
            // נוסיף שוב \"You are here\" וניטען אירועים ברדיוס החדש
            moveCameraToLocation(lastKnownLocation);
            loadEventsMarkers(lastKnownLocation);
        } else {
            loadEventsMarkers(null);
        }
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

    // --- טעינת אירועים כפונקציה של רדיוס ---
    private void loadEventsMarkers(Location userLocation) {
        db.collection("events")
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) {
                            Log.e("EVENTS_LOADER", "Failed to convert document to Event object.");
                            continue;
                        }


                        if (!event.isActive()) {
                            Log.d("EVENTS_LOADER", "Skipping inactive event: " + event.getName());
                            continue;
                        }

                        double lat = event.getLatitude();
                        double lng = event.getLongitude();

                        if (lat == 0 && lng == 0){
                            Log.w("EVENTS_LOADER", "Skipping event with 0,0 coordinates: " + event.getName());
                            continue;
                        }

                        // סינון לפי רדיוס דינמי
                        if (userLocation != null) {
                            float[] results = new float[1];
                            Location.distanceBetween(
                                    userLocation.getLatitude(), userLocation.getLongitude(),
                                    lat, lng,
                                    results
                            );
                            float distanceMeters = results[0];

                        }


                        LatLng pos = new LatLng(lat, lng);

                        // 2. שומרים את המרקר שנוצר במשתנה Marker
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(event.getName()));
                        String eventId = doc.getId();
                        // 3. משתמשים במשתנה זה כדי להוסיף את מזהה האירוע
                        if (marker != null) {
                            marker.setTag(eventId);
                        }
                    }
                });
    }

    private void showEmpty(String msg) {
        emptyText.setText(msg);
        emptyText.setVisibility(View.VISIBLE);
    }

    private void hideEmpty() {
        emptyText.setVisibility(View.GONE);
    }

    private void loadMyEventsOnMap() {
        if (mMap == null) return;

        showEmpty("טוען...");
        mMap.clear();

        // אם יש מיקום - תחזיר את ה-"You are here"
        if (lastKnownLocation != null) {
            moveCameraToLocation(lastKnownLocation);
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    List<String> ids = (List<String>) doc.get("registeredEventIds");

                    if (ids == null || ids.isEmpty()) {
                        showEmpty("עוד לא נרשמת לאירועים 🙂");
                        return;
                    }

                    loadEventsByIdsAndDrawMarkers(ids);
                })
                .addOnFailureListener(e -> showEmpty("שגיאה בטעינת My Events"));
    }
    private void loadEventsByIdsAndDrawMarkers(List<String> ids) {
        final int totalChunks = (ids.size() + 9) / 10;
        final int[] done = {0};
        final int[] markersCount = {0};

        for (int i = 0; i < ids.size(); i += 10) {
            List<String> chunk = ids.subList(i, Math.min(i + 10, ids.size()));

            db.collection("events")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        for (DocumentSnapshot d : snapshot.getDocuments()) {
                            Event event = d.toObject(Event.class);
                            if (event == null) continue;
                            if (!event.isActive()) continue;

                            double lat = event.getLatitude();
                            double lng = event.getLongitude();
                            if (lat == 0 && lng == 0) continue;

                            LatLng pos = new LatLng(lat, lng);
                            Marker marker = mMap.addMarker(new MarkerOptions().position(pos).title(event.getName()));
                            if (marker != null) marker.setTag(d.getId()); // <-- זה מה שמאפשר details בלחיצה
                            markersCount[0]++;
                        }

                        done[0]++;
                        if (done[0] == totalChunks) {
                            if (markersCount[0] == 0) showEmpty("אין אירועים להצגה");
                            else hideEmpty();
                        }
                    })
                    .addOnFailureListener(e -> {
                        done[0]++;
                        if (done[0] == totalChunks) {
                            if (markersCount[0] == 0) showEmpty("שגיאה בטעינת אירועים");
                            else hideEmpty();
                        }
                    });
        }
    }


}
