package com.wcare.android.gocoro.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.common.eventbus.Subscribe;
import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.bluetooth.BluetoothLeDriver;
import com.wcare.android.gocoro.bluetooth.ErrorEvent;
import com.wcare.android.gocoro.core.GoCoRoDevice;
import com.wcare.android.gocoro.bluetooth.ProfileStatusEvent;
import com.wcare.android.gocoro.bluetooth.StateChangeEvent;
import com.wcare.android.gocoro.model.RoastData;
import com.wcare.android.gocoro.model.RoastProfile;
import com.wcare.android.gocoro.ui.dialog.AlertDialog;
import com.wcare.android.gocoro.utils.Utils;
import com.wcare.android.gocoro.widget.EventButton;
import com.wcare.android.gocoro.widget.LineMarkerView;
import com.wcare.android.gocoro.widget.CustomNumberPicker;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;

/**
 * Created by ttonway on 2016/12/13.
 */
public class ActivityPlot extends BaseActivity
        implements OnChartValueSelectedListener {
    private static final String TAG = ActivityPlot.class.getSimpleName();

    public static void startRoast(Context context, String profileUuid, String referenceUuid) {

        Intent intent = new Intent(context, ActivityPlot.class);
        intent.putExtra(ActivityPlot.PARAM_UUID, profileUuid);
        intent.putExtra(ActivityPlot.PARAM_REFERENCE_UUID, referenceUuid);
        intent.putExtra(PARAM_ROAST, true);
        context.startActivity(intent);
    }

    public static void viewPlot(Context context, String profileUuid) {

        Intent intent = new Intent(context, ActivityPlot.class);
        intent.putExtra(ActivityPlot.PARAM_UUID, profileUuid);
        intent.putExtra(PARAM_ROAST, false);
        context.startActivity(intent);
    }


    private static final String PARAM_UUID = "ActivityPlot:uuid";
    private static final String PARAM_REFERENCE_UUID = "ActivityPlot:reference";
    private static final String PARAM_ROAST = "ActivityPlot:roast";

    public static final int COLOR_LINE = 0xfffff100;
    public static final int COLOR_FIRE = 0xb275001b;


//    private static final int[] COLOR_BAR = new int[]{0xfff7b27f, 0xffff8123, 0xfff75441, 0xffed3861, 0xfff40a3f};
//    public static final float[] FIRE_LEVEL0 = new float[]{0, 0, 0, 0, 0};
//    public static final float[] FIRE_LEVEL1 = new float[]{1, 0, 0, 0, 0};
//    public static final float[] FIRE_LEVEL2 = new float[]{1, 1, 0, 0, 0};
//    public static final float[] FIRE_LEVEL3 = new float[]{1, 1, 1, 0, 0};
//    public static final float[] FIRE_LEVEL4 = new float[]{1, 1, 1, 1, 0};
//    public static final float[] FIRE_LEVEL5 = new float[]{1, 1, 1, 1, 1};

    public static final float TEMPERATURE_MAX = 250.f;
    public static final float TEMPERATURE_MIN = 0;
    public static final float FIRE_MAX = 10;
    public static final float FIRE_MIN = 0;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.top_container)
    View mTopContainer;
    @BindView(R.id.bottom_container)
    View mBottomContainer;

    @BindView(R.id.chart)
    CombinedChart mChart;
    @BindView(R.id.picker_minute)
    CustomNumberPicker mMinutePicker;
    @BindView(R.id.picker_second)
    CustomNumberPicker mSecondPicker;
    @BindView(R.id.ratingbar)
    RatingBar mRatingBar;

    @BindView(R.id.text_country)
    TextView mCountryTextView;
    @BindView(R.id.text_bean)
    TextView mBeanTextView;
    @BindView(R.id.text_weight)
    TextView mWeightTextView;

    @BindView(R.id.event1)
    EventButton mEventButton1;
    @BindView(R.id.event2)
    EventButton mEventButton2;
    @BindView(R.id.event3)
    EventButton mEventButton3;
    @BindView(R.id.event4)
    EventButton mEventButton4;

    RoastProfile mProfile;
    RoastProfile mReferenceProfile;
    boolean mRoast;
    boolean mRoastStarted;
    boolean mCompleteDialogShowed;


    LineDataSet mTempDataSet;
    LineDataSet mPreHeatDataSet;
    LineDataSet mRoastDataSet;
    LineDataSet mCoolDataSet;
    ScatterDataSet mEventDataSet;
    LineDataSet mFireDataSet;

    LineDataSet mReferenceTempDataSet;
    LineDataSet mReferenceFireDataSet;

    Handler mHandler;
    GoCoRoDevice mDevice;
    Realm mRealm;


    @Subscribe
    public void onDeviceStateChanged(final StateChangeEvent e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                supportInvalidateOptionsMenu();
            }
        });
    }

    @Subscribe
    public void onProfileStatusChanged(final ProfileStatusEvent e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mProfile.isComplete() && !mCompleteDialogShowed) {
                    mCompleteDialogShowed = true;
                    AlertDialog dialog = AlertDialog.newInstance(getString(R.string.roast_completed));
                    dialog.show(getSupportFragmentManager(), "alert");
                }
            }
        });
    }

    @Subscribe
    public void onError(final ErrorEvent e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String message = getString(R.string.error_connect_x, e.cause);
                if (e.cause == BluetoothLeDriver.ERROR_WRONG_DEVICE) {
                    message = getString(R.string.error_wrong_device);
                } else if (e.cause == BluetoothLeDriver.ERROR_TIMEOUT) {
                    message = getString(R.string.error_timeout);
                } else if (e.cause == BluetoothLeDriver.ERROR_CONNECTION_FAIL) {
                    message = getString(R.string.error_connection_error);
                }
                AlertDialog dialog = AlertDialog.newInstance(message);
                dialog.show(getSupportFragmentManager(), "alert");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupChart();
        mMinutePicker.setFormatter(CustomNumberPicker.getTwoDigitFormatter());
        mMinutePicker.setMaxValue(59);
        mMinutePicker.setMinValue(0);
        mSecondPicker.setFormatter(CustomNumberPicker.getTwoDigitFormatter());
        mSecondPicker.setMaxValue(59);
        mSecondPicker.setMinValue(0);
        mEventButton1.setEventName(getString(R.string.event_burst1_start));
        mEventButton2.setEventName(getString(R.string.event_burst1));
        mEventButton3.setEventName(getString(R.string.event_burst2_start));
        mEventButton4.setEventName(getString(R.string.event_burst2));
        mEventButton1.setEventStatus(getString(R.string.event_unrecorded));
        mEventButton2.setEventStatus(getString(R.string.event_unrecorded));
        mEventButton3.setEventStatus(getString(R.string.event_unrecorded));
        mEventButton4.setEventStatus(getString(R.string.event_unrecorded));

        String uuid = getIntent().getStringExtra(PARAM_UUID);
        String referenceUuid = getIntent().getStringExtra(PARAM_REFERENCE_UUID);
        mRoast = getIntent().getBooleanExtra(PARAM_ROAST, false);
        Log.d(TAG, "uuid " + uuid + ", reference-uuid " + referenceUuid);

        mHandler = new Handler();
        mDevice = GoCoRoDevice.getInstance(this);
        mDevice.registerReceiver(this);
        mRealm = Realm.getDefaultInstance();
        mProfile = mRealm.where(RoastProfile.class).equalTo("uuid", uuid).findFirst();
        mReferenceProfile = mRealm.where(RoastProfile.class).equalTo("uuid", referenceUuid).findFirst();
        Log.d(TAG, "profile " + mProfile);
        Log.d(TAG, "reference " + mReferenceProfile);


        int minute = 0;
        int second = 0;
        int fire = 3;
        if (mReferenceProfile != null) {
            minute = mReferenceProfile.getStartDruation() / 60;
            second = mReferenceProfile.getStartDruation() % 60;
            fire = mReferenceProfile.getStartFire();
        }
        mMinutePicker.setValue(minute);
        mSecondPicker.setValue(second);
        mRatingBar.setRating(fire);

        CombinedData data = new CombinedData();
        data.setData(createLineData());
        data.setData(createScatterData());
//        data.setData(createBarData());
        mChart.setData(data);

        final RealmChangeListener<RoastProfile> listener = new RealmChangeListener<RoastProfile>() {
            @Override
            public void onChange(RoastProfile result) {
                if (!TextUtils.equals(mCountryTextView.getText(), result.getBeanCountry())) {
                    mCountryTextView.setText(result.getBeanCountry());
                }
                if (!TextUtils.equals(mBeanTextView.getText(), result.getBeanName())) {
                    mBeanTextView.setText(result.getBeanName());
                }
                String weight = getString(R.string.x_g_unit, result.getStartWeight());
                if (!TextUtils.equals(mWeightTextView.getText(), weight)) {
                    mWeightTextView.setText(weight);
                }
                int count = mTempDataSet.getEntryCount();
                for (; count < result.plotDatas.size(); count++) {
                    addPlotData(result.plotDatas.get(count), mRoast);
                }
                mChart.notifyDataSetChanged();
                mChart.invalidate();
            }
        };
        mProfile.addChangeListener(listener);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onChange(mProfile);
            }
        });

        if (mRoast) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持常亮的屏幕的状态
            setTitle("");

            mDevice.openDevice();
        } else {
            XAxis xAxis = mChart.getXAxis();
            Number maxTime = mProfile.plotDatas.max("time");
            float max = 3 * 60;
            if (maxTime != null) {
                max += maxTime.intValue();
            }
            xAxis.setAxisMaximum(max);

            mTopContainer.setVisibility(View.GONE);
            mBottomContainer.setVisibility(View.GONE);
        }
        mChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mRoast) {
            mDevice.openDevice();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDevice.unregisterReceiver(this);

        if (mRoast && !mRoastStarted) {
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    mProfile.deleteFromRealm();
                }
            });
        }
        mRealm.close();
    }

    void setupChart() {
        mChart.setLogEnabled(true);

        mChart.setOnChartValueSelectedListener(this);

        mChart.getDescription().setEnabled(false);

//        mChart.setVisibleXRangeMaximum(60 * 15);

        LineMarkerView mv = new LineMarkerView(this, R.layout.linechart_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv);

        int textColor = getResources().getColor(R.color.text_light_gray);
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setAxisMaximum(TEMPERATURE_MAX);
        leftAxis.setAxisMinimum(TEMPERATURE_MIN);
        leftAxis.setTextColor(textColor);
//        leftAxis.setAxisLineWidth(2f);
//        leftAxis.setAxisLineColor(COLOR_TEXT);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int val = (int) value;
                if (val < 0) {
                    return "";
                } else {

                    return getString(R.string.x_celsius_unit, val);
                }
            }
        });

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setAxisMaximum(FIRE_MAX);
        rightAxis.setAxisMinimum(FIRE_MIN);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawZeroLine(false);
        rightAxis.setGranularityEnabled(false);
        rightAxis.setEnabled(false);
        rightAxis.removeAllLimitLines();
        for (int i = 1; i <= 5; i++) {
            LimitLine ll = new LimitLine(i, getString(R.string.label_fire_x, i));
            ll.setLineWidth(1f);
            ll.setLineColor(0xff2a2630);
            ll.setTextColor(textColor);
            ll.setTextSize(10f);
            rightAxis.addLimitLine(ll);
        }

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setTextColor(textColor);
//        xAxis.setAxisLineWidth(2f);
//        xAxis.setAxisLineColor(COLOR_TEXT);
        xAxis.setGranularity(60);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(10 * 60);
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return Utils.formatTime((int) value);
            }
        });

        Legend l = mChart.getLegend();
        l.setEnabled(false);
    }

    ScatterData createScatterData() {
        ScatterDataSet dataSet = new ScatterDataSet(null, "event");
        dataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        dataSet.setColor(COLOR_LINE);
        dataSet.setScatterShapeHoleColor(0xffe50014);
        dataSet.setScatterShapeSize(com.github.mikephil.charting.utils.Utils.convertDpToPixel(10f));
        dataSet.setScatterShapeHoleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(false);
        mEventDataSet = dataSet;

        ArrayList<IScatterDataSet> dataSets = new ArrayList<IScatterDataSet>();
        dataSets.add(dataSet);
        return new ScatterData(dataSets);
    }


    LineData createLineData() {
        mTempDataSet = createTemperatureDataSet("temperature");
        mPreHeatDataSet = createStatusLineDataSet("preheat", getResources().getDrawable(R.drawable.fill_pre_heat), getResources().getColor(R.color.fill_preheat_start));
        mRoastDataSet = createStatusLineDataSet("roast", getResources().getDrawable(R.drawable.fill_roast), getResources().getColor(R.color.fill_roast_start));
        mCoolDataSet = createStatusLineDataSet("cool", getResources().getDrawable(R.drawable.fill_cool), getResources().getColor(R.color.fill_cool_start));
        mFireDataSet = createFireLineDataSet("fire");

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        if (mReferenceProfile != null) {
            mReferenceTempDataSet = createTemperatureDataSet("reference-temperature");
            mReferenceTempDataSet.setColor(COLOR_LINE, 50);
            mReferenceTempDataSet.setHighlightEnabled(false);

            mReferenceFireDataSet = createFireLineDataSet("reference-fire");
            mReferenceFireDataSet.setColor(COLOR_FIRE, 50);

            for (RoastData data : mReferenceProfile.plotDatas) {
                mReferenceTempDataSet.addEntry(new Entry(data.getTime(), data.getTemperature()));
                mReferenceFireDataSet.addEntry(new Entry(data.getTime(), data.getFire()));
            }

            dataSets.add(mReferenceFireDataSet);
            dataSets.add(mReferenceTempDataSet);

            Number maxTime = mReferenceProfile.plotDatas.max("time");
            if (maxTime != null) {
                XAxis xAxis = mChart.getXAxis();
                xAxis.setAxisMaximum(maxTime.intValue());
            }
        }
        dataSets.add(mPreHeatDataSet);
        dataSets.add(mRoastDataSet);
        dataSets.add(mCoolDataSet);
        dataSets.add(mFireDataSet);
        dataSets.add(mTempDataSet);

        return new LineData(dataSets);
    }

    LineDataSet createTemperatureDataSet(String label) {
        LineDataSet dataSet = new LineDataSet(null, label);
        dataSet.setColor(COLOR_LINE);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircleHole(false);
        dataSet.setHighlightEnabled(true);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setDrawVerticalHighlightIndicator(true);
        return dataSet;
    }

    LineDataSet createStatusLineDataSet(String label, Drawable fillDrawable, int fillColor) {

        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(1f);
        set.setColor(fillColor);
        set.setHighlightEnabled(false);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setDrawCircleHole(false);
        set.setDrawFilled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            set.setFillDrawable(fillDrawable);
        } else {
            set.setFillColor(fillColor);
            set.setFillAlpha(Color.alpha(fillColor));
        }
        set.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return mChart.getAxisLeft().getAxisMinimum();
            }
        });

        return set;
    }

    LineDataSet createFireLineDataSet(String label) {
        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setLineWidth(1f);
        set.setColor(COLOR_FIRE);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setDrawCircleHole(false);
        set.setHighlightEnabled(false);
        set.setDrawFilled(true);
        set.setFillColor(COLOR_FIRE);
        set.setFillAlpha(Color.alpha(COLOR_FIRE));
        set.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return mChart.getAxisRight().getAxisMinimum();
            }
        });

        return set;
    }


