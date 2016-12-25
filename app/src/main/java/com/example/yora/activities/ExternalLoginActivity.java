package com.example.yora.activities;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.example.yora.R;

public class ExternalLoginActivity extends BaseActivity implements View.OnClickListener {
    public static final String EXTRA_EXTERNAL_SERVICE = "EXTRA_EXTERNAL_SERVICE";

    private Button _testButton;
    private WebView _webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_external_login);

        _testButton = (Button) findViewById(R.id.activity_external_login_testButton);
        _webView = (WebView) findViewById(R.id.activity_external_login_webView);

        _testButton.setOnClickListener(this);
        _testButton.setText("Log In With: " + getIntent().getStringExtra(EXTRA_EXTERNAL_SERVICE));
    }

    @Override
    public void onClick(View view) {
        if (view == _testButton) {
            application.getAuth().getUser().setLoggedIn(true);
            setResult(RESULT_OK);
            finish();
        }
    }
}
