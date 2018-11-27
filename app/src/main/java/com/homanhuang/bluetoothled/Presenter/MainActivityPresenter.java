package com.homanhuang.bluetoothled.Presenter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;
import com.homanhuang.bluetoothled.R;
import com.homanhuang.bluetoothled.View.Activity.MainActivity;
import com.homanhuang.bluetoothled.View.common.BaseFragment;
import com.homanhuang.bluetoothled.ViewPresenterHelper.MainActivityVP;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import rx2.receiver.android.RxReceiver;

import java.util.ArrayList;

import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;


/**
 * the main presenter has the main activity presentation logic and the navigation logic
 */
public class MainActivityPresenter implements MainActivityVP.Presenter {

    /* Toast shortcut */
    public static void msg(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /* Log tag and shortcut */
    final static String TAG = "MYLOG MainPrst";
    public static void ltag(String message) { Log.i(TAG, message); }

    // Main class instance
    private MainActivity activity;
    public Context context;

    // Bluetooth device
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice deviceHC05;
    private ArrayList<BluetoothDevice> deviceList;
    // connect to customized HC05
    private String BLUETOOTH_NAME = "Homan_Bluetooth";
    private String BLUETOOTH_PASSWORD = "3434";
    private String BLUETOOTH_ADDRESS = "98:D3:32:70:B1:C9";
    private boolean isHc05Paired = false;

    // RxJava
    // Bluetooth adapter
    private IntentFilter bluetoothAdapterIF;
    private Observable<Intent> bluetoothAdapterReceiver;
    private Disposable bluetoothAdapterDisposable;
    // Discovery
    private IntentFilter discoveryIF;
    private Observable<Intent> discoveryReceiver;
    private Disposable discoveryDisposable;
    // Pairing
    private IntentFilter pairingIF;
    private Observable<Intent> pairingReceiver;
    private Disposable pairingDisposable;
    // Bonding
    private IntentFilter bondingIF;
    private Observable<Intent> bondingReceiver;
    private Disposable bondingDisposable;


    // Constructor
    public MainActivityPresenter(MainActivity activity, BluetoothAdapter mBluetoothAdapter) {
        this.activity = activity;
        context = activity.getApplicationContext();
        this.mBluetoothAdapter = mBluetoothAdapter;

        // Bluetooth adapter
        bluetoothAdapterIF = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothAdapterReceiver = RxReceiver.receives(context, bluetoothAdapterIF);

        // Discovery receiver
        discoveryIF = new IntentFilter();
        discoveryIF.addAction(BluetoothDevice.ACTION_FOUND);
        discoveryIF.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        discoveryIF.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        discoveryReceiver = RxReceiver.receives(context, discoveryIF);

        // Pairing
        pairingIF = new IntentFilter();
        pairingIF.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        pairingIF.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        pairingReceiver = RxReceiver.receives(context, pairingIF);

        // Bonding
        bondingIF = new IntentFilter();
        bondingIF.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bondingReceiver = RxReceiver.receives(context, bondingIF);



    }

    /**
     * this method from base presenter tells the view to show
     * the param fragment
     * @param fragment
     */
    @Override
    public void addFragment(BaseFragment fragment) {
        activity.setFragment(fragment);
    }

    public void regBluetoothAdapterReceiver() {
        // callback from Bluetooth adapter
        DisposableObserver<Intent> bluetoothAdapterObserver = new DisposableObserver<Intent>() {
            @Override
            public void onNext(Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                    final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);

                    InfoFragmentPresenter infoFragmentPresenter = new InfoFragmentPresenter(activity);
                    switch (bluetoothState) {
                        case BluetoothAdapter.STATE_OFF:
                            infoFragmentPresenter.showMsg(context.getString(R.string.shut_down));
                            activity.updateBluetoothStatus(context.getString(R.string.off));
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            msg(context, context.getString(R.string.shut_down));
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            msg(context, context.getString(R.string.loading_bluetooth));
                            activity.updateBluetoothStatus(context.getString(R.string.turn_on));
                            break;
                        case BluetoothAdapter.STATE_ON:
                            msg(context, context.getString(R.string.bluetooth_on));
                            infoFragmentPresenter.showMsg(context.getString(R.string.bluetooth_on));
                            activity.updateBluetoothStatus(context.getString(R.string.on));
                            break;
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                ltag("Bluetooth cannot be started. Please check your hardware!");
            }

            @Override
            public void onComplete() {
                ltag("Please unregister your receiver!");
            }
        };

        ltag("Observe bluetooth adapter receiver.");
        bluetoothAdapterDisposable = bluetoothAdapterReceiver
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(bluetoothAdapterObserver);

    }

    public void unregBluetoothAdapterReceiver() {
        if (bluetoothAdapterDisposable != null) {
            bluetoothAdapterDisposable.dispose();
            ltag("Dispose Bluetooth Adapter Receiver.");
        }
    }

    public void regDiscoveryReceiver() {
        // callback from Bluetooth adapter
        DisposableObserver<Intent> discoveryObserver = new DisposableObserver<Intent>() {
            @Override
            public void onNext(Intent intent) {
                final String action = intent.getAction();

                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        ltag("Bluetooth Discovery >>> Begin <<<.");
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:

                        ltag("Bluetooth Discovery -=* End *=-.");

                        activity.printNearDeviceList(deviceList);
                        deviceList.clear();

                        if (deviceHC05 == null) {
                            msg(context, BLUETOOTH_NAME+" NOT found!");
                        }
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);


                        String deviceName = device.getName();
                        if (deviceName == null) deviceName = "Unknown";

                        deviceList.add(device);

                        ltag("discoveryReceiver: Device: "+deviceName+
                                ", MAC: "+device.getAddress()+
                                ", type: "+device.getType() );

                        // Found HC05
                        if (deviceName.equals(BLUETOOTH_NAME)) {
                            deviceHC05 = device;
                            msg(context, BLUETOOTH_NAME+" is FOUND.");
                            isHc05Paired = true;
                            activity.pairedViewEffect(isHc05Paired);

                            //try to bond the device
                            activity.pairingDevice(device);
                        }

                        break;
                    default:
                        throw new IllegalArgumentException("Invalid Entry: " + action);
                }
            }

            @Override
            public void onError(Throwable e) {
                ltag("Error: "+e.getMessage());
            }

            @Override
            public void onComplete() {
                ltag("Please unregister your receiver!");
            }
        };

