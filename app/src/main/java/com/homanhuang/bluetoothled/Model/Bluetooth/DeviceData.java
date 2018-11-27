package com.homanhuang.bluetoothled.Model.Bluetooth;

import java.io.Serializable;

public class DeviceData implements Serializable {

    private String mName;
    private String mMac;

    public DeviceData(String mName, String mMac) {
        this.mName = mName;
        this.mMac = mMac;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmMac() {
        return mMac;
    }

    public void setmMac(String mMac) {
        this.mMac = mMac;
    }

    @Override
    public String toString() {

        if (mName.equals("")) {
            mName = "Unknown";
        }

        return "Name: "+mName+"; MAC: "+mMac;
    }
}