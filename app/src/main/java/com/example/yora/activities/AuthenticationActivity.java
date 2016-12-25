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
        // 下面的 _auth 是 Refactor 自动生成的
        _auth = application.getAuth();
        //如果没有Token ，就去手动登陆
        if (!_auth.hasAuthToken()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bus.post(new Account.LoginWithLocalTokenRequest(_auth.getAuthToken()));
    }

    @Subscribe
    public void onLoginWithLocalToken(Account.LoginWithLocalTokenResponse response) {
        //如果由于某种原因 Token 找不到了，那就去手动登陆。
        if (!response.didSucceed()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            _auth.setAuthToken(null);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else {
            // 自动登陆成功之后，可以返回到之前的界面，查看 BaseAuthenticatedActivity
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
