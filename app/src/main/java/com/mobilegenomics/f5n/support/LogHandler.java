package com.mobilegenomics.f5n.support;

import android.os.Handler;

import com.mobilegenomics.f5n.activity.MinITActivity;

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
