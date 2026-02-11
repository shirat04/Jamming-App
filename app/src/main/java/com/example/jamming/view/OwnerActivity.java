package com.example.jamming.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.navigation.OwnerMenuHandler;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.utils.DateUtils;
import com.example.jamming.utils.GenreUtils;
import androidx.activity.OnBackPressedCallback;

import com.example.jamming.utils.NotificationHelper;
import com.example.jamming.viewmodel.OwnerViewModel;

/**
 * Main screen for event owners.
 *
 * Displays the owner's upcoming events, allows creating and editing events,
 * and handles navigation and basic UI interactions.
 *
 * Follows MVVM:
 * - UI logic and rendering are handled here (View)
 * - Data and business logic are handled in OwnerViewModel
 */
public class OwnerActivity extends BaseActivity {

    // ViewModel that provides owner data and event lists
    private OwnerViewModel viewModel;

    // UI elements
    private TextView emptyEventsText;
    private LinearLayout eventsContainer;
    private Button createEventBtn;

    // Navigation handler for the drawer menu
    private OwnerMenuHandler menuHandler;

    // State for "double back to exit" behavior
    private long lastBackPressTime = 0;
    private static final long BACK_PRESS_INTERVAL = 2000;
    private OwnerViewModel ownerViewModel;

    /**
     * Activity creation lifecycle method.
     * Initializes the layout, ViewModel, views, observers, listeners,
     * and registers a custom back-press handler.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBase(
                R.menu.owner_menu,
                R.layout.activity_owner
        );

        // Initialize ViewModel and menu handler
        viewModel = new ViewModelProvider(this).get(OwnerViewModel.class);
        menuHandler = new OwnerMenuHandler(this);
        ownerViewModel = new ViewModelProvider(this).get(OwnerViewModel.class);

        // Initialize UI, observers, and listeners
        initViews();
        observeViewModel();
        setupListeners();

        // Load owner's display name for the title
        viewModel.loadOwnerName();

        // Register custom back-press behavior
        setupBackPressedHandler();

        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        ownerViewModel.startCapacityMonitoring(currentUserId, (id, name) -> {
            // קריאה לפונקציה החדשה שיצרנו בסעיף 1
            NotificationHelper.showOwnerNotification(this, "אירוע מלא!", "האירוע " + name + " הגיע למכסה.");
        });
    }

    /**
     * Finds and caches all required view references from the layout.
     */
    private void initViews() {
        createEventBtn = findViewById(R.id.createEventButton);
        eventsContainer = findViewById(R.id.eventsContainer);
        emptyEventsText = findViewById(R.id.emptyEventsText);
    }

    /**
     * Sets up click listeners for UI actions such as
     * navigating to past events and creating a new event.
     */
    private void setupListeners() {
        // Navigate to the past events screen
        findViewById(R.id.btnPastEvents).setOnClickListener(v ->
                startActivity(new Intent(this, OwnerPastEventsActivity.class))
        );

        // Navigate to the create-new-event screen
        createEventBtn.setOnClickListener(v ->
                startActivity(new Intent(this, CreateNewEventActivity.class))
        );
    }

    /**
     * Called when the activity returns to the foreground.
     * Reloads the owner's events to ensure the list is up to date.
     */
    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadOwnerEvents();
    }

    /**
     * Subscribes to ViewModel LiveData and updates the UI accordingly:
     * - Updates the title with the owner's name
     * - Renders the list of upcoming events
     * - Shows empty state or error messages when needed
     */
    private void observeViewModel() {
        // Observe owner's name and update the title
        viewModel.getOwnerName().observe(this, name ->
                setTitleText(getString(R.string.hello_user, name))
        );

        // Observe upcoming events list and render them
        viewModel.getUpcomingEvents().observe(this, events -> {
            eventsContainer.removeAllViews();

            if (events == null || events.isEmpty()) {
                emptyEventsText.setVisibility(View.VISIBLE);
                eventsContainer.setVisibility(View.GONE);
            } else {
                emptyEventsText.setVisibility(View.GONE);
                eventsContainer.setVisibility(View.VISIBLE);
                for (Event event : events) {
                    addEventCard(event);
                }
            }
        });

        // Observe general messages (errors, confirmations) and show as Toast
        viewModel.getMessage().observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Creates and binds a single event card view for the given event,
     * including text fields and action buttons (edit / delete).
     */
    private void addEventCard(Event event) {
        // Inflate the event card layout
        View card = getLayoutInflater()
                .inflate(R.layout.activity_item_event_owner_card, eventsContainer, false);

        // Find views inside the card
        TextView title = card.findViewById(R.id.myEventTitle);
        TextView location = card.findViewById(R.id.myEventLocation);
        TextView date = card.findViewById(R.id.myEventDate);
        TextView genre = card.findViewById(R.id.myEventGenre);
        TextView capacity = card.findViewById(R.id.myEventCapacity);
        Button editBtn = card.findViewById(R.id.btnMyEventDetails);
        Button cancelBtn = card.findViewById(R.id.btnCancelMyEvent);

        // Bind basic event data
        title.setText(event.getName());
        location.setText(event.getAddress());
        long eventDateTime = event.getDateTime();

        // Convert genres to user-friendly text, with a fallback if empty
        String dateTimeText = DateUtils.formatOnlyDate(eventDateTime) + " • " + DateUtils.formatOnlyTime(eventDateTime);
        date.setText(dateTimeText);

        String genreText = GenreUtils.genresToTextFromEnums(event.getMusicGenresEnum());
        if (genreText == null || genreText.isEmpty()) {
            genreText = getString(R.string.no_genre_specified);
        }
        genre.setText(genreText);

        // Format and display capacity information
        String capacityText = getString(
                R.string.participants_format,
                event.getReserved(),
                event.getMaxCapacity()
        );
        capacity.setText(capacityText);

        // Edit button opens the edit event screen
        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditEventActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            startActivity(intent);
        });

        // Delete button opens a confirmation dialog
        cancelBtn.setOnClickListener(v ->
                showDeleteEventDialog(event.getId())
        );
        eventsContainer.addView(card);
    }

    /**
     * Shows a confirmation dialog before deleting an event.
     * If confirmed, delegates the deletion to the ViewModel.
     */
    private void showDeleteEventDialog(String eventId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_delete_event_title))
                .setMessage(getString(R.string.dialog_delete_event_message_confirm))
                .setPositiveButton(getString(R.string.dialog_yes_delete), (dialog, which) -> {
                    viewModel.deleteEvent(eventId);
                })
                .setNegativeButton(getString(R.string.dialog_no), (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Handles selection from the navigation drawer menu
     * by delegating to the OwnerMenuHandler.
     */
    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }

    /**
     * Registers a custom back-press handler.
     *
     * If the navigation drawer is open, it is closed.
     * Otherwise, the user must press back twice within a short interval
     * to exit the activity ("double back to exit" behavior).
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
                            OwnerActivity.this,
                            getString(R.string.back_press_exit),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

}
