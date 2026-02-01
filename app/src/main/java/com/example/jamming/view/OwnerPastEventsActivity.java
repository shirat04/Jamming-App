package com.example.jamming.view;

import static com.example.jamming.utils.GenreUtils.genresToText;

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
import com.example.jamming.viewmodel.OwnerViewModel;

public class OwnerPastEventsActivity extends BaseActivity {

    private OwnerViewModel viewModel;
    private LinearLayout pastEventsContainer;
    private TextView emptyPastEventsText;
    private OwnerMenuHandler menuHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupBase(
                R.menu.owner_menu,
                R.layout.activity_owner_past_events
        );
        setTitleText(getString(R.string.past_events));
        showRightActions();

        pastEventsContainer = findViewById(R.id.pastEventsContainer);
        emptyPastEventsText = findViewById(R.id.emptyPastEventsText);

        viewModel = new ViewModelProvider(this).get(OwnerViewModel.class);
        menuHandler = new OwnerMenuHandler(this);

        observeViewModel();

        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = menuHandler.handle(item.getItemId());
            drawerLayout.closeDrawers();
            return handled;
        });

        viewModel.loadOwnerEvents();
    }

    private void observeViewModel() {
        viewModel.pastEvents.observe(this, events -> {
            pastEventsContainer.removeAllViews();

            if (events == null || events.isEmpty()) {
                emptyPastEventsText.setVisibility(View.VISIBLE);
                return;
            }

            emptyPastEventsText.setVisibility(View.GONE);

            for (Event event : events) {
                addPastEventCard(event);
            }
        });

        viewModel.message.observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }

    private void addPastEventCard(Event event) {
        View card = getLayoutInflater()
                .inflate(
                        R.layout.activity_item_event_owner_card,
                        pastEventsContainer,
                        false
                );

        ((TextView) card.findViewById(R.id.myEventTitle))
                .setText(event.getName());

        ((TextView) card.findViewById(R.id.myEventLocation))
                .setText(event.getAddress());

        long time = event.getDateTime();
        String dateTimeText = DateUtils.formatOnlyDate(time) + " • " + DateUtils.formatOnlyTime(time);
        ((TextView) card.findViewById(R.id.myEventDate))
                .setText(dateTimeText);

        ((TextView) card.findViewById(R.id.myEventGenre))
                .setText(genresToText(event.getMusicTypes()));

        String capacityText = getString(
                R.string.participants_format,
                event.getReserved(),
                event.getMaxCapacity()
        );
        ((TextView) card.findViewById(R.id.myEventCapacity))
                .setText(capacityText);

        card.findViewById(R.id.btnMyEventDetails)
                .setVisibility(View.GONE);

        card.findViewById(R.id.btnCancelMyEvent)
                .setOnClickListener(v ->
                        showDeleteEventDialog(event.getId())
                );

        pastEventsContainer.addView(card);
    }

    private void showDeleteEventDialog(String eventId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("This event has already passed.\nAre you sure you want to delete it?")
                .setPositiveButton("Yes, delete", (dialog, which) ->
                        viewModel.deleteEvent(eventId)
                )
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss()
                )
                .show();
    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }
}
