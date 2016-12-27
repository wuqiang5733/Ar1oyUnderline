package com.example.yora.activities;

import android.os.Bundle;

public class MessageActivity extends BaseAuthenticatedActivity {
    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    public static final int RESULT_MESSAGE_DELETED = 100;
    public static final String RESULT_EXTRA_MESSAGE_ID = "RESULT_EXTRA_MESSAGE_ID";

    @Override
    protected void onYoraCreate(Bundle savedInstanceState) {
    }
}
