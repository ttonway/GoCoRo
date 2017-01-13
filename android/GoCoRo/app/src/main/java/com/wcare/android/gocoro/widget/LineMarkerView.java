
package com.wcare.android.gocoro.widget;

import android.content.Context;
import android.widget.*;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastData;
import com.wcare.android.gocoro.utils.Utils;

/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
public class LineMarkerView extends MarkerView {

    private TextView tvContent;

    public LineMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        tvContent = (TextView) findViewById(android.R.id.text1);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        RoastData data = (RoastData) e.getData();
        if (e instanceof CandleEntry) {

            CandleEntry ce = (CandleEntry) e;

            tvContent.setText("Error");
        } else {
            StringBuilder sb = new StringBuilder(Utils.formatTime(data.getTime()));
            sb.append("  ").append(getContext().getString(R.string.x_celsius_unit, data.getTemperature()));
            if (data.getFire() != 0) {
                sb.append("-").append(getContext().getString(R.string.label_fire_x, data.getFire()));
            }
            if (data.getEvent() != null) {
                sb.append("-").append(getContext().getString(data.getEventNameResId()));
            }

            tvContent.setText(sb);
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
