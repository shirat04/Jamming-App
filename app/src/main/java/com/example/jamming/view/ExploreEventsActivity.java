package com.example.jamming.view;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.viewmodel.ExploreEventsViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class ExploreEventsActivity extends BaseMapActivity {
    private ExploreEventsViewModel viewModel;
    private TextView emptyText;
    private Button btnMyEvents, btnEventsNearMe, btnAllEvent;
    private boolean isMapReady = false;
    private List<Event> pendingEvents = new ArrayList<>();
    private UserMenuHandler menuHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBase(
                R.menu.user_menu,
                R.layout.activity_explore_events
        );
        showRightActions();

        initViews();
        initMap();
        viewModel = new ViewModelProvider(this).get(ExploreEventsViewModel.class);
        menuHandler = new UserMenuHandler(this, viewModel);
        observeViewModel();
        setupListeners();

        viewModel.loadAllEvents();
        viewModel.loadUserGreeting();
        menuHandler = new UserMenuHandler(this, viewModel);

    }

    private void initViews() {

        emptyText = findViewById(R.id.emptyText);
        btnAllEvent = findViewById(R.id.btnAllEvents);
        btnEventsNearMe = findViewById(R.id.btnEventsNearMe);
        btnMyEvents = findViewById(R.id.btnMyEvents);
    }

    private void initMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapContainer);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, mapFragment)
                    .commit();
        }

        mapFragment.getMapAsync(this);
    }
    private void setupListeners() {

        btnAllEvent.setOnClickListener(v ->
                viewModel.loadAllEvents()
        );

        btnEventsNearMe.setOnClickListener(v ->
                viewModel.loadEventsNearMe()
        );

        btnMyEvents.setOnClickListener(v ->
                startActivity(new Intent(this, MyEventUserActivity.class))
        );

    }
    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }

    // Observers
    private void observeViewModel() {
        viewModel.getUserGreeting().observe(this, this::setTitleText);

        viewModel.getEvents().observe(this, events -> {
            pendingEvents = events;
            if (!isMapReady || mMap == null) return;
            drawEventsOnMap(events);
        });

        viewModel.getIsEmpty().observe(this, isEmpty ->
                emptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE)
        );
    }

    @Override
    protected void onMapReadyCustom() {
        isMapReady = true;
        enableMyLocationSafe();
        setupMarkerClick();


        if (!pendingEvents.isEmpty()) {
            drawEventsOnMap(pendingEvents);
        }
        fetchLastLocation(this::handleInitialLocation);
    }

    private void handleInitialLocation(Location location) {

        if (location != null) {
            LatLng here = new LatLng(
                    location.getLatitude(),
                    location.getLongitude()
            );
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 13f));
        } else {
            LatLng israel = new LatLng(31.0461, 34.8516);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(israel, 7f));
        }

        viewModel.onUserLocationAvailable(location);
    }

    private void setupMarkerClick() {
        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof String) {
                Intent intent = new Intent(this, EventDetailActivity.class);
                intent.putExtra("EVENT_ID", (String) tag);
                startActivity(intent);
            }
            return false;
        });
    }

    private void drawEventsOnMap(List<Event> events) {
        if (mMap == null) return;
        MapUiHelper.drawEvents(
                mMap,
                events,
                this::enableMyLocationSafe
        );
    }

}
