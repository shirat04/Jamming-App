package com.example.jamming.view;

import android.widget.Button;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.widget.LinearLayout;
import androidx.lifecycle.ViewModelProvider;
import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.model.MusicGenre;
import com.example.jamming.navigation.UserMenuHandler;
import com.example.jamming.utils.DateUtils;
import com.example.jamming.viewmodel.EventDetailViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity responsible for displaying the details of a single event.
 *
 * This class acts as the View in the MVVM architecture:
 * - Observes LiveData from EventDetailViewModel
 * - Updates the UI accordingly
 * - Forwards user actions (register / cancel) to the ViewModel
 */
public class EventDetailActivity extends BaseActivity  {
    // ViewModel holding the business logic and UI state
    private EventDetailViewModel viewModel;

    // UI elements for displaying event data
    private TextView titleEvent, dateTextView,  locationTextView, eventDescription, capacityEvent, genreEvent, soldOutLabel;

    // Action buttons
    private Button registerBtn, cancelRegistrationBtn;

    // Layout used to hide/show content during loading
    private LinearLayout contentLayout;

    // Handles top menu actions
    private UserMenuHandler menuHandler;

    /**
     * Initializes the activity, sets up the layout, ViewModel, observers,
     * and triggers loading of the event data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configure base layout and menu
        setupBase(
                R.menu.user_menu,
                R.layout.activity_event_detail
        );
        hideRightActions();

        // Bind UI components
        initUI();

        // Initialize ViewModel and navigation handler
        viewModel = new ViewModelProvider(this).get(EventDetailViewModel.class);
        menuHandler = new UserMenuHandler(this);

        // Register observers to react to ViewModel state changes
        observeViewModel();

        // Extract event ID from the intent and validate it
        String eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_missing_event_id), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Requests the ViewModel to load the event data
        viewModel.loadEvent(eventId);

        // Delegate user actions to the ViewModel
        registerBtn.setOnClickListener(v -> viewModel.registerToEvent());
        cancelRegistrationBtn.setOnClickListener(v -> {
            showCancelRegistrationDialog();
        });

    }

    /**
     * Binds all required UI components from the layout to class fields.
     */
    private void initUI() {
        titleEvent = findViewById(R.id.titleEvent);
        locationTextView = findViewById(R.id.locationTextView);
        dateTextView = findViewById(R.id.dateTextView);
        eventDescription = findViewById(R.id.eventDescription);
        capacityEvent = findViewById(R.id.capacityEvent);
        genreEvent = findViewById(R.id.genreTextView);
        registerBtn = findViewById(R.id.registerBtn);
        soldOutLabel = findViewById(R.id.soldOutLabel);
        cancelRegistrationBtn = findViewById(R.id.CancelRegistrationBtn);
        contentLayout = findViewById(R.id.contentLayout);

    }

    /**
     * Observes all relevant LiveData objects from the ViewModel and updates
     * the UI according to data changes, loading states, and one-time UI events.
     */
    private void observeViewModel() {

        // Observe the loaded event and update the UI when it changes
        viewModel.getEventLiveData().observe(this, event -> {
            if (event == null) return;

            displayEventData(event);
        });

        // Observe loading state and toggle content visibility accordingly
        viewModel.getIsLoading().observe(this, loading -> {
            if (loading != null && loading) {
                contentLayout.setVisibility(View.GONE);
            } else {
                contentLayout.setVisibility(View.VISIBLE);
            }
        });

        // Observe error messages and display them once to the user
        viewModel.getErrorMessageResId().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
            viewModel.clearErrorMessage();
        });

        // Observe one-time UI events such as registration success or failure
        viewModel.getUiEvent().observe(this, event -> {
            if (event == null) return;

            switch (event) {
                case REGISTER_SUCCESS:
                    Toast.makeText(this, getString(R.string.msg_register_success), Toast.LENGTH_SHORT).show();
                    break;
                case CANCEL_SUCCESS:
                    Toast.makeText(this, getString(R.string.msg_cancel_success), Toast.LENGTH_SHORT).show();
                    break;
                case ALREADY_REGISTERED:
                    Toast.makeText(this, getString(R.string.msg_already_registered), Toast.LENGTH_SHORT).show();
                    break;

            }
            viewModel.clearUiEvent();

        });

        // Observe registration-related UI state and update buttons and labels
        viewModel.getRegistrationUiState().observe(this, state -> {
            if (state == null) return;

            registerBtn.setEnabled(state.canRegister);
            cancelRegistrationBtn.setEnabled(state.canCancel);
            registerBtn.setText(state.mainText);
            cancelRegistrationBtn.setVisibility(state.canCancel ? View.VISIBLE : View.GONE);


            if (state.secondaryText != null) {
                soldOutLabel.setText(state.secondaryText);
                soldOutLabel.setVisibility(View.VISIBLE);
            } else {
                soldOutLabel.setVisibility(View.GONE);
            }
        });

    }

    /**
     * Shows a confirmation dialog before performing registration cancellation.
     */
    private void showCancelRegistrationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_cancel_registration_title)
                .setMessage(R.string.dialog_cancel_registration_message)
                .setPositiveButton(R.string.dialog_yes_cancel, (dialog, which) -> {
                    viewModel.cancelRegistration();
                })
                .setNegativeButton(R.string.dialog_no, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Updates the screen with the given event's data, including basic details,
     * formatted date/time, capacity information, and music genres.
     */
    private void displayEventData(Event event) {

        // Basic event information
        titleEvent.setText(event.getName());
        locationTextView.setText(event.getAddress());

        // Format and display date and time
        String dateTimeText =
                DateUtils.formatOnlyDate(event.getDateTime())
                        + " • "
                        + DateUtils.formatOnlyTime(event.getDateTime());

        dateTextView.setText(dateTimeText);

        // Display description
        eventDescription.setText(event.getDescription());

        // Format and display capacity information
        String capacity = getString(
                R.string.participants_format,
                event.getReserved(),
                event.getMaxCapacity()
        );
        capacityEvent.setText(capacity);

        // Display genres or a fallback text if none are defined
        List<MusicGenre> genres = event.getMusicGenresEnum();

        if (genres == null || genres.isEmpty()) {
            genreEvent.setText(R.string.no_genre_specified);
        } else {
            List<String> names = new ArrayList<>();
            for (MusicGenre genre : genres) {
                names.add(genre.getDisplayName());
            }
            genreEvent.setText(String.join(" , ", names));
        }
    }

    /**
     * Delegates handling of navigation menu selections to UserMenuHandler.
     */
    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }

}