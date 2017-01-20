package com.wcare.android.gocoro.core;

import android.content.Context;
import android.util.Log;

/**
 * Created by ttonway on 2017/1/2.
 */

public abstract class GoCoRoDriver {
    public static final int STATE_CLOSE = 0;
    public static final int STATE_OPENING = 1;
    public static final int STATE_OPEN = 2;
    public static final int STATE_CLOSING = 3;

    public static final int ERROR_WRONG_DEVICE = 1;
    public static final int ERROR_TIMEOUT = 2;
    public static final int ERROR_CONNECTION_FAIL = 3;

    protected Context mContext;
    protected int mState;
    protected DriverCallback mCallback;

    public GoCoRoDriver(Context context, DriverCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    public synchronized void setState(int state) {
        Log.d(this.getClass().getSimpleName(), "setState() " + mState + " -> " + state);
        this.mState = state;

        this.mCallback.onStateChanged(state);
    }

    public synchronized int getState() {
        return mState;
    }

    abstract public String isDriverSupported();

    abstract public boolean openDevice(String device);

    abstract public boolean closeDevice();

    abstract public int WriteData(byte[] bytes, int length);
}
