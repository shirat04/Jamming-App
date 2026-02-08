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

/**
 * Base activity that provides a shared layout with a navigation drawer.
 * Child activities inflate their own content and handle menu actions.
 */
public abstract class BaseActivity extends AppCompatActivity {
    // Shared UI components defined in activity_base_menu.xml
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected TextView titleText;
    protected ImageButton btnMenu;
    protected LinearLayout rightActions;
    protected FrameLayout contentFrame;



    /**
     * Sets the base layout, inflates the screen-specific content,
     * and configures the navigation drawer menu.
     */
    protected void setupBase(int menuRes, int contentLayout) {
        setContentView(R.layout.activity_base_menu);
        initViews();

        // Open the navigation drawer when the menu button is clicked
        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        // Inflate the child activity layout into the content container
        getLayoutInflater().inflate(contentLayout, contentFrame, true);

        // Load the requested menu into the navigation view
        navigationView.getMenu().clear();
        navigationView.inflateMenu(menuRes);

        // Delegate menu item handling to the subclass
        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = onMenuItemSelected(item.getItemId());
            drawerLayout.closeDrawers();
            return handled;
        });
    }

    /**
     * Binds all shared views from the base layout.
     */
    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        titleText = findViewById(R.id.titleText);
        btnMenu = findViewById(R.id.btnMenu);
        rightActions = findViewById(R.id.rightActions);
        contentFrame = findViewById(R.id.contentFrame);
    }

    /**
     * Updates the title displayed in the toolbar area.
     */
    protected void setTitleText(String text) {
        if (titleText != null) {
            titleText.setText(text);
        }
    }

    /**
     * Called when a navigation menu item is selected.
     * Subclasses should override this method to handle their own actions.
     */
    protected boolean onMenuItemSelected(int itemId) {
        return false;
    }

    /**
     * Hides and clears the right-side action container.
     * Useful for screens that do not require additional action buttons.
     */
    protected void hideRightActions() {
        if (rightActions != null) {
            rightActions.removeAllViews();
            rightActions.setVisibility(View.GONE);
        }
    }

}