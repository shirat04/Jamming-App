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

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;

    private TextView titleEvent, dateTextView,  locationTextView, eventDescription, capacityEvent, generEevet;

    private Button registerBtn;
    private LinearLayout contentLayout;

    private Button addToCalendarBtn;
    private ImageView eventImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = FirebaseFirestore.getInstance();
        initUI();

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("EVENT_ID")) {
            eventId = extras.getString("EVENT_ID");

            if (eventId != null && !eventId.isEmpty()) {
                loadEventDetails(eventId);
                registerBtn.setOnClickListener(v -> registerToEvent());
            } else {
                Toast.makeText(this, "שגיאה: ID אירוע חסר או ריק.", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(this, "שגיאה: לא נשלח ID אירוע ב-Intent.", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    private void initUI() {
        // 1. אתחול TextViews
        titleEvent = findViewById(R.id.titleEvent);
        locationTextView = findViewById(R.id.locationTextView);
        dateTextView = findViewById(R.id.dateTextView);
        eventDescription = findViewById(R.id.eventDescription);
        capacityEvent = findViewById(R.id.capacityEvent);
        generEevet = findViewById(R.id.genreTextView);

        // 2. אתחול Buttons
        registerBtn = findViewById(R.id.registerBtn);
        addToCalendarBtn = findViewById(R.id.addToCalendarBtn);

        // 3. אתחול ImageView
        eventImage = findViewById(R.id.eventImage);
        contentLayout = findViewById(R.id.contentLayout);

    }
    private void loadEventDetails(String eventId) {
        db.collection("events").document(eventId).get() // גישה ישירה למסמך לפי ה-ID
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // המרת המסמך לאובייקט Event
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            // 3. הצגת הנתונים ב-UI
                            displayEventData(event);
                        }
                    } else {
                        Toast.makeText(this, "שגיאה: אירוע לא נמצא.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EVENT_DETAIL", "Failed to load event details", e);
                    Toast.makeText(this, "שגיאה בטעינת הנתונים.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void registerToEvent() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid).set(new java.util.HashMap<String, Object>()
                {{put("firebaseId", uid);}}
                        ,com.google.firebase.firestore.SetOptions.merge())   // מוודא שמסמך משתמש קיים
                .continueWithTask(task ->
                        db.collection("events").document(eventId)
                                .update("reserved", FieldValue.increment(1))
                )
                .continueWithTask(task ->
                        db.collection("users").document(uid)
                                .update("registeredEventIds", FieldValue.arrayUnion(eventId))
                )
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "נרשמת לאירוע 🎉", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e -> {
                    Log.e("REGISTER", "failed", e);
                    Toast.makeText(this, "שגיאה בהרשמה", Toast.LENGTH_SHORT).show();
                });
    }


    private void displayEventData(Event event) {

        titleEvent.setText(event.getName());
        locationTextView.setText(event.getAddress());

        String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(event.getDateTime()));
        dateTextView.setText(formattedDate);

        eventDescription.setText(event.getDescription());

        String capacity = event.getReserved() + " / " + event.getMaxCapacity() + " משתתפים";
        capacityEvent.setText(capacity);

        List<String> genres = event.getMusicTypes();

        if (genres != null && !genres.isEmpty()) {
            generEevet.setText(String.join(" / ", genres));
        } else {
            generEevet.setText("No genre specified");
        }

        contentLayout.setVisibility(View.VISIBLE);
    }
}

