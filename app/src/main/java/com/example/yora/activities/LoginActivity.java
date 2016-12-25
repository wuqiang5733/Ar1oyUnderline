package com.example.yora.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.yora.R;
import com.example.yora.fragments.LoginFragment;

public class LoginActivity extends BaseActivity implements View.OnClickListener, LoginFragment.Callbacks {
    private static final int REQUEST_NARROW_LOGIN = 1;
    private static final int REQUEST_REGISTER = 2;
    private static final int REQUEST_EXTERNAL_LOGIN = 3;

    private View _loginButton;
    private View _registerButton;
    private View _facebookLoginButton;
    private View _googleLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _loginButton = findViewById(R.id.activity_login_login);
        _registerButton = findViewById(R.id.activity_login_register);
        _facebookLoginButton = findViewById(R.id.activity_login_facebook);
        _googleLoginButton = findViewById(R.id.activity_login_google);
        if (_loginButton != null) {
            _loginButton.setOnClickListener(this);
        }
        _registerButton.setOnClickListener(this);
        _facebookLoginButton.setOnClickListener(this);
        _googleLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == _loginButton) {
            startActivityForResult(new Intent(this, LoginNarrowActivity.class), REQUEST_NARROW_LOGIN);
        } else if (view == _registerButton) {
            startActivityForResult(new Intent(this, RegisterActivity.class), REQUEST_REGISTER);
        } else if (view == _facebookLoginButton) {
            doExternalLogin("Facebook");
        } else if (view == _googleLoginButton) {
            doExternalLogin("Google");
        }
    }

    private void doExternalLogin(String externalService) {
        Intent intent = new Intent(this, ExternalLoginActivity.class);
        intent.putExtra(ExternalLoginActivity.EXTRA_EXTERNAL_SERVICE, externalService);
        startActivityForResult(intent, REQUEST_EXTERNAL_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == REQUEST_NARROW_LOGIN ||
            requestCode == REQUEST_REGISTER ||
            requestCode == REQUEST_EXTERNAL_LOGIN)
            finishLogin();
    }

    private void finishLogin() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onLoggedIn() {
        finishLogin();
    }
}
