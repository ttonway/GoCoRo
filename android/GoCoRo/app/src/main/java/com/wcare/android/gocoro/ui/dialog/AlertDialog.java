package com.wcare.android.gocoro.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.wcare.android.gocoro.R;

/**
 * Created with IntelliJ IDEA.
 * User: ttonway
 * Date: 14-5-26
 * Time: 下午1:31
 */
public class AlertDialog extends DialogFragment {
    private static final String KEY_MESSAGE = "AlertDialog:msg";

    public static AlertDialog newInstance(String message) {
        AlertDialog frag = new AlertDialog();
        Bundle b = new Bundle();
        b.putString(KEY_MESSAGE, message);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new android.app.AlertDialog.Builder(getActivity())
                .setMessage(getArguments().getString(KEY_MESSAGE))
                .setPositiveButton(R.string.btn_ok, null)
                .create();
    }
}
