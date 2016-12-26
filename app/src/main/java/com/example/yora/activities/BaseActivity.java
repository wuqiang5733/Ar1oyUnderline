package com.example.yora.activities;


import android.animation.Animator;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;

import com.example.yora.infrastructure.ActionScheduler;
import com.example.yora.infrastructure.YoraApplication;
import com.example.yora.R;
import com.example.yora.views.NavDrawer;
import com.squareup.otto.Bus;

public abstract class BaseActivity extends AppCompatActivity {
    private boolean _isRegisteredWithBus;

    protected YoraApplication application;
    protected Toolbar toolbar;
    protected NavDrawer navDrawer;
    protected boolean isTablet;
    protected Bus bus;
    protected ActionScheduler scheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (YoraApplication) getApplication();
        bus = application.getBus();
        scheduler = new ActionScheduler(application);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        isTablet = (metrics.widthPixels / metrics.density) >= 600;

        bus.register(this);
        _isRegisteredWithBus = true;
    }

    public ActionScheduler getScheduler() {
        return scheduler;
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduler.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scheduler.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (_isRegisteredWithBus) {
            bus.unregister(this);
            _isRegisteredWithBus = false;
        }
        if (navDrawer != null)
            navDrawer.destroy();
    }

    @Override
    public void finish() {
        super.finish();

        // onDestroy might not be called after the activity is finished, so this ensures
        // immediate unregistering from the bus and prevents unwanted issues.
        // It seems that "finish" is also called internally by the system in most scenarios
        if (_isRegisteredWithBus) {
            bus.unregister(this);
            _isRegisteredWithBus = false;
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar);
        if (toolbar != null)
            setSupportActionBar(toolbar);
    }

    public void fadeOut(final FadeOutListener listener){
        View rootView = findViewById(android.R.id.content);
        rootView.animate()
                .alpha(0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        listener.onFadeOutEnd();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .setDuration(300)
                .start();
    }

    protected void setNavDrawer(NavDrawer navDrawer) {
        this.navDrawer = navDrawer;
        this.navDrawer.create();

        overridePendingTransition(0, 0);
        View rootView = findViewById(android.R.id.content);
        rootView.setAlpha(0);
        rootView.animate()
                .alpha(1)
                .setDuration(450)
                .start();
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public YoraApplication getYoraApplication() {
        return application;
    }

    public interface FadeOutListener {
        void onFadeOutEnd();
    }
}
