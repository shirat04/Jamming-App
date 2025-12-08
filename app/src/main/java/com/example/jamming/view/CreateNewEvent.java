package com.example.jamming.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jamming.R;
import com.example.jamming.model.Event;

import java.util.Calendar;
import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class CreateNewEvent extends AppCompatActivity {

    // Views
    private EditText eventNameInput;
    private EditText eventLocationInput;
    private EditText dateInput;
    private EditText timeInput;
    private EditText genreInput;
    private EditText capacityInput;
    private EditText descriptionInput;
    private Button publishBtn;
    private TextView cancelBtn;

    private Calendar selectedDateTime = Calendar.getInstance();

    // Firebase
    private DatabaseReference eventsRef;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);

        eventsRef = FirebaseDatabase.getInstance().getReference("events");
        auth = FirebaseAuth.getInstance();

        initViews();

        setupListeners();
    }
    private void initViews() {
        eventNameInput = findViewById(R.id.eventNameInput);
        eventLocationInput = findViewById(R.id.eventLocationInput);
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        genreInput = findViewById(R.id.genreSpinner);
        capacityInput = findViewById(R.id.eventCapacityInput);
        descriptionInput = findViewById(R.id.eventDescriptionInput);
        publishBtn = findViewById(R.id.publishEventBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
    }

    private void setupListeners() {

        dateInput.setOnClickListener(v -> showDatePicker());


        timeInput.setOnClickListener(v -> showTimePicker());


        publishBtn.setOnClickListener(v -> publishEvent());

        cancelBtn.setOnClickListener(v -> finish());
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
            dateInput.setText(sdf.format(selectedDateTime.getTime()));
        }, year, month, day);


        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }
    private void showTimePicker() {
        int hour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDateTime.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, m) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, h);
            selectedDateTime.set(Calendar.MINUTE, m);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeInput.setText(sdf.format(selectedDateTime.getTime()));
        }, hour, minute, true);

        dialog.show();
    }

    private void publishEvent() {

        String name = eventNameInput.getText().toString().trim();
        String location = eventLocationInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        String genre = genreInput.getText().toString().trim();
        String capacityStr = capacityInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();


        if (!validateInputs(name, location, date, time, capacityStr)) {
            return;
        }

        int capacity = Integer.parseInt(capacityStr);


        String ownerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown";


        Event event = new Event(ownerId, name, "", description, genre, location, selectedDateTime.getTimeInMillis(), capacity);

        saveEventToFirebase(event);
    }

    private boolean validateInputs(String name, String location, String date, String time, String capacity) {
        if (name.isEmpty()) {
            eventNameInput.setError("נא להזין שם אירוע");
            eventNameInput.requestFocus();
            return false;
        }

        if (location.isEmpty()) {
            eventLocationInput.setError("נא להזין מיקום");
            eventLocationInput.requestFocus();
            return false;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "נא לבחור תאריך", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (time.isEmpty()) {
            Toast.makeText(this, "נא לבחור שעה", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (capacity.isEmpty()) {
            capacityInput.setError("נא להזין קיבולת מקסימלית");
            capacityInput.requestFocus();
            return false;
        }

        try {
            int cap = Integer.parseInt(capacity);
            if (cap <= 0) {
                capacityInput.setError("קיבולת חייבת להיות מספר חיובי");
                capacityInput.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            capacityInput.setError("נא להזין מספר תקין");
            capacityInput.requestFocus();
            return false;
        }

        return true;
    }

    private void saveEventToFirebase(Event event) {
        String eventId = eventsRef.push().getKey();

        if (eventId == null) {
            Toast.makeText(this, "שגיאה ביצירת האירוע", Toast.LENGTH_SHORT).show();
            return;
        }

        event.setId(eventId);

        eventsRef.child(eventId).setValue(event)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "האירוע פורסם בהצלחה!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בפרסום האירוע: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

