package com.example.jamming.view;
import android.widget.Button;
import android.widget.ImageView;
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

public class EventDetailActivity extends BaseActivity  {
    private EventDetailViewModel viewModel;

    private TextView titleEvent, dateTextView,  locationTextView, eventDescription, capacityEvent, generEevet, soldOutLabel;

    private Button registerBtn, cancelRegistrationBtn;
    private LinearLayout contentLayout;
    private UserMenuHandler menuHandler;

    private ImageView eventImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBase(
                R.menu.user_menu,
                R.layout.activity_event_detail
        );
        hideRightActions();

        initUI();

        viewModel = new ViewModelProvider(this).get(EventDetailViewModel.class);
        menuHandler = new UserMenuHandler(this);

        observeViewModel();

        String eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Something went wrong. Event ID is missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        viewModel.loadEvent(eventId);

        registerBtn.setOnClickListener(v -> viewModel.registerToEvent());
        cancelRegistrationBtn.setOnClickListener(v -> {
            showCancelRegistrationDialog();
        });

    }


    private void initUI() {
        titleEvent = findViewById(R.id.titleEvent);
        locationTextView = findViewById(R.id.locationTextView);
        dateTextView = findViewById(R.id.dateTextView);
        eventDescription = findViewById(R.id.eventDescription);
        capacityEvent = findViewById(R.id.capacityEvent);
        generEevet = findViewById(R.id.genreTextView);
        registerBtn = findViewById(R.id.registerBtn);
        soldOutLabel = findViewById(R.id.soldOutLabel);
        cancelRegistrationBtn = findViewById(R.id.CancelRegistrationBtn);
        eventImage = findViewById(R.id.eventImage);
        contentLayout = findViewById(R.id.contentLayout);

    }

    private void observeViewModel() {

        viewModel.getEventLiveData().observe(this, event -> {
            if (event == null) return;

            displayEventData(event);
        });

        viewModel.getIsLoading().observe(this, loading -> {
            if (loading != null && loading) {
                contentLayout.setVisibility(View.GONE);
            } else {
                contentLayout.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getErrorMessageResId().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getUiEvent().observe(this, event -> {
            if (event == null) return;

            switch (event) {
                case REGISTER_SUCCESS:
                    Toast.makeText(this, "You're registered! 🎉", Toast.LENGTH_SHORT).show();
                    break;
                case CANCEL_SUCCESS:
                    Toast.makeText(this, "Registration canceled", Toast.LENGTH_SHORT).show();
                    break;
                case ALREADY_REGISTERED:
                    Toast.makeText(this, "You're already registered", Toast.LENGTH_SHORT).show();
                    break;
            }
        });



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

    private void showCancelRegistrationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancel Registration")
                .setMessage("Are you sure you want to cancel your registration for the event?")
                .setPositiveButton("Yes, cancel", (dialog, which) -> {
                    viewModel.cancelRegistration();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void displayEventData(Event event) {

        titleEvent.setText(event.getName());
        locationTextView.setText(event.getAddress());

        String dateTimeText =
                DateUtils.formatOnlyDate(event.getDateTime())
                        + " • "
                        + DateUtils.formatOnlyTime(event.getDateTime());

        dateTextView.setText(dateTimeText);


        eventDescription.setText(event.getDescription());

        String capacity = " Participants: " + event.getReserved() + " / " + event.getMaxCapacity() ;
        capacityEvent.setText(capacity);

        List<MusicGenre> genres = event.getMusicGenresEnum();

        if (genres == null || genres.isEmpty()) {
            generEevet.setText("No genre specified");
        } else {
            List<String> names = new ArrayList<>();
            for (MusicGenre gener : genres) {
                names.add(gener.getDisplayName());
            }
            generEevet.setText(String.join(" , ", names));
        }


    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }

}

