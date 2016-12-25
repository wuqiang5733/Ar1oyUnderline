package com.example.yora.activities;

import android.content.Intent;
import android.os.Bundle;

public abstract class BaseAuthenticatedActivity extends BaseActivity {

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!application.getAuth().getUser().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // This way if activity is replaced with LoginActivity, its onCreate method will
        // not executed, preventing useless extra proccessing.
        onYoraCreate(savedInstanceState);
    }

    protected abstract void onYoraCreate(Bundle savedInstanceState);

}
