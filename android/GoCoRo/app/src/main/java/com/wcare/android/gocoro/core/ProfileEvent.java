package com.wcare.android.gocoro.core;

import com.wcare.android.gocoro.model.RoastProfile;

/**
 * Created by ttonway on 2016/11/11.
 */
public class ProfileEvent {
    public static final int TYPE_PROFILE_RESET = 1;
    public static final int TYPE_PROFILE_CONTINUE = 2;

    public final int type;
    public final String profileUid;

    public ProfileEvent(int type, String uid) {
        this.type = type;
        this.profileUid = uid;
    }
}