//    BarData createBarData() {
//        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
//        entries.add(new BarEntry(0, FIRE_LEVEL0));
//
//        BarDataSet dataSet = new BarDataSet(entries, "fire");
//        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
//        dataSet.setColors(COLOR_BAR);
//        dataSet.setDrawValues(false);
//        dataSet.setHighlightEnabled(false);
//        mFireDataSet = dataSet;
//
//        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
//        dataSets.add(dataSet);
//
//        BarData data = new BarData(dataSets);
//        data.setBarWidth(1);
//
//        return data;
//    }


    void addPlotData(RoastData data, boolean updateAxis) {
        mTempDataSet.addEntry(new Entry(data.getTime(), data.getTemperature(), data));
        mFireDataSet.addEntry(new Entry(data.getTime(), data.getFire()));
        if (data.getStatus() == RoastData.STATUS_PREHEATING) {
            mPreHeatDataSet.addEntry(new Entry(data.getTime(), TEMPERATURE_MAX));
        } else if (data.getStatus() == RoastData.STATUS_ROASTING) {
            mRoastDataSet.addEntry(new Entry(data.getTime(), TEMPERATURE_MAX));
        } else if (data.getStatus() == RoastData.STATUS_COOLING) {
            mCoolDataSet.addEntry(new Entry(data.getTime(), TEMPERATURE_MAX));
        }

        if (data.getEvent() != null) {
            mEventDataSet.addEntry(new Entry(data.getTime(), data.getTemperature()));
        }

//        switch (data.getFire()) {
//            case 0:
//                mFireDataSet.addEntry(new BarEntry(data.getTime(), FIRE_LEVEL0));
//                break;
//            case 1:
//                mFireDataSet.addEntry(new BarEntry(data.getTime(), FIRE_LEVEL1));
//                break;
//            case 2:
//                mFireDataSet.addEntry(new BarEntry(data.getTime(), FIRE_LEVEL2));
//                break;
//            case 3:
//                mFireDataSet.addEntry(new BarEntry(data.getTime(), FIRE_LEVEL3));
//                break;
//            case 4:
//                mFireDataSet.addEntry(new BarEntry(data.getTime(), FIRE_LEVEL4));
//                break;
//            case 5:
//                mFireDataSet.addEntry(new BarEntry(data.getTime(), FIRE_LEVEL5));
//                break;
//        }

        if (updateAxis) {
            XAxis xAxis = mChart.getXAxis();
            if (data.getTime() > xAxis.getAxisMaximum()) {
                xAxis.setAxisMaximum(data.getTime() + 5 * 60);
            }

//            mChart.moveViewToX(data.getTime());
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i(TAG, "Entry selected " + e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i(TAG, "Nothing selected.");
    }

    @OnClick(R.id.btn_roast)
    void startRoast(Button button) {
        mMinutePicker.clearFocus();
        mSecondPicker.clearFocus();
        if (!mDevice.isOpen()) {
            Toast.makeText(this, R.string.toast_device_unconnected, Toast.LENGTH_SHORT).show();
        } else {
            final int seconds = mMinutePicker.getValue() * 60 + mSecondPicker.getValue();
            final int fire = (int) mRatingBar.getRating();

            if (mRoastStarted) {
                if (mProfile.isComplete()) {
                    Toast.makeText(this, R.string.toast_already_completed, Toast.LENGTH_SHORT).show();
                    return;
                }

                RoastData data = mProfile.plotDatas.isEmpty() ? null : mProfile.plotDatas.last();
                if (data == null) {
                    return;
                }

                mDevice.setRoast(seconds, fire);

                mRealm.beginTransaction();
                data.setChangeFire(fire);
                data.setChangeTime(seconds);
                mRealm.commitTransaction();

            } else {
                if (mDevice.isRoasting() || mDevice.isDeviceBusy()) {
                    Toast.makeText(this, R.string.toast_now_roasting, Toast.LENGTH_SHORT).show();
                    return;
                }

                mDevice.readyProfile(mProfile);
                mDevice.startRoast(seconds, fire);

                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mProfile.setStartTime(System.currentTimeMillis());
                        mProfile.setStartFire(fire);
                        mProfile.setStartDruation(seconds);
                    }
                });
                mRoastStarted = true;
                button.setText(R.string.btn_set);
            }

            mMinutePicker.setValue(0);
            mSecondPicker.setValue(0);
        }
    }

    @OnClick(R.id.btn_cool)
    void stopRoast(Button button) {
        if (!mDevice.isOpen()) {
            Toast.makeText(this, R.string.toast_device_unconnected, Toast.LENGTH_SHORT).show();
        } else {
            mDevice.stopRoast();
        }
    }

    @OnClick({R.id.event1, R.id.event2, R.id.event3, R.id.event4})
    void addEvent(EventButton view) {
        RoastData data = mProfile.plotDatas.isEmpty() ? null : mProfile.plotDatas.last();
        if (data == null || !TextUtils.isEmpty(data.getEvent())) {
            return;
        }
        if (mProfile.isComplete()) {
            return;
        }

        if (view.isSelected()) {
            return;
        }
        view.setSelected(true);
        view.setEventStatus(getString(R.string.event_recorded));

        String event = null;
        switch (view.getId()) {
            case R.id.event1:
                event = RoastData.EVENT_BURST1_START;
                break;
            case R.id.event2:
                event = RoastData.EVENT_BURST1;
                break;
            case R.id.event3:
                event = RoastData.EVENT_BURST2_START;
                break;
            case R.id.event4:
                event = RoastData.EVENT_BURST2;
                break;
        }

        mRealm.beginTransaction();
        data.setEvent(event);
        mRealm.commitTransaction();

        mEventDataSet.addEntry(new Entry(data.getTime(), data.getTemperature()));
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);

