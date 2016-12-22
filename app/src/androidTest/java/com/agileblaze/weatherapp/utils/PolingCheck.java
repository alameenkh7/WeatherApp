package com.agileblaze.weatherapp.utils;

import junit.framework.Assert;

import java.util.concurrent.Callable;

/**
 * Created by manager on 17/9/16.
 */
public abstract class PolingCheck {
    private static final long TIME_SLICE = 50;
    private long mTimeout = 3000;

    public PolingCheck() {
    }

    public PolingCheck(long timeout) {
        mTimeout = timeout;
    }

    protected abstract boolean check();


    public void run() {
        if (check()) {
            return;
        }

        long timeout = mTimeout;
        while (timeout > 0) {
            try {
                Thread.sleep(TIME_SLICE);
            } catch (InterruptedException e) {
                Assert.fail("unexpected InterruptedException");
            }

            if (check()) {
                return;
            }

            timeout -= TIME_SLICE;
        }

        Assert.fail("unexpected timeout");
    }

    public static void check(CharSequence message, long timeout, Callable<Boolean> condition)
            throws Exception {
        while (timeout > 0) {
            if (condition.call()) {
                return;
            }

            Thread.sleep(TIME_SLICE);
            timeout -= TIME_SLICE;
        }

        Assert.fail(message.toString());
    }

}
