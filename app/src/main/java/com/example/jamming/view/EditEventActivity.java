package com.example.jamming.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity {

    private String eventId;
    private FirebaseFirestore db;

    private EditText etEventTitle, etEventDescription, etEventLocation, etEventDate, etEventTime, etEventCapacity;
    private Button btnSaveEvent, btnDeleteEvent;

    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        db = FirebaseFirestore.getInstance();

        initViews();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("EVENT_ID")) {
            eventId = intent.getStringExtra("EVENT_ID");
            if (eventId != null && !eventId.isEmpty()) {
                loadEventData(eventId);
            }
        }

        setupListeners();
    }

    private void initViews() {
        etEventTitle = findViewById(R.id.etEventTitle);
        etEventDescription = findViewById(R.id.etEventDescription);
        etEventLocation = findViewById(R.id.etEventLocation);
        etEventDate = findViewById(R.id.etEventDate);
        etEventTime = findViewById(R.id.etEventTime);
        etEventCapacity = findViewById(R.id.etEventCapacity);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
    }

    private void setupListeners() {
        etEventDate.setOnClickListener(v -> showDatePicker());
        etEventTime.setOnClickListener(v -> showTimePicker());

        btnSaveEvent.setOnClickListener(v -> saveEventChanges());
        btnDeleteEvent.setOnClickListener(v -> deleteEvent());
    }

    private void loadEventData(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            etEventTitle.setText(event.getName());
                            etEventDescription.setText(event.getDescription());
                            etEventLocation.setText(event.getAddress()); // Assuming location is address
                            etEventCapacity.setText(String.valueOf(event.getMaxCapacity()));

                            selectedDateTime.setTimeInMillis(event.getDateTime());
                            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            etEventDate.setText(sdfDate.format(selectedDateTime.getTime()));

                            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            etEventTime.setText(sdfTime.format(selectedDateTime.getTime()));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditEventActivity.this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDatePicker() {
        int year = selectedDateTime.get(Calendar.YEAR);
        int month = selectedDateTime.get(Calendar.MONTH);
        int day = selectedDateTime.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            selectedDateTime.set(Calendar.YEAR, y);
            selectedDateTime.set(Calendar.MONTH, m);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, d);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etEventDate.setText(sdf.format(selectedDateTime.getTime()));
        }, year, month, day);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showTimePicker() {
        int hour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDateTime.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, h, m) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, h);
            selectedDateTime.set(Calendar.MINUTE, m);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            etEventTime.setText(sdf.format(selectedDateTime.getTime()));
        }, hour, minute, true).show();
    }

    private void saveEventChanges() {
        String title = etEventTitle.getText().toString().trim();
        String description = etEventDescription.getText().toString().trim();
        String location = etEventLocation.getText().toString().trim();
        String capacityStr = etEventCapacity.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || location.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedEvent = new HashMap<>();
        updatedEvent.put("name", title);
        updatedEvent.put("description", description);
        updatedEvent.put("address", location);
        updatedEvent.put("maxCapacity", Integer.parseInt(capacityStr));
        updatedEvent.put("date", selectedDateTime.getTimeInMillis());

        db.collection("events").document(eventId).update(updatedEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating event.", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteEvent() {
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting event.", Toast.LENGTH_SHORT).show();
                });
    }
}
