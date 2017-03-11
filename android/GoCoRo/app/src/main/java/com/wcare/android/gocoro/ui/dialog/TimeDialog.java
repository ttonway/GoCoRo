package com.wcare.android.gocoro.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.ui.ActivityPlot;
import com.wcare.android.gocoro.widget.TimePicker;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by ttonway on 2016/11/3.
 */
public class TimeDialog extends DialogFragment {
    private static final String KEY_SECONDS = "EventTimeDialog:seconds";

    public static TimeDialog newInstance(int seconds) {
        TimeDialog frag = new TimeDialog();
        Bundle b = new Bundle();
        b.putInt(KEY_SECONDS, seconds);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int seconds = getArguments().getInt(KEY_SECONDS);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(seconds * 1000L);

        final MyTimePickerDialog timePicker = new MyTimePickerDialog(getActivity(), new MyTimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute, int seconds) {
                int s = minute * 60 + seconds;

                ActivityPlot activity = (ActivityPlot) getActivity();
                activity.changeRoastTime(s);
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), true);
        timePicker.setTitle(R.string.label_roast_time);
        timePicker.getStatusPicker().setVisibility(View.GONE);
        timePicker.disableHourPicker();

        return timePicker;
    }
}
