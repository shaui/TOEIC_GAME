package com.example.toeic_game.widget;


import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MyCountDownTimer {

    private long millisInFuture, millisLeft, countDownInterval;
    private long startTime;
    private boolean isStart = false, isCancelled = false, isPaused = false;

    private Handler handler = new Handler(Looper.getMainLooper());
    private ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture future;
    private Runnable TimerRunnable = () -> {
        if(isCancelled) {
            future.cancel(true);
            handler.post(this::onCancel);
        }
        else if(isPaused)
            handler.post(this::onPause);
        else {
            millisLeft -= countDownInterval;
            handler.post(() -> {
                if(millisLeft <= 0) {
                    Log.i(this.getClass().getName(), "passTime" + (SystemClock.elapsedRealtime() - startTime));
                    onFinish();
                    future.cancel(true);
                    isStart = false;
                }
                else {
                    onTick(millisLeft);
                }
            });
        }
    };

    public MyCountDownTimer(long millisInFuture, long countDownInterval) {
        this.millisInFuture = millisInFuture;
        millisLeft = millisInFuture;
        this.countDownInterval = countDownInterval;
    }

    public MyCountDownTimer(long millisInFuture) {
        this.millisInFuture = millisInFuture;
        millisLeft = millisInFuture;
        this.countDownInterval = millisInFuture;
    }

    public final synchronized void start() {
        if(!isStart) {
            isStart = true;
            isCancelled = false;
            isPaused = false;
            millisLeft = millisInFuture + countDownInterval;
            future = scheduled.scheduleWithFixedDelay(TimerRunnable, 0, countDownInterval, TimeUnit.MILLISECONDS);
            startTime = SystemClock.elapsedRealtime();
        }
    }

    public final synchronized void cancel() {
        isStart = false;
        isCancelled = true;
    }

    public final synchronized void pause() {
        isPaused = true;
    }

    public final synchronized void resume() {
        isPaused = false;
    }

    public final synchronized void setMillisInFuture(long millisInFuture) {
        this.millisInFuture = millisInFuture;
    }

    public void onTick(long millisLeft) {}
    public void onFinish() {}
    public void onCancel() {}
    public void onPause() {}

}
