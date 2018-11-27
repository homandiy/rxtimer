package com.homanhuang.bluetoothled.Model.Bluetooth;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class BleData implements Serializable {

    private List<UUID> mUuids;
    private String mName;

    public BleData(List<UUID> mUuids, String mName) {
        this.mUuids = mUuids;
        this.mName = mName;
    }

    public List<UUID> getmUuids() {
        return mUuids;
    }

    public String getmName() {
        return mName;
    }

    @Override
    public String toString() {
        if (mName.equals("")) {
            mName = "Unknown";
        }

        return "Name: "+mName+"; UUID: "+ mUuids.toString();
    }
}
