package com.example.jamming.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OwnerActivity extends AppCompatActivity {
    private Button createEventButton;
    private LinearLayout eventsContainer;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_owner);
        TextView greeting = findViewById(R.id.ownerGreeting);

        createEventButton = findViewById(R.id.createEventButton);
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerActivity.this, CreateNewEvent.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        loadOwnerName();
        eventsContainer = findViewById(R.id.eventsContainer);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOwnerEvents();
    }

    @SuppressLint("SetTextI18n")
    private void loadOwnerName() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        TextView greeting = findViewById(R.id.ownerGreeting);

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("fullName"); // או "username" לפי מה ששמרת
                        greeting.setText("Hello " + name);
                    } else {
                        greeting.setText("Hello Owner");
                    }
                })
                .addOnFailureListener(e -> greeting.setText("Hello Owner"));
    }


    private void loadOwnerEvents() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUserId = auth.getCurrentUser().getUid();

        eventsContainer.removeAllViews(); // Clear previous views

        db.collection("events")
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            addEventCardToView(event, document.getId());
                        }
                    } else {
                        Toast.makeText(OwnerActivity.this, "Failed to load events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addEventCardToView(Event event, String eventId) {
        View eventCardView = getLayoutInflater().inflate(R.layout.item_event_card, eventsContainer, false);

        TextView eventName = eventCardView.findViewById(R.id.eventName);
        TextView eventLocation = eventCardView.findViewById(R.id.eventLocation);
        TextView eventDate = eventCardView.findViewById(R.id.eventDate);
        TextView eventGenre = eventCardView.findViewById(R.id.eventGenre);
        TextView eventSpots = eventCardView.findViewById(R.id.eventSpots);
        Button btnEdit = eventCardView.findViewById(R.id.btnEditEvent);
        Button btnCancel = eventCardView.findViewById(R.id.btnCancelEvent);

        // Format date and time
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy • HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(event.getDateTime()));

        // Populate the views
        eventName.setText(event.getName());
        eventLocation.setText("📍 " + event.getAddress() + ", " + event.getCity());
        eventDate.setText("🕒 " + formattedDate);
        eventGenre.setText("🎵 " + String.join(", ", event.getMusicTypes()));
        eventSpots.setText("👥 " + event.getReserved() + "/" + event.getMaxCapacity() + " spots");

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerActivity.this, EditEventActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        btnCancel.setOnClickListener(v -> {
            // Add logic to cancel/delete the event
            db.collection("events").document(eventId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(OwnerActivity.this, "Event cancelled", Toast.LENGTH_SHORT).show();
                        loadOwnerEvents(); // Refresh the list
                    })
                    .addOnFailureListener(e -> Toast.makeText(OwnerActivity.this, "Error cancelling event", Toast.LENGTH_SHORT).show());
        });

        eventsContainer.addView(eventCardView);
    }
}
