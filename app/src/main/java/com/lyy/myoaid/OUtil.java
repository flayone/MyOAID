//package com.lyy.myoaid;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.text.TextUtils;
//
//public class OUtil {
//    public static final String SP_OAID = "meryOaid";
//
//
//    public static void saveOAID(Context context, String oaid) {
//        try {
//            if (TextUtils.isEmpty(oaid)) {
//                ADLog.e("oaid为空,未获取到oaid");
//                return;
//            }
//            OAIDManger.getInstance().setOaId(oaid);
//            saveString(context, SP_OAID, oaid);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//    private static String NAME = "oaid_setting_sp";
//
//    public static void saveString(Context context, String key, String value) {
//        try {
//            context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putString(key, value).commit();
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static String getSavedString(Context context, String key) {
//        try {
//            SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
//            return sp.getString(key, "");
//        } catch (Throwable e) {
//            e.printStackTrace();
//            return "";
//        }
//    }
//}
