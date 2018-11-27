package com.homanhuang.bluetoothled.ViewPresenterHelper;

import com.homanhuang.bluetoothled.View.common.BaseFragment;

public interface MainActivityVP {
    interface View{
        void setFragment(BaseFragment fragment);
    }

    // Handle broadcast receiver
    interface Presenter {
        void addFragment(BaseFragment fragment);
    }
}