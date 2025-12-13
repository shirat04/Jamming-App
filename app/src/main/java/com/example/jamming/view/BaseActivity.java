package com.example.jamming.view;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setupMenu(ImageButton btnMenu, String userType) {

        btnMenu.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, view, Gravity.START);

            if ("OWNER".equals(userType)) {
                popupMenu.getMenuInflater().inflate(R.menu.owner_menu, popupMenu.getMenu());
            } else {
                popupMenu.getMenuInflater().inflate(R.menu.user_menu, popupMenu.getMenu());
            }

            popupMenu.setOnMenuItemClickListener(item -> handleMenuClick(item, userType));
            popupMenu.show();
        });
    }

    private boolean handleMenuClick(MenuItem item, String userType) {
        int id = item.getItemId();

        if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }

        if ("USER".equals(userType)) {
            if (id == R.id.menu_preferences) {
                startActivity(new Intent(this, profilePreferencesActivity.class));
                return true;
            }
            if (id == R.id.menu_notifications) {
                startActivity(new Intent(this, NotificationsUserActivity.class));
                return true;
            }

            if ("OWNER".equals(userType)) {
                if (id == R.id.menu_owner_dashboard) {
                    startActivity(new Intent(this, OwnerActivity.class));
                    return true;
                }
                if (id == R.id.menu_my_event) {
                    startActivity(new Intent(this, CreateNewEvent.class));
                    return true;
                }
            }

            return false;
        }
        return false;
    }
}