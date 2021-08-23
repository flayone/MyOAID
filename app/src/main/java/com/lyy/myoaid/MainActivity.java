package com.lyy.myoaid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.flayone.oaid.MyOAID;
import com.flayone.oaid.ResultCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final int PERMISSION_REQUEST_CODE = 1024;

    TextView osvT;
    TextView makeT;
    TextView modelT;
    TextView imeiT;
    TextView imsiT;
    TextView oaidT;
    TextView aidT;
    TextView ipT;
    TextView macT;
    TextView uaT;

    Button copy;
    TextView refresh;

    Context app;
    JSONObject jsonObject;
    String deviceInfResult = "";
    List<String> lackedPermission = new ArrayList<>();

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
        aidT = findViewById(R.id.aid);
        ipT = findViewById(R.id.ip);
        macT = findViewById(R.id.mac);
        uaT = findViewById(R.id.ua);

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


                deviceInfResult = osvT.getText() +
                        "\n" +
                        makeT.getText() +
                        "\n" +
                        modelT.getText() +
                        "\n" +
                        imeiT.getText() +
                        "\n" +
                        oaidT.getText() +
                        "\n" +
                        aidT.getText() +
                        "\n" +
                        ipT.getText() +
                        "\n" +
                        macT.getText() +
                        "\n" +
                        uaT.getText();
                copyInf(deviceInfResult);
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
        jsonObject = new JSONObject();

        try {
            //必须，初始化获取oaid程序
            MyOAID.init(this, new ResultCallBack() {
                @Override
                public void onResult(String oaid) {
//                    因为部分平台结果为异步返回，可以选择在ResultCallBack回调中第一时间更新该值
                    oaidT.setText("OAID ：" + oaid);
                }
            });

            //快速获取oaid方式，本地长久缓存方式，不用担心异步问题
            String oaid = MyOAID.getOAID(this);
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

            String ip = getOutIP();
            jsonObject.put("ip", ip);
            ipT.setText("IP地址   ：" + ip);

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

            String aid = getAndroidId();
            jsonObject.put("androidid", aid);
            aidT.setText("Android ID:   " + getAndroidId());

            String ua = getUserAgent(app);
            uaT.setText("UA     ：" + ua);
            jsonObject.put("ua", ua);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ADLog.d("jsonObject.toString() == " + jsonObject.toString());
    }


    @TargetApi(Build.VERSION_CODES.M)
    void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLackedPermission();

            // 权限都已经有了
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //申请权限结果返回
        if (requestCode == PERMISSION_REQUEST_CODE) {
            checkLackedPermission();
            // 权限都已经有了
            if (lackedPermission.size() == 0) {
                getInf();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkLackedPermission() {
        lackedPermission = new ArrayList<>();
        if (!(this.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
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

    public String getAndroidId() {
        try {
            return Settings.System.getString(app.getContentResolver(), Settings.System.ANDROID_ID);
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

    /**
     * 可以返回wifi下外网ip的方法,服务端不需要外网ip，所以可以不用
     *
     * @return ip
     */
    public String getOutIP() {
        try {
            String ip = "";
            ip = ADSetting.getInstance().getIp();

//            ADSetting.getInstance().getIp();
//            if (!TextUtils.isEmpty(ip)) {
//                return ip;
//            }

//            NetworkInfo info = ((ConnectivityManager) app
//                    .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
//            if (info != null && info.isConnected()) {
//                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
//                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
//                        NetworkInterface intf = en.nextElement();
//                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                            InetAddress inetAddress = enumIpAddr.nextElement();
//                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
//                                ip = inetAddress.getHostAddress();
//                            }
//                        }
//                    }
//                } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
//
//                    WifiManager wifiManager = (WifiManager) app.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//                    //调用方法将int转换为地址字符串
//                    ip = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
//                }
            //ip地址获取不到的话，则通过网络请求ip；不为空则保存在单例中
            if (TextUtils.isEmpty(ip)) {
                GetNetIp();
            } else {
                ADSetting.getInstance().setIp(ip);
            }
//            }
            return ip;
        } catch (Throwable e) {
            return "";
        }
    }


    /**
     * 获取外网IP地址
     *
     * @return
     */
    private void GetNetIp() {
        try {
            new Thread() {
                @Override
                public void run() {
                    String line;
                    URL infoUrl;
                    InputStream inStream;
                    try {
                        infoUrl = new URL("https://pv.sohu.com/cityjson?ie=utf-8");
                        URLConnection connection = infoUrl.openConnection();
                        HttpURLConnection httpConnection = (HttpURLConnection) connection;
                        int responseCode = httpConnection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            inStream = httpConnection.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                            StringBuilder strber = new StringBuilder();
                            while ((line = reader.readLine()) != null)
                                strber.append(line).append("\n");
                            inStream.close();
                            // 从反馈的结果中提取出IP地址
                            int start = strber.indexOf("{");
                            int end = strber.indexOf("}");
                            String json = strber.substring(start, end + 1);
                            if (json != null) {
                                try {
                                    JSONObject jsonObjectIP = new JSONObject(json);
                                    final String ip = jsonObjectIP.optString("cip");
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                jsonObject.put("ip", ip);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            ipT.setText("IP地址   ：" + ip);

                                        }
                                    });

                                    ADSetting.getInstance().setIp(line);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
