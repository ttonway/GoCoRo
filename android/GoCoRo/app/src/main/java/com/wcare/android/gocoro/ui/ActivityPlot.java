package com.wcare.android.gocoro.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
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
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.common.eventbus.Subscribe;
import com.wcare.android.gocoro.Constants;
import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.bluetooth.BluetoothLeDriver;
import com.wcare.android.gocoro.core.ErrorEvent;
import com.wcare.android.gocoro.core.GoCoRoDevice;
import com.wcare.android.gocoro.core.ProfileEvent;
import com.wcare.android.gocoro.core.StateChangeEvent;
import com.wcare.android.gocoro.http.RemoteModel;
import com.wcare.android.gocoro.http.ServiceFactory;
import com.wcare.android.gocoro.model.RoastData;
import com.wcare.android.gocoro.model.RoastProfile;
import com.wcare.android.gocoro.ui.dialog.AlertDialog;
import com.wcare.android.gocoro.ui.dialog.EventTimeDialog;
import com.wcare.android.gocoro.ui.dialog.ProgressDialog;
import com.wcare.android.gocoro.ui.dialog.TimeDialog;
import com.wcare.android.gocoro.utils.Utils;
import com.wcare.android.gocoro.widget.EventButton;
import com.wcare.android.gocoro.widget.LineMarkerView;
import com.wcare.android.gocoro.widget.TimeTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ttonway on 2016/12/13.
 */
