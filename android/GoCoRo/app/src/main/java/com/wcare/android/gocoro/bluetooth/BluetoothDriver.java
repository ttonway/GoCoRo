/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.wcare.android.gocoro.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import com.splunk.mint.Mint;
import com.wcare.android.gocoro.core.DriverCallback;
import com.wcare.android.gocoro.core.GoCoRoDriver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothDriver extends GoCoRoDriver {
    private static final String TAG = BluetoothDriver.class.getSimpleName();

    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothConn";

    // Unique UUID for this application
    private static final UUID CUSTOM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private String mBluetoothDeviceAddress;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = STATE_CLOSE;       // we're doing nothing
    public static final int STATE_LISTEN = 4;     // now listening for incoming connections
    public static final int STATE_CONNECTING = STATE_OPENING; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = STATE_OPEN;  // now connected to a remote device

    public BluetoothDriver(Context context, DriverCallback callback) {
        super(context, callback);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    @Override
    public String isDriverSupported() {
        return null;
    }

    @Override
    public boolean openDevice(String deviceAddress) {
        Log.d(TAG, "openDevice " + deviceAddress);

        if (deviceAddress == null) {
            Log.w(TAG, "device address is null.");
            return false;
        }

        synchronized (this) {
            if (!TextUtils.equals(deviceAddress, mBluetoothDeviceAddress) && mState != STATE_NONE) {
                closeDevice();
            }

            if (mState == STATE_CONNECTED) {
                Log.w(TAG, "Already opened.");
                return true;
            }
            if (mState == STATE_CONNECTING) {
                Log.w(TAG, "Now openeding.");
                return true;
            }

            // Cancel any thread currently running a connection
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            BluetoothDevice device = mAdapter.getRemoteDevice(deviceAddress);
            if (device == null) {
                Log.e(TAG, "device unavailable");
                return false;
            }

            // Start the thread to connect with the given device
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
            mBluetoothDeviceAddress = deviceAddress;

            setState(STATE_CONNECTING);
        }
        return true;
    }

    @Override
    public boolean closeDevice() {
        Log.d(TAG, "closeDevice");

        synchronized (this) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            if (mAcceptThread != null) {
                mAcceptThread.cancel();
                mAcceptThread = null;
            }

            mBluetoothDeviceAddress = null;
        }

        setState(STATE_NONE);
        return true;
    }

    @Override
    public int WriteData(byte[] bytes, int length) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return -1;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        boolean success = r.write(Arrays.copyOf(bytes, length));

        return success ? length : -1;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {
        Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        mCallback.onError(ERROR_CONNECTION_FAIL);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        mCallback.onError(ERROR_CONNECTION_FAIL);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                if (DeviceDependency.shouldUseSecure()) {
                    mSocketType = "Secure";
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, CUSTOM_UUID);
                } else {
                    mSocketType = "Insecure";
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, CUSTOM_UUID);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothDriver.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (DeviceDependency.shouldUseFixChannel()) {
                    Method m;
                    try {
                        m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
                        tmp = (BluetoothSocket) m.invoke(device, 6);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Mint.logException(e);
                    }
                }
                //issc
                else if (DeviceDependency.shouldUseSecure()) {
                    tmp = device.createRfcommSocketToServiceRecord(CUSTOM_UUID);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(CUSTOM_UUID);
                }

            } catch (IOException e) {
                Log.e(TAG, "create Rfcomm socket failed", e);
                Mint.logException(e);
            }
            mmSocket = tmp;
            Log.d(TAG, "Rfcomm socket: " + tmp);
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            if (mmSocket == null) {
                connectionFailed();
                return;
            }

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, "connect fail. " + e, e);
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }

                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothDriver.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                if (mmSocket != null) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mCallback.onReadData(buffer, bytes);
                } catch (IOException e) {
                    Log.e(TAG, "read failed. " + e, e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public boolean write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
                return false;
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
