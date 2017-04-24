package com.wcare.android.gocoro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastProfile;
import com.wcare.android.gocoro.ui.adapter.SelectProfileAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by ttonway on 2016/12/20.
 */
public class ActivityProfilePicker extends BaseActivity {

    private static final String RESULT_PROFILE_UUID = "ActivityProfilePicker:result";

    public static RoastProfile getResult(Intent intent) {
        String uuid = intent.getStringExtra(RESULT_PROFILE_UUID);

        Realm realm = Realm.getDefaultInstance();
        return realm.where(RoastProfile.class).equalTo("uuid", uuid).findFirst();
    }

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.list)
    ListView mListView;
    @BindView(R.id.internalEmpty)
    TextView mEmptyView;

    RealmResults<RoastProfile> mProfiles;

    SelectProfileAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Realm realm = Realm.getDefaultInstance();
        mProfiles = realm.where(RoastProfile.class).greaterThan("startTime", 0).findAll().sort("startTime", Sort.DESCENDING);

        mAdapter = new SelectProfileAdapter(this, mProfiles);
        mProfiles.addChangeListener(new RealmChangeListener<RealmResults<RoastProfile>>() {
            @Override
            public void onChange(RealmResults<RoastProfile> results) {
                mAdapter.notifyDataSetChanged();
            }
        });

        mEmptyView.setText(R.string.label_no_profile);
        mListView.setEmptyView(mEmptyView);
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoastProfile profile = (RoastProfile) parent.getItemAtPosition(position);
                if (profile != null) {
//                    RoastProfile selected = mAdapter.clickProfile(profile);

                    Intent result = new Intent();
                    result.putExtra(RESULT_PROFILE_UUID, profile.getUuid());
                    setResult(RESULT_OK, result);
                    finish();
                }
            }
        });
    }
}
