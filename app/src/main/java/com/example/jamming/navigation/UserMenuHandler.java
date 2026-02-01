package com.example.jamming.navigation;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.view.ExploreEventsActivity;
import com.example.jamming.view.LoginActivity;
import com.example.jamming.view.MyEventUserActivity;
import com.example.jamming.view.NotificationsUserActivity;
import com.example.jamming.view.UserProfileActivity;

/**
 * Handles navigation logic for the user menu.
 * Responsible for reacting to menu item selections
 * and navigating to the appropriate screens.
 */
public class UserMenuHandler {

    private final AppCompatActivity activity;
    private final AuthRepository authRepository = new AuthRepository();

    /**
     * Constructs a menu handler bound to the given activity.
     *
     * @param activity Current activity hosting the menu
     */
    public UserMenuHandler(AppCompatActivity activity) {
        this.activity = activity;
    }

    /**
     * Handles menu item selection.
     * Starts the relevant activity based on the selected menu item.
     *
     * @param itemId ID of the selected menu item
     * @return True if the item was handled, false otherwise
     */
    public boolean handle(int itemId) {

        // Navigate to the main dashboard (Explore Events)
        if (itemId == R.id.menu_user_dashboard) {
            if (!(activity instanceof ExploreEventsActivity)) {
                activity.startActivity(
                        new Intent(activity, ExploreEventsActivity.class)
                );
            }
            return true;
        }

        // Navigate to the user profile screen
        if (itemId == R.id.menu_profile) {
            if (!(activity instanceof UserProfileActivity)) {
                activity.startActivity(
                        new Intent(activity, UserProfileActivity.class)
                );
            }
            return true;
        }

        // Navigate to the notifications screen
        if (itemId == R.id.menu_notifications) {
            if (!(activity instanceof NotificationsUserActivity)) {
                activity.startActivity(
                        new Intent(activity, NotificationsUserActivity.class)
                );
            }
            return true;
        }

        // Navigate to the user's registered events screen
        if (itemId == R.id.menu_my_events) {
            if (!(activity instanceof MyEventUserActivity)) {
                activity.startActivity(
                        new Intent(activity, MyEventUserActivity.class)
                );
            }
            return true;
        }

        // Handle user logout and redirect to login screen
        if (itemId == R.id.menu_logout) {
            authRepository.logout();
            activity.startActivity(
                    new Intent(activity, LoginActivity.class)
            );
            activity.finish();
            return true;
        }

        return false;
    }
}
