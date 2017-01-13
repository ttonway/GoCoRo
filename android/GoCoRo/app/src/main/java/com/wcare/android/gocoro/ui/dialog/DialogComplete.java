package com.wcare.android.gocoro.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.ui.ActivityPlot;
import com.wcare.android.gocoro.utils.Utils;

/**
 * Created by ttonway on 2016/11/3.
 */
public class DialogComplete extends DialogFragment {

    public static DialogComplete newInstance() {
        DialogComplete frag = new DialogComplete();
        return frag;
    }

    EditText mEditText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mEditText = new EditText(getActivity());
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        return new AlertDialog.Builder(getActivity())
                .setTitle("烘焙完成，请输入出豆重量")
                .setView(mEditText)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityPlot activity = (ActivityPlot) getActivity();
//                        activity.completeRoast(Utils.parseInt(mEditText.getText().toString()));
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .create();
    }
}
