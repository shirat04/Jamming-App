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
import com.example.jamming.model.Event;
import com.example.jamming.model.MusicGenre;
import com.example.jamming.navigation.UserMenuHandler;
import com.example.jamming.utils.DateUtils;
import com.example.jamming.viewmodel.MyEventUserViewModel;

import java.util.ArrayList;
import java.util.List;


public class MyEventUserActivity extends BaseActivity {

    private MyEventUserViewModel viewModel;
    private LinearLayout container;
    private TextView emptyMessageText;
    private UserMenuHandler menuHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBase(R.menu.user_menu, R.layout.activity_my_event_user);
        hideRightActions();

        container = findViewById(R.id.myEventsContainer);
        emptyMessageText = findViewById(R.id.emptyMessageText);
        setTitleText(getString(R.string.my_events));

        viewModel = new ViewModelProvider(this).get(MyEventUserViewModel.class);
        menuHandler = new UserMenuHandler(this);

        observeViewModel();

        viewModel.loadMyEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadMyEvents();
    }

    private void observeViewModel() {

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


        viewModel.getEmptyMessage().observe(this, msg -> {
            if (msg != null) {
                emptyMessageText.setText(msg);
                emptyMessageText.setVisibility(View.VISIBLE);
                container.setVisibility(View.GONE);
            }
        });


        viewModel.getCancelSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Registration canceled", Toast.LENGTH_SHORT).show();
                viewModel.resetCancelSuccess();
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCancelRegistrationDialog(String eventId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancel Registration")
                .setMessage("Are you sure you want to cancel your registration for this event?")
                .setPositiveButton("Yes, cancel", (dialog, which) -> {
                    viewModel.cancelRegistration(eventId);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void showRemoveEventDialog(String eventId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Remove event")
                .setMessage("This event has already ended.\nDo you want to remove it from your list?")
                .setPositiveButton("Yes, remove", (dialog, which) -> {
                    viewModel.removeEventFromMyList(eventId);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void addEventCard(MyEventUserViewModel.EventWithId wrapper) {
        Event event = wrapper.event;
        boolean isPast = wrapper.isPast;

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

        title.setText(event.getName());
        location.setText(event.getAddress());

        List<MusicGenre> genres = event.getMusicGenresEnum();
        if (isPast) {
            statusLabel.setVisibility(View.VISIBLE);
            card.setAlpha(0.75f);
        }


        if (genres != null && !genres.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (MusicGenre musicGenre : genres) {
                names.add(musicGenre.getDisplayName());
            }
            genre.setText(String.join(" , ", names));
        } else {
            genre.setText("No genre");
        }
        String capacityText = " Participants: " + event.getReserved() + " / " + event.getMaxCapacity();
        capacity.setText(capacityText);

        String dateTimeText = DateUtils.formatOnlyDate(event.getDateTime()) +
                " • " + DateUtils.formatOnlyTime(event.getDateTime());
        date.setText(dateTimeText);


        if (isPast) {
            cancelBtn.setText("Remove");
            cancelBtn.setOnClickListener(v ->
                    showRemoveEventDialog(wrapper.id)
            );

        } else {
            cancelBtn.setText("Cancel registration");
            cancelBtn.setOnClickListener(v ->
                    showCancelRegistrationDialog(wrapper.id)
            );
        }

        detailsBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, EventDetailActivity.class);
            i.putExtra("EVENT_ID", wrapper.id);
            startActivity(i);
        });

        container.addView(card);
    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }


}
