package com.example.jamming.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.navigation.UserMenuHandler;
import com.example.jamming.viewmodel.EventDetailViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailActivity extends BaseActivity {

    private UserMenuHandler menuHandler;
    private EventDetailViewModel viewModel;
    private FirebaseFirestore db;
    private ListenerRegistration eventListener; // משתנה לשמירת המאזין לשינויים בזמן אמת
    private String currentEventId;

    // רכיבי המסך
    private TextView titleTv, dateTv, locationTv, descriptionTv, spotsTv;
    private Button registerBtn, cancelRegistrationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. הגדרת הבסיס והתפריט
        setupBase(R.menu.user_menu, R.layout.activity_event_detail);
        hideRightActions(); // הסתרת כפתורים עליונים אם צריך

        menuHandler = new UserMenuHandler(this);
        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(this).get(EventDetailViewModel.class);

        // 2. הפעלת התפריט הצדדי (חשוב!)
        setupNavigation();

        // 3. חיבור לרכיבי המסך (Binding)
        initUI();

        // 4. חילוץ ה-ID של האירוע
        currentEventId = getIntent().getStringExtra("EVENT_ID");

        // ניסיון גיבוי: אם הגיע כאובייקט שלם, נחלץ ממנו את ה-ID
        if (currentEventId == null) {
            Event passedEvent = (Event) getIntent().getSerializableExtra("event");
            if (passedEvent != null) {
                currentEventId = passedEvent.getId();
            }
        }

        // בדיקת תקינות
        if (currentEventId == null || currentEventId.isEmpty()) {
            Toast.makeText(this, "Error: Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 5. טעינת נתונים ראשונית ל-ViewModel (בשביל סטטוס ההרשמה)
        viewModel.loadEvent(currentEventId);

        // 6. === התיקון הקריטי: האזנה לשינויים בזמן אמת מ-Firebase ===
        listenToEventChanges(currentEventId);

        // 7. הגדרת כפתורים (הרשמה/ביטול)
        setupButtons();

        // מעקב אחרי ViewModel לשינויים בסטטוס הרשמה (האם הכפתור צריך להיות "הירשם" או "בטל")
        observeRegistrationStatus();
    }

    private void initUI() {
        titleTv = findViewById(R.id.eventTitle);
        dateTv = findViewById(R.id.eventDate);
        locationTv = findViewById(R.id.eventLocation);
        descriptionTv = findViewById(R.id.eventDescription);
        spotsTv = findViewById(R.id.eventSpots);

        registerBtn = findViewById(R.id.btnRegister); // ודאי שה-ID תואם ל-XML שלך
        cancelRegistrationBtn = findViewById(R.id.btnCancelRegistration); // כנ"ל
    }

    // פונקציה שמאזינה למסמך הספציפי ב-Firebase ומעדכנת את המסך
    private void listenToEventChanges(String eventId) {
        eventListener = db.collection("events").document(eventId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) {
                        return; // טיפול בשגיאות
                    }

                    // המרת המידע העדכני לאובייקט
                    Event updatedEvent = snapshot.toObject(Event.class);
                    if (updatedEvent != null) {
                        updatedEvent.setId(snapshot.getId()); // חשוב לשמור על ה-ID
                        // עדכון המסך עם המידע החדש ביותר!
                        updateUI(updatedEvent);
                    }
                });
    }

    // פונקציה לעדכון הטקסטים במסך
    private void updateUI(Event event) {
        if (titleTv != null) titleTv.setText(event.getName());
        if (locationTv != null) locationTv.setText(event.getAddress());
        if (descriptionTv != null) descriptionTv.setText(event.getDescription());

        if (dateTv != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            dateTv.setText(sdf.format(new Date(event.getDateTime())));
        }

        if (spotsTv != null) {
            spotsTv.setText("Spots left: " + event.getAvailableSpots());
        }
    }

    private void setupButtons() {
        if (registerBtn != null) {
            registerBtn.setOnClickListener(v -> viewModel.registerToEvent());
        }
        if (cancelRegistrationBtn != null) {
            cancelRegistrationBtn.setOnClickListener(v -> {
                // לוגיקה לביטול הרשמה (דיאלוג וכו')
                // אם יש לך פונקציה כזו, קראי לה כאן. לדוגמה:
                // showCancelRegistrationDialog();
                Toast.makeText(this, "Cancel logic here", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void observeRegistrationStatus() {
        // כאן אנחנו מקשיבים ל-ViewModel כדי לדעת איזה כפתור להציג (הירשם או בטל)
        viewModel.getRegistrationStatus().observe(this, isRegistered -> {
            if (isRegistered) {
                if (registerBtn != null) registerBtn.setVisibility(View.GONE);
                if (cancelRegistrationBtn != null) cancelRegistrationBtn.setVisibility(View.VISIBLE);
            } else {
                if (registerBtn != null) registerBtn.setVisibility(View.VISIBLE);
                if (cancelRegistrationBtn != null) cancelRegistrationBtn.setVisibility(View.GONE);
            }
        });
    }

    // הפונקציה שמפעילה את התפריט הצדדי
    private void setupNavigation() {
        com.google.android.material.navigation.NavigationView navigationView = findViewById(R.id.navigationView);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                boolean handled = menuHandler.handle(item.getItemId());
                if (handled) {
                    androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawerLayout);
                    if (drawer != null) drawer.closeDrawers();
                }
                return handled;
            });
        }
    }

    // חשוב: ניתוק המאזין כשיוצאים מהמסך כדי לחסוך סוללה
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventListener != null) {
            eventListener.remove();
        }
    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }
}