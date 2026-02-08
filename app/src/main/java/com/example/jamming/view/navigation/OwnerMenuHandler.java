package com.example.jamming.view.navigation;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.view.CreateNewEventActivity;
import com.example.jamming.view.LoginActivity;
import com.example.jamming.view.NotificationsOwnerActivity;
import com.example.jamming.view.OwnerActivity;
import com.example.jamming.view.OwnerPastEventsActivity;
import com.example.jamming.view.OwnerProfileActivity;

/**
 * Handles navigation logic for the owner menu.
 * Responsible for routing menu selections to the
 * appropriate owner-related screens.
 */
public class OwnerMenuHandler {
    private final AppCompatActivity activity;
    private final AuthRepository authRepository = new AuthRepository();

    /**
     * Constructs a menu handler bound to the given activity.
     *
     * @param activity Current activity hosting the owner menu
     */
    public OwnerMenuHandler(AppCompatActivity activity) {
        this.activity = activity;
    }

    /**
     * Handles menu item selection.
     * Starts the relevant activity based on the selected menu item.
     *
     * @param id ID of the selected menu item
     * @return True if the item was handled, false otherwise
     */
    public boolean handle(int id) {

        // Navigate to owner dashboard
        if (id == R.id.menu_owner_dashboard) {
            if (!(activity instanceof OwnerActivity)){
            activity.startActivity(
                    new Intent(activity, OwnerActivity.class)
            );
            }
            return true;
        }

        // Navigate to owner profile screen
        if (id == R.id.menu_profile) {
            if (!(activity instanceof OwnerProfileActivity)) {
                activity.startActivity(
                        new Intent(activity, OwnerProfileActivity.class)
                );
                return true;
            }
        }

        // Navigate to owner profile screen
        if (id == R.id.menu_past_event) {
            if (!(activity instanceof OwnerPastEventsActivity)) {
                activity.startActivity(
                        new Intent(activity, OwnerPastEventsActivity.class)
                );
                return true;
            }
        }

        // Navigate to owner notifications screen
        if (id == R.id.menu_notifications) {
            if (!(activity instanceof NotificationsOwnerActivity)) {
                activity.startActivity(
                        new Intent(activity, NotificationsOwnerActivity.class)
                );
            }
            return true;
        }

        // Navigate to event creation screen
        if (id == R.id.create_new_event) {
            if (!(activity instanceof CreateNewEventActivity)) {
                activity.startActivity(
                        new Intent(activity, CreateNewEventActivity.class)
                );
            }
            return true;
        }

        // Handle logout and clear back stack
        if (id == R.id.menu_logout) {
            authRepository.logout();

            Intent i = new Intent(activity, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(i);
            return true;
        }

        return false;
    }
}
