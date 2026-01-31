package com.example.jamming.view;

import android.os.Bundle;
import com.example.jamming.R;
import com.example.jamming.navigation.UserMenuHandler;

public class UserProfileActivity extends BaseActivity {

    private UserMenuHandler menuHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupBase(
                R.menu.user_menu,
                R.layout.activity_user_profile
        );

        setTitleText("My Profile");
        menuHandler = new UserMenuHandler(this);
    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }
}
