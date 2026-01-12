package com.example.jamming.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;

import com.example.jamming.R;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.utils.DateUtils;
import com.example.jamming.viewmodel.OwnerViewModel;

public class OwnerActivity extends BaseActivity {
    private OwnerViewModel viewModel;
    private TextView emptyEventsText;
    private OwnerMenuHandler menuHandler;

    private LinearLayout eventsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBase(
                R.menu.owner_menu,
                R.layout.activity_owner
        );
        showRightActions();


        Button createEventBtn = findViewById(R.id.createEventButton);
        eventsContainer = findViewById(R.id.eventsContainer);
        emptyEventsText = findViewById(R.id.emptyEventsText);


        viewModel = new ViewModelProvider(this).get(OwnerViewModel.class);
        menuHandler = new OwnerMenuHandler(this, viewModel);

        observeViewModel();

        viewModel.loadOwnerName();
        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = menuHandler.handle(item.getItemId());
            drawerLayout.closeDrawers();
            return handled;
        });

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
        viewModel.ownerName.observe(this, name ->
                setTitleText(getString(R.string.hello_user, name))
        );

        viewModel.events.observe(this, events -> {
            eventsContainer.removeAllViews();
            if (events == null || events.isEmpty()) {
                emptyEventsText.setVisibility(View.VISIBLE);
            } else {
                emptyEventsText.setVisibility(View.GONE);
                for (EventRepository.EventWithId e : events) {
                    addEventCard(e);
                }
            }
        });

        viewModel.message.observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }
    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }
    private void addEventCard(EventRepository.EventWithId e) {
        View card = getLayoutInflater()
                .inflate(R.layout.activity_item_event_owner_card, eventsContainer, false);

        TextView title = card.findViewById(R.id.myEventTitle);
        TextView location = card.findViewById(R.id.myEventLocation);
        TextView date = card.findViewById(R.id.myEventDate);
        TextView genre = card.findViewById(R.id.myEventGenre);
        TextView capacity = card.findViewById(R.id.myEventCapacity);
        Button editBtn = card.findViewById(R.id.btnMyEventDetails);
        Button cancelBtn = card.findViewById(R.id.btnCancelMyEvent);

        title.setText(e.event.getName());
        location.setText(e.event.getAddress());
        long ts = e.event.getDateTime();
        String dateTimeText = DateUtils.formatOnlyDate(ts) + " • " + DateUtils.formatOnlyTime(ts);
        date.setText(dateTimeText);

        genre.setText(String.join(", ", e.event.getMusicTypes()));
        String text = getString(
                R.string.participants_format,
                e.event.getReserved(),
                e.event.getMaxCapacity()
        );

        capacity.setText(text);

        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditEventActivity.class);
            intent.putExtra("EVENT_ID", e.id);
            startActivity(intent);
        });

        cancelBtn.setOnClickListener(v ->
                viewModel.deleteEvent(e.id)
        );

        eventsContainer.addView(card);
    }
}
