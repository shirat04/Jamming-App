package com.example.jamming.view;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.GravityCompat;
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
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Main exploration screen for end users.
 * Responsibilities:
 * - Display events on a Google Map
 * - Apply and persist user-defined filters (distance, date, time, music, capacity)
 * - React to ViewModel state changes using LiveData observers
 * - Handle navigation actions from the drawer menu
 * - Provide UX behaviors such as "double back to exit"
 * Architecture:
 * - Follows MVVM: UI logic here, business/data logic in ExploreEventsViewModel and repositories.
 */
public class ExploreEventsActivity extends BaseMapActivity {

    // ViewModel providing data and business logic
    private ExploreEventsViewModel viewModel;

    // UI elements
    private TextView emptyText;
    private Button btnMyEvents, btnAllEvent, filterDistance, filterDate, filterTime, filterMusic, filterCapacity;

    // Map state
    private boolean isMapReady = false;

    // Holds events received before the map is fully ready
    private List<Event> pendingEvents = new ArrayList<>();

    // Drawer/menu handler
    private UserMenuHandler menuHandler;

    // Back press handling (double press to exit)
    private long lastBackPressTime = 0;
    private static final long BACK_PRESS_INTERVAL = 2000; // milliseconds

    /**
     * Activity creation lifecycle callback.
     * Initializes the base layout, views, map, ViewModel, observers, and listeners.
     * Also loads the initial filter, events, and user name.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the base layout with navigation drawer and this screen's content
        setupBase(
                R.menu.user_menu,
                R.layout.activity_explore_events
        );

        // Initialize UI and map
        initViews();
        initMap();

        // Initialize ViewModel and menu handler
        viewModel = new ViewModelProvider(this).get(ExploreEventsViewModel.class);
        menuHandler = new UserMenuHandler(this);

        // Bind UI to ViewModel state
        observeViewModel();
        setupListeners();

        // Initialize filter (loads last saved filter if exists, otherwise default)
        viewModel.initFilter();

        // Load initial data
        viewModel.loadAllEvents();
        viewModel.loadUserName();
        setupBackPressedHandler();

    }

    /**
     * Called when the Activity comes to the foreground.
     * Used here to refresh the events list to ensure up-to-date data
     * (e.g., after returning from details/edit screens).
     */
    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadAllEvents();
    }

    /**
     * Finds and caches all required view references.
     */
    private void initViews() {
        emptyText = findViewById(R.id.emptyText);
        btnAllEvent = findViewById(R.id.btnAllEvents);
        btnMyEvents = findViewById(R.id.btnMyEvents);
        filterDistance = findViewById(R.id.filterDistance);
        filterDate = findViewById(R.id.filterDate);
        filterTime = findViewById(R.id.filterTime);
        filterMusic = findViewById(R.id.filterMusic);
        filterCapacity = findViewById(R.id.filterCapacity);
    }

    /**
     * Initializes the Google Map fragment and requests an async map load.
     */
    private void initMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapContainer);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    /**
     * Sets up all UI event listeners and delegates actions to the ViewModel.
     * This keeps UI logic in the Activity and business logic in the ViewModel.
     */
    private void setupListeners() {
        // Update title with user's name once available
        viewModel.getUserName().observe(this, name -> {
            if (name == null || name.isEmpty()) return;
            setTitleText(getString(R.string.hello_user, name));
        });

        // Clear all filters and show all events
        btnAllEvent.setOnClickListener(v -> viewModel.clearFilter());

        // Navigate to "My Events" screen
        btnMyEvents.setOnClickListener(v -> startActivity(new Intent(this, MyEventUserActivity.class)));

        // Capacity and availability filter
        filterCapacity.setOnClickListener(v -> {
            EventFilter eventFilter = viewModel.getFilter().getValue();
            if (eventFilter == null) return;

            FilterDialogs.showCapacityCombinedFilter(
                    this,
                    eventFilter.getMinAvailableSpots(),
                    eventFilter.getMaxAvailableSpots(),
                    eventFilter.getMinCapacity(),
                    eventFilter.getMaxCapacity(),
                    (minA, maxA, minC, maxC) -> {
                        if (!viewModel.isValidCapacityRange(minA, maxA, minC, maxC)) {
                            Toast.makeText(this, R.string.invalid_capacity_range, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        viewModel.updateFilter(filter -> {
                            filter.setAvailableSpotsRange(minA, maxA);
                            filter.setCapacityRange(minC, maxC);
                        });
                    }
            );
        });


        // Music genre filter
        filterMusic.setOnClickListener(v -> {
            EventFilter eventFilter = viewModel.getFilter().getValue();
            if (eventFilter == null) return;

            MusicGenre[] allGenres = MusicGenre.values();
            FilterDialogs.showMusic(this, new HashSet<>(eventFilter.getMusicTypes()), allGenres,
                    selected ->
                            viewModel.updateFilter(filter -> filter.setMusicTypes(new ArrayList<>(selected))));
        });

        // Time range filter
        filterTime.setOnClickListener(v -> {
            EventFilter eventFilter = viewModel.getFilter().getValue();
            if (eventFilter == null) return;

            FilterDialogs.showTimeRange(
                    this,
                    eventFilter.getStartMinute(),
                    eventFilter.getEndMinute(),
                    (start, end) ->
                            viewModel.updateFilter(filter -> filter.setTimeRange(start, end))
            );
        });



        // Date range filter
        filterDate.setOnClickListener(v -> {
            EventFilter eventFilter = viewModel.getFilter().getValue();
            if (eventFilter == null) return;

            FilterDialogs.showDateRange(
                    this,
                    eventFilter.getStartDateMillis(),
                    eventFilter.getEndDateMillis(),
                    (start, end) -> {
                        if (!viewModel.isValidDateRange(start, end)) {
                            Toast.makeText(this, R.string.end_after_start, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        viewModel.updateFilter(filter -> filter.setDateRange(start, end));
                    }
            );
        });


        // Distance filter (requires last known location)
        filterDistance.setOnClickListener(v -> {
            fetchLastLocation(location -> {
                if (location == null) return;

                EventFilter eventFilter = viewModel.getFilter().getValue();
                if (eventFilter  == null) return;

                FilterDialogs.showDistance(this, location, eventFilter.getRadiusKm(),
                        (lat, lng, radiusKm) ->
                                viewModel.updateFilter(filter -> filter.setLocation(lat, lng, radiusKm)));
            });
        });
    }

    /**
     * Subscribes to ViewModel LiveData and updates the UI accordingly:
     * - Draws events on the map
     * - Shows appropriate empty-state messages
     */
    private void observeViewModel() {
        // Observe filtered events list
        viewModel.getFilteredEvents().observe(this, events -> {
            pendingEvents = events;
            if (!isMapReady || mMap == null) return;
            drawEventsOnMap(events);
        });

        // Observe empty state and display a relevant message
        viewModel.getEmptyState().observe(this, state -> {
            switch (state) {
                case NO_EVENTS_AT_ALL:
                    emptyText.setText(getString(R.string.empty_no_events));
                    emptyText.setVisibility(View.VISIBLE);
                    break;

                case NO_MATCHING_FILTERS:
                    emptyText.setText(getString(R.string.empty_no_matching_filters));
                    emptyText.setVisibility(View.VISIBLE);
                    break;

                case NONE:
                    emptyText.setVisibility(View.GONE);
                    break;
            }
        });


    }

    /**
     * Handles selection from the navigation drawer menu by delegating
     * the action to a dedicated menu handler.
     */
    @Override
    protected boolean onMenuItemSelected(int itemId) {return menuHandler.handle(itemId);}

    /**
     * Callback invoked when the Google Map is ready.
     * Enables location features, sets marker click behavior,
     * draws pending events, and centers the camera.
     */
    @Override
    protected void onMapReadyCustom() {
        isMapReady = true;
        enableMyLocationSafe();
        setupMarkerClick();

        // Draw any events that arrived before the map was ready
        if (!pendingEvents.isEmpty()) {
            drawEventsOnMap(pendingEvents);
        }
        // Center camera to user's last known location (or a default location)
        fetchLastLocation(this::handleInitialLocation);
    }

    /**
     * Moves the camera to the user's location if available,
     * otherwise moves it to a predefined fallback location.
     */
    private void handleInitialLocation(Location location) {
        if (location != null) {
            LatLng here = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 13f));
        } else {
            // Fallback: center on Israel if location is not available
            LatLng israel = new LatLng(31.0461, 34.8516);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(israel, 7f));
        }
    }

    /**
     * Sets a click listener on map markers to open the EventDetailActivity
     * for the selected event.
     */
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

    /**
     * Draws the given events on the map using a dedicated UI helper class.
     */
    private void drawEventsOnMap(List<Event> events) {
        if (mMap == null) return;
        MapUiHelper.drawEvents(mMap, events, this::enableMyLocationSafe);
    }

    /**
     * Registers a custom back-press handler using the OnBackPressedDispatcher.
     *
     * If the navigation drawer is open, the back button closes it.
     * Otherwise, the user must press back twice within a short time interval
     * in order to exit the activity ("double back to exit" behavior).
     */
    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawers();
                    return;
                }

                long now = System.currentTimeMillis();
                if (now - lastBackPressTime < BACK_PRESS_INTERVAL) {
                    finish();
                } else {
                    lastBackPressTime = now;
                    Toast.makeText(
                            ExploreEventsActivity.this,
                            getString(R.string.back_press_exit),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

}
