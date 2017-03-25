/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wcare.android.gocoro.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.eventbus.Subscribe;
import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.core.StateChangeEvent;
import com.wcare.android.gocoro.core.GoCoRoDevice;
import com.wcare.android.gocoro.ui.adapter.BluetoothDeviceAdapter;

import java.lang.reflect.Method;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class ActivityClassicScan extends AppCompatActivity
        implements AdapterView.OnItemClickListener {
    private static final String TAG = ActivityClassicScan.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 2;


    private BluetoothDeviceAdapter mDeviceListAdapter;
    private BluetoothAdapter mBtAdapter;
    private boolean mScanning;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.text_logo)
    TextView mLogoTextView;
    @BindView(R.id.image_background)
    ImageView mBackgroundImage;
    @BindView(R.id.list)
    ListView mListView;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDeviceListAdapter.addDevice(device);
                        mDeviceListAdapter.notifyDataSetChanged();
                    }
                });
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mScanning = false;
                invalidateOptionsMenu();
            }
        }
    };

    @Subscribe
    public void onDeviceStateChanged(final StateChangeEvent e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceListAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogoTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Bauhaus93.ttf"));
        mBackgroundImage.setImageResource(R.drawable.background1);

        // Initializes list view adapter.
        mDeviceListAdapter = new BluetoothDeviceAdapter(this);
        mListView.setAdapter(mDeviceListAdapter);
        mListView.setOnItemClickListener(this);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        GoCoRoDevice.getInstance(this).registerReceiver(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);
        if (device != null) {
            GoCoRoDevice goCoRo = GoCoRoDevice.getInstance(this);
            if (!goCoRo.isCurrentDevice(device)) {
                goCoRo.closeDevice();
                goCoRo.setDeviceAddress(device.getAddress());
                goCoRo.openDevice();
            } else {
                if (goCoRo.isOpen()) {
                    goCoRo.closeDevice();
                } else {
                    goCoRo.openDevice();
                }
            }


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            MenuItemCompat.setActionView(menu.findItem(R.id.menu_refresh), null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            MenuItemCompat.setActionView(menu.findItem(R.id.menu_refresh), R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.menu_clear:
                Set<BluetoothDevice> devices = mBtAdapter.getBondedDevices();
                for (BluetoothDevice device : devices) {
                    unpairDevice(device);
                }
                break;
            case R.id.menu_scan:
                mDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    public void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, "unpairDevice fail.", e);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        mDeviceListAdapter.clear();
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mDeviceListAdapter.addDevice(device);
            }
        }
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        scanLeDevice(false);
        mDeviceListAdapter.clear();
        unregisterReceiver(mReceiver);

        GoCoRoDevice.getInstance(this).unregisterReceiver(this);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mScanning = true;

            // If we're already discovering, stop it
            if (mBtAdapter.isDiscovering()) {
                mBtAdapter.cancelDiscovery();
            }
            // Request discover from BluetoothAdapter
            mBtAdapter.startDiscovery();
        } else {
            mScanning = false;
            mBtAdapter.cancelDiscovery();
        }
        invalidateOptionsMenu();
    }


}