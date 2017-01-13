package com.wcare.android.gocoro.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.wcare.android.gocoro.model.RoastData;
import com.wcare.android.gocoro.model.RoastProfile;

import java.util.Arrays;

import io.realm.Realm;


/**
 * Created by ttonway on 2016/11/7.
 */
public class GoCoRoDevice implements DriverCallback {
    private static final String TAG = GoCoRoDevice.class.getSimpleName();

    public static final byte CMD_ROAST = (byte) 0x01;
    public static final byte CMD_SET = (byte) 0x02;
    public static final byte CMD_STOP = (byte) 0x03;
    public static final byte CMD_STATUS = (byte) 0xff;

    private static GoCoRoDevice instance = null;

    Context mContext;
    BluetoothLeDriver mDriver;
    Handler mHandler;
    EventBus mEventBus;

    SharedPreferences mPreferences;
    String mDeviceAddress;
    int mDeviceStatus = RoastData.STATUS_UNKNOWN;
    long mLastUpdateTime;

    Realm mRealm;
    RoastProfile mProfile;

    final Runnable mTimeoutCheckRunnable = new Runnable() {
        @Override
        public void run() {
            mEventBus.post(new ErrorEvent(BluetoothLeDriver.ERROR_TIMEOUT));
            resetProfile();
        }
    };

    public static synchronized GoCoRoDevice getInstance(Context context) {
        if (instance == null) {
            instance = new GoCoRoDevice(context);
        }
        return instance;
    }

    private GoCoRoDevice(Context context) {
        this.mContext = context;
        this.mDriver = new BluetoothLeDriver(mContext, this);
        this.mHandler = new Handler();
        this.mEventBus = new EventBus();

        this.mPreferences = context.getSharedPreferences("device", Context.MODE_PRIVATE);
        this.mDeviceAddress = mPreferences.getString("address", null);

        this.mRealm = Realm.getDefaultInstance();
    }

    public BluetoothLeDriver getDriver() {
        return mDriver;
    }

    public int getState() {
        return mDriver.getState();
    }


    public boolean isRoasting() {
        return mProfile != null && !mProfile.isComplete();
    }

    public void setDeviceAddress(String address) {
        mDeviceAddress = address;
        mPreferences.edit().putString("address", address).apply();
    }

    public boolean isCurrentDevice(BluetoothDevice device) {
        return TextUtils.equals(device.getAddress(), mDeviceAddress);
    }

    public void registerReceiver(Object object) {
        mEventBus.register(object);
    }

    public void unregisterReceiver(Object object) {
        mEventBus.unregister(object);
    }

    public boolean openDevice() {
        Log.d(TAG, "openDevice");
        return mDriver.openDevice(mDeviceAddress);
    }

    public void closeDevice() {
        Log.d(TAG, "closeDevice");
        mDriver.closeDevice();

        mLastUpdateTime = 0;
        mDeviceStatus = RoastData.STATUS_UNKNOWN;

        resetProfile();
    }

    public boolean isOpen() {
        return mDriver.getState() == BluetoothLeDriver.STATE_OPEN;
    }

    private byte makeParity(byte[] buf, int length) {
        byte parity = 0;
        for (int i = 0; i < length; i++) {
            parity = (byte) (parity ^ buf[i]);
        }
        return parity;
    }

    private boolean writeData(byte cmd, byte[] data) {
        byte[] buf = new byte[64];
        int index = 0;
        buf[index++] = '@';
        buf[index++] = (byte) (data.length + 2);
        buf[index++] = cmd;
        for (byte b : data) {
            buf[index++] = b;
        }
        buf[index++] = makeParity(buf, index);
        buf[index++] = '$';

        Log.d(TAG, "[WriteData] " + toHexString(buf, index));
        int retval = mDriver.WriteData(buf, index);
        return retval >= 0;
    }

    public void readyProfile(RoastProfile profile) {
        Log.d(TAG, "readyProfile");
        this.mProfile = profile;

        BackgroundService.startService(mContext);
        mHandler.removeCallbacks(mTimeoutCheckRunnable);
        mHandler.postDelayed(mTimeoutCheckRunnable, 60 * 1000);//timeout for 1min
    }

    public void resetProfile() {
        Log.d(TAG, "resetProfile");
        mProfile = null;
        mEventBus.post(new ProfileStatusEvent());

        BackgroundService.stopService(mContext);
        mHandler.removeCallbacks(mTimeoutCheckRunnable);
    }

