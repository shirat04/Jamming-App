package com.example.jamming.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jamming.R;
import com.example.jamming.navigation.OwnerMenuHandler;
import com.example.jamming.repository.EventRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationsOwnerActivity extends BaseActivity {

    private OwnerMenuHandler menuHandler;
    private RecyclerView recyclerView;
    private EventRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. הגדרת התצוגה (חשוב: ודאי שבקובץ הזה יש RecyclerView!)
        setupBase(
                R.menu.owner_menu,
                R.layout.activity_notifications_owner
        );

        setTitleText("Notifications");
        menuHandler = new OwnerMenuHandler(this);

        // 2. מציאת הרשימה מהמסך הראשי
        // ודאי שב-activity_notifications_owner.xml ה-ID הוא recyclerNotifications
        recyclerView = findViewById(R.id.recyclerNotifications);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        // 3. משיכת הנתונים מה-Firebase
        repository = new EventRepository();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        repository.loadOwnerNotifications(currentUserId, new EventRepository.OnNotificationsLoadedCallback() {
            @Override
            public void onLoaded(List<Map<String, Object>> notifications) {
                if (notifications != null) {
                    // יצירת האדפטר שמחבר את המידע לקובץ העיצוב שלך
                    NotificationAdapter adapter = new NotificationAdapter(notifications);
                    recyclerView.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }

    // --- האדפטר: המחלקה שמחברת בין הנתונים לעיצוב שלך ---
    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private List<Map<String, Object>> data;

        public NotificationAdapter(List<Map<String, Object>> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // כאן אנחנו טוענים את קובץ ה-XML ששלחת לי (item_notification)
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> item = data.get(position);

            // שליפת הכותרת וההודעה
            String title = (String) item.get("title");
            String message = (String) item.get("message");

            // טיפול בתאריך (המרה מ-Timestamp של פיירבייס לשעה יפה)
            String dateText = "";
            Object timestampObj = item.get("timestamp");
            if (timestampObj instanceof Timestamp) {
                Timestamp ts = (Timestamp) timestampObj;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                dateText = sdf.format(ts.toDate());
            }

            // הצבה בשדות
            holder.textTitle.setText(title != null ? title : "התראה");
            holder.textMessage.setText(message != null ? message : "");
            holder.textDate.setText(dateText);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        // המחלקה שמחזיקה את ה-IDs מהקובץ XML שלך
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textTitle, textMessage, textDate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textTitle = itemView.findViewById(R.id.notifTitle);     // תואם ל-XML שלך
                textMessage = itemView.findViewById(R.id.notifMessage); // תואם ל-XML שלך
                textDate = itemView.findViewById(R.id.notifDate);       // תואם ל-XML שלך
            }
        }
    }
}