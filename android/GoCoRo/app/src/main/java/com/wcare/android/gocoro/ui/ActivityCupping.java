package com.wcare.android.gocoro.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.umeng.socialize.UMShareAPI;
import com.wcare.android.gocoro.Constants;
import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.http.RemoteModel;
import com.wcare.android.gocoro.http.ServiceFactory;
import com.wcare.android.gocoro.model.Cupping;
import com.wcare.android.gocoro.model.RoastProfile;
import com.wcare.android.gocoro.ui.dialog.ProgressDialog;
import com.wcare.android.gocoro.utils.Utils;
import com.wcare.android.gocoro.widget.RadarMarkerView;
import com.wcare.android.gocoro.widget.SeekBar;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.techery.progresshint.ProgressHintDelegate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ttonway on 2017/1/3.
 */
public class ActivityCupping extends BaseActivity {
    private static final String TAG = ActivityCupping.class.getSimpleName();

    public static void startCupping(Context context, String profileUuid) {
        Intent intent = new Intent(context, ActivityCupping.class);
        intent.putExtra(PARAM_PROFILE_UUID, profileUuid);
        context.startActivity(intent);
    }

    public static void viewCupping(Context context, String cuppingUuid) {
        Intent intent = new Intent(context, ActivityCupping.class);
        intent.putExtra(PARAM_CUPPING_UUID, cuppingUuid);
        context.startActivity(intent);
    }

    private static final String PARAM_PROFILE_UUID = "ActivityCupping:profile";
    private static final String PARAM_CUPPING_UUID = "ActivityCupping:cupping";
    private static final String STATE_EDIT = "state:edit";
    private static final String STATE_PROFILE_UUID = "state:profile";
    private static final int REQUEST_PICK_PROFILE = 555;


    public static final int COLOR_FILL = 0x8fb83f2e;
    public static final int COLOR_CIRCLE = 0xfffff100;

