package com.wcare.android.gocoro.ui.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.core.GoCoRoDevice;
import com.wcare.android.gocoro.core.GoCoRoDriver;

import java.util.ArrayList;

// Adapter for holding devices found through scanning.
public class BluetoothDeviceAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> mDevices;
    private Context mContext;
    private LayoutInflater mInflator;


    public BluetoothDeviceAdapter(Context context) {
        super();
        mContext = context;
        mDevices = new ArrayList<>();
        mInflator = LayoutInflater.from(context);
    }

    public void addDevice(BluetoothDevice device) {
        if (!mDevices.contains(device)) {
            mDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mDevices.get(position);
    }

    public void clear() {
        mDevices.clear();
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.list_item_device, null);
            viewHolder = new ViewHolder();
            viewHolder.indicator = view.findViewById(R.id.indicator);
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.deviceStatus = (TextView) view.findViewById(R.id.device_status);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mDevices.get(i);
        if (GoCoRoDevice.getInstance(mContext).isCurrentDevice(device)) {
            viewHolder.indicator.setVisibility(View.VISIBLE);
            int state = GoCoRoDevice.getInstance(mContext).getState();
            switch (state) {
                case GoCoRoDriver.STATE_OPEN:
                    viewHolder.deviceStatus.setText(R.string.state_open);
                    break;
                case GoCoRoDriver.STATE_OPENING:
                    viewHolder.deviceStatus.setText(R.string.state_opening);
                    break;
                case GoCoRoDriver.STATE_CLOSE:
                    viewHolder.deviceStatus.setText(R.string.state_close);
                    break;
                case GoCoRoDriver.STATE_CLOSING:
                    viewHolder.deviceStatus.setText(R.string.state_closing);
                    break;
                default:
                    viewHolder.deviceStatus.setText("Error State");
                    break;
            }
        } else {
            viewHolder.indicator.setVisibility(View.INVISIBLE);
            viewHolder.deviceStatus.setText(null);
        }
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.label_unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }

    static class ViewHolder {
        View indicator;
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceStatus;
    }
}