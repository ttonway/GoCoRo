package com.wcare.android.gocoro.widget;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;

import io.techery.progresshint.ProgressHintDelegate;
import io.techery.progresshint.addition.HorizontalProgressHintDelegate;

/**
 * Created by ttonway on 2017/1/6.
 */

public class SeekBar extends android.widget.SeekBar implements
        ProgressHintDelegate.SeekBarHintDelegateHolder {

    private ProgressHintDelegate hintDelegate;

    public SeekBar(Context context) {
        super(context);
        init(null, 0);
    }

    public SeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        if (!isInEditMode()) {
            hintDelegate = new HorizontalProgressHintDelegate(this, attrs, defStyle) {
                @Override
                protected Point getFollowHintOffset() {
                    int xOffset = getHorizontalOffset(mSeekBar.getProgress());
                    int yOffset = getVerticalOffset();
                    return new Point(xOffset, yOffset);
                }

                private int getHorizontalOffset(int progress) {
                    return getFollowPosition(progress) - mPopupView.getMeasuredWidth() / 2 + mSeekBar.getThumb().getIntrinsicWidth() / 2;
                }

                private int getVerticalOffset() {
                    return -(mSeekBar.getHeight() + mPopupView.getMeasuredHeight() + mPopupOffset);
                }

                @Override
                protected int getFollowPosition(int progress) {
                    return (int) (progress * (mSeekBar.getWidth()
                            - mSeekBar.getPaddingLeft()
                            - mSeekBar.getPaddingRight()
                            -  mSeekBar.getThumb().getIntrinsicWidth()
                            + mSeekBar.getThumbOffset() * 2) / (float) mSeekBar.getMax() + 0.5f) + mSeekBar.getPaddingLeft() - mSeekBar.getThumbOffset();
                }
            };
        }
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        super.setOnSeekBarChangeListener(hintDelegate.setOnSeekBarChangeListener(l));
    }

    @Override
    public ProgressHintDelegate getHintDelegate() {
        return hintDelegate;
    }
}
