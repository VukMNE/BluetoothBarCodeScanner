package bluetooth.navira.me.bluetoothskener.model;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class AsyncParamBT {
    private BluetoothDevice device;
    private BluetoothAdapter adapter;


    public AsyncParamBT(BluetoothAdapter adapter, BluetoothDevice device){
        this.adapter = adapter;
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(BluetoothAdapter adapter) {
        this.adapter = adapter;
    }
}
