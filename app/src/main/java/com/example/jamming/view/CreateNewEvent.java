package com.example.jamming.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.model.Owner;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateNewEvent extends AppCompatActivity {

    private EditText eventNameInput, genreInput, capacityInput, descriptionInput, dateInput, timeInput;

    private Button publishBtn;
    private TextView cancelBtn;


    private Calendar selectedDateTime = Calendar.getInstance();

    private FirebaseAuth auth;
    private UserRepository userRepository;
    private EventRepository eventRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);

        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
        eventRepository = new EventRepository();

        initViews();
        setupListeners();
    }

    private void initViews() {
        eventNameInput = findViewById(R.id.eventNameInput);
        genreInput = findViewById(R.id.genreSpinner);
        capacityInput = findViewById(R.id.eventCapacityInput);
        descriptionInput = findViewById(R.id.eventDescriptionInput);
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
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
            dateInput.setError(null);
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
            timeInput.setError(null);
        }, hour, minute, true);

        dialog.show();
    }

    private void publishEvent() {

        String name = eventNameInput.getText().toString().trim();
        String genreStr = genreInput.getText().toString().trim();
        String capacityStr = capacityInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (!validateInputs(name, genreStr, capacityStr)) {
            return;
        }

        int capacity = Integer.parseInt(capacityStr);
        List<String> musicTypes = Arrays.asList(genreStr.split(","));

        String ownerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (ownerId == null) {
            Toast.makeText(this, "שגיאה בזיהוי בעל האירוע", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.getUserById(ownerId).addOnSuccessListener(doc -> {
            Owner owner = doc.toObject(Owner.class);

            if (owner == null) {
                Toast.makeText(this, "שגיאה בפרטי הבעלים", Toast.LENGTH_SHORT).show();
                return;
            }

            Event event = new Event(
                    ownerId,
                    name,
                    "",
                    description,
                    musicTypes,
                    owner.getAddress(),
                    owner.getCity(),
                    selectedDateTime.getTimeInMillis(),
                    capacity,
                    owner.getLatitude(),
                    owner.getLongitude()
            );

            eventRepository.createEvent(event)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "האירוע נוצר בהצלחה!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "שגיאה בטעינת פרטי הבעלים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInputs(String name, String genre, String capacity) {

        if (name.isEmpty()) {
            eventNameInput.setError("נא להזין שם אירוע");
            capacityInput.requestFocus();
            return false;
        }
        if (dateInput.getText().toString().isEmpty()) {
            dateInput.setError("נא להזין את תאריך האירוע");
            capacityInput.requestFocus();
            return false;
        }
        if( timeInput.getText().toString().isEmpty()){
            timeInput.setError("נא להזין את שעת האירוע");
            capacityInput.requestFocus();
            return false;
        }

        if (genre.isEmpty()) {
            genreInput.setError("נא להזין ז'אנר");
            capacityInput.requestFocus();
            return false;
        }

        if (capacity.isEmpty()) {
            capacityInput.setError("נא להזין מספר מקומות");
            capacityInput.requestFocus();
            return false;
        }

        try {
            int cap = Integer.parseInt(capacity);
            if (cap <= 0) {
                capacityInput.setError("מספר המקומות חייב להיות חיובי");
                capacityInput.requestFocus();
                return false;
            }
        } catch (Exception e) {
            capacityInput.setError("נא להזין מספר תקין");
            capacityInput.requestFocus();
            return false;
        }

        return true;
    }
}
