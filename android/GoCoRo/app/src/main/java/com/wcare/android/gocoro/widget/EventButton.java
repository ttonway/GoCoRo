package com.wcare.android.gocoro.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wcare.android.gocoro.R;

/**
 * Created by ttonway on 2017/1/5.
 */

public class EventButton extends FrameLayout {

    TextView mNameTextView;
    TextView mStatusTextView;

    public EventButton(Context context) {
        super(context);
        initViews();
    }

    public EventButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initViews();
    }

    void initViews() {

        View view = inflate(getContext(), R.layout.event_button, this);

        mNameTextView = (TextView) view.findViewById(R.id.text_event_name);
        mStatusTextView = (TextView) view.findViewById(R.id.text_event_status);
    }

    public final void setEventName(CharSequence text) {
        mNameTextView.setText(text);
    }

    public final void setEventStatus(CharSequence text) {
        mStatusTextView.setText(text);
    }
}
