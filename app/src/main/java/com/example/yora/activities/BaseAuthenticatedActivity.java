package com.example.yora.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public abstract class BaseAuthenticatedActivity extends BaseActivity {

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        // 强制必须登陆，如果没有登陆，就转到 登陆界面
        super.onCreate(savedInstanceState);
        Log.e("BaseAuthenActivity","onCreate");
        if (!application.getAuth().getUser().isLoggedIn()) {
            //下面是自动登陆的部分
            if (application.getAuth().hasAuthToken()) {
                Intent intent = new Intent(this, AuthenticationActivity.class);
                // 下面的 getClass().getName() 是为了在自动登陆之后，回到之前的 Activity
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
        onYoraCreate(savedInstanceState); // 经典 ！
    }

    protected abstract void onYoraCreate(Bundle savedInstanceState);

}
