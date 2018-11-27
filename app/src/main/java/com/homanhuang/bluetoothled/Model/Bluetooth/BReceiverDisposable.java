package com.homanhuang.bluetoothled.Model.Bluetooth;


import android.content.BroadcastReceiver;
import android.content.Context;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class BReceiverDisposable implements Disposable {

    private BroadcastReceiver receiver;
    private Context context;

    private boolean isDisposed = false;

    BReceiverDisposable(@NonNull BroadcastReceiver receiver, @NonNull Context context) {
        this.receiver = receiver;
        this.context = context;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            context.unregisterReceiver(receiver);
            isDisposed = true;
            receiver = null;
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }
}
