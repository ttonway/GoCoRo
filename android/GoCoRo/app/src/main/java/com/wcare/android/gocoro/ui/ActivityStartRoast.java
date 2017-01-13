package com.wcare.android.gocoro.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastProfile;
import com.wcare.android.gocoro.ui.adapter.SelectProfileAdapter;
import com.wcare.android.gocoro.utils.Utils;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by ttonway on 2016/12/21.
 */
public class ActivityStartRoast extends BaseActivity {
    private static final String TAG = "ActivityStartRoast";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.input_country)
    EditText mInputContry;
    @BindView(R.id.input_bean)
    EditText mInputBean;
    @BindView(R.id.input_people)
    EditText mInputPeople;
    @BindView(R.id.input_weight)
    EditText mInputWeight;
    @BindView(R.id.input_temperature)
    EditText mInputTemperature;

    @BindView(R.id.list)
    ListView mListView;
    SelectProfileAdapter mAdapter;

    Realm mRealm;
    RealmResults<RoastProfile> mProfiles;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRealm = Realm.getDefaultInstance();
        mProfiles = mRealm.where(RoastProfile.class).greaterThan("startTime", 0).findAll().sort("startTime", Sort.DESCENDING);
        mAdapter = new SelectProfileAdapter(this, mProfiles);
        mProfiles.addChangeListener(new RealmChangeListener<RealmResults<RoastProfile>>() {
            @Override
            public void onChange(RealmResults<RoastProfile> results) {
                mAdapter.notifyDataSetChanged();
            }
        });

        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoastProfile profile = (RoastProfile) parent.getItemAtPosition(position);
                if (profile != null) {
                    RoastProfile selected = mAdapter.clickProfile(profile);

                    if (selected != null) {
                        mInputContry.setText(selected.getBeanCountry());
                        mInputBean.setText(selected.getBeanName());
                        mInputPeople.setText(selected.getPeople());
                        mInputWeight.setText(String.valueOf((int) selected.getStartWeight()));
                        mInputTemperature.setText(String.valueOf(selected.getEnvTemperature()));
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }

    @OnClick(R.id.btn_startup)
    void startupRoast() {
        mRealm.beginTransaction();
        RoastProfile profile = mRealm.createObject(RoastProfile.class, UUID.randomUUID().toString());
        profile.setPeople(mInputPeople.getText().toString());
        profile.setBeanCountry(mInputContry.getText().toString());
        profile.setBeanName(mInputBean.getText().toString());
        profile.setStartWeight(Utils.parseInt(mInputWeight.getText().toString()));
        profile.setEnvTemperature(Utils.parseInt(mInputTemperature.getText().toString()));
        mRealm.commitTransaction();

        RoastProfile reference = mAdapter.getSelectedProfile();

        ActivityPlot.startRoast(this, profile.getUuid(), reference == null ? null : reference.getUuid());
        finish();
    }
}
