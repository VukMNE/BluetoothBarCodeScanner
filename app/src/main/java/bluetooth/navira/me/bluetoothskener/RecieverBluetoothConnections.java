package bluetooth.navira.me.bluetoothskener;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class RecieverBluetoothConnections extends BroadcastReceiver {

    public final static String CTAG = "ReceiverBlue";
    public Set<BluetoothDevice> connectedDevices = new HashSet<BluetoothDevice>();

    @Override
    public void onReceive(Context context, Intent intent) {
        final BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equalsIgnoreCase( action ) )   {
            Log.d(CTAG, "We are now connected to " + device.getName() );
            if (!connectedDevices.contains(device))
                connectedDevices.add(device);
        }

        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equalsIgnoreCase( action ) )    {
            Log.d(CTAG, "We have just disconnected from " + device.getName() );
            connectedDevices.remove(device);
        }
    }
}


