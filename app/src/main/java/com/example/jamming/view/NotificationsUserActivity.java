package com.example.jamming.view;

import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jamming.R;
import com.example.jamming.model.NotificationModel;
import com.example.jamming.navigation.UserMenuHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class NotificationsUserActivity extends BaseActivity {

    private UserMenuHandler menuHandler;

    // משתנים חדשים עבור הרשימה
    private RecyclerView recyclerView;
    private View emptyView;
    private NotificationsAdapter adapter;
    private List<NotificationModel> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setupBase(
                R.menu.user_menu,
                R.layout.activity_notifications_user
        );

        setTitleText("Notifications");
        menuHandler = new UserMenuHandler(this);


        recyclerView = findViewById(R.id.notificationsRecyclerView);
        emptyView = findViewById(R.id.emptyView);

        // הגדרת ה-RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        adapter = new NotificationsAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        // 3. טעינת הנתונים מ-Firebase
        loadNotifications();
    }

    private void loadNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING) // הכי חדש למעלה
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        notificationList.clear();
                        notificationList.addAll(value.toObjects(NotificationModel.class));
                        adapter.notifyDataSetChanged();

                        // אם אין התראות, נציג טקסט "ריק"
                        if (notificationList.isEmpty()) {
                            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            if (emptyView != null) emptyView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    // זה נשאר כמו בקוד המקורי שלך כדי שהתפריט יעבוד
    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }
}