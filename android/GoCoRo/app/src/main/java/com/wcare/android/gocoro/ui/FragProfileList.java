package com.wcare.android.gocoro.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastProfile;
import com.wcare.android.gocoro.ui.adapter.ProfileAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by ttonway on 2016/12/12.
 */
public class FragProfileList extends BaseFragment {

    Realm mRealm;
    RealmResults<RoastProfile> mProfiles;

    ProfileAdapter mAdapter;

    @BindView(R.id.text_title)
    TextView mTitleTextView;
    @BindView(R.id.list)
    ListView mListView;
    private Unbinder mUnbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRealm = Realm.getDefaultInstance();
        mProfiles = mRealm.where(RoastProfile.class).greaterThan("startTime", 0).findAll().sort("startTime", Sort.DESCENDING);
        mAdapter = new ProfileAdapter(getActivity(), mProfiles);
        mProfiles.addChangeListener(new RealmChangeListener<RealmResults<RoastProfile>>() {
            @Override
            public void onChange(RealmResults<RoastProfile> results) {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_toolbar_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        mTitleTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_star, 0, 0, 0);
        mTitleTextView.setText(R.string.activity_profile_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoastProfile item = (RoastProfile)parent.getItemAtPosition(position);
                if (item != null) {
                    ActivityPlot.viewPlot(getActivity(), item.getUuid());
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }
}
