package com.example.jamming.view;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.model.EventFilter;
import com.example.jamming.model.MusicGenre;
import com.example.jamming.navigation.UserMenuHandler;
import com.example.jamming.utils.MapUiHelper;
import com.example.jamming.view.dialog.FilterDialogs;
import com.example.jamming.viewmodel.ExploreEventsViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ExploreEventsActivity extends BaseMapActivity {
    private ExploreEventsViewModel viewModel;
    private TextView emptyText;
    private Button btnMyEvents, btnAllEvent, filterDistance, filterDate, filterTime, filterMusic, filterCapacity;
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
        menuHandler = new UserMenuHandler(this);
        observeViewModel();
        setupListeners();

        viewModel.loadAllEvents();
        viewModel.loadUserGreeting();

    }

    private void initViews() {
        emptyText = findViewById(R.id.emptyText);

        btnAllEvent = findViewById(R.id.btnAllEvents);
        btnMyEvents = findViewById(R.id.btnMyEvents);

        filterDistance = findViewById(R.id.filterDistance);
        filterDate = findViewById(R.id.filterDate);
        filterTime = findViewById(R.id.filterTime);
        filterMusic = findViewById(R.id.filterMusic);
        filterCapacity = findViewById(R.id.filterCapacity);
        filterTooltipText();
    }

    private void filterTooltipText(){
        filterDistance.setTooltipText("Filter by location");
        filterDate.setTooltipText("Filter by date");
        filterTime.setTooltipText("Filter by time");
        filterMusic.setTooltipText("Filter by music genre");
        filterCapacity.setTooltipText("Filter by available spots and event size");
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
                viewModel.clearFilter()
        );

        btnMyEvents.setOnClickListener(v ->
                startActivity(new Intent(this, MyEventUserActivity.class))
        );

        filterCapacity.setOnClickListener(v -> {
            EventFilter eventFilter = viewModel.getFilter().getValue();
            if (eventFilter == null) return;

            FilterDialogs.showCapacityCombinedFilter(
                    this,
                    eventFilter.getMinAvailableSpots(),
                    eventFilter.getMaxAvailableSpots(),
                    eventFilter.getMinCapacity(),
                    eventFilter.getMaxCapacity(),
                    (minA, maxA, minC, maxC) ->
                            viewModel.updateFilter(filter -> {
                                filter.setAvailableSpotsRange(minA, maxA);
                                filter.setCapacityRange(minC, maxC);
                            })
            );
        });



        filterMusic.setOnClickListener(v -> {
            EventFilter eventFilter = viewModel.getFilter().getValue();
            if (eventFilter == null) return;

            MusicGenre[] allGenres = MusicGenre.values();

            String[] displayNames = new String[allGenres.length];
            for (int i = 0; i < allGenres.length; i++) {
                displayNames[i] = allGenres[i].getDisplayName();
            }

            FilterDialogs.showMusic(
                    this,
                    new HashSet<>(eventFilter.getMusicTypes()),
                    allGenres,
                    selected ->
                            viewModel.updateFilter(filter ->
                                    filter.setMusicTypes(selected)
                            )
            );

        });


        filterTime.setOnClickListener(v -> {
            EventFilter eventFilter = viewModel.getFilter().getValue();

            FilterDialogs.showTimeRange(
                    getSupportFragmentManager(),
                    this,
                    eventFilter.getStartMinute(),
                    eventFilter.getEndMinute(),
                    (start, end) ->
                            viewModel.updateFilter(filter ->
                                    filter.setTimeRange(start, end)
                            )
            );
        });

        filterDate.setOnClickListener(v -> {
            EventFilter eventFilter = viewModel.getFilter().getValue();

            FilterDialogs.showDateRange(
                    getSupportFragmentManager(),
                    this,
                    eventFilter.getStartDateMillis(),
                    eventFilter.getEndDateMillis(),
                    (start, end) ->
                            viewModel.updateFilter(filter ->
                                    filter.setDateRange(start, end)
                            )
            );
        });

        filterDistance.setOnClickListener(v -> {
            fetchLastLocation(location -> {
                if (location == null) return;

                EventFilter eventFilter = viewModel.getFilter().getValue();

                FilterDialogs.showDistance(
                        this,
                        location,
                        eventFilter.getRadiusKm(),
                        (lat, lng, radiusKm) ->
                                viewModel.updateFilter(filter ->
                                        filter.setLocation(lat, lng, radiusKm)
                                )
                );
            });
        });


    }
    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }

    // Observers
    private void observeViewModel() {
        viewModel.getUserGreeting().observe(this, this::setTitleText);

        viewModel.getFilteredEvents().observe(this, events -> {
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
