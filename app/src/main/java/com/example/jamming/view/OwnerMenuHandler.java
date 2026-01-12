package com.example.jamming.view;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.viewmodel.OwnerViewModel;

public class OwnerMenuHandler {
    private final AppCompatActivity activity;
    private final OwnerViewModel viewModel;

    public OwnerMenuHandler(
            AppCompatActivity activity,
            OwnerViewModel viewModel
    ) {
        this.activity = activity;
        this.viewModel = viewModel;
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