public class ActivityPlot extends BaseActivity
        implements OnChartValueSelectedListener, OnChartGestureListener, RatingBar.OnRatingBarChangeListener {
    private static final String TAG = ActivityPlot.class.getSimpleName();

    public static void startRoast(Context context, String profileUuid) {

        Intent intent = new Intent(context, ActivityPlot.class);
        intent.putExtra(ActivityPlot.PARAM_UUID, profileUuid);
        intent.putExtra(PARAM_ROAST, true);
        context.startActivity(intent);
    }

    public static void viewPlot(Context context, String profileUuid) {
        boolean roast = false;
        RoastProfile profile = GoCoRoDevice.getInstance(context).getProfile();
        if (profile != null && TextUtils.equals(profileUuid, profile.getUuid())) {
            roast = true;
        }

        Intent intent = new Intent(context, ActivityPlot.class);
        intent.putExtra(ActivityPlot.PARAM_UUID, profileUuid);
        intent.putExtra(PARAM_ROAST, roast);
        context.startActivity(intent);
    }


    private static final String PARAM_UUID = "ActivityPlot:uuid";
    private static final String PARAM_ROAST = "ActivityPlot:roast";

    public static final int COLOR_LINE = 0xfffff100;
    public static final int COLOR_FIRE = 0xb275001b;
    public static final int COLOR_LINE2 = 0xff0075c9;
    public static final int COLOR_FIRE2 = 0x32c20430;

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
    private static final float ONE_MIN_IN_SECONDS = 60;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.top_container)
    View mTopContainer;
    @BindView(R.id.bottom_container)
    View mBottomContainer;

    @BindView(R.id.crop_layout)
    ViewGroup mCropLayout;
    @BindView(R.id.chart)
    CombinedChart mChart;
    @BindView(R.id.text_minute)
    TimeTextView mMinuteTextView;
    @BindView(R.id.text_second)
    TimeTextView mSecondTextView;
    @BindView(R.id.ratingbar)
    RatingBar mRatingBar;
    @BindView(R.id.btn_roast)
    Button mSetButton;

    @BindView(R.id.text_country)
    TextView mCountryTextView;
    @BindView(R.id.text_bean)
    TextView mBeanTextView;
    @BindView(R.id.text_weight)
    TextView mWeightTextView;
    @BindView(R.id.text_roast_time)
    TextView mRoastTimeTextView;

    @BindView(R.id.event1)
    EventButton mEventButton1;
    @BindView(R.id.event2)
    EventButton mEventButton2;
    @BindView(R.id.event3)
    EventButton mEventButton3;
    @BindView(R.id.event4)
    EventButton mEventButton4;

    ProgressDialog mProgressDialog;

    RoastProfile mProfile;
    RoastProfile mReferenceProfile;
    int mReferenceIndex = 0;
    boolean mRoast;
    boolean mCompleteDialogShowed;
    boolean mAutoChangeFire;


    Highlight mHighlight;
    boolean mAutoHighlightEnabled = true;
    final Runnable mEnableAutoHighlightRunnable = new Runnable() {
        @Override
        public void run() {
            mAutoHighlightEnabled = true;
        }
    };

    LineDataSet mTempDataSet;
    LineDataSet mPreHeatDataSet;
    LineDataSet mRoastDataSet;
    LineDataSet mCoolDataSet;
    ScatterDataSet mEventDataSet;
    LineDataSet mFireDataSet;

    LineDataSet mReferenceTempDataSet;
    LineDataSet mReferenceFireDataSet;

    Realm mRealm;
    final RealmChangeListener<RoastProfile> mProfileChangeListener = new RealmChangeListener<RoastProfile>() {
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
            if (result.getStartDruation() > 0) {
                String time = getString(R.string.label_roast_time_x, Utils.formatTime(result.getStartDruation()));
                if (!TextUtils.equals(mRoastTimeTextView.getText(), time)) {
                    mRoastTimeTextView.setText(time);
                }
            }

            String btnText = getString(result.getPlotDatas().isEmpty() ? R.string.btn_start : R.string.btn_set);
            if (!TextUtils.equals(mSetButton.getText(), btnText)) {
                mSetButton.setText(btnText);
            }

            int count = mTempDataSet.getEntryCount();
            RoastData currentData = null;// get the lastest one
            for (; count < result.getPlotDatas().size(); count++) {
                currentData = result.getPlotDatas().get(count);
                addPlotData(currentData, mRoast);
            }

            if (mRoast && mAutoHighlightEnabled && currentData != null) {
                Highlight highlight = new Highlight(currentData.getTime(), Float.NaN, mHighlight.getDataSetIndex());
                highlight.setDataIndex(mHighlight.getDataIndex());
                mChart.highlightValue(highlight);
            }

            mChart.notifyDataSetChanged();
            mChart.invalidate();

            // 自动调整火力
            if (mAutoChangeFire && mReferenceProfile != null && currentData != null && currentData.getStatus() == RoastData.STATUS_ROASTING) {
                int currentTime = currentData.getTime();
                int currentFire = currentData.getFire();
                int targetFire = -1;
                for (; mReferenceIndex < mReferenceProfile.getPlotDatas().size(); mReferenceIndex++) {
                    RoastData d = mReferenceProfile.getPlotDatas().get(mReferenceIndex);
                    if (d.getStatus() == RoastData.STATUS_ROASTING && d.getTime() > currentTime) {
                        targetFire = d.getFire();
                        break;
                    }
                }
                if (targetFire != -1 && currentFire != targetFire) {
                    mDevice.setRoast(0, targetFire);
                }
            }
        }
    };

    @Subscribe
    public void onDeviceStateChanged(final StateChangeEvent e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                supportInvalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onProfileStatusChanged(final ProfileEvent e) {
        super.onProfileStatusChanged(e);

        if (mRoast && e.type == ProfileEvent.TYPE_PROFILE_RESET) {
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
    }

    @Subscribe
    public void onError(final ErrorEvent e) {
        if (mRoast) {
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
    }

    public RoastProfile getProfile() {
        return mProfile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupChart();
//        mMinutePicker.setFormatter(CustomNumberPicker.getTwoDigitFormatter());
//        mMinutePicker.setMaxValue(59);
//        mMinutePicker.setMinValue(0);
//        mSecondPicker.setFormatter(CustomNumberPicker.getTwoDigitFormatter());
//        mSecondPicker.setMaxValue(59);
//        mSecondPicker.setMinValue(0);
        mEventButton1.setEventName(getString(R.string.event_burst1_start_abbr));
        mEventButton2.setEventName(getString(R.string.event_burst1_abbr));
        mEventButton3.setEventName(getString(R.string.event_burst2_start_abbr));
        mEventButton4.setEventName(getString(R.string.event_burst2_abbr));
        mEventButton1.setEventStatus(getString(R.string.event_unrecorded));
        mEventButton2.setEventStatus(getString(R.string.event_unrecorded));
        mEventButton3.setEventStatus(getString(R.string.event_unrecorded));
        mEventButton4.setEventStatus(getString(R.string.event_unrecorded));
        mRatingBar.setOnRatingBarChangeListener(this);

        String uuid = getIntent().getStringExtra(PARAM_UUID);
        mRoast = getIntent().getBooleanExtra(PARAM_ROAST, false);
        Log.d(TAG, "uuid " + uuid);

        mRealm = Realm.getDefaultInstance();
        mProfile = mRealm.where(RoastProfile.class).equalTo("uuid", uuid).findFirst();
        mReferenceProfile = mRoast ? mProfile.getReferenceProfile() : null;
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

        CombinedData data = new CombinedData();
        data.setData(createLineData());
        data.setData(createScatterData());
//        data.setData(createBarData());
        mChart.setData(data);
        int dataIndex = data.getDataIndex(data.getLineData());
        int dataSetIndex = data.getIndexOfDataSet(mTempDataSet);
        mHighlight = new Highlight(0, Float.NaN, dataSetIndex);
        mHighlight.setDataIndex(dataIndex);

        for (RoastData entry : mProfile.getPlotDatas()) {
            if (TextUtils.equals(entry.getEvent(), RoastData.EVENT_BURST1_START)) {
                mEventButton1.setTag(entry);
                mEventButton1.setSelected(true);
                mEventButton1.setEventStatus(getString(R.string.event_recorded));
            } else if (TextUtils.equals(entry.getEvent(), RoastData.EVENT_BURST1)) {
                mEventButton2.setTag(entry);
                mEventButton2.setSelected(true);
                mEventButton2.setEventStatus(getString(R.string.event_recorded));
            } else if (TextUtils.equals(entry.getEvent(), RoastData.EVENT_BURST2_START)) {
                mEventButton3.setTag(entry);
                mEventButton3.setSelected(true);
                mEventButton3.setEventStatus(getString(R.string.event_recorded));
            } else if (TextUtils.equals(entry.getEvent(), RoastData.EVENT_BURST2)) {
                mEventButton4.setTag(entry);
                mEventButton4.setSelected(true);
                mEventButton4.setEventStatus(getString(R.string.event_recorded));
            }
        }


        mProfile.addChangeListener(mProfileChangeListener);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mProfileChangeListener.onChange(mProfile);
            }
        });

        if (mRoast) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持常亮的屏幕的状态
            setTitle("");

            mDevice.openDevice();
            if (!mProfile.isComplete() && mProfile.getStartDruation() > 0) {
                restoreRoast();

                fire = mProfile.getStartFire();
            }
        } else {
            XAxis xAxis = mChart.getXAxis();
            Number maxTime = mProfile.getPlotDatas().max("time");
            float max = ONE_MIN_IN_SECONDS;
            if (maxTime != null) {
                max += maxTime.intValue();
            }
            xAxis.setAxisMaximum(max);

            mTopContainer.setVisibility(View.GONE);
            mBottomContainer.setVisibility(View.GONE);
        }
        mMinuteTextView.setValue(minute);
        mSecondTextView.setValue(second);
        mRatingBar.setRating(fire);
        mChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mRoast) {
            supportInvalidateOptionsMenu();
            mDevice.openDevice();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mProfile.removeChangeListener(mProfileChangeListener);

        if (mRoast && mProfile.getPlotDatas().isEmpty()) {
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

        mChart.setOnChartGestureListener(this);

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
                int time = (int) value;
                return Utils.formatTime(getTimeInStatus(time));
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
            mReferenceTempDataSet.setColor(COLOR_LINE2);
            mReferenceTempDataSet.setHighlightEnabled(false);

            mReferenceFireDataSet = createFireLineDataSet("reference-fire");
            mReferenceFireDataSet.setColor(COLOR_FIRE2);

            for (RoastData data : mReferenceProfile.getPlotDatas()) {
                mReferenceTempDataSet.addEntry(new Entry(data.getTime(), data.getTemperature()));
                mReferenceFireDataSet.addEntry(new Entry(data.getTime(), data.getFire()));
            }

            dataSets.add(mReferenceFireDataSet);
            dataSets.add(mReferenceTempDataSet);

            Number maxTime = mReferenceProfile.getPlotDatas().max("time");
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

    int getTimeInStatus(int time) {
        if (mProfile.getCoolTime() != 0 && time >= mProfile.getCoolTime()) {
            time -= mProfile.getCoolTime();
        } else if (mProfile.getRoastTime() != 0 && time >= mProfile.getRoastTime()) {
            time -= mProfile.getRoastTime();
        }
        return time;
    }

    public CharSequence getPlotDataLabel(RoastData data) {
        StringBuilder sb = new StringBuilder(Utils.formatTime(getTimeInStatus(data.getTime())));
        sb.append("  ").append(getString(R.string.x_celsius_unit, data.getTemperature()));
        if (data.getFire() != 0) {
            sb.append("-").append(getString(R.string.label_fire_x, data.getFire()));
        }
        if (data.getEvent() != null) {
            sb.append("-").append(data.getEventName(this));
        }
        return sb;
    }


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
            if (data.getTime() + ONE_MIN_IN_SECONDS > xAxis.getAxisMaximum()) {
                xAxis.setAxisMaximum(data.getTime() + 5 * ONE_MIN_IN_SECONDS);
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


    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        mHandler.removeCallbacks(mEnableAutoHighlightRunnable);
        mAutoHighlightEnabled = false;
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        mHandler.postDelayed(mEnableAutoHighlightRunnable, 5000);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @OnClick({R.id.text_minute, R.id.text_second})
    public void changeRoastTime() {
        final int seconds = mMinuteTextView.getValue() * 60 + mSecondTextView.getValue();
        TimeDialog dialog = TimeDialog.newInstance(seconds);
        dialog.show(getSupportFragmentManager(), "roast-time");
    }

    public void changeRoastTime(int seconds) {
        int minutes = seconds / 60;
        seconds = seconds - minutes * 60;

        mMinuteTextView.setValue(minutes);
        mSecondTextView.setValue(seconds);
    }

    public void restoreRoast() {
        mSetButton.setText(R.string.btn_set);

        if (mDevice.isOpen()) {
            if (mDevice.getProfile() == null) {
                mDevice.readyProfile(mProfile);
            }
        }
    }

    @OnClick(R.id.btn_roast)
    void startRoast(Button button) {
        final int seconds = mMinuteTextView.getValue() * 60 + mSecondTextView.getValue();
        final int fire = (int) mRatingBar.getRating();

        if (seconds == 0) {
            Toast.makeText(this, R.string.toast_roast_time_zero, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mDevice.isOpen()) {
            Toast.makeText(this, R.string.toast_device_unconnected, Toast.LENGTH_SHORT).show();
        } else {

            if (!mProfile.getPlotDatas().isEmpty()) {
                boolean roasting = mProfile.getRoastTime() != 0 && mProfile.getCoolTime() == 0;
                if (mProfile.isComplete()) {
                    Toast.makeText(this, R.string.toast_already_completed, Toast.LENGTH_SHORT).show();
                    return;
                } else if (!roasting) {
                    return;
                }

                if (mAutoChangeFire) {
                    mAutoChangeFire = false;
                }

                mDevice.setRoast(seconds, fire);

            } else {
                if (mDevice.getProfile() != null || mDevice.isDeviceBusy()) {
                    Toast.makeText(this, R.string.toast_now_roasting, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mReferenceProfile != null && mReferenceProfile.getStartDruation() == seconds && mReferenceProfile.getStartFire() == fire) {
                    mAutoChangeFire = true;
                }

                mDevice.readyProfile(mProfile);
                mDevice.startRoast(seconds, fire, mProfile.getCoolTemperature());

                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mProfile.setDeviceId(mDevice.getDeviceAddress());
                        mProfile.setStartTime(System.currentTimeMillis());
                        mProfile.setStartFire(fire);
                        mProfile.setStartDruation(seconds);
                        mProfile.setDirty(true);
                    }
                });
            }

            mMinuteTextView.setValue(0);
            mSecondTextView.setValue(0);
        }
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (fromUser) {
            Log.d(TAG, "onRatingChanged " + rating);

            final int fire = (int) rating;
            boolean roasting = mProfile.getRoastTime() != 0 && mProfile.getCoolTime() == 0;
            if (mDevice.isOpen() && roasting && !mProfile.isComplete()) {

                RoastData data = mProfile.getLastPlotData();
                if (data != null) {

                    if (mAutoChangeFire) {
                        mAutoChangeFire = false;
                    }

                    mDevice.setRoast(0, fire);
                }
            }
        }
    }

    @OnClick(R.id.btn_cool)
    void stopRoast(Button button) {
        if (!mDevice.isOpen()) {
            Toast.makeText(this, R.string.toast_device_unconnected, Toast.LENGTH_SHORT).show();
        } else {
            RoastData data = mProfile.getLastPlotData();
            if (data != null) {
                mDevice.stopRoast();

                mRealm.beginTransaction();
                data.setManualCool(true);
                mRealm.commitTransaction();
            }
        }
    }

    @OnClick({R.id.event1, R.id.event2, R.id.event3, R.id.event4})
    void addEvent(EventButton view) {
        RoastData lastData = mProfile.getLastPlotData();
        if (view.isSelected()) {
            RoastData data = (RoastData) view.getTag();
            int seconds = getTimeInStatus(data.getTime());
            boolean enableHour = false;
            int roastTime = mProfile.getCoolTime() != 0 ? mProfile.getCoolTime() : lastData.getTime() + 1;
            roastTime = roastTime - mProfile.getRoastTime();
            int coolTime = mProfile.getCoolTime() != 0 ? lastData.getTime() + 1 - mProfile.getCoolTime() : 0;
            if (roastTime > 3600 || coolTime > 3600) {
                enableHour = true;
            }
            EventTimeDialog dialog = EventTimeDialog.newInstance(data.getEvent(), data.getStatus(), seconds, enableHour);
            dialog.show(getSupportFragmentManager(), "event-time");
            return;
        }

        if (lastData == null || lastData.getStatus() == RoastData.STATUS_PREHEATING || !TextUtils.isEmpty(lastData.getEvent())) {
            return;
        }

        view.setTag(lastData);
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
        lastData.setEvent(event);
        mRealm.commitTransaction();

        mEventDataSet.addEntry(new Entry(lastData.getTime(), lastData.getTemperature()));
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    public void changeEventTime(String event, int status, int seconds) {
        EventButton button;
        if (event.equals(RoastData.EVENT_BURST1_START)) {
            button = mEventButton1;
        } else if (event.equals(RoastData.EVENT_BURST1)) {
            button = mEventButton2;
        } else if (event.equals(RoastData.EVENT_BURST2_START)) {
            button = mEventButton3;
        } else if (event.equals(RoastData.EVENT_BURST2)) {
            button = mEventButton4;
        } else {
            return;
        }

        if (status == RoastData.STATUS_ROASTING) {
            if (mProfile.getRoastTime() == 0) {
                Toast.makeText(this, R.string.toast_event_time_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            seconds += mProfile.getRoastTime();
        } else if (status == RoastData.STATUS_COOLING) {
            if (mProfile.getCoolTime() == 0) {
                Toast.makeText(this, R.string.toast_event_time_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            seconds += mProfile.getCoolTime();
        }

        RoastData oldData = (RoastData) button.getTag();
        RoastData newData = null;
        for (RoastData data : mProfile.getPlotDatas()) {
            if (data.getEvent() != null) {
                continue;
            }
            int distance = Math.abs(data.getTime() - seconds);
            if (data.getStatus() == status && distance < 3) {
                if (newData == null) {
                    newData = data;
                } else if (distance < Math.abs(newData.getTime() - seconds)) {
                    newData = data;
                }

                if (distance == 0) {
                    break;
                }
            } else if (newData != null) {
                break;
            }
        }

        if (newData == null) {
            Toast.makeText(this, R.string.toast_event_time_invalid, Toast.LENGTH_SHORT).show();
            return;
        }


        mRealm.beginTransaction();
        mProfile.setDirty(true);
        oldData.setEvent(null);
        newData.setEvent(event);
        mRealm.commitTransaction();

        button.setTag(newData);

        List<Entry> removes = mEventDataSet.getEntriesForXValue(oldData.getTime());
        for (Entry en : removes) {
            mEventDataSet.removeEntry(en);
        }
        mEventDataSet.addEntryOrdered(new Entry(newData.getTime(), newData.getTemperature()));
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
        if (mRoast) {
            menu.findItem(R.id.action_share).setVisible(false);
            menu.findItem(R.id.action_form).setVisible(false);

            if (mDevice.getState() == BluetoothLeDriver.STATE_OPEN) {
                menu.findItem(R.id.action_device).setIcon(R.drawable.ic_menu_connected);
            } else if (mDevice.getState() == BluetoothLeDriver.STATE_CLOSE) {
                menu.findItem(R.id.action_device).setIcon(R.drawable.ic_menu_unconnected);
            } else {
                MenuItemCompat.setActionView(menu.findItem(R.id.action_device), R.layout.actionbar_indeterminate_progress);
            }
        } else {
            menu.findItem(R.id.action_device).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_device:
                startActivity(new Intent(this, ActivityClassicScan.class));
                return true;
            case R.id.action_form:
                Intent intent = new Intent(this, ActivityForm.class);
                intent.putExtra(ActivityForm.PARAM_UUID, mProfile.getUuid());
                startActivity(intent);
                return true;
            case R.id.action_share:

                if (mProfile.getSid() != 0 && !mProfile.isDirty()) {
                    shareProfile(mProfile.getFullName(), mProfile.getSid());
                } else {
                    mProgressDialog = ProgressDialog.show(this);

                    Call<RemoteModel> call = ServiceFactory.getWebService().uploadProfile(mProfile);
                    call.enqueue(new Callback<RemoteModel>() {
                        @Override
                        public void onResponse(Call<RemoteModel> call, Response<RemoteModel> response) {
                            mProgressDialog.dismissAllowingStateLoss();
                            if (response.isSuccessful()) {
                                final RemoteModel result = response.body();
                                if (mProfile.isValid()) {
                                    mRealm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            mProfile.setSid(result.sid);
                                            mProfile.setDirty(false);
                                        }
                                    });
                                }

                                shareProfile(mProfile.getFullName(), result.sid);
                            } else {
                                Toast.makeText(ActivityPlot.this, getString(R.string.error_network_x, ""), Toast.LENGTH_SHORT).show();
                                try {
                                    Log.e(TAG, "onResponse error: " + response.errorBody().string());
                                } catch (IOException e) {
                                    Log.e(TAG, "onResponse error.", e);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<RemoteModel> call, Throwable t) {
                            mProgressDialog.dismissAllowingStateLoss();
                            Toast.makeText(ActivityPlot.this, getString(R.string.error_network_x, t.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onFailure.", t);
                        }
                    });
                }

//                mCropLayout.setDrawingCacheEnabled(true);
//                Bitmap bitmap = Bitmap.createBitmap(mCropLayout.getDrawingCache());
//                mCropLayout.destroyDrawingCache();
//
//                Utils.shareContent(this, bitmap);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareProfile(String title, int sid) {
        Bitmap bitmap = Utils.getChartBitmap(mChart);
        Utils.shareContent(this, title, bitmap, String.format(Constants.PROFILE_WEB_URL, sid));
    }
}
