package com.example.yora.services;

import android.util.Log;

import com.example.yora.infrastructure.YoraApplication;

public class Module {
    public static void register(YoraApplication application) {
        Log.e("Module", "LIVE REGISTER METHOD CALLED");
    }
}