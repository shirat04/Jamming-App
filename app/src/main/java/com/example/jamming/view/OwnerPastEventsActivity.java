package com.example.jamming.view;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.navigation.OwnerMenuHandler;
import com.example.jamming.utils.DateUtils;
import com.example.jamming.utils.GenreUtils;
import com.example.jamming.viewmodel.OwnerViewModel;

/**
 * Activity responsible for displaying the owner's past events.
 *
 * This screen observes the OwnerViewModel and renders a dynamic list of past events.
 * The Activity follows the MVVM pattern:
 * - UI logic only (no business logic)
 * - Data is provided via LiveData from the ViewModel
 * - User actions (e.g., delete event) are delegated back to the ViewModel
 */
public class OwnerPastEventsActivity extends BaseActivity {

    /** ViewModel that provides past events data and handles event deletion */
    private OwnerViewModel viewModel;

    /** Container that holds dynamically inflated event cards */
    private LinearLayout pastEventsContainer;

    /** Text shown when there are no past events to display */
    private TextView emptyPastEventsText;

    /** Handles navigation menu actions for owner screens */
    private OwnerMenuHandler menuHandler;

    /**
     * Initializes the screen:
     * - Sets up the base layout and toolbar title
     * - Binds UI elements
     * - Initializes ViewModel and menu handler
     * - Registers observers and triggers initial data load
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupBase(
                R.menu.owner_menu,
                R.layout.activity_owner_past_events
        );
        setTitleText(getString(R.string.past_events));

        // Bind UI components from the layout
        pastEventsContainer = findViewById(R.id.pastEventsContainer);
        emptyPastEventsText = findViewById(R.id.emptyPastEventsText);

        // Initialize ViewModel and menu handler
        viewModel = new ViewModelProvider(this).get(OwnerViewModel.class);
        menuHandler = new OwnerMenuHandler(this);

        // Start observing LiveData exposed by the ViewModel
        observeViewModel();

        // Trigger initial load of owner's events (past & upcoming are split in the ViewModel)
        viewModel.loadOwnerEvents();
    }

    /**
     * Registers observers to the ViewModel's LiveData:
     * - Past events list: updates the UI list dynamically
     * - Message events: shows short user feedback via Toast
     */
    private void observeViewModel() {
        viewModel.getPastEvents().observe(this, events -> {
            pastEventsContainer.removeAllViews();

            if (events == null || events.isEmpty()) {
                emptyPastEventsText.setVisibility(View.VISIBLE);
                pastEventsContainer.setVisibility(View.GONE);
                return;
            }

            emptyPastEventsText.setVisibility(View.GONE);
            pastEventsContainer.setVisibility(View.VISIBLE);

            for (Event event : events) {
                addPastEventCard(event);
            }
        });

        // Observe general messages from the ViewModel (errors, confirmations, etc.)
        viewModel.getMessage().observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Inflates and configures a single UI card representing a past event.
     *
     * The method:
     * - Populates textual fields (title, location, date, genres, capacity)
     * - Hides actions that are not relevant for past events (e.g., details button)
     * - Attaches a delete action that delegates the operation to the ViewModel
     *
     * @param event The past event to render
     */
    private void addPastEventCard(Event event) {
        // Inflate a reusable card layout into the container
        View card = getLayoutInflater()
                .inflate(
                        R.layout.activity_item_event_owner_card,
                        pastEventsContainer,
                        false
                );

        // Bind and populate basic event information
        ((TextView) card.findViewById(R.id.myEventTitle))
                .setText(event.getName());

        ((TextView) card.findViewById(R.id.myEventLocation))
                .setText(event.getAddress());

        // Format and display event date and time
        long time = event.getDateTime();
        String dateTimeText = DateUtils.formatOnlyDate(time) + " • " + DateUtils.formatOnlyTime(time);
        ((TextView) card.findViewById(R.id.myEventDate))
                .setText(dateTimeText);

        // Convert genre list to user-friendly text
        String genreText = GenreUtils.genresToTextFromEnums(event.getMusicGenresEnum());

        if (genreText == null || genreText.isEmpty()) {
            genreText = getString(R.string.no_genre_specified);
        }

        ((TextView) card.findViewById(R.id.myEventGenre))
                .setText(genreText);

        // Format and display capacity information using a string resource template
        String capacityText = getString(
                R.string.participants_format,
                event.getReserved(),
                event.getMaxCapacity()
        );
        ((TextView) card.findViewById(R.id.myEventCapacity))
                .setText(capacityText);

        // Past events do not support viewing details, so hide this button
        card.findViewById(R.id.btnMyEventDetails)
                .setVisibility(View.GONE);

        // Attach delete action for past events
        card.findViewById(R.id.btnCancelMyEvent)
                .setOnClickListener(v ->
                        showDeleteEventDialog(event.getId())
                );

        pastEventsContainer.addView(card);
    }

    /**
     * Shows a confirmation dialog before deleting a past event.
     *
     * If the user confirms, the deletion request is delegated to the ViewModel,
     * which handles the actual data operation and refreshes the list.
     *
     * @param eventId The ID of the event to delete
     */
    private void showDeleteEventDialog(String eventId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_delete_event_title))
                .setMessage(getString(R.string.dialog_delete_event_message))
                .setPositiveButton(getString(R.string.dialog_yes_delete), (dialog, which) ->
                        viewModel.deleteEvent(eventId)
                )
                .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) ->
                        dialog.dismiss()
                )
                .show();
    }

    /**
     * Handles navigation menu item selections.
     *
     * The actual navigation logic is delegated to OwnerMenuHandler
     * to keep this Activity focused only on UI concerns.
     */
    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }
}
