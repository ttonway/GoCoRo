package com.wcare.android.gocoro.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ttonway on 2017/2/21.
 */

public class ActivityCoporate extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coporate);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
