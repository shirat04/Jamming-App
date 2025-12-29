package com.example.jamming.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class MyEventUserActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_event_user);

        db = FirebaseFirestore.getInstance();
        container = findViewById(R.id.myEventsContainer);

        loadMyEvents();
    }

    private void loadMyEvents() {
        container.removeAllViews();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    List<String> ids = (List<String>) doc.get("registeredEventIds");

                    if (ids == null || ids.isEmpty()) {
                        addEmptyMessage("עוד לא נרשמת לאירועים 🙂");
                        return;
                    }

                    loadEventsByIds(ids);
                })
                .addOnFailureListener(e -> addEmptyMessage("שגיאה בטעינת My Events"));
    }

    private void loadEventsByIds(List<String> ids) {
        // Firestore whereIn מוגבל ל-10
        for (int i = 0; i < ids.size(); i += 10) {
            List<String> chunk = ids.subList(i, Math.min(i + 10, ids.size()));

            db.collection("events")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot == null || snapshot.isEmpty()) {
                            // אל תציג empty מיד, כי אולי יש chunk אחר שיחזיר תוצאות.
                            return;
                        }

                        for (DocumentSnapshot d : snapshot.getDocuments()) {
                            Event event = d.toObject(Event.class);
                            if (event == null) continue;

                            // שומרים ID למסך פרטים
                            String eventId = d.getId();

                            // (אופציונלי) אם יש לך גם event.getId() ואתה רוצה ליישר:
                            // event.setId(eventId);

                            addEventCard(eventId, event);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "שגיאה בטעינת אירועים", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void addEventCard(String eventId, Event event) {
        View card = getLayoutInflater().inflate(R.layout.item_my_event_card, container, false);

        TextView title = card.findViewById(R.id.myEventTitle);
        TextView location = card.findViewById(R.id.myEventLocation);
        TextView date = card.findViewById(R.id.myEventDate);
        TextView gener = card.findViewById(R.id.myEventGenre);
        TextView capacity = card.findViewById(R.id.myEventCapacity);

        Button detailsBtn = card.findViewById(R.id.btnMyEventDetails);
        Button cancelBtn = card.findViewById(R.id.btnCancelMyEvent);

        title.setText(event.getName());
        location.setText(event.getAddress());

        List<String> genres = event.getMusicTypes();
        if (genres != null && !genres.isEmpty()) {
            gener.setText(String.join(" / ", genres));
        } else {
            gener.setText("No genre");
        }

        String capacityText = event.getReserved() + " / " + event.getMaxCapacity() + " משתתפים";
        capacity.setText(capacityText);;


        // אצלך event.getDateTime() הוא millis (כמו שראיתי ב-EventDetailActivity)
        String formattedDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(new Date(event.getDateTime()));
        date.setText(formattedDate);

        detailsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        cancelBtn.setOnClickListener(v -> unregisterFromEvent(eventId));

        container.addView(card);
    }

    private void unregisterFromEvent(String eventId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid)
                .update("registeredEventIds", FieldValue.arrayRemove(eventId))
                .continueWithTask(task ->
                        db.collection("events").document(eventId)
                                .update("reserved", FieldValue.increment(-1))
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "ההרשמה בוטלה", Toast.LENGTH_SHORT).show();
                    loadMyEvents(); // ריענון
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בביטול הרשמה", Toast.LENGTH_SHORT).show()
                );
    }

    private void addEmptyMessage(String msg) {
        container.removeAllViews();

        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextSize(18);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad, pad, pad);

        container.addView(tv);
    }
}
