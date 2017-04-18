package com.wcare.android.gocoro.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.wcare.android.gocoro.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ttonway on 2016/12/20.
 */
public class ActivityCuppingList extends BaseActivity {

    public static void viewCuppingList(Context context, String profileUuid) {
        Intent intent = new Intent(context, ActivityCuppingList.class);
        intent.putExtra(PARAM_PROFILE_UUID, profileUuid);
        context.startActivity(intent);
    }
    private static final String PARAM_PROFILE_UUID = "ActivityCuppingList:profile";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.text_logo)
    TextView mLogoTextView;

    FragCuppingList mFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cupping_list);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogoTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Bauhaus93.ttf"));

        mFragment = (FragCuppingList) getSupportFragmentManager().findFragmentByTag("cupping-list");
        if (mFragment == null) {
            mFragment = FragCuppingList.newFragment(getIntent().getStringExtra(PARAM_PROFILE_UUID), false);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, mFragment, "cupping-list").commit();
        }
    }
}
