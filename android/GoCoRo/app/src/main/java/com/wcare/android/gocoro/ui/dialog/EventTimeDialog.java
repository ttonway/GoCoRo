package com.wcare.android.gocoro.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.NumberPicker;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastData;
import com.wcare.android.gocoro.ui.ActivityPlot;
import com.wcare.android.gocoro.widget.TimePicker;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by ttonway on 2016/11/3.
 */
public class EventTimeDialog extends DialogFragment {
    private static final String KEY_EVENT = "EventTimeDialog:event";
    private static final String KEY_STATUS = "EventTimeDialog:status";
    private static final String KEY_SECONDS = "EventTimeDialog:seconds";
    private static final String KEY_ENABLE_HOUR = "EventTimeDialog:enable_hour";

    public static EventTimeDialog newInstance(String event, int status, int seconds, boolean enableHour) {
        EventTimeDialog frag = new EventTimeDialog();
        Bundle b = new Bundle();
        b.putString(KEY_EVENT, event);
        b.putInt(KEY_STATUS, status);
        b.putInt(KEY_SECONDS, seconds);
        b.putBoolean(KEY_ENABLE_HOUR, enableHour);
        frag.setArguments(b);
        return frag;
    }

    NumberPicker mStatusPicker;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String event = getArguments().getString(KEY_EVENT);
        int status = getArguments().getInt(KEY_STATUS);
        int seconds = getArguments().getInt(KEY_SECONDS);
        boolean enableHour = getArguments().getBoolean(KEY_ENABLE_HOUR, true);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(seconds * 1000L);


        final MyTimePickerDialog timePicker = new MyTimePickerDialog(getActivity(), new MyTimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute, int seconds) {
                int s = ((hourOfDay * 60) + minute) * 60 + seconds;

                ActivityPlot activity = (ActivityPlot) getActivity();
                activity.changeEventTime(event, mStatusPicker.getValue(), s);

            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), true);
        timePicker.setTitle(RoastData.getEventNameResId(event));
        mStatusPicker = timePicker.getStatusPicker();
        mStatusPicker.setMinValue(RoastData.STATUS_ROASTING);
        mStatusPicker.setMaxValue(RoastData.STATUS_COOLING);
        mStatusPicker.setValue(status);
        mStatusPicker.setDisplayedValues(new String[] {getString(R.string.category_roast), getString(R.string.category_cool)});

        if (!enableHour) {
            timePicker.disableHourPicker();
        }

        return timePicker;
    }
}
