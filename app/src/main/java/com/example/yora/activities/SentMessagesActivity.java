package com.example.yora.activities;

import android.os.Bundle;

import com.example.yora.R;
import com.example.yora.views.MainNavDrawer;

public class SentMessagesActivity extends BaseAuthenticatedActivity {
    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sent_message);
        getSupportActionBar().setTitle("Sent Messages");
        setNavDrawer(new MainNavDrawer(this));
    }
}
