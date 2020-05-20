package com.mobilegenomics.genopo.support;

import android.os.Handler;

import com.mobilegenomics.genopo.activity.MinITActivity;

import java.lang.ref.WeakReference;

public class LogHandler extends Handler {
    private WeakReference<MinITActivity> mActivity;

    public LogHandler(MinITActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public WeakReference<MinITActivity> getmActivity() {
        return mActivity;
    }
}
