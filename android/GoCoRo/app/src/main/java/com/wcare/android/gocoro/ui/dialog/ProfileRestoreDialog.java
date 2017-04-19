package com.wcare.android.gocoro.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.model.RoastProfile;
import com.wcare.android.gocoro.ui.ActivityPlot;
import com.wcare.android.gocoro.ui.BaseActivity;

/**
 * Created by ttonway on 2016/11/3.
 */
public class ProfileRestoreDialog extends DialogFragment {
    private static final String KEY_PROFILE_UID = "ProfileContinueDialog:uid";


    public static ProfileRestoreDialog newInstance(String profileUid) {
        ProfileRestoreDialog frag = new ProfileRestoreDialog();
        Bundle b = new Bundle();
        b.putString(KEY_PROFILE_UID, profileUid);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.continue_uncompleted_profile)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BaseActivity activity = (BaseActivity) getActivity();
                        String uid = getArguments().getString(KEY_PROFILE_UID);

                        if (activity instanceof ActivityPlot) {
                            ActivityPlot plotAct = (ActivityPlot)activity;
                            RoastProfile profile = plotAct.getProfile();
                            if (profile != null && TextUtils.equals(profile.getUuid(), uid)) {
                                plotAct.restoreRoast();
                                return;
                            }
                        }

                        ActivityPlot.startRoast(activity, uid);
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .create();
    }
}
