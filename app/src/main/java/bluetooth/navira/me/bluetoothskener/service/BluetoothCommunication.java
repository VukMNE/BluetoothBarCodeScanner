package bluetooth.navira.me.bluetoothskener.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import bluetooth.navira.me.bluetoothskener.util.Konstante;

/**
 * Created by Vuk on 7.6.2018..
 */

public class BluetoothCommunication {
    //private final BluetoothServerSocket mmServerSocket; u slucaju SERVERA
    private String TAG = "BluetoothCommunication";

    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private Handler handler;
    private String deviceName;
    private BluetoothAdapter mBluetoothAdapter;
    private UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    int MESSAGE_CONNECTED = 1;
    int MESSAGE_DATA_RECIEVED = 2;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    int stanje;
    int novoStanje;

    public BluetoothCommunication(Context context, Handler handler) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        stanje = Konstante.STANJE_NULA;
        novoStanje = stanje;
        this.handler = handler;
    }

    public synchronized int getStanje() {
        return stanje;
    }

    private synchronized void azurirajStanjeVeze() {
        stanje = getStanje();
        Log.d(TAG, "updateUserInterfaceTitle: " + stanje + " prelazi u -> " + novoStanje);
        novoStanje = stanje;

        // Give the new state to the Handler so the UI Activity can update
        handler.obtainMessage(Konstante.PORUKA_PROMIJENJENO_STANJE, novoStanje, -1).sendToTarget();
    }



    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID);

            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
            stanje = Konstante.STANJE_POVEZIVANJE;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed(mmDevice.getName());
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothCommunication.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect  socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            stanje = Konstante.STANJE_POVEZANI;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            setName("ConnectedThread");

            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (stanje == Konstante.STANJE_POVEZANI) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    handler.obtainMessage(Konstante.PORUKA_CITAJ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                handler.obtainMessage(Konstante.PORUKA_ŠALJI, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    public synchronized void start(BluetoothDevice device) {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if(mConnectThread == null){
            mConnectThread = new ConnectThread(device);
        }

        // Update UI title
        azurirajStanjeVeze();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (stanje == Konstante.STANJE_POVEZIVANJE) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        // Update UI title
        azurirajStanjeVeze();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {
        Log.d(TAG, "connected");
        mmDevice = device;
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }



        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = handler.obtainMessage(Konstante.PORUKA_UREĐAJ);
        Bundle bundle = new Bundle();
        bundle.putString(Konstante.NAZIV_UREĐAJA, device.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);
        // Update UI title
        azurirajStanjeVeze();
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        stanje = Konstante.STANJE_NULA;
        // Update UI title
        azurirajStanjeVeze();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (stanje != Konstante.STANJE_POVEZANI) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed(String imeUredjaja) {
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(Konstante.PORUKA_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Konstante.TOAST, "Izgubljena veza sa uređajem");
        bundle.putString(Konstante.NAZIV_UREĐAJA, imeUredjaja);
        msg.setData(bundle);
        handler.sendMessage(msg);

        stanje = Konstante.STANJE_NULA;
        // Update UI title
        azurirajStanjeVeze();

        // Start the service over to restart listening mode
       // BluetoothCommunication.this.start(mmDevice);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(Konstante.PORUKA_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Konstante.TOAST, "Izgubljena veza sa uređajem" );
        //bundle.putString(Konstante.NAZIV_UREĐAJA, imeUredjaja);
        msg.setData(bundle);
        handler.sendMessage(msg);

        stanje = Konstante.STANJE_NULA;
        // Update UI title
        azurirajStanjeVeze();

        // Start the service over to restart listening mode
//        BluetoothCommunication.this.start(mmDevice);
    }


}
