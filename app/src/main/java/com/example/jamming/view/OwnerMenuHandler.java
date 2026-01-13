package com.example.jamming.view;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.repository.AuthRepository;

public class OwnerMenuHandler {
    private final AppCompatActivity activity;
    private final AuthRepository authRepository = new AuthRepository();

    public OwnerMenuHandler(AppCompatActivity activity) {
        this.activity = activity;
    }

    public boolean handle(int id) {

        if (id == R.id.menu_owner_dashboard) {
            if (!(activity instanceof OwnerActivity)){
            activity.startActivity(
                    new Intent(activity, OwnerActivity.class)
            );
            }
            return true;
        }

        if (id == R.id.menu_profile) {
            if (!(activity instanceof UpdateOwnerDetailsActivity)) {
                activity.startActivity(
                        new Intent(activity, UpdateOwnerDetailsActivity.class)
                );
                return true;
            }
        }
        if (id == R.id.menu_notifications) {
            if (!(activity instanceof NotificationsOwnerActivity)) {
                activity.startActivity(
                        new Intent(activity, NotificationsOwnerActivity.class)
                );
            }
            return true;
        }
        if (id == R.id.create_new_event) {
            if (!(activity instanceof CreateNewEventActivity)) {
                activity.startActivity(
                        new Intent(activity, CreateNewEventActivity.class)
                );
            }
            return true;
        }
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
