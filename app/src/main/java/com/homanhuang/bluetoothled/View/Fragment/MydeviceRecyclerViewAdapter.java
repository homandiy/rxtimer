package com.homanhuang.bluetoothled.View.Fragment;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.homanhuang.bluetoothled.Model.Bluetooth.DeviceData;
import com.homanhuang.bluetoothled.R;
import com.homanhuang.bluetoothled.View.Fragment.DeviceListFragment.OnDeviceSelected;

import java.util.List;

/**
 *
 * TODO: Replace the implementation with code for your data type.
 */
public class MydeviceRecyclerViewAdapter extends RecyclerView.Adapter<MydeviceRecyclerViewAdapter.ViewHolder> {

    private List<BluetoothDevice> mDeviceList;
    private final OnDeviceSelected mListener;

    /* Log tag and shortcut */
    final static String TAG = "MYLOG RecView";
    public static void ltag(String message) { Log.i(TAG, message); }

    public MydeviceRecyclerViewAdapter(List<BluetoothDevice> items,
                                       OnDeviceSelected listener) {
        mDeviceList = items;
        mListener = listener;

        ltag("initial");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.deviceData = mDeviceList.get(position);
        holder.nameTV.setText(holder.deviceData.getName());
        holder.macTV.setText(holder.deviceData.getAddress());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListDeviceSelected(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView nameTV;
        public final TextView macTV;
        public BluetoothDevice deviceData;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            nameTV = (TextView) view.findViewById(R.id.deviceNameTV);
            macTV = (TextView) view.findViewById(R.id.deviceMacTV);
        }

        @Override
        public String toString() {
            return "Bluetooth Name: " + deviceData.getName() + ", MAC: " + deviceData.getAddress();
        }
    }
}
