package com.example.jamming.view;

import android.os.Bundle;
import com.example.jamming.R;
import com.example.jamming.view.navigation.UserMenuHandler;

public class UpdateUserProfileActivity extends BaseActivity {

    private UserMenuHandler menuHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupBase(
                R.menu.user_menu,
                R.layout.activity_update_user_profile
        );

        setTitleText("Update Profile");
        menuHandler = new UserMenuHandler(this);
    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }
}
