package com.wcare.android.gocoro.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastProfile;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by ttonway on 2016/12/12.
 */
public class FragHome extends BaseFragment {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.text_title)
    TextView mTitleTextView;

    @BindView(R.id.text1)
    TextView mLastBeanTextView;
    @BindView(R.id.text2)
    TextView mLastTimeTextView;

    private Unbinder mUnbinder;

    Realm mRealm;
    RealmResults<RoastProfile> mProfiles;
    DateFormat mDateFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRealm = Realm.getDefaultInstance();
        mProfiles = mRealm.where(RoastProfile.class).greaterThan("startTime", 0).findAll().sort("startTime", Sort.DESCENDING);

        mDateFormat = DateFormat.getDateTimeInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        mTitleTextView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Bauhaus93.ttf"));
        mToolbar.setNavigationIcon(R.drawable.ic_menu_setting);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ActivityClassicScan.class));
            }
        });


        if (mProfiles.size() > 0) {
            RoastProfile profile = mProfiles.first();
            mLastBeanTextView.setText(profile.getFullName());
            mLastTimeTextView.setText(mDateFormat.format(new Date(profile.getStartTime())));
        } else {
            mLastBeanTextView.setText(R.string.label_no_profile);
            mLastTimeTextView.setText(null);
        }
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

    @OnClick(R.id.btn_startup)
    void startupRoast() {
        startActivity(new Intent(getActivity(), ActivityStartRoast.class));
    }
}
