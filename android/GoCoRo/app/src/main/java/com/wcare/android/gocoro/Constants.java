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

package com.wcare.android.gocoro;

import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class Constants {
    public static String GOCORO_SERVICE = "49535343-fe7d-4ae5-8fa9-9fafd205e455";

    public static String NOTIFY_CHARACTERISTIC = "49535343-1E4D-4BD9-BA61-23C647249616";
    public static String WRITE_CHARACTERISTIC = "49535343-8841-43F4-A8D4-ECBE34729BB3";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static UUID GOCORO_SERVICE_UUID = UUID.fromString(GOCORO_SERVICE);
    public static UUID[] GOCORO_SERVICE_UUIDS = new UUID[]{GOCORO_SERVICE_UUID};

    public static UUID NOTIFY_CHARACTERISTIC_UUID = UUID.fromString(NOTIFY_CHARACTERISTIC);
    public static UUID WRITE_CHARACTERISTIC_UUID = UUID.fromString(WRITE_CHARACTERISTIC);
}
