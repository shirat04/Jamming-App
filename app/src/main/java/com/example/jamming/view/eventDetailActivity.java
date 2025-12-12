package com.example.jamming.view;// קובץ: EventDetailActivity.java
import android.util.Log;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Date;
// ... (ייבוא מחלקת Event ורכיבי UI כמו TextView)

public class eventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;

    // רכיבי UI שצריך לעדכן (לפי activity_event_detail.xml)
    private TextView titleTextView;
    private TextView artistTextView;
    private TextView dateTextView;
    private TextView locationTextView;
    private TextView capacityTextView;
    private TextView descriptionTextView;
    // ... הוסף את כל הרכיבים שתרצי לעדכן

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // אתחול Firebase ורכיבי UI
        db = FirebaseFirestore.getInstance();
        initUI(); // קריאה לפונקציה שתאתחל את ה-TextViews

        // 1. קליטת ה-EVENT_ID מה-Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("EVENT_ID")) {
            eventId = extras.getString("EVENT_ID");

            // 2. אם יש ID, נתחיל לטעון את הנתונים
            if (eventId != null) {
                loadEventDetails(eventId);
            } else {
                Toast.makeText(this, "שגיאה: ID אירוע חסר.", Toast.LENGTH_LONG).show();
                finish(); // סגור את המסך אם אין ID
            }
        } else {
            Toast.makeText(this, "שגיאה: לא נשלח ID אירוע.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // פונקציה לאתחול רכיבי ה-UI
    private void initUI() {
        titleTextView = findViewById(R.id.titleEvent);      // לפי ה-XML שלך
        artistTextView = findViewById(R.id.subTitleEvent);
        locationTextView = findViewById(R.id.locationTextView);  // @+id/locationText
        dateTextView = findViewById(R.id.dateTextView);
        capacityTextView = findViewById(R.id.capacityEvent);
        descriptionTextView = findViewById(R.id.descriptionEvent);
        // ... אתחל רכיבים נוספים
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

    // קובץ: EventDetailActivity.java (המשך)

    private void displayEventData(Event event) {
        // 1. שדות טקסט רגילים
        titleTextView.setText(event.getName());
        artistTextView.setText(event.getArtistName());
        descriptionTextView.setText(event.getDescription());
        locationTextView.setText(event.getAddress() + ", " + event.getCity());
        descriptionTextView.setText(event.getDescription());

        // 2. תאריך ושעה (דורש פירמוט)
        // הערה: יש צורך להמיר את long dateTime לפורמט תאריך נקי
        String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(event.getDateTime()));
        dateTextView.setText(formattedDate);

        // 3. קיבולת
        String capacity = event.getReserved() + " / " + event.getMaxCapacity() + " משתתפים";
        capacityTextView.setText(capacity);

        // 4. (אופציונלי) טעינת תמונה
        // אם יש לך URL של תמונה בשדה כלשהו ב-Event, תוכלי להשתמש בספרייה כמו Glide או Picasso
        // כדי לטעון אותה לתוך ה-ImageView (eventImage).

        // ... טיפול בכפתורי הרשמה (registerBtn) בהתאם ל-event.canRegister()
    }
    // ...
}   // קובץ: EventDetailActivity.java (המשך)

