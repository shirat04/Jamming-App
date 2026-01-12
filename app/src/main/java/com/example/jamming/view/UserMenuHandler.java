package com.example.jamming.view;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.viewmodel.ExploreEventsViewModel;

public class UserMenuHandler {

    private final AppCompatActivity activity;
    private final ExploreEventsViewModel viewModel;

    public UserMenuHandler(
            AppCompatActivity activity,
            ExploreEventsViewModel viewModel
    ) {
        this.activity = activity;
        this.viewModel = viewModel;
    }

    public boolean handle(int id) {

        if (id == R.id.menu_user_dashboard) {
            if (!(activity instanceof ExploreEventsActivity)) {
                activity.startActivity(
                        new Intent(activity, ExploreEventsActivity.class)
                );
            }
            return true;
        }
        if (id == R.id.menu_preferences) {
            if (!(activity instanceof ProfilePreferencesActivity)) {
                activity.startActivity(
                        new Intent(activity, ProfilePreferencesActivity.class)
                );
            }
            return true;
        }
        if (id == R.id.menu_notifications) {
            if (!(activity instanceof NotificationsUserActivity)) {
                activity.startActivity(
                        new Intent(activity, NotificationsUserActivity.class)
                );
            }
            return true;
        }
        if (id == R.id.menu_logout) {
            viewModel.logout();

            Intent i = new Intent(activity, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(i);
            return true;
        }

        return false;
    }
}
