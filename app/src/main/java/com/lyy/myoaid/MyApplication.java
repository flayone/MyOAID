package com.lyy.myoaid;

import android.app.Application;

import com.flayone.oaid.MyOAID;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();


        //MSA 方式获取Android10 设备的oaid
//        new OAIDHelper(new OAIDHelper.OaidUpdater() {
//            @Override
//            public void IdReceived(@NonNull String id) {
//                OUtil.saveOAID(MyApplication.this, id);
//            }
//        }).getDeviceIds(MyApplication.this);

    }
}
