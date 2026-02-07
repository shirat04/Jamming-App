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
import com.example.jamming.utils.DateUtils;
import com.example.jamming.viewmodel.OwnerViewModel;
import static com.example.jamming.utils.GenreUtils.genresToText;

public class OwnerActivity extends BaseActivity {
    private OwnerViewModel viewModel;
    private TextView emptyEventsText;
    private OwnerMenuHandler menuHandler;
    private Button createEventBtn;
    private long lastBackPressTime = 0;
    private static final long BACK_PRESS_INTERVAL = 2000;

    private LinearLayout eventsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBase(
                R.menu.owner_menu,
                R.layout.activity_owner
        );


        viewModel = new ViewModelProvider(this).get(OwnerViewModel.class);
        menuHandler = new OwnerMenuHandler(this);
        initViews();
        observeViewModel();
        setupListeners();

        viewModel.loadOwnerName();
        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = menuHandler.handle(item.getItemId());
            drawerLayout.closeDrawers();
            return handled;
        });

    }
    private void initViews() {
        createEventBtn = findViewById(R.id.createEventButton);
        eventsContainer = findViewById(R.id.eventsContainer);
        emptyEventsText = findViewById(R.id.emptyEventsText);
    }

    private void setupListeners() {
        findViewById(R.id.btnPastEvents).setOnClickListener(v ->
                startActivity(new Intent(this, OwnerPastEventsActivity.class))
        );

        createEventBtn.setOnClickListener(v ->
                startActivity(new Intent(this, CreateNewEventActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadOwnerEvents();
    }

    private void observeViewModel() {
        viewModel.getOwnerName().observe(this, name ->
                setTitleText(getString(R.string.hello_user, name))
        );


        viewModel.getUpcomingEvents().observe(this, events -> {
            eventsContainer.removeAllViews();

            if (events == null || events.isEmpty()) {
                emptyEventsText.setVisibility(View.VISIBLE);
            } else {
                emptyEventsText.setVisibility(View.GONE);
                for (Event event : events) {
                    addEventCard(event);
                }
            }
        });

        viewModel.getMessage().observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }


    private void addEventCard(Event event) {
        View card = getLayoutInflater()
                .inflate(R.layout.activity_item_event_owner_card, eventsContainer, false);

        TextView title = card.findViewById(R.id.myEventTitle);
        TextView location = card.findViewById(R.id.myEventLocation);
        TextView date = card.findViewById(R.id.myEventDate);
        TextView genre = card.findViewById(R.id.myEventGenre);
        TextView capacity = card.findViewById(R.id.myEventCapacity);
        Button editBtn = card.findViewById(R.id.btnMyEventDetails);
        Button cancelBtn = card.findViewById(R.id.btnCancelMyEvent);

        title.setText(event.getName());
        location.setText(event.getAddress());
        long eventDateTime = event.getDateTime();

        String dateTimeText = DateUtils.formatOnlyDate(eventDateTime) + " • " + DateUtils.formatOnlyTime(eventDateTime);
        date.setText(dateTimeText);

        genre.setText(genresToText(event.getMusicTypes()));
        String capacityText = getString(
                R.string.participants_format,
                event.getReserved(),
                event.getMaxCapacity()
        );
        capacity.setText(capacityText);

        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditEventActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            startActivity(intent);
        });

        cancelBtn.setOnClickListener(v ->
                showDeleteEventDialog(event.getId())
        );
        eventsContainer.addView(card);
    }


    private void showDeleteEventDialog(String eventId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes, delete", (dialog, which) -> {
                    viewModel.deleteEvent(eventId);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastBackPressTime < BACK_PRESS_INTERVAL) {
            super.onBackPressed();
        } else {
            lastBackPressTime = now;
            Toast.makeText(this, "Click again to exit.", Toast.LENGTH_SHORT).show();
        }
    }
}
