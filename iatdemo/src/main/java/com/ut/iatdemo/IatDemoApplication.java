package com.ut.iatdemo;

import android.app.Application;

import com.ut.iatdemolib.util.IatUiUtils;

public class IatDemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        IatUiUtils.initXunFeiIatSdk(this);
    }
}
