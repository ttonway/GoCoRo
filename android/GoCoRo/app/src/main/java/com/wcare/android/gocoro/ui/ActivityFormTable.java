package com.wcare.android.gocoro.ui;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastData;
import com.wcare.android.gocoro.model.RoastProfile;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by ttonway on 2016/12/20.
 */
public class ActivityFormTable extends BaseActivity {

    public static final String PARAM_UUID = "ActivityFormTable:uuid";

    RoastProfile mProfile;

    @BindView(R.id.text_people)
    TextView mTextPeople;
    @BindView(R.id.text_country)
    TextView mTextCountry;
    @BindView(R.id.text_date)
    TextView mTextDate;
    @BindView(R.id.text_bean)
    TextView mTextBean;
    @BindView(R.id.text_begin_time)
    TextView mTextBeginTime;
    @BindView(R.id.text_end_time)
    TextView mTextEndTime;
    @BindView(R.id.text_begin_weight)
    TextView mTextBeginWeight;
    @BindView(R.id.text_end_weight)
    TextView mTextEndWeight;

    @BindView(R.id.text_burst1_start_time)
    TextView mTextBurst1StartTime;
    @BindView(R.id.text_burst1_start_fire)
    TextView mTextBurst1StartFire;
    @BindView(R.id.text_burst2_start_time)
    TextView mTextBurst2StartTime;
    @BindView(R.id.text_burst2_start_fire)
    TextView mTextBurst2StartFire;
    @BindView(R.id.text_env_temperature)
    TextView mTextEnvTemperature;
    @BindView(R.id.text_start_fire)
    TextView mTextStartFire;
    @BindView(R.id.text_burst1_time)
    TextView mTextBurst1Time;
    @BindView(R.id.text_burst1_fire)
    TextView mTextBurst1Fire;
    @BindView(R.id.text_burst2_time)
    TextView mTextBurst2Time;
    @BindView(R.id.text_burst2_fire)
    TextView mTextBurst2Fire;
    @BindView(R.id.text_weight_ratio)
    TextView mTextWeightRatio;

    @BindView(R.id.table_preheat)
    TableLayout mTablePreheat;
    @BindView(R.id.table_roast)
    TableLayout mTableRoast;
    @BindView(R.id.table_cool)
    TableLayout mTableCool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_table);
        ButterKnife.bind(this);

        String uuid = getIntent().getStringExtra(PARAM_UUID);
        Realm realm = Realm.getDefaultInstance();
        mProfile = realm.where(RoastProfile.class).equalTo("uuid", uuid).findFirst();


        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        mTextCountry.setText(mProfile.getBeanCountry());
        mTextDate.setText(dateFormat.format(new Date(mProfile.getStartTime())));
        mTextBean.setText(mProfile.getBeanName());
        mTextBeginTime.setText(dateFormat.format(new Date(mProfile.getStartTime())));
        mTextEndTime.setText(dateFormat.format(new Date(mProfile.getEndTime())));
        mTextBeginWeight.setText(String.valueOf(mProfile.getStartWeight()));
        mTextEndWeight.setText(String.valueOf(mProfile.getEndWeight()));

        mTextEnvTemperature.setText(mProfile.getEnvTemperature() + "℃");
        mTextStartFire.setText(String.valueOf(mProfile.getStartFire()));
        mTextWeightRatio.setText(String.valueOf(mProfile.getEndWeight() / mProfile.getStartWeight()));

        int preHeatTime = 0;
        int roastTime = 0;
        int coolTime = 0;
        int minutes = 0;
        for (RoastData entry : mProfile.getPlotDatas()) {
            int m = entry.getTime() / 60;
            if (minutes == m) {
                continue;
            } else {
                minutes = m;
            }

            TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.row_form_entry, null);
            TextView text1 = (TextView) row.findViewById(R.id.text_time);
            TextView text2 = (TextView) row.findViewById(R.id.text_temperature);

            text2.setText(entry.getTemperature() + "℃");

            if (entry.getStatus() == RoastData.STATUS_PREHEATING) {
                text1.setText(String.valueOf(m));
                mTablePreheat.addView(row);
            } else if (entry.getStatus() == RoastData.STATUS_ROASTING) {
                if (roastTime == 0) {
                    roastTime = m;
                }
                text1.setText(String.valueOf(m - roastTime));
                mTableRoast.addView(row);
            } else if (entry.getStatus() == RoastData.STATUS_COOLING) {
                if (coolTime == 0) {
                    coolTime = m;
                }
                text1.setText(String.valueOf(m - coolTime));
                mTableCool.addView(row);
            }
        }
    }
}
