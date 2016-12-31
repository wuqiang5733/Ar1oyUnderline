package com.example.yora.services;

import com.example.yora.infrastructure.YoraApplication;
import com.squareup.otto.Bus;

public class BaseLiveService {
    protected final Bus bus;
    protected final YoraWebService api;
    protected final YoraApplication application;


    protected BaseLiveService(YoraApplication application, YoraWebService api) {
        this.application = application;
        this.api = api;
        bus = application.getBus();
        bus.register(this);
    }

}
