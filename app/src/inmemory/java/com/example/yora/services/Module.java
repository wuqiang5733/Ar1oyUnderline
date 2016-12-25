package com.example.yora.services;

import com.example.yora.infrastructure.YoraApplication;

public class Module {
    public static void register(YoraApplication application) {
        // It won't be garbage collected because Bus holds a reference to it
        new InMemoryAccountService(application);
    }
}