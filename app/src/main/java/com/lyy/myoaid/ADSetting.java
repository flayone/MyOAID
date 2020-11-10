package com.lyy.myoaid;

public class ADSetting {
    private static ADSetting instance;

    public static synchronized ADSetting getInstance() {
        if (instance == null) {
            instance = new ADSetting();
        }
        return instance;
    }


    private String ip;


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


}
