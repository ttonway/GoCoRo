package com.wcare.android.gocoro.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by ttonway on 2017/3/11.
 */
public class TimeTextView extends TextView {

    private int mValue;

    public TimeTextView(Context context) {
        super(context);
    }

    public TimeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;

        setText(String.format("%02d", value));
    }
}
