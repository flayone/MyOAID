package com.lyy.myoaid;

import android.util.Log;


public class ADLog {
//    public static String SDK_LOG_TAG = ADSetting.getInstance().debugTag;

    public static boolean isDebug = true;
//    public static boolean isDev = ADSetting.getInstance().isDev();

    public static String getTag() {
        return "myoaid";
    }

    public static void d(String tag, String s) {
        if (isDebug) {
            Log.d(tag, s);
        }
    }

    public static void d(String s) {
        if (isDebug) {
            Log.d(getTag(), s);
        }
    }

    //dev-debug 只有开发者可以看到的log
    public static void dd(String tag, String s) {
        if (isDebug) {
            Log.d(tag, s);
        }
    }

    //dev-debug 只有开发者可以看到的log
    public static void dd(String s) {
        if (isDebug) {
            Log.d(getTag(), s);
        }
    }


    public static void i(String msg) {
        Log.i(getTag(), msg);
    }

    public static void e(String msg) {
        Log.e(getTag(), msg);
    }


    public static void e(String msg, Throwable e) {
        if (e == null) {
            e(msg);
        } else {
            Log.e(getTag(), msg, e);
        }
    }

    public static void w(String msg, Throwable e) {
        if (e == null) {
            w(msg);
        } else {
            Log.w(getTag(), msg, e);
        }
    }

    public static void w(String msg) {
        Log.w(getTag(), msg);
    }

}
