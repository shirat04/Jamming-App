package com.example.jamming.view;// קובץ: EventDetailActivity.java
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
// ... (ייבוא מחלקת Event ורכיבי UI כמו TextView)

public class eventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;

    private TextView titleEvent;        // מותאם ל-R.id.titleEvent
    private TextView subTitleEvent;     // מותאם ל-R.id.subTitleEvent
    private TextView dateTextView;      // מותאם ל-R.id.dateTextView
    private TextView locationTextView;  // מותאם ל-R.id.locationTextView
    private TextView eventDescription;  // מותאם ל-R.id.eventDescription
    private TextView capacityEvent;     // מותאם ל-R.id.capacityEvent
    private Button registerBtn;
    private Button addToCalendarBtn;
    private ImageView eventImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // אתחול Firebase ורכיבי UI
        db = FirebaseFirestore.getInstance();
        initUI(); // קריאה לפונקציה שתאתחל את ה-TextViews והכפתורים

        // 1. קליטת ה-EVENT_ID מה-Intent
        //intent כלי להעברת מסכים
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("EVENT_ID")) {
            eventId = extras.getString("EVENT_ID");

            // 2. אם יש ID, נתחיל לטעון את הנתונים
            if (eventId != null && !eventId.isEmpty()) {
                loadEventDetails(eventId);
                registerBtn.setOnClickListener(v -> registerToEvent());
            } else {
                Toast.makeText(this, "שגיאה: ID אירוע חסר או ריק.", Toast.LENGTH_LONG).show();
                finish(); // סגור את המסך אם אין ID
            }
        } else {
            Toast.makeText(this, "שגיאה: לא נשלח ID אירוע ב-Intent.", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    // פונקציה לאתחול רכיבי ה-UI
    private void initUI() {
        // 1. אתחול TextViews
        titleEvent = findViewById(R.id.titleEvent);
        subTitleEvent = findViewById(R.id.subTitleEvent);
        locationTextView = findViewById(R.id.locationTextView);
        dateTextView = findViewById(R.id.dateTextView);
        eventDescription = findViewById(R.id.eventDescription);
        capacityEvent = findViewById(R.id.capacityEvent); // שימו לב ל-ID capacityEvent

        // 2. אתחול Buttons
        registerBtn = findViewById(R.id.registerBtn);
        addToCalendarBtn = findViewById(R.id.addToCalendarBtn);

        // 3. אתחול ImageView
        eventImage = findViewById(R.id.eventImage);

        // ניתן להוסיף כאן מאזיני לחיצות (Click Listeners) לכפתורים
        // registerBtn.setOnClickListener(v -> handleRegistration());
        // addToCalendarBtn.setOnClickListener(v -> addToCalendar());
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

        db.collection("users")
                .document(uid)
                .set(new java.util.HashMap<String, Object>() {{
                    put("firebaseId", uid);
                }}, com.google.firebase.firestore.SetOptions.merge())   // מוודא שמסמך משתמש קיים
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


    // קובץ: EventDetailActivity.java (המשך)

    private void displayEventData(Event event) {
        // 1. שדות טקסט רגילים

        // מתאים ל-@+id/titleEvent
        titleEvent.setText(event.getName());

        // מתאים ל-@+id/subTitleEvent (נניח שזה האמן/סוג מוזיקה)
        List<String> musicTypes = event.getMusicTypes();
        subTitleEvent.setText(musicTypes == null ? "" : String.join(", ", musicTypes));

        // מתאים ל-@+id/locationTextView
        locationTextView.setText(event.getAddress());

        // 2. תאריך ושעה (דורש פירמוט)
        String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(event.getDateTime()));
        // מתאים ל-@+id/dateTextView
        dateTextView.setText(formattedDate);

        // מתאים ל-@+id/eventDescription - שימוש נכון בשם ה-ID
        eventDescription.setText(event.getDescription());

        // יש שדה תיאור נוסף ב-XML: descriptionEvent.
        // נראה שזה מיותר ב-XML, או שצריך להחליט איזה מהם הוא התיאור הראשי.



        // 3. קיבולת
        String capacity = event.getReserved() + " / " + event.getMaxCapacity() + " משתתפים";
        // מתאים ל-@+id/capacityEvent
        capacityEvent.setText(capacity);

        // 4. (אופציונלי) טעינת תמונה...
    }
}

