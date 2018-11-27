package com.homanhuang.bluetoothled.Presenter;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import com.homanhuang.bluetoothled.Model.Bluetooth.DeviceData;
import com.homanhuang.bluetoothled.View.Fragment.DeviceListFragment;
import com.homanhuang.bluetoothled.ViewPresenterHelper.MainActivityVP;

import java.util.ArrayList;

// Get data from Bluetooth and present to InfoFragment
public class DeviceListPresenter {

    MainActivityVP.View view;

    public DeviceListPresenter(MainActivityVP.View view) {
        this.view = view;
    }

    // Send data to View
    public void showList(ArrayList<BluetoothDevice> list,
                         String title) {
        Bundle listBundle = new Bundle();
        listBundle.putString("title", title);
        listBundle.putSerializable("deviceList", list);

        DeviceListFragment deviceListFragment =
                new DeviceListFragment();
        deviceListFragment.setArguments(listBundle);

        view.setFragment(deviceListFragment);
    }
}


