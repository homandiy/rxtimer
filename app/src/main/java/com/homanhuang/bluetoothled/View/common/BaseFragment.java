package com.homanhuang.bluetoothled.View.common;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {

    // the root view
    protected View rootView;

    /**
     * navigation presenter instance
     * declared in base for easier access
     */
    protected BasePresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        rootView = inflater.inflate(getLayout(), container, false);
        return rootView;
    }

    protected abstract int getLayout();

    /**
     * set the navigation presenter instance
     * @param presenter
     */
    public void attachPresenter(BasePresenter presenter) {
        this.presenter = presenter;
    }
}
