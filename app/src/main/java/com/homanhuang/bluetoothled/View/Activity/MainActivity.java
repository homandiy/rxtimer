package com.homanhuang.bluetoothled.View.Activity;

import android.Manifest;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.homanhuang.bluetoothled.Model.Bluetooth.BleData;
import com.homanhuang.bluetoothled.Model.Bluetooth.BluetoothUtil;
import com.homanhuang.bluetoothled.Model.Bluetooth.DeviceData;
import com.homanhuang.bluetoothled.Presenter.DeviceListPresenter;
import com.homanhuang.bluetoothled.Presenter.InfoFragmentPresenter;
import com.homanhuang.bluetoothled.Presenter.MainActivityPresenter;
import com.homanhuang.bluetoothled.R;
import com.homanhuang.bluetoothled.View.Fragment.DeviceListFragment;
import com.homanhuang.bluetoothled.View.common.BaseFragment;
import com.homanhuang.bluetoothled.ViewPresenterHelper.MainActivityVP;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;


public class MainActivity extends AppCompatActivity implements
        DeviceListFragment.OnDeviceSelected, MainActivityVP.View {

    /* Log tag and shortcut */
    final static String TAG = "MYLOG Main";


    public static void ltag(String message) { Log.i(TAG, message); }

    /* Toast shortcut */
    public static void msg(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Variables
     */
    boolean isHc05Paired = false;

    private BluetoothAdapter mBluetoothAdapter;

    // connect to customized HC05
    private String BLUETOOTH_NAME = "Homan_Bluetooth";
    private String BLUETOOTH_PASSWORD = "3434";
    private String BLUETOOTH_ADDRESS = "98:D3:32:70:B1:C9";

    // layout variables
    TextView bleStatusTV;
    TextView pairedTV;
    Button pairedBT;
    Button unpairBT;
    Button bluetoothSwitchBT;
    ProgressDialog progressCircle;

    FrameLayout status_container;

    private FragmentTransaction ft;
    private FragmentManager fragmentManager;

    // MVP
    private MainActivityPresenter mainPresenter;
    private InfoFragmentPresenter infoFragmentPresenter;
    private DeviceListPresenter deviceListPresenter;

    /**
     * Bluetooth Variables
     */
    ArrayList<DeviceData> deviceList = new ArrayList<>();


    private long startTime = 0;
    private long endTime = 0;

    private BluetoothDevice deviceHC05;

    enum ConnectState {
        NONE, DISCOVERY, PAIRING, BONDING;
    }
    private ConnectState connectState;

    /**
     * End of Variables
     */

    //region implements Permission Requests

    static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 201; // any code you want.
    public void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                //checkSelfPermission(Manifest.permission.BLUETOOTH_PRIVILEGED) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                ltag("Permission is granted");
            } else {
                ltag("Permission is revoked");
                ltag("INTERNET Permission: "+checkSelfPermission(Manifest.permission.INTERNET) );
                ltag("BLUETOOTH Permission: "+checkSelfPermission(Manifest.permission.BLUETOOTH));
                ltag("BLUETOOTH_ADMIN Permission: "+checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN));
                //ltag("BLUETOOTH_PRIVILEGED Permission: "+checkSelfPermission(Manifest.permission.BLUETOOTH_PRIVILEGED));
                ltag("ACCESS_FINE_LOCATION Permission: "+checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION));

                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.INTERNET,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                Manifest.permission.BLUETOOTH_PRIVILEGED,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        ltag("Permission: " + permissions.toString());

        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                    msg(this, "Permissions granted!");
                    ltag("Permissions granted!");
                } else {
                    msg(this, "You don't have the permission!");
                    ltag("You don't have the permission!");
                }

                break;
        }
    }

    //endregion implements Permission Requests

    /**
     * Check Paired list
     */
    private boolean isPaired() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if ( device.getName().equals(BLUETOOTH_NAME) ) {
                    ltag("Found the device with the same name.");

                    if (device.getAddress().equals(BLUETOOTH_ADDRESS)) {
                        ltag("Found my Bluetooth device.");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean unPaired(String deviceName) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                try {
                    if (device.getName().contains(deviceName)) {
                        Method m = device.getClass()
                                .getMethod("removeBond", (Class[]) null);
                        m.invoke(device, (Object[]) null);
                        msg(this, deviceName+" has removed from the list.");
                        ltag(deviceName+" has removed from the list.");

                        isHc05Paired = false;
                        deviceHC05 = null;

                        printPairedList();
                    }
                } catch (Exception e) {
                    ltag(e.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    //region implements resume pause destroy

    @Override
    protected void onResume() {
        super.onResume();

        switch (connectState) {
            case DISCOVERY:
                mainPresenter.regDiscoveryReceiver();
                break;
            case PAIRING:
                mainPresenter.regPairingReceiver();
                break;
            case BONDING:
                mainPresenter.regBondingReceiver();
                break;
            case NONE:
                ltag("Done nothing for the device");
                break;
        }

        mainPresenter.regBluetoothAdapterReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mainPresenter.unregDiscoveryReceiver();
        mainPresenter.unregParingReceiver();
        mainPresenter.unregBondingReceiver();
        mainPresenter.unregBluetoothAdapterReceiver();
    }

    @Override
    protected void onDestroy() {
        mainPresenter.unregDiscoveryReceiver();
        mainPresenter.unregParingReceiver();
        mainPresenter.unregBondingReceiver();
        mainPresenter.unregBluetoothAdapterReceiver();

        super.onDestroy();
    }

    //endregion implements resume pause destroy

    protected void discoverDevice() {
        // Stop Discovering
        if (mBluetoothAdapter.isDiscovering()) { mBluetoothAdapter.cancelDiscovery(); }
        mainPresenter.regDiscoveryReceiver();
        connectState = ConnectState.DISCOVERY;
        mBluetoothAdapter.startDiscovery();
    }

    //region implements Bond/Pair

    /**
     * Paring with bluetooth module HC05
     */
    public void pairingDevice(BluetoothDevice device) {

        ltag("Pairing Device: "+device.getName());
        infoFragmentPresenter.showMsg("Pairing Device: "+device.getName());

        // Stop Discovering
        if (mBluetoothAdapter.isDiscovering()) { mBluetoothAdapter.cancelDiscovery(); }
        mainPresenter.unregDiscoveryReceiver(); // the discovery receiver is trash in here.

        // Receiver
        mainPresenter.regPairingReceiver();
        connectState = ConnectState.PAIRING;

        device.createBond();
    }

    /**
     * Connect to the device
     */
    private void connectDevice() {
        mainPresenter.regBondingReceiver();
        connectState = ConnectState.BONDING;
        deviceHC05.createBond();
    }

    // Show paired device list
    public void printPairedList() {
        Set<BluetoothDevice> pairedDevices =
                mBluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
        deviceList.addAll(pairedDevices);

        deviceListPresenter.showList(deviceList, "paired");
    }

    // Show nearby devices
    public void printNearDeviceList(
            ArrayList<BluetoothDevice> deviceList) {
        deviceListPresenter.showList(deviceList, "nearby");
    }

    /**
     * Change view objects with paired condition
     */
    public void pairedViewEffect(boolean isHc05Paired) {
        if (isHc05Paired) {
            unpairBT.setClickable(true);
            unpairBT.setTextColor(Color.MAGENTA);

            pairedTV.setTextColor(Color.GREEN);
            pairedTV.setText("Paired!");

            pairedBT.setClickable(false);
            pairedBT.setTextColor(Color.GRAY);
        } else {
            unpairBT.setClickable(false);
            unpairBT.setTextColor(Color.GRAY);

            pairedTV.setTextColor(Color.GRAY);
            pairedTV.setText("Not in list");

            pairedBT.setClickable(true);
            pairedBT.setTextColor(Color.BLACK);
        }
    }

    //region implements Buttons

    /*
        Pair button
     */
    public void pairHC05(View view) {
        if (isHc05Paired) {
            startTime = System.currentTimeMillis();
            connectDevice();
        } else{
            startTime = System.currentTimeMillis();
            discoverDevice();
        }
    }

    public void unpairHC05(View view) {
        unPaired(BLUETOOTH_NAME);
    }

    //endregion implements Buttons


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.en:
                //your action
                break;
            case R.id.cn:
                //your action
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /*
        Initial bluetooth adapter
    */
        private void initBluetooth() {

            isHc05Paired = false;
            unpairBT.setClickable(false);

            // force to turn on bluetooth
            if (mBluetoothAdapter != null) {
                if (mBluetoothAdapter.enable()) {
                    updateBluetoothStatus(getString(R.string.on));
                }
            }
        }

    public void updateBluetoothStatus(String status) {

        if (status.equals(getString(R.string.on))) {
            bluetoothSwitchBT.setOnClickListener( view -> mBluetoothAdapter.disable());
            bluetoothSwitchBT.setText(getString(R.string.turn_off));
        } else if (status.equals(getString(R.string.off))) {
            bluetoothSwitchBT.setOnClickListener( view -> mBluetoothAdapter.enable());
            bluetoothSwitchBT.setText(getString(R.string.turn_on));
        }
        bleStatusTV.setText(status);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        requestPermissions();

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // MVP
        mainPresenter = new MainActivityPresenter(this, mBluetoothAdapter);
        infoFragmentPresenter = new InfoFragmentPresenter(this);
        deviceListPresenter = new DeviceListPresenter(this);

        // Initial layout
        bleStatusTV = findViewById(R.id.bleStatusTV);
        pairedTV = findViewById(R.id.pairedTV);
        pairedBT = findViewById(R.id.pairBT);
        unpairBT = findViewById(R.id.unpairBT);
        bluetoothSwitchBT = findViewById(R.id.bluetoothSwitchBT);
        status_container = findViewById(R.id.status_container);

        // Fragment
        fragmentManager = getSupportFragmentManager();

        // Check for Bluetooth Adapter Receiver
        mainPresenter.regBluetoothAdapterReceiver();

        // Indicator
        progressCircle = new ProgressDialog(this);

        connectState = ConnectState.NONE;

        initBluetooth();
    }

    @Override
    public void onListDeviceSelected(int position) {
        msg(getApplicationContext(), "Clicked: "+deviceList.get(position).toString() );
    }


    @Override
    public void setFragment(BaseFragment fragment) {

        status_container.removeAllViews();
        if( fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }

        //showing the presenter on screen
        ft = getSupportFragmentManager().beginTransaction();

        if (fragment.isAdded()) {
            ft.detach(fragment);
            ft.attach(fragment);
        } else {
            ft.replace(R.id.status_container, fragment);
        }

        ft.addToBackStack(null);
        ft.commit();
    }
}