//        MenuItemCompat.setShowAsAction(menu.findItem(R.id.menu_scan),
//                MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
//        MenuItemCompat.setShowAsAction(menu.findItem(R.id.menu_refresh),
//                MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        menu.findItem(R.id.action_plot).setVisible(false);
        if (mRoast) {
            menu.findItem(R.id.action_share).setVisible(false);
            menu.findItem(R.id.action_form).setVisible(false);

            if (mDevice.getState() == BluetoothLeDriver.STATE_OPEN) {
                menu.findItem(R.id.action_setting).setIcon(R.drawable.ic_menu_connected);
            } else if (mDevice.getState() == BluetoothLeDriver.STATE_CLOSE) {
                menu.findItem(R.id.action_setting).setIcon(R.drawable.ic_menu_unconnected);
            } else {
                MenuItemCompat.setActionView(menu.findItem(R.id.action_setting), R.layout.actionbar_indeterminate_progress);
            }
        } else {
            menu.findItem(R.id.action_setting).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                startActivity(new Intent(this, ActivityClassicScan.class));
                return true;
            case R.id.action_form:
                Intent intent = new Intent(this, ActivityForm.class);
                intent.putExtra(ActivityForm.PARAM_UUID, mProfile.getUuid());
                startActivity(intent);
                return true;
            case R.id.action_share:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
