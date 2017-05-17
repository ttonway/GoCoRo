package com.wcare.android.gocoro.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.common.eventbus.Subscribe;
import com.wcare.android.gocoro.core.GoCoRoDevice;
import com.wcare.android.gocoro.core.ProfileEvent;
import com.wcare.android.gocoro.ui.dialog.ProfileRestoreDialog;

/**
 * Created by ttonway on 2016/12/21.
 */
public class BaseActivity extends AppCompatActivity {

    Handler mHandler;
    GoCoRoDevice mDevice;

    @Subscribe
    public void onProfileStatusChanged(final ProfileEvent e) {
        if (e.type == ProfileEvent.TYPE_PROFILE_CONTINUE) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ProfileRestoreDialog dialog = ProfileRestoreDialog.newInstance(e.profileUid);
                    dialog.show(getSupportFragmentManager(), "restore");
                }
            });
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        mDevice = GoCoRoDevice.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDevice.registerReceiver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mDevice.unregisterReceiver(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
