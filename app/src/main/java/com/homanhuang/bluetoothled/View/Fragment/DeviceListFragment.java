package com.homanhuang.bluetoothled.View.Fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.homanhuang.bluetoothled.Model.Bluetooth.DeviceData;
import com.homanhuang.bluetoothled.R;
import com.homanhuang.bluetoothled.View.common.BaseFragment;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnDeviceSelected}
 * interface.
 */
public class DeviceListFragment extends BaseFragment {

    @Override
    protected int getLayout() {
        return R.layout.fragment_device_list;
    }

    /* Log tag and shortcut */
    final static String TAG = "MYLOG Ble List";
    public static void ltag(String message) { Log.i(TAG, message); }

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mDeviceCount = 1;
    private OnDeviceSelected mDeviceSelected;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private TextView devTitleTV;

    // Recyclerview vars
    private RecyclerView rvDeviceList;
    private MydeviceRecyclerViewAdapter rvAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DeviceListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DeviceListFragment newInstance(int columnCount) {
        DeviceListFragment fragment = new DeviceListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mDeviceCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_device_list,
                container, false);

        // Get data
        Bundle deviceBundle = this.getArguments();

        if (deviceBundle != null) {
            String title = deviceBundle.getString("title");

            if (title.equals("paired")) {
                devTitleTV.setText(R.string.paired_list_title);
            } else if (title.equals("nearby")) {
                devTitleTV.setText(R.string.nearby_list);
            }

            deviceList = (ArrayList<BluetoothDevice>) deviceBundle
                    .getSerializable("deviceList");
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ltag("onViewCreated...");

        devTitleTV = (TextView)
                view.findViewById(R.id.devTitleTV);

        // Setup Recyclerview
        rvDeviceList = view.findViewById(R.id.deviceListRV);
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getContext());
        rvDeviceList.setLayoutManager(mLayoutManager);
        rvAdapter = new MydeviceRecyclerViewAdapter(
                deviceList, mDeviceSelected);
        rvDeviceList.setAdapter(rvAdapter);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDeviceSelected) {
            mDeviceSelected = (OnDeviceSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDeviceSelected");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDeviceSelected = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDeviceSelected {
        // TODO: Update argument type and name
        void onListDeviceSelected(int position);
    }
}
