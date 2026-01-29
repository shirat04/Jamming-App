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

public class UserMenuHandler {

    private final AppCompatActivity activity;
    private final AuthRepository authRepository = new AuthRepository();

    public UserMenuHandler(AppCompatActivity activity) {
        this.activity = activity;
    }

    public boolean handle(int itemId) {
        if (itemId == R.id.menu_user_dashboard) {
            if (!(activity instanceof ExploreEventsActivity)) {
                activity.startActivity(
                        new Intent(activity, ExploreEventsActivity.class)
                );
            }
            return true;
        }
        if (itemId == R.id.menu_profile) {
            if (!(activity instanceof UserProfileActivity)) {
                activity.startActivity(
                        new Intent(activity, UserProfileActivity.class)
                );
            }
            return true;
        }
        if (itemId == R.id.menu_notifications) {
            if (!(activity instanceof NotificationsUserActivity)) {
                activity.startActivity(
                        new Intent(activity, NotificationsUserActivity.class)
                );
            }
            return true;
        }

        if (itemId == R.id.menu_my_events) {
            if (!(activity instanceof MyEventUserActivity)) {
                activity.startActivity(
                        new Intent(activity, MyEventUserActivity.class)
                );
            }
            return true;
        }

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
