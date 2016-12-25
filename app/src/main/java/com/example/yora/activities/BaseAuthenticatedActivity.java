package com.example.yora.activities;

import android.content.Intent;
import android.os.Bundle;

public abstract class BaseAuthenticatedActivity extends BaseActivity {

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!application.getAuth().getUser().isLoggedIn()) {
            //下面是自动登陆的代码
            if (application.getAuth().hasAuthToken()) {
                Intent intent = new Intent(this, AuthenticationActivity.class);
                //下面是 不管在那个界面下（Activity),自动登陆之后，都可以返回
                intent.putExtra(AuthenticationActivity.EXTRA_RETURN_TO_ACTIVITY, getClass().getName());
                startActivity(intent);
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }

            finish();
            return;
        }

        // This way if activity is replaced with LoginActivity, its onCreate method will
        // not executed, preventing useless extra proccessing.
        onYoraCreate(savedInstanceState);
    }

    protected abstract void onYoraCreate(Bundle savedInstanceState);

}
