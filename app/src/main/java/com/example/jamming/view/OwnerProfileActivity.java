package com.example.jamming.view;

import android.os.Bundle;
import com.example.jamming.R;
import com.example.jamming.navigation.OwnerMenuHandler;

public class OwnerProfileActivity extends BaseActivity {

    private OwnerMenuHandler menuHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupBase(
                R.menu.owner_menu,
                R.layout.activity_owner_profile
        );

        setTitleText("My Profile");
        menuHandler = new OwnerMenuHandler(this);
    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }
}