    public static final float CUPPING_SCORE_MIN = 6.f;
    public static final float CUPPING_SCORE_MAX = 10.f;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.text_logo)
    TextView mLogoTextView;
    @BindView(R.id.crop_layout)
    ViewGroup mCropLayout;
    @BindView(R.id.radar_chart)
    RadarChart mChart;
    @BindView(R.id.input_name)
    EditText mNameInput;
    @BindView(R.id.input_profile)
    TextView mProfileInput;
    @BindView(R.id.input_comment)
    EditText mCommentInput;
    @BindView(R.id.text_time)
    TextView mTimeTextView;
    @BindView(R.id.text_score)
    TextView mScoreTextView;
    @BindViews({R.id.seekbar1, R.id.seekbar2, R.id.seekbar3, R.id.seekbar4, R.id.seekbar5, R.id.seekbar6, R.id.seekbar7})
    List<SeekBar> mSeekBars;
    @BindView(R.id.seekbar_container)
    TableLayout mSeekBarContainer;

    ProgressDialog mProgressDialog;

    RadarDataSet mRadarDataSet;

    Realm mRealm;
    Cupping mCupping;
    RoastProfile mProfile;
    boolean mEditMode;
    DateFormat mDateFormat = DateFormat.getDateTimeInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cupping);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogoTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Bauhaus93.ttf"));

        mRealm = Realm.getDefaultInstance();
        if (getIntent().hasExtra(PARAM_CUPPING_UUID)) {
            String uuid = getIntent().getStringExtra(PARAM_CUPPING_UUID);
            mCupping = mRealm.where(Cupping.class).equalTo("uuid", uuid).findFirst();
            mProfile = mCupping.getProfile();
        } else {
            mCupping = new Cupping();
            mCupping.setUuid(UUID.randomUUID().toString());
            mCupping.setTime(System.currentTimeMillis());
            mCupping.setScore1(6.f);
            mCupping.setScore2(6.f);
            mCupping.setScore3(6.f);
            mCupping.setScore4(6.f);
            mCupping.setScore5(6.f);
            mCupping.setScore6(6.f);
            mCupping.setScore7(6.f);
            mCupping.setScore8(10.f);
            mCupping.setScore9(10.f);
            mCupping.setScore10(10.f);

            String uuid = getIntent().getStringExtra(PARAM_PROFILE_UUID);
            mProfile = mRealm.where(RoastProfile.class).equalTo("uuid", uuid).findFirst();

            mEditMode = true;
        }

        if (savedInstanceState != null) {
            mEditMode = savedInstanceState.getBoolean(STATE_EDIT, mEditMode);
            String uuid = savedInstanceState.getString(STATE_PROFILE_UUID);
            Log.d(TAG, "restore profile " + uuid);
            mProfile = mRealm.where(RoastProfile.class).equalTo("uuid", uuid).findFirst();
        }

        setupChart();
        mChart.setData(createRadarData());
        mChart.invalidate();

        mNameInput.setText(mCupping.getName());
        mProfileInput.setText(mProfile == null ? null : mProfile.getFullName());
        mCommentInput.setText(mCupping.getComment());
        mTimeTextView.setText(mDateFormat.format(new Date(mCupping.getTime())));
        mScoreTextView.setText(getString(R.string.label_score_x, String.valueOf(mCupping.getTotalScore())));

        setSeekBarProgress(mSeekBars.get(0), mCupping.getScore1());
        setSeekBarProgress(mSeekBars.get(1), mCupping.getScore2());
        setSeekBarProgress(mSeekBars.get(2), mCupping.getScore3());
        setSeekBarProgress(mSeekBars.get(3), mCupping.getScore4());
        setSeekBarProgress(mSeekBars.get(4), mCupping.getScore5());
        setSeekBarProgress(mSeekBars.get(5), mCupping.getScore6());
        setSeekBarProgress(mSeekBars.get(6), mCupping.getScore7());
        for (SeekBar seekBar : mSeekBars) {
            seekBar.getHintDelegate().setHintAdapter(new ProgressHintDelegate.SeekBarHintAdapter() {
                @Override
                public String getHint(android.widget.SeekBar seekBar, int progress) {
                    return String.valueOf(getScore(seekBar));
                }
            });
            seekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                    updateRadarChart();
                }

                @Override
                public void onStartTrackingTouch(android.widget.SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(android.widget.SeekBar seekBar) {

                }
            });
        }

        if (!mEditMode) {
            mNameInput.setEnabled(false);
            mProfileInput.setEnabled(false);
            mCommentInput.setEnabled(false);
            mCommentInput.setHint("");
            mSeekBarContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "save profile " + mProfile);
        outState.putBoolean(STATE_EDIT, mEditMode);
        outState.putString(STATE_PROFILE_UUID, mProfile == null ? null : mProfile.getUuid());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }

    @OnClick(R.id.input_profile)
    void selectProfile() {
        if (mEditMode) {
            startActivityForResult(new Intent(this, ActivityProfilePicker.class), REQUEST_PICK_PROFILE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_PROFILE) {
            if (resultCode == RESULT_OK) {
                mProfile = ActivityProfilePicker.getResult(data);
                mProfileInput.setText(mProfile == null ? null : mProfile.getFullName());
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    float getScore(android.widget.SeekBar seekBar) {
        float score = (CUPPING_SCORE_MAX - CUPPING_SCORE_MIN) * seekBar.getProgress() / (float) seekBar.getMax() + CUPPING_SCORE_MIN;
        int m = (int) (score / 0.25f + 0.5f);
        score = m * 0.25f;
        return score;
    }

    void setSeekBarProgress(SeekBar seekBar, float score) {
        seekBar.setProgress((int) ((score - CUPPING_SCORE_MIN) / (CUPPING_SCORE_MAX - CUPPING_SCORE_MIN) * seekBar.getMax()));
    }

    void updateRadarChart() {

        float total = 30;
        mRadarDataSet.clear();
        for (int i = 0; i < 7; i++) {
            float s = getScore(mSeekBars.get(i));
            mRadarDataSet.addEntry(new RadarEntry(s));
            total += s;
        }
        mChart.invalidate();

        mScoreTextView.setText(getString(R.string.label_score_x, String.valueOf(total)));
    }

    void setupChart() {
        mChart.getDescription().setEnabled(false);

        mChart.setWebLineWidth(1f);
        mChart.setWebColor(Color.LTGRAY);
        mChart.setWebLineWidthInner(1f);
        mChart.setWebColorInner(Color.LTGRAY);
        mChart.setWebAlpha(100);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MarkerView mv = new RadarMarkerView(this, R.layout.radarchart_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

//        mChart.animateXY(
//                1400, 1400,
//                Easing.EasingOption.EaseInOutQuad,
//                Easing.EasingOption.EaseInOutQuad);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextSize(14f);
        xAxis.setYOffset(0f);
        xAxis.setXOffset(0f);
        xAxis.setTextColor(getResources().getColor(R.color.text_light_gray));
        final String[] activities = new String[7];
        int index = 0;
        for (int strId : new int[]{R.string.cupping_item1, R.string.cupping_item2, R.string.cupping_item3, R.string.cupping_item4, R.string.cupping_item5, R.string.cupping_item6, R.string.cupping_item7}) {
            activities[index++] = getString(strId);
        }
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return activities[(int) value % activities.length];
            }
        });


        YAxis yAxis = mChart.getYAxis();
        yAxis.setLabelCount((int) CUPPING_SCORE_MAX - (int) CUPPING_SCORE_MIN + 2, true);
        yAxis.setTextSize(8f);
        yAxis.setTextColor(0xff545472);
        yAxis.setAxisMinimum(CUPPING_SCORE_MIN - 1);
        yAxis.setAxisMaximum(CUPPING_SCORE_MAX);
        yAxis.setDrawLabels(true);
        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value >= CUPPING_SCORE_MIN && value <= CUPPING_SCORE_MAX) {
                    return String.valueOf((int) value);
                }
                return "";
            }
        });

        Legend l = mChart.getLegend();
        l.setEnabled(false);
    }

    public RadarData createRadarData() {
        ArrayList<RadarEntry> entries = new ArrayList<RadarEntry>();
        entries.add(new RadarEntry(mCupping.getScore1()));
        entries.add(new RadarEntry(mCupping.getScore2()));
        entries.add(new RadarEntry(mCupping.getScore3()));
        entries.add(new RadarEntry(mCupping.getScore4()));
        entries.add(new RadarEntry(mCupping.getScore5()));
        entries.add(new RadarEntry(mCupping.getScore6()));
        entries.add(new RadarEntry(mCupping.getScore7()));

        RadarDataSet set = new RadarDataSet(entries, "cupping");
        set.setLineWidth(2f);
        set.setColor(COLOR_FILL);
        set.setFillColor(COLOR_FILL);
        set.setFillAlpha(Color.alpha(COLOR_FILL));
        set.setHighlightCircleStrokeWidth(1.5f);
        set.setHighlightCircleInnerRadius(0f);
        set.setHighlightCircleOuterRadius(3f);
        set.setHighlightCircleStrokeColor(COLOR_CIRCLE);
        set.setHighlightCircleStrokeAlpha(255);
        set.setHighlightCircleFillColor(COLOR_FILL | 0xff000000);
        set.setDrawFilled(true);
        set.setDrawHighlightCircleEnabled(true);
        set.setDrawHighlightIndicators(false);
        mRadarDataSet = set;

        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set);

        RadarData data = new RadarData(sets);
        data.setDrawValues(false);
        return data;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cupping, menu);
        if (mCupping.isManaged()) {

            menu.findItem(R.id.action_share).setVisible(true);
            menu.findItem(R.id.action_done).setTitle(mEditMode ? R.string.action_done : R.string.action_edit);
        } else {

            menu.findItem(R.id.action_share).setVisible(false);
            menu.findItem(R.id.action_done).setTitle(R.string.action_done);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                if (mCupping.isManaged()) {
                    if (!mEditMode) {
                        mEditMode = true;
                        mNameInput.setEnabled(true);
                        mProfileInput.setEnabled(true);
                        mCommentInput.setEnabled(true);
                        mCommentInput.setHint(R.string.hint_comment);
                        mSeekBarContainer.setVisibility(View.VISIBLE);

                        supportInvalidateOptionsMenu();
                    } else {
                        saveOrUpdateCupping();
                    }
                } else {
                    saveOrUpdateCupping();
                }
                return true;
            case R.id.action_share:

                if (mCupping.getSid() != 0) {
                    shareCupping(mCupping.getSid());
                } else {
                    mProgressDialog = ProgressDialog.show(this);

                    Call<RemoteModel> call = ServiceFactory.getWebService().uploadCupping(mCupping);
                    call.enqueue(new Callback<RemoteModel>() {
                        @Override
                        public void onResponse(Call<RemoteModel> call, Response<RemoteModel> response) {
                            mProgressDialog.dismissAllowingStateLoss();
                            if (response.isSuccessful()) {
                                final RemoteModel result = response.body();
                                if (mCupping.isValid()) {
                                    mRealm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            mCupping.setSid(result.sid);
                                        }
                                    });
                                }

                                shareCupping(result.sid);
                            } else {
                                Toast.makeText(ActivityCupping.this, getString(R.string.error_network_x, ""), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ActivityCupping.this, getString(R.string.error_network_x, t.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onFailure.", t);
                        }
                    });
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareCupping(int sid) {
        Bitmap bitmap = Utils.getChartBitmap(mChart);
        Utils.shareContent(this, bitmap, String.format(Constants.CUPPING_WEB_URL, sid));
    }

    void saveOrUpdateCupping() {
        if (TextUtils.isEmpty(mNameInput.getText())) {
            Toast.makeText(this, R.string.toast_cupping_name_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        mRealm.beginTransaction();
        mCupping.setName(mNameInput.getText().toString());
        mCupping.setComment(mCommentInput.getText().toString());
        mCupping.setProfile(mProfile);
        mCupping.setScore1(getScore(mSeekBars.get(0)));
        mCupping.setScore2(getScore(mSeekBars.get(1)));
        mCupping.setScore3(getScore(mSeekBars.get(2)));
        mCupping.setScore4(getScore(mSeekBars.get(3)));
        mCupping.setScore5(getScore(mSeekBars.get(4)));
        mCupping.setScore6(getScore(mSeekBars.get(5)));
        mCupping.setScore7(getScore(mSeekBars.get(6)));

        if (mCupping.isManaged()) {
            mRealm.commitTransaction();
        } else {
            mRealm.copyToRealm(mCupping);
            mRealm.commitTransaction();
        }

        finish();
    }
}