        ltag("Observe bluetooth adapter receiver.");
        discoveryDisposable = discoveryReceiver
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(discoveryObserver);

        mBluetoothAdapter.startDiscovery();
    }

    public void unregDiscoveryReceiver() {
        if (discoveryDisposable != null) {
            discoveryDisposable.dispose();
            ltag("Dispose discovery receiver");
        }
    }

    public void regPairingReceiver() {
        // callback from pairing receiver
        DisposableObserver<Intent> pairingObserver = new DisposableObserver<Intent>() {
            @Override
            public void onNext(Intent intent) {
                String action = intent.getAction();
                if (action == BluetoothDevice.ACTION_PAIRING_REQUEST) {

                    final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);

                    if (type == BluetoothDevice.PAIRING_VARIANT_PIN) {

                        device.setPin(BLUETOOTH_PASSWORD.getBytes());
                        ltag("Auto insert the PIN: "+BLUETOOTH_PASSWORD);

                        device.createBond();
                        onComplete();
                    }
                    else {
                        ltag("Unexpected pairing type: " + type);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                ltag("Error: "+e.getMessage());
            }

            @Override
            public void onComplete() {
                ltag("Please unregister your receiver!");
            }
        };

        pairingDisposable = pairingReceiver
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(pairingObserver);
    }

    public void unregParingReceiver() {
        if (pairingDisposable != null) {
            pairingDisposable.dispose();
            ltag("Dispose pairing receiver.");
        }
    }

    public void regBondingReceiver() {
        // callback from Bluetooth adapter
        DisposableObserver<Intent> bondingObserver = new DisposableObserver<Intent>() {
            @Override
            public void onNext(Intent intent) {
                String action = intent.getAction();
                if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    ltag("bondReceiver: state = "+ device.getBondState());
                    InfoFragmentPresenter infoFragmentPresenter = new InfoFragmentPresenter(activity);

                    // 3 cases:
                    switch (device.getBondState()) {
                        // bonding
                        case BluetoothDevice.BOND_BONDING:
                            ltag("Bond Receiver: Bonding is processing.");

                            break;

                        // bond in progress
                        case BluetoothDevice.BOND_BONDED:
                            ltag("Bond Receiver: Bond Bonded");
                            infoFragmentPresenter.showMsg(BLUETOOTH_NAME+" is bonded.");
                            activity.printPairedList();
                            break;

                        // breaking bond
                        case BluetoothDevice.BOND_NONE:
                            ltag("Bond Receiver: Bonding is broken!");
                            infoFragmentPresenter.showMsg("Bonding is broken!");
                            break;

                        default:
                            throw new IllegalArgumentException("Invalid bonding state!");
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                ltag("Error: "+e.getMessage());
            }

            @Override
            public void onComplete() {
                ltag("Please unregister your receiver!");
            }
        };

        bondingDisposable = pairingReceiver
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(bondingObserver);
    }

    public void unregBondingReceiver() {
        if (bondingDisposable != null) {
            bondingDisposable.dispose();
            ltag("Dispose bonding receiver.");
        }
    }
}
