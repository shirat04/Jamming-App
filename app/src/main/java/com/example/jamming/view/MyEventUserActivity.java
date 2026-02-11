package com.example.jamming.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.model.MusicGenre;
import com.example.jamming.navigation.UserMenuHandler;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.utils.DateUtils;
import com.example.jamming.utils.NotificationHelper;
import com.example.jamming.viewmodel.MyEventUserViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays the list of events the current user is registered to.
 *
 * This screen follows the MVVM pattern:
 * - The Activity is responsible only for UI rendering and user interactions.
 * - The ViewModel provides the data and exposes UI state via LiveData.
 * - All business logic (loading events, canceling registration, removing events) is delegated
 *   to the ViewModel.
 */
public class MyEventUserActivity extends BaseActivity {

    // ViewModel that provides the user's events data and handles related actions
    private MyEventUserViewModel viewModel;

    // Container that holds the dynamically created event cards
    private LinearLayout container;

    // Text shown when there are no events or when an empty/error state is displayed
    private TextView emptyMessageText;

    // Helper responsible for handling navigation drawer menu actions
    private UserMenuHandler menuHandler;




    /**
     * Initializes the screen, sets up the base layout, binds UI elements,
     * creates the ViewModel instance, and registers observers.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBase(R.menu.user_menu, R.layout.activity_my_event_user);
        hideRightActions();

        // Bind UI elements
        container = findViewById(R.id.myEventsContainer);
        emptyMessageText = findViewById(R.id.emptyMessageText);
        setTitleText(getString(R.string.my_events));

        // Initialize ViewModel and menu handler
        viewModel = new ViewModelProvider(this).get(MyEventUserViewModel.class);
        menuHandler = new UserMenuHandler(this);

        observeViewModel();
    }

    /**
     * Reloads the user's events whenever the screen becomes visible again.
     * This ensures the list stays up-to-date after returning from other screens.
     */
    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadMyEvents();
    }

    /**
     * Observes all relevant LiveData objects from the ViewModel and updates the UI
     * according to the current data and screen state.
     */
    private void observeViewModel() {
        // Observe the list of events the user is registered to
        viewModel.getMyEvents().observe(this, events -> {
            container.removeAllViews();

            if (events == null || events.isEmpty()) {
                emptyMessageText.setVisibility(View.VISIBLE);
                container.setVisibility(View.GONE);
                return;
            }

            emptyMessageText.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);

            for (MyEventUserViewModel.EventWithId e : events) {
                addEventCard(e);
            }
        });

        // Observe general UI state such as errors or empty states
        viewModel.getState().observe(this, state -> {
            switch (state) {
                case NOT_LOGGED_IN:
                    emptyMessageText.setText(getString(R.string.error_user_not_logged_in));
                    break;
                case NO_REGISTERED_EVENTS:
                    emptyMessageText.setText(getString(R.string.no_registered_events));
                    break;
                case LOAD_ERROR:
                    Toast.makeText(this, getString(R.string.error_failed_to_load_events), Toast.LENGTH_SHORT).show();
                    break;
                case NONE:
                    emptyMessageText.setVisibility(View.GONE);
                    break;
            }
        });

        // Observe one-time success flag for canceling registration
        viewModel.getCancelSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, getString(R.string.msg_cancel_success), Toast.LENGTH_SHORT).show();
                viewModel.resetCancelSuccess();
            }
        });
    }

    /**
     * Displays a confirmation dialog before canceling the user's registration
     * to an upcoming event.
     */
    private void showCancelRegistrationDialog(String eventId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_cancel_registration_title))
                .setMessage(getString(R.string.dialog_cancel_registration_message))
                .setPositiveButton(getString(R.string.dialog_yes_cancel), (dialog, which) -> {
                    viewModel.cancelRegistration(eventId);
                })
                .setNegativeButton(getString(R.string.dialog_no), (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Displays a confirmation dialog for removing a past event from the user's list.
     * This is used only for events that have already ended.
     */
    private void showRemoveEventDialog(String eventId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_remove_event_title))
                .setMessage(getString(R.string.dialog_remove_event_message))
                .setPositiveButton(getString(R.string.dialog_yes_remove), (dialog, which) -> {
                    viewModel.removeEventFromMyList(eventId);
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Creates a visual card for a single event, binds all event data to the UI,
     * and configures the available actions according to the event's state.
     */
    private void addEventCard(MyEventUserViewModel.EventWithId wrapper) {
        Event event = wrapper.event;
        boolean isPast = event.getDateTime() < System.currentTimeMillis();

        View card = getLayoutInflater()
                .inflate(R.layout.activity_item_event_user_card, container, false);

        TextView title = card.findViewById(R.id.myEventTitle);
        TextView location = card.findViewById(R.id.myEventLocation);
        TextView date = card.findViewById(R.id.myEventDate);
        TextView genre = card.findViewById(R.id.myEventGenre);
        TextView capacity = card.findViewById(R.id.myEventCapacity);
        TextView statusLabel = card.findViewById(R.id.eventStatusLabel);
        Button cancelBtn = card.findViewById(R.id.btnCancelMyEvent);
        Button detailsBtn = card.findViewById(R.id.btnMyEventDetails);

        // Bind basic event information
        title.setText(event.getName());
        location.setText(event.getAddress());

        // Visually mark past events
        if (isPast) {
            statusLabel.setText(getString(R.string.status_event_ended));
            statusLabel.setVisibility(View.VISIBLE);
            card.setAlpha(0.75f);
        }

        // Render the list of genres or show a fallback text
        List<MusicGenre> genres = event.getMusicGenresEnum();
        if (genres != null && !genres.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (MusicGenre musicGenre : genres) {
                names.add(musicGenre.getDisplayName());
            }
            genre.setText(String.join(" , ", names));
        } else {
            genre.setText(getString(R.string.no_genre));
        }

        // Format and display the capacity information
        String capacityText = getString(
                R.string.participants_format,
                event.getReserved(),
                event.getMaxCapacity()
        );
        capacity.setText(capacityText);

        // Format and display date and time
        String dateTimeText = DateUtils.formatOnlyDate(event.getDateTime()) +
                " • " + DateUtils.formatOnlyTime(event.getDateTime());
        date.setText(dateTimeText);

        // Configure the main action button according to the event state
        if (isPast) {
            cancelBtn.setText(getString(R.string.action_remove));
            cancelBtn.setOnClickListener(v ->
                    showRemoveEventDialog(wrapper.id)
            );

        } else {
            cancelBtn.setText(getString(R.string.action_cancel_registration));
            cancelBtn.setOnClickListener(v ->
                    showCancelRegistrationDialog(wrapper.id)
            );
        }

        // Navigate to the event details screen
        detailsBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, EventDetailActivity.class);
            i.putExtra("EVENT_ID", wrapper.id);
            startActivity(i);
        });

        container.addView(card);
    }

    /**
     * Handles selection of items from the navigation drawer menu.
     */
    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }





}
