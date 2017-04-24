package com.wcare.android.gocoro.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by ttonway on 2017/4/24.
 */
public class LogoView extends AppCompatTextView {

    public LogoView(Context context) {
        super(context);

        initialize();
    }

    public LogoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initialize();
    }

    public LogoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize();
    }

    void initialize() {
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Bauhaus93.ttf"));
    }
}
