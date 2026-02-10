package com.example.jamming.view;

import android.os.Bundle;
import com.example.jamming.R;
import com.example.jamming.navigation.OwnerMenuHandler;

public class UpdateOwnerProfileActivity extends BaseActivity {

    private OwnerMenuHandler menuHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupBase(
                R.menu.owner_menu,
                R.layout.activity_update_owner_profile
        );

        setTitleText("Edit Profile");
        menuHandler = new OwnerMenuHandler(this);
    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }
}
