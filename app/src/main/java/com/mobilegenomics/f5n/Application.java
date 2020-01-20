package com.mobilegenomics.f5n;

import android.content.Context;

public class Application extends android.app.Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Application.mContext = this;
    }

    public static Context getAppContext() {
        return Application.mContext;
    }

}
