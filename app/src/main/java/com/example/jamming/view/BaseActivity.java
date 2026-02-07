package com.example.jamming.view;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.jamming.R;
import com.google.android.material.navigation.NavigationView;

public abstract class BaseActivity extends AppCompatActivity {
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected TextView titleText;
    protected ImageButton btnMenu;
    protected LinearLayout rightActions;
    protected FrameLayout contentFrame;




    protected void setupBase(int menuRes, int contentLayout) {
        setContentView(R.layout.activity_base_menu);
        initViews();


        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );


        getLayoutInflater().inflate(contentLayout, contentFrame, true);

        navigationView.getMenu().clear();
        navigationView.inflateMenu(menuRes);

        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = onMenuItemSelected(item.getItemId());
            drawerLayout.closeDrawers();
            return handled;
        });

}
    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        titleText = findViewById(R.id.titleText);
        btnMenu = findViewById(R.id.btnMenu);
        rightActions = findViewById(R.id.rightActions);
        contentFrame = findViewById(R.id.contentFrame);
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