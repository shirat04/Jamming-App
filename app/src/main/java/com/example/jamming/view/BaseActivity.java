package com.example.jamming.view;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.jamming.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected TextView titleText;
    protected LinearLayout rightActions;
    protected ImageButton btnMenu;


    protected void setupBase(String title, String userType, int contentLayout) {
        setContentView(R.layout.activity_base);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        titleText = findViewById(R.id.titleText);
        rightActions = findViewById(R.id.rightActions);
        btnMenu = findViewById(R.id.btnMenu);

        titleText.setText(title);

        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        FrameLayout contentFrame = findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(contentLayout, contentFrame, true);

        navigationView.inflateMenu(
                "OWNER".equals(userType) ? R.menu.owner_menu : R.menu.user_menu
        );

        navigationView.setNavigationItemSelectedListener(this::handleMenuClick);
    }

    protected void setTitleText(String text) {
        titleText.setText(text);
    }

    protected void showProfileImage(int drawableRes) {
        rightActions.removeAllViews();

        ImageButton profile = new ImageButton(this);
        profile.setImageResource(drawableRes);
        profile.setBackground(null);

        int size = (int) (36 * getResources().getDisplayMetrics().density);
        rightActions.addView(profile,
                new LinearLayout.LayoutParams(size, size));
    }

    protected void hideRightActions() {
        rightActions.removeAllViews();
    }

    protected void showMenuButton(boolean show) {
        btnMenu.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private boolean handleMenuClick(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }

        if (id == R.id.menu_preferences) {
            startActivity(new Intent(this, profilePreferencesActivity.class));
            return true;
        }

        if (id == R.id.menu_notifications) {
            startActivity(new Intent(this, NotificationsUserActivity.class));
            return true;
        }

        if (id == R.id.menu_owner_dashboard) {
            startActivity(new Intent(this, OwnerActivity.class));
            return true;
        }

        drawerLayout.closeDrawers();
        return true;
    }

}