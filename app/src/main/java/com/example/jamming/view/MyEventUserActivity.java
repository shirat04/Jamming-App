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
import com.example.jamming.utils.DateUtils;
import com.example.jamming.viewmodel.MyEventUserViewModel;

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
                addEventCard(e.id, e.event);
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
                Toast.makeText(this, "ההרשמה בוטלה", Toast.LENGTH_SHORT).show();
                viewModel.resetCancelSuccess();
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addEventCard(String eventId, Event event) {

        View card = getLayoutInflater()
                .inflate(R.layout.item_my_event_card, container, false);

        TextView title = card.findViewById(R.id.myEventTitle);
        TextView location = card.findViewById(R.id.myEventLocation);
        TextView date = card.findViewById(R.id.myEventDate);
        TextView genre = card.findViewById(R.id.myEventGenre);
        TextView capacity = card.findViewById(R.id.myEventCapacity);

        Button cancelBtn = card.findViewById(R.id.btnCancelMyEvent);
        Button detailsBtn = card.findViewById(R.id.btnMyEventDetails);

        title.setText(event.getName());
        location.setText(event.getAddress());

        List<String> genres = event.getMusicTypes();
        genre.setText(
                genres != null && !genres.isEmpty()
                        ? String.join(" / ", genres)
                        : "No genre"
        );

        capacity.setText(
                event.getReserved() + " / " + event.getMaxCapacity() + " משתתפים"
        );

        date.setText(
                DateUtils.formatOnlyDate(event.getDateTime()) +
                        " • " +
                        DateUtils.formatOnlyTime(event.getDateTime())
        );


        cancelBtn.setOnClickListener(v ->
                viewModel.cancelRegistration(eventId)
        );

        detailsBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, EventDetailActivity.class);
            i.putExtra("EVENT_ID", eventId);
            startActivity(i);
        });

        container.addView(card);
    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }


}
