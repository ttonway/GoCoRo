package com.wcare.android.gocoro.ui;


import android.support.v4.app.Fragment;

/**
 * Created by ttonway on 2016/12/12.
 */
public class BaseFragment extends Fragment {

    final public BaseActivity getBaseActivity() {
        return (BaseActivity)getActivity();
    }
}
