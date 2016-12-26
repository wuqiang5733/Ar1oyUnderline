package com.example.yora.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.yora.R;
import com.example.yora.infrastructure.Auth;
import com.example.yora.services.Account;
import com.squareup.otto.Subscribe;

public class AuthenticationActivity extends BaseActivity {
    public static final String EXTRA_RETURN_TO_ACTIVITY = "EXTRA_RETURN_TO_ACTIVITY";

    private Auth _auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        _auth = application.getAuth();
        if (!_auth.hasAuthToken()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bus.post(new Account.LoginWithLocalTokenRequest(_auth.getAuthToken()));
    }

    @Subscribe
    public void onLoginWithLocalToken(Account.LoginWithLocalTokenResponse response) {
        if (!response.didSucceed()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            _auth.setAuthToken(null);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else {
            String returnTo = getIntent().getStringExtra(EXTRA_RETURN_TO_ACTIVITY);
            Intent intent = new Intent(this, MainActivity.class);
            if (returnTo != null) {
                try {
                    intent = new Intent(this, Class.forName(returnTo));
                }
                catch (Exception ignored) {
                }
            }
            startActivity(intent);
            finish();
        }
    }
}
