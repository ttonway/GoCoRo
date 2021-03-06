package com.wcare.android.gocoro.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.core.GoCoRoDevice;
import com.wcare.android.gocoro.http.AppVersion;
import com.wcare.android.gocoro.http.ServiceFactory;
import com.wcare.android.gocoro.ui.dialog.UpdateAppDialog;
import com.wcare.android.gocoro.utils.Utils;
import com.wcare.android.gocoro.widget.TabManager;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ActivityMain extends BaseActivity
        implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = ActivityMain.class.getSimpleName();

    static final int TAB_COUNT = 4;
    static int[] TAB_ICONS = new int[]{R.drawable.ic_coffee_bean, R.drawable.ic_record, R.drawable.ic_cup, R.drawable.ic_more};
    static int[] TAB_TITLES = new int[]{R.string.tab_home, R.string.tab_roast_history, R.string.tab_cupping_history, R.string.tab_more};
    static String[] TAB_TAGS = new String[]{"home", "roast", "cupping", "more"};
    static Class<?>[] TAB_CLS = new Class[]{FragHome.class, FragProfileList.class, FragCuppingList.class, FragMore.class};
    static int[] TAB_IDS = new int[]{R.id.radio_button0, R.id.radio_button1, R.id.radio_button2, R.id.radio_button3};
    final RadioButton[] TAB_BUTTONS = new RadioButton[TAB_COUNT];

    @BindView(android.R.id.tabhost)
    TabHost mTabHost;
    @BindView(R.id.tab_radio_group)
    RadioGroup mRadioGroup;
    TabManager mTabManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initTabHost();

        GoCoRoDevice.getInstance(this).openDevice();

        checkVersion();
    }

    private void initTabHost() {
        mTabHost.setup();

        mRadioGroup.check(TAB_IDS[0]);
        mRadioGroup.setOnCheckedChangeListener(this);

        mTabManager = new TabManager(this, getSupportFragmentManager(),
                mTabHost, R.id.realtabcontent);

        for (int i = 0; i < TAB_COUNT; i++) {
            String title = getString(TAB_TITLES[i]);
            Drawable icon = getResources().getDrawable(TAB_ICONS[i]);
            TAB_BUTTONS[i] = (RadioButton) findViewById(TAB_IDS[i]);

            final Drawable wrappedDrawable = DrawableCompat.wrap(icon);
            DrawableCompat.setTintList(wrappedDrawable, getResources().getColorStateList(R.color.tab_button_tintcolor));
            wrappedDrawable.setBounds(0, 0, wrappedDrawable.getIntrinsicWidth(), wrappedDrawable.getIntrinsicHeight());
            TAB_BUTTONS[i].setText(title);
            TAB_BUTTONS[i].setCompoundDrawables(null, wrappedDrawable, null, null);

            mTabManager.addTab(
                    mTabHost.newTabSpec(TAB_TAGS[i]).setIndicator(title, icon),
                    TAB_CLS[i], null);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        for (int i = 0; i < TAB_IDS.length; i++) {
            if (TAB_IDS[i] == checkedId) {
                mTabHost.setCurrentTab(i);
                return;
            }

        }
    }

    void checkVersion() {
        Call<AppVersion> call = ServiceFactory.getWebService().queryAndroidVerion();
        call.enqueue(new Callback<AppVersion>() {
            @Override
            public void onResponse(Call<AppVersion> call, Response<AppVersion> response) {
                if (response.isSuccessful()) {
                    final AppVersion result = response.body();
                    if (result.versionCode > Utils.getAppVersionCode(ActivityMain.this)) {
                        if (!isFinishing()) {
                            UpdateAppDialog dialg = UpdateAppDialog.newInstance(result.apkUrl, null);
                            dialg.show(getSupportFragmentManager(), "update-app");
                        }
                    }
                } else {
                    try {
                        Log.e(TAG, "onResponse error: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse error.", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<AppVersion> call, Throwable t) {
                Log.e(TAG, "onFailure.", t);
            }
        });
    }
}
