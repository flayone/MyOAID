package com.lyy.myoaid;

import android.app.Application;

import androidx.annotation.NonNull;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //获取Android10 设备的oaid
        new OAIDHelper(new OAIDHelper.OaidUpdater() {
            @Override
            public void IdReceived(@NonNull String id) {
                OUtil.saveOAID(MyApplication.this, id);
            }
        }).getDeviceIds(MyApplication.this);
    }
}
