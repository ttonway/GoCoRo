package com.wcare.android.gocoro.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.wcare.android.gocoro.R;


/**
 * Created with IntelliJ IDEA.
 * User: ttonway
 * Date: 14-5-26
 * Time: 下午1:31
 */
public class ProgressDialog extends DialogFragment {
    private static final String KEY_MESSAGE = "ProgressDialog:msg";

    public static ProgressDialog show(AppCompatActivity activity, String message) {
        ProgressDialog frag = new ProgressDialog();
        Bundle b = new Bundle();
        b.putString(KEY_MESSAGE, message);
        frag.setArguments(b);
        frag.show(activity.getSupportFragmentManager(), "progress");
        return frag;
    }

    public static ProgressDialog show(AppCompatActivity activity) {
        return show(activity, activity.getString(R.string.waiting_progress));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        android.app.ProgressDialog dialog = new android.app.ProgressDialog(getActivity());
        dialog.setMessage(getArguments().getString(KEY_MESSAGE));
        return dialog;
    }
}
