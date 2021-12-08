package com.fntj.app.util;

import android.os.Handler;

public class RecognizeWaitTimer {

    private Handler handler = new Handler();
    private Runnable task;

    private static RecognizeWaitTimer instance;

    public static RecognizeWaitTimer getInstance() {
        if (instance == null) {
            instance = new RecognizeWaitTimer();
        }
        return instance;
    }

    public void startWait(int seconds, final Runnable onTimeout) {
        if (task != null) {
            handler.removeCallbacks(task);
        }

        task = () -> {
            onTimeout.run();
            task = null;
        };

        handler.postDelayed(task, seconds * 1000);
    }

    public void cancel() {
        if (task != null) {
            handler.removeCallbacks(task);
        }
    }
}
