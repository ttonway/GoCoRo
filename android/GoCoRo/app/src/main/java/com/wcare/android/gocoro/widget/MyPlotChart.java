package com.wcare.android.gocoro.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CombinedChart;

/**
 * Created by ttonway on 2017/2/3.
 */
public class MyPlotChart extends CombinedChart {


    public MyPlotChart(Context context) {
        super(context);
    }

    public MyPlotChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPlotChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        mChartTouchListener = new PlotChartTouchListener(this, mViewPortHandler.getMatrixTouch(), 3f);
    }
}
