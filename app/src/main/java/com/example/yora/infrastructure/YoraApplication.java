package com.example.yora.infrastructure;

import android.app.Application;

import com.example.yora.services.Module;
import com.squareup.otto.Bus;

public class YoraApplication extends Application {
    private Auth _auth;
    private Bus _bus;


    public YoraApplication() {
        // It's also correct; but because object has not been completely initiallized at this point,
        // it may cause problem in Auth.
        // _auth = new Auth(this);

        _bus = new Bus();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _auth = new Auth(this);
        Module.register(this);
    }

    public Auth getAuth() {
        return _auth;
    }

    public Bus getBus() {
        return _bus;
    }
}
