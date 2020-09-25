package com.lyy.myoaid;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final int PERMISSION_REQUEST_CODE = 1024;

    TextView osvT;
    TextView makeT;
    TextView modelT;
    TextView imeiT;
    TextView imsiT;
    TextView oaidT;
    TextView ipT;
    TextView macT;

    Button copy;
    TextView refresh;

    String infDetail = "";
    Context app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        osvT = findViewById(R.id.osv);
        makeT = findViewById(R.id.make);
        modelT = findViewById(R.id.model);
        imeiT = findViewById(R.id.imei);
        imsiT = findViewById(R.id.imsi);
        oaidT = findViewById(R.id.oaid);
        ipT = findViewById(R.id.ip);
        macT = findViewById(R.id.mac);

        copy = findViewById(R.id.copy);
        refresh = findViewById(R.id.refresh);

        app = this.getApplicationContext();

        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29) {
            checkAndRequestPermission();
        } else {
            getInf();
        }


        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyInf(infDetail);
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInf();
                Toast.makeText(MainActivity.this, "数据已更新", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void copyInf(String cc) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
// 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", cc);
// 将ClipData内容放到系统剪贴板里。
        assert cm != null;
        cm.setPrimaryClip(mClipData);
        Toast.makeText(this, "已成功复制到剪切板", Toast.LENGTH_LONG).show();
    }

    private void getInf() {
        JSONObject jsonObject = new JSONObject();

        try {
            String insOaid = OAIDManger.getInstance().getOaId();
            String savedOaid = OUtil.getSavedString(this, OUtil.SP_OAID);
            String oaid;
            if (TextUtils.isEmpty(insOaid)) {
                oaid = savedOaid;
            } else {
                oaid = insOaid;
            }
            oaidT.setText("OAID ：" + oaid);

            String model = Build.MODEL;
            modelT.setText("设备型号：" + model);
            jsonObject.put("model", model);
            String make = Build.MANUFACTURER;
            makeT.setText("制造商： " + make);
            jsonObject.put("make", make);
            String osv = Build.VERSION.RELEASE;
            jsonObject.put("osv", osv);
            osvT.setText("Android版本：" + osv);

            String ip = getIP();
            jsonObject.put("ip", ip);
            ipT.setText("IP地址   ：" + ip);

            String ua = getUserAgent(app);
            jsonObject.put("ua", ua);
            jsonObject.put("oaid", oaid);
            String imei = getPhoneIMEI();
            jsonObject.put("imei", imei);
            imeiT.setText("IMEI:     " + imei);
            String imsi = getIMSI();
            jsonObject.put("imsi", imsi);
            imsiT.setText("IMSI:     " + imsi);
            String mac = getMacAddress();
            jsonObject.put("mac", mac);
            macT.setText("mac地址:  " + mac);

            Integer sw = app.getResources()
                    .getDisplayMetrics().widthPixels;
            // 屏幕高度(px)
            Integer sh = this.getApplicationContext().getResources()
                    .getDisplayMetrics().heightPixels;
            Integer ppi = app.getResources()
                    .getDisplayMetrics().densityDpi;
            jsonObject.put("sw", sw);
            jsonObject.put("sh", sh);
            jsonObject.put("ppi", ppi);
            String carrier = getCarrier();
            jsonObject.put("carrier", carrier);
            Integer network = getNetwork();
            jsonObject.put("network", network);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        infDetail = jsonObject.toString();
        ADLog.d("infDetail == " + infDetail);
    }


    @TargetApi(Build.VERSION_CODES.M)
    void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> lackedPermission = new ArrayList<String>();
            if (!(this.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
            }
//
//            if (!(this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
//                lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            }

            // 权限都已经有了，那么直接调用SDK
            if (lackedPermission.size() == 0) {
                getInf();
            } else {
                // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用SDK，否则不要调用SDK。
                String[] requestPermissions = new String[lackedPermission.size()];
                lackedPermission.toArray(requestPermissions);
                this.requestPermissions(requestPermissions, PERMISSION_REQUEST_CODE);
            }
        }
    }


    private String getCarrier() {
        try {
            TelephonyManager telManager = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
            return telManager.getSimOperator();
        } catch (Throwable e) {
            return "";
        }

    }

    public static String getUserAgent(Context context) {
        try {
            String userAgent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                try {
                    userAgent = WebSettings.getDefaultUserAgent(context);
                } catch (Throwable e) {
                    userAgent = System.getProperty("http.agent");
                }
            } else {
                userAgent = System.getProperty("http.agent");
            }
            if (userAgent == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0, length = userAgent.length(); i < length; i++) {
                char c = userAgent.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } catch (Throwable e) {
            return "";
        }
    }

    public String getPhoneIMEI() {
        try {
            //vivo和OPPO 使用getDeviceId不会返回真实的值，需要用getImeiNew方法通过反射来获取。
            if (RomUtils.isVivo() || RomUtils.isOppo()) {
                return getImeiNew(app);
            }
            try {
                TelephonyManager mTm = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
                return mTm.getDeviceId();
            } catch (Throwable e) {
                e.printStackTrace();
                return "";
            }
        } catch (Throwable e) {
            return "";
        }
    }


    private static String getImeiNew(Context context) {
        String imei = "";
        try {
            if (context != null) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        try {
                            Method method = tm.getClass().getMethod("getImei", new Class[0]);
                            method.setAccessible(true);
                            imei = (String) method.invoke(tm, new Object[0]);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                        if (TextUtils.isEmpty(imei)) {
                            imei = tm.getDeviceId();
                        }
                    } else {
                        imei = tm.getDeviceId();
                    }
                }
            }
        } catch (Throwable ee) {
            ee.printStackTrace();
        }
        return imei;
    }

    /**
     * 获取wifi下内网ip、移动网络下ip地址
     *
     * @return ip
     */
    public String getIP() {
        try {
            NetworkInfo info = ((ConnectivityManager) app
                    .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                    WifiManager wifiManager = (WifiManager) app.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //调用方法将int转换为地址字符串
                    return intIP2StringIP(wifiInfo.getIpAddress());
                }
            }
        } catch (Throwable e) {
            return "";
        }
        return "";
    }


    private String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    public String getIMSI() {
        try {
            TelephonyManager mTm = (TelephonyManager) app.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            return mTm.getSubscriberId();
        } catch (Throwable e) {
            return "";

        }
    }

    public String getMacAddress() {
        /*获取mac地址有一点需要注意的就是android 6.0版本后，以下注释方法不再适用，不管任何手机都会返回"02:00:00:00:00:00"这个默认的mac地址，这是googel官方为了加强权限管理而禁用了getSYstemService(Context.WIFI_SERVICE)方法来获得mac地址。*/
        //        String macAddress= "";
//        WifiManager wifiManager = (WifiManager) MyApp.getContext().getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        macAddress = wifiInfo.getMacAddress();
//        return macAddress;
        try {
            String macAddress;
            StringBuffer buf = new StringBuffer();
            NetworkInterface networkInterface;
            networkInterface = NetworkInterface.getByName("eth1");
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0");
            }
            if (networkInterface == null) {
                return "02:00:00:00:00:02";
            }
            byte[] addr = networkInterface.getHardwareAddress();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            macAddress = buf.toString();
            return macAddress;
        } catch (Throwable e) {
            return "";
        }
    }


    private Integer getNetwork() {
        try {
            int network = 0;
            ConnectivityManager connectivity = (ConnectivityManager) app
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return network;
            }

            NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    network = 1;
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    String _strSubTypeName = networkInfo.getSubtypeName();

                    // TD-SCDMA   networkType is 17
                    int networkType = networkInfo.getSubtype();
                    switch (networkType) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                            network = 2;
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                        case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                        case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                            network = 3;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                            network = 4;
                            break;
                        case TelephonyManager.NETWORK_TYPE_NR:    //api<11 : replace by 13
                            network = 5;
                            break;
                        default:
                            //中国移动 联通 电信 三种3G制式
                            if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                network = 3;
                            } else {
                                network = 0;
                            }
                            break;
                    }
                }
            }
            return network;
        } catch (Throwable e) {
            return 0;
        }

    }
}
