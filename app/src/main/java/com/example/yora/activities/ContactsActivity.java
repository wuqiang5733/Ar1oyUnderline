package com.example.yora.activities;

import android.os.Bundle;

import com.example.yora.R;
import com.example.yora.views.MainNavDrawer;

public class ContactsActivity extends BaseAuthenticatedActivity {
    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_contacts);
        getSupportActionBar().setTitle("Contacts");
        setNavDrawer(new MainNavDrawer(this));
    }
}
