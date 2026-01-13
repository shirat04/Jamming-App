package com.example.jamming.view;// קובץ: EventDetailActivity.java
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.viewmodel.EventDetailViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class EventDetailActivity extends BaseActivity  {
    private EventDetailViewModel viewModel;

    private TextView titleEvent, dateTextView,  locationTextView, eventDescription, capacityEvent, generEevet;

    private Button registerBtn;
    private LinearLayout contentLayout;
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
        observeViewModel();

        String eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "שגיאה: לא נשלח ID אירוע ב-Intent.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        viewModel.loadEvent(eventId);
        registerBtn.setOnClickListener(v -> viewModel.registerToEvent());

    }


    private void initUI() {
        titleEvent = findViewById(R.id.titleEvent);
        locationTextView = findViewById(R.id.locationTextView);
        dateTextView = findViewById(R.id.dateTextView);
        eventDescription = findViewById(R.id.eventDescription);
        capacityEvent = findViewById(R.id.capacityEvent);
        generEevet = findViewById(R.id.genreTextView);
        registerBtn = findViewById(R.id.registerBtn);
        eventImage = findViewById(R.id.eventImage);
        contentLayout = findViewById(R.id.contentLayout);

    }

    private void observeViewModel() {

        viewModel.getEventLiveData().observe(this, event -> {
            if (event != null) {
                displayEventData(event);
                contentLayout.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getIsAlreadyRegistered().observe(this, isRegistered -> {
            if (isRegistered != null && isRegistered) {
                registerBtn.setEnabled(false);
            }
        });
        viewModel.getShowAlreadyRegisteredMessage().observe(this, show -> {
            if (show != null && show) {
                Toast.makeText(this, "כבר נרשמת לאירוע הזה", Toast.LENGTH_SHORT).show();
            }
        });


        viewModel.getRegistrationSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "נרשמת לאירוע 🎉", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayEventData(Event event) {

        titleEvent.setText(event.getName());
        locationTextView.setText(event.getAddress());

        String formattedDate = DateFormat
                .getDateInstance(DateFormat.MEDIUM)
                .format(new Date(event.getDateTime()));
        dateTextView.setText(formattedDate);

        eventDescription.setText(event.getDescription());

        String capacity = event.getReserved() + " / " + event.getMaxCapacity() + " משתתפים";
        capacityEvent.setText(capacity);

        List<String> genres = event.getMusicTypes();
        generEevet.setText(
                genres != null && !genres.isEmpty()
                        ? String.join(" / ", genres)
                        : "No genre specified"
        );
    }

}

