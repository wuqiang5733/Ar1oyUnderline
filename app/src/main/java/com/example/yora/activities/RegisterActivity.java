package com.example.yora.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.yora.R;
import com.example.yora.services.Account;
import com.squareup.otto.Subscribe;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    private Button _registerButton;
    private EditText _userNameText;
    private EditText _emailText;
    private EditText _passwordText;
    private View _progressBar;
    private String _defaultRegisterButtonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        _registerButton = (Button) findViewById(R.id.activity_register_registerButton);
        _userNameText = (EditText) findViewById(R.id.activity_register_userName);
        _emailText = (EditText) findViewById(R.id.activity_register_email);
        _passwordText = (EditText) findViewById(R.id.activity_register_password);
        _progressBar = findViewById(R.id.activity_register_progressBar);
        _defaultRegisterButtonText = _registerButton.getText().toString();

        _registerButton.setOnClickListener(this);
        _progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (view == _registerButton) {
            _progressBar.setVisibility(View.VISIBLE);
            _registerButton.setText("");
            _registerButton.setEnabled(false);
            _userNameText.setEnabled(false);
            _passwordText.setEnabled(false);
            _emailText.setEnabled(false);

            bus.post(new Account.RegisterRequest(
                    _userNameText.getText().toString(),
                    _passwordText.getText().toString(),
                    _emailText.getText().toString()));
        }
    }

    @Subscribe
    public void onRegisterResponse(Account.RegisterResponse response) {
        onUserResponse(response);
    }

    @Subscribe
    public void onExternalRegisterResponse(Account.RegisterWithExternalTokenResponse response) {
        onUserResponse(response);
    }

    private void onUserResponse(Account.UserResponse response) {
        _progressBar.setVisibility(View.GONE);
        _registerButton.setText(_defaultRegisterButtonText);
        _registerButton.setEnabled(true);
        _userNameText.setEnabled(true);
        _passwordText.setEnabled(true);
        _emailText.setEnabled(true);

        response.showErrorToast(this);
        _userNameText.setError(response.getPropertyError("userName"));
        _passwordText.setError(response.getPropertyError("password"));
        _emailText.setError(response.getPropertyError("email"));

        if (response.didSucceed()) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
