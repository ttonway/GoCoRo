package com.wcare.android.gocoro.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by ttonway on 2017/2/23.
 */

public class MyImageView extends ImageView {

    private final AspectRatioMeasure.Spec mMeasureSpec = new AspectRatioMeasure.Spec();

    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureSpec.width = widthMeasureSpec;
        mMeasureSpec.height = heightMeasureSpec;
        AspectRatioMeasure.updateMeasureSpec(
                mMeasureSpec,
                4.f / 3.f,
                getLayoutParams(),
                getPaddingLeft() + getPaddingRight(),
                getPaddingTop() + getPaddingBottom());
        super.onMeasure(mMeasureSpec.width, mMeasureSpec.height);

    }

}
