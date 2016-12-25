package com.example.yora.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.yora.R;
import com.example.yora.services.Account;
import com.squareup.otto.Subscribe;

public class LoginFragment extends BaseFragment implements View.OnClickListener {
    private Button _loginButton;
    private EditText _userNameText;
    private EditText _passwordText;
    private View _progressBar;
    private String _defaultLoginButtonText;
    private Callbacks _callbacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        _loginButton = (Button) view.findViewById(R.id.fragment_login_loginButton);
        _loginButton.setOnClickListener(this);
        _defaultLoginButtonText = _loginButton.getText().toString();
        _userNameText = (EditText) view.findViewById(R.id.fragment_login_userName);
        _passwordText = (EditText) view.findViewById(R.id.fragment_login_password);
        _progressBar =  view.findViewById(R.id.activity_login_progressBar);
        _progressBar.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view == _loginButton) {
            _progressBar.setVisibility(View.VISIBLE);
            _loginButton.setText("");
            _loginButton.setEnabled(false);
            _userNameText.setEnabled(false);
            _passwordText.setEnabled(false);

            bus.post(new Account.LoginWithUserNameRequest(
                    _userNameText.getText().toString(),
                    _passwordText.getText().toString()));

        }
    }

    @Subscribe
    public void onLoginWithUserName(Account.LoginWithUserNameResponse response) {
        _progressBar.setVisibility(View.GONE);
        _loginButton.setText(_defaultLoginButtonText);
        _loginButton.setEnabled(true);
        _userNameText.setEnabled(true);
        _passwordText.setEnabled(true);

        response.showErrorToast(getActivity());
        _userNameText.setError(response.getPropertyError("userName"));
        _passwordText.setError(response.getPropertyError("password"));

        if (response.didSucceed())
            _callbacks.onLoggedIn();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _callbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _callbacks = null;
    }

    // Observer Pattern
    public interface Callbacks {
        void onLoggedIn();
    }
}
