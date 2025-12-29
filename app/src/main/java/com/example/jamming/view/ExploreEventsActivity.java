package com.example.jamming.view;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.repository.UserRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.view.View;
import android.widget.Button;

public class ExploreEventsActivity extends BaseMapActivity {

    private TextView title;
    private ImageButton btnMenu;
    private TextView radiusLabel;          // תצוגת רדיוס
    private int eventRadiusKm = 10;        // רדיוס התחלתי בק״מ
    private FirebaseFirestore db;
    private TextView emptyText;
    private Button btnMyEvents;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explore_events);

        emptyText = findViewById(R.id.emptyText);
        btnMenu = findViewById(R.id.btnMore);
        btnMyEvents = findViewById(R.id.btnMyEvents);
        btnMyEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, MyEventUserActivity.class));
        });

        title = findViewById(R.id.exploreTitle);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        new UserRepository().getUserById(uid)
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getString("fullName");
                    if (name == null) name = "";
                    title.setText("Hello " + name);
                });

        db = FirebaseFirestore.getInstance();
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    private void handleMarkerClick(Marker marker) {
        // 1. קוראים את ה-ID מתוך תגית המרקר
        Object tag = marker.getTag();
        if (tag == null || !(tag instanceof String)) {
            // המרקר הזה לא מכיל ID של אירוע (אולי זה מרקר "You are here")
            return;
        }

        String eventId = (String) tag;

        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("EVENT_ID", eventId);

        startActivity(intent);
    }

    @Override
    protected void onMapReadyCustom() {

        // חשוב: זה מדליק את ה-blue dot (אם יש הרשאה)
        enableMyLocationSafe();

        // להדליק את לחיצות המרקרים
        mMap.setOnMarkerClickListener(marker -> {
            handleMarkerClick(marker);
            return false;
        });

        // עכשיו באמת להביא מיקום ולהציג אירועים
        fetchLastLocation(location -> {
            if (location != null) {
                LatLng here = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 13f));

                // <-- זה מה שחסר לך כרגע:
                loadEventsMarkers(location);
            } else {
                // fallback אם אין מיקום
                LatLng israel = new LatLng(31.0461, 34.8516);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(israel, 7f));

                loadEventsMarkers(null);
            }
        });
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



}
