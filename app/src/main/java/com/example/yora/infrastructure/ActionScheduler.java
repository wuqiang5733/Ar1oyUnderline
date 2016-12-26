package com.example.yora.infrastructure;

import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ActionScheduler {
    private final YoraApplication _application;
    private final Handler _handler;
    private final ArrayList<TimedCallback> _timedCallbacks;
    private final HashMap<Class, Runnable> _onResumeActions;
    private boolean _isPaused;


    public ActionScheduler(YoraApplication application) {
        _application = application;
        _handler = new Handler();
        _timedCallbacks = new ArrayList<>();
        _onResumeActions = new HashMap<>();
    }

    public void onPause() {
        _isPaused = true;
    }

    public void onResume() {
        _isPaused = false;

        for (TimedCallback callback : _timedCallbacks) {
            callback.schedule();
        }

        for (Runnable runnable : _onResumeActions.values()) {
            runnable.run();
        }
        _onResumeActions.clear();
    }

    public void invokeOnResume(Class cls, Runnable runnable) {
        if (!_isPaused) {
            runnable.run();
            return;
        }
        // Only the last runnable of a class should be stored and previous runnables should be
        // discarded. Using a HashMap helps us to achieve this behavior.
        _onResumeActions.put(cls, runnable);
    }

    public void postDelayed(Runnable runnable, long milliseconds){
        _handler.postDelayed(runnable, milliseconds);
    }

    public void invokeEveryMilliseconds(Runnable runnable, long milliseconds) {
        invokeEveryMilliseconds(runnable, milliseconds, true);
    }

    public void invokeEveryMilliseconds(Runnable runnable, long milliseconds, boolean runImmediately) {
        TimedCallback callback = new TimedCallback(runnable, milliseconds);
        _timedCallbacks.add(callback);
        if (runImmediately) {
            callback.run();
        } else {
            postDelayed(callback, milliseconds);
        }
    }

    public void postEveryMilliseconds(Object request, long milliseconds) {
        postEveryMilliseconds(request, milliseconds, true);

    }

    public void postEveryMilliseconds(final Object request, long milliseconds, boolean postImmediately) {
        invokeEveryMilliseconds(new Runnable() {
            @Override
            public void run() {
                _application.getBus().post(request);
            }
        }, milliseconds, postImmediately);
    }

    private class TimedCallback implements Runnable {
        private final Runnable _runnable;
        private final long _delay;

        public TimedCallback(Runnable runnable, long delay) {
            _runnable = runnable;
            _delay = delay;
        }

        @Override
        public void run() {
            if (_isPaused)
                return;
            _runnable.run();
            schedule();
        }

        public void schedule(){
            _handler.postDelayed(this, _delay);
        }
    }
}