    public void startRoast(int seconds, int fire) {
        Log.d(TAG, "startRoast " + seconds + " " + fire);
        byte[] data = new byte[3];
        data[0] = (byte) ((seconds >> 8) & 0xFF);
        data[1] = (byte) (seconds & 0xFF);
        data[2] = (byte) (fire & 0xFF);
        writeData(CMD_ROAST, data);
    }

    public void setRoast(int seconds, int fire) {
        Log.d(TAG, "setRoast " + seconds + " " + fire);
        byte[] data = new byte[3];
        data[0] = (byte) ((seconds >> 8) & 0xFF);
        data[1] = (byte) (seconds & 0xFF);
        data[2] = (byte) (fire & 0xFF);
        writeData(CMD_SET, data);
    }

    public void stopRoast() {
        Log.d(TAG, "stopRoast");
        writeData(CMD_STOP, new byte[]{});
    }

    @Override
    public void onStateChanged(int state) {
        String s = null;
        switch (state) {
            case BluetoothLeDriver.STATE_OPEN:
                s = "OPEN";
                break;
            case BluetoothLeDriver.STATE_OPENING:
                s = "OPENING";
                break;
            case BluetoothLeDriver.STATE_CLOSE:
                s = "CLOSE";
                break;
            case BluetoothLeDriver.STATE_CLOSING:
                s = "CLOSING";
                break;
        }
        Log.d(TAG, "onStateChanged " + s);
        mEventBus.post(new StateChangeEvent(state));
    }

    @Override
    public void onError(int error) {
        Log.d(TAG, "onError " + error);
        mEventBus.post(new ErrorEvent(error));

        closeDevice();
    }

    @Override
    public void onReadData(byte[] buf, int length) {
        String recv = toHexString(buf, length);
        Log.d(TAG, "[ReadData] " + recv);
        try {
            onReadCommand(buf, length);
        } catch (Exception e) {
            Log.e(TAG, "onReadCommand fail.", e);
        }
    }

    public void onReadCommand(byte[] buf, int length) {
        if (length < 5) {
            Log.e(TAG, "[onReadData] length too short.");
            return;
        }
        if (buf[0] != '@' || buf[length - 1] != '$') {
            Log.e(TAG, "[onReadData] wrong tags.");
            return;
        }
        if (buf[1] != length - 3) {
            Log.e(TAG, "[onReadData] wrong frame length.");
            return;
        }
        byte parity = makeParity(buf, length - 2);
        if (buf[length - 2] != parity) {
            Log.e(TAG, "[onReadData] wrong parity.");
            return;
        }

        byte cmd = buf[2];
        final byte[] data = Arrays.copyOfRange(buf, 3, length - 2);
        switch (cmd) {
            // 数据帧
            case CMD_STATUS: {
                final byte status = data[0];
                final int time = (data[1] & 0xFF) << 8 + data[2];
                final byte fire = data[3];
                final byte temp = data[4];

                mHandler.removeCallbacks(mTimeoutCheckRunnable);
                mLastUpdateTime = System.currentTimeMillis();
                mDeviceStatus = status;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                if (mProfile == null) {
                                 Log.w(TAG, "Add roast data to a null profile.");
                                } else {
                                    if (!mProfile.isComplete() && status == RoastData.STATUS_IDLE) {
                                        mProfile.setComplete(true);
                                        resetProfile();
                                        return;
                                    }

                                    final RoastData data = realm.createObject(RoastData.class);
                                    data.setStatus(RoastData.STATUS_PREHEATING);
                                    data.setTime(time);
                                    data.setFire(fire);
                                    data.setTemperature(temp);
                                    mProfile.plotDatas.add(data);
                                }
                            }
                        });
                    }
                });
                break;
            }
            default: {
                Log.e(TAG, "Unhandled frame.");
                break;
            }
        }
    }

    /**
     * 将byte[]数组转化为String类型
     *
     * @param arg    需要转换的byte[]数组
     * @param length 需要转换的数组长度
     * @return 转换后的String队形
     */
    public static String toHexString(byte[] arg, int length) {
        String result = new String();
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }

    /**
     * 将String转化为byte[]数组
     *
     * @param arg 需要转换的String对象
     * @return 转换后的byte[]数组
     */
    public static byte[] toByteArray(String arg) {
        if (arg != null) {
            /* 1.先去除String中的' '，然后将String转换为char数组 */
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            /* 将char数组中的值转成一个实际的十进制数组 */
            int EvenLength = (length % 2 == 0) ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                for (int i = 0; i < length; i++) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - '0';
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 'a' + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 'A' + 10;
                    }
                }
                /* 将 每个char的值每两个组成一个16进制数据 */
                byte[] byteArray = new byte[EvenLength / 2];
                for (int i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }
                return byteArray;
            }
        }
        return new byte[]{};
    }

}
