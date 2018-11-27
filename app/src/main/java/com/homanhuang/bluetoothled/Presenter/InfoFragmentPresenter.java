package com.homanhuang.bluetoothled.Presenter;

import android.os.Bundle;
import com.homanhuang.bluetoothled.View.Fragment.InfoFragment;
import com.homanhuang.bluetoothled.ViewPresenterHelper.MainActivityVP;

// Get data from Bluetooth and present to InfoFragment
public class InfoFragmentPresenter {

    MainActivityVP.View view;

    public InfoFragmentPresenter(MainActivityVP.View view) {
        this.view = view;
    }

    // Send data to View
    public void showMsg(String info) {

        InfoFragment infoFragment = new InfoFragment();
        Bundle infoBundle = new Bundle();
        infoBundle.putString("info", info );
        infoFragment.setArguments(infoBundle);

        view.setFragment(infoFragment);
    }
}


