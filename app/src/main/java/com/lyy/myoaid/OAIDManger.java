package com.lyy.myoaid;

public class OAIDManger {
    private static OAIDManger instance;
    private String oaId;


    public static synchronized OAIDManger getInstance() {
        if (instance == null) {
            instance = new OAIDManger();
        }
        return instance;
    }



    public String getOaId() {
        return oaId;
    }

    public void setOaId(String oaId) {
        this.oaId = oaId;
    }

}
