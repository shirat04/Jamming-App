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
    protected ImageButton btnMenu;
    protected LinearLayout rightActions;




    protected void setupBase(int menuRes, int contentLayout) {
        setContentView(R.layout.activity_base);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        titleText = findViewById(R.id.titleText);
        btnMenu = findViewById(R.id.btnMenu);
        rightActions = findViewById(R.id.rightActions);

        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        FrameLayout contentFrame = findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(contentLayout, contentFrame, true);

        navigationView.getMenu().clear();
        navigationView.inflateMenu(menuRes);

        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = onMenuItemSelected(item.getItemId());
            drawerLayout.closeDrawers();
            return handled;
        });


}
    protected void setTitleText(String text) {
        if (titleText != null) {
            titleText.setText(text);
        }
    }
    protected boolean onMenuItemSelected(int itemId) {
        return false;
    }
    protected void hideRightActions() {
        if (rightActions != null) {
            rightActions.removeAllViews();
            rightActions.setVisibility(View.GONE);
        }
    }

    protected void showRightActions() {
        if (rightActions != null) {
            rightActions.setVisibility(View.VISIBLE);
        }
    }

}