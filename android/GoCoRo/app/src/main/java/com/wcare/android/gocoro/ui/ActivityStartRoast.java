package com.wcare.android.gocoro.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastProfile;
import com.wcare.android.gocoro.ui.adapter.SelectProfileAdapter;
import com.wcare.android.gocoro.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    @BindView(R.id.spinner_cool_temp)
    Spinner mSpinnerCoolTemp;

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

        List<CoolTemperature> list = new ArrayList<>();
        final int[] array = new int[]{40, 50, 60, 70, 80};
        for (int t : array) {
            list.add(new CoolTemperature(t, getString(R.string.x_celsius_unit, t)));
        }
        ArrayAdapter<CoolTemperature> adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        mSpinnerCoolTemp.setAdapter(adapter);
        mSpinnerCoolTemp.setSelection(3);

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
                        int coolTemp = selected.getCoolTemperature();
                        int index = Arrays.binarySearch(array, coolTemp);
                        if (index >= 0) {
                            mSpinnerCoolTemp.setSelection(index);
                        }
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
        CoolTemperature bean = (CoolTemperature) mSpinnerCoolTemp.getSelectedItem();
        profile.setCoolTemperature(bean.temperature);

        profile.setReferenceProfile(mAdapter.getSelectedProfile());
        mRealm.commitTransaction();


        ActivityPlot.startRoast(this, profile.getUuid());
        finish();
    }


    private static class CoolTemperature {
        int temperature;
        String name;

        public CoolTemperature(int temperature, String name) {
            this.temperature = temperature;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
