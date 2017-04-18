package com.wcare.android.gocoro.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastProfile;
import com.wcare.android.gocoro.ui.adapter.FormAdapter;
import com.wcare.android.gocoro.utils.Utils;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by ttonway on 2016/12/20.
 */
public class ActivityForm extends BaseActivity {

    public static final String PARAM_UUID = "ActivityFormTable:uuid";

    Realm mRealm;
    RoastProfile mProfile;

    final ListHeaderBinder mHeaderBinder = new ListHeaderBinder();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.expand_list)
    ExpandableListView mListView;

    FormAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String uuid = getIntent().getStringExtra(PARAM_UUID);
        mRealm = Realm.getDefaultInstance();
        mProfile = mRealm.where(RoastProfile.class).equalTo("uuid", uuid).findFirst();

        View header = getLayoutInflater().inflate(R.layout.list_header_form, mListView, false);
        ButterKnife.bind(mHeaderBinder, header);
        mListView.addHeaderView(header);
        View footer = getLayoutInflater().inflate(R.layout.list_footer_form, mListView, false);
        mListView.addFooterView(footer);

        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();
        mHeaderBinder.mTextPeople.setText(mProfile.getPeople());
        mHeaderBinder.mTextCountry.setText(mProfile.getBeanCountry());
        mHeaderBinder.mTextDate.setText(dateFormat.format(new Date(mProfile.getStartTime())));
        mHeaderBinder.mTextBean.setText(mProfile.getBeanName());
        mHeaderBinder.mTextBeginTime.setText(timeFormat.format(new Date(mProfile.getStartTime())));
        mHeaderBinder.mTextEndTime.setText(mProfile.getEndTime() == 0 ? "-" : timeFormat.format(new Date(mProfile.getEndTime())));
        mHeaderBinder.mTextBeginWeight.setText(String.valueOf(mProfile.getStartWeight()));
        mHeaderBinder.mTextEndWeight.setText(String.valueOf(mProfile.getEndWeight()));
        mHeaderBinder.mTextEnvTemperature.setText(String.valueOf(mProfile.getEnvTemperature()));
        mHeaderBinder.mTextStartFire.setText(String.valueOf(mProfile.getStartFire()));
        mHeaderBinder.mTextWeightRatio.setText(mProfile.formatWeightRatio(mProfile.getStartWeight(), mProfile.getEndWeight()));

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                int start = Utils.parseInt(mHeaderBinder.mTextBeginWeight.getText().toString());
                int end = Utils.parseInt(mHeaderBinder.mTextEndWeight.getText().toString());
                mHeaderBinder.mTextWeightRatio.setText(mProfile.formatWeightRatio(start, end));
            }
        };
        mHeaderBinder.mTextBeginWeight.addTextChangedListener(textWatcher);
        mHeaderBinder.mTextEndWeight.addTextChangedListener(textWatcher);

        mAdapter = new FormAdapter(this, mProfile);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mRealm.beginTransaction();
        mProfile.setPeople(mHeaderBinder.mTextPeople.getText().toString());
        mProfile.setBeanCountry(mHeaderBinder.mTextCountry.getText().toString());
        mProfile.setBeanName(mHeaderBinder.mTextBean.getText().toString());
        mProfile.setStartWeight(Utils.parseInt(mHeaderBinder.mTextBeginWeight.getText().toString()));
        mProfile.setEndWeight(Utils.parseInt(mHeaderBinder.mTextEndWeight.getText().toString()));
        mProfile.setEnvTemperature(Utils.parseInt(mHeaderBinder.mTextEnvTemperature.getText().toString()));
        mRealm.commitTransaction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);

        menu.findItem(R.id.action_device).setVisible(false);
        menu.findItem(R.id.action_plot).setVisible(false);
        menu.findItem(R.id.action_form).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_share:
                mListView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(mListView.getDrawingCache());
                mListView.destroyDrawingCache();

                Utils.shareContent(this, bitmap, "");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    static class ListHeaderBinder {
        @BindView(R.id.text_people)
        EditText mTextPeople;
        @BindView(R.id.text_country)
        EditText mTextCountry;
        @BindView(R.id.text_bean)
        EditText mTextBean;
        @BindView(R.id.text_begin_weight)
        EditText mTextBeginWeight;
        @BindView(R.id.text_end_weight)
        EditText mTextEndWeight;
        @BindView(R.id.text_env_temperature)
        EditText mTextEnvTemperature;

        @BindView(R.id.text_date)
        TextView mTextDate;
        @BindView(R.id.text_begin_time)
        TextView mTextBeginTime;
        @BindView(R.id.text_end_time)
        TextView mTextEndTime;
        @BindView(R.id.text_start_fire)
        TextView mTextStartFire;
        @BindView(R.id.text_weight_ratio)
        TextView mTextWeightRatio;
    }
}
