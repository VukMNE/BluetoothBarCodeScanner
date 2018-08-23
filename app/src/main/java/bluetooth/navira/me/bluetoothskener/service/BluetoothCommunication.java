package bluetooth.navira.me.bluetoothskener.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import bluetooth.navira.me.bluetoothskener.BluetoothAsyncResponse;
import bluetooth.navira.me.bluetoothskener.model.AsyncParamBT;
import bluetooth.navira.me.bluetoothskener.util.Konstante;

/**
 * Created by Vuk on 7.6.2018..
 */

public class BluetoothCommunication extends AsyncTask<AsyncParamBT, String, String> {
    //private final BluetoothServerSocket mmServerSocket; u slucaju SERVERA
    public BluetoothAsyncResponse delegate = null;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private Handler handler;
    private String deviceName;
    private BluetoothAdapter mBluetoothAdapter;
    private UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    int MESSAGE_CONNECTED = 1;
    int MESSAGE_DATA_RECIEVED = 2;



    @Override
    protected String doInBackground(AsyncParamBT... asyncParamBTS) {
        mBluetoothAdapter = asyncParamBTS[0].getAdapter();
        mmDevice = asyncParamBTS[0].getDevice();
        BluetoothSocket tmp = null;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(Konstante.BT_TAG, "Socket's create() method failed", e);
        }
        mmSocket  = tmp;
        mBluetoothAdapter.cancelDiscovery();
        if(konektujSe()){
            String poruka = mmDevice.getName() + " konektovan";
            publishProgress(poruka);
        }

        manageMyConnectedSocket();
        return "kraj";
    }




    private boolean konektujSe(){
        try {
            Log.d(Konstante.BT_TAG, "OK SMO");
            mmSocket.connect();
            String poruka = mmDevice.getName() + " konektovan";
            return true;
        }catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                Log.d(Konstante.BT_TAG, "ZATVARAMO");
                Log.d(Konstante.BT_TAG, connectException.getMessage());
                Log.d(Konstante.BT_TAG, connectException.getLocalizedMessage());
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(Konstante.BT_TAG, "Could not close the client socket", closeException);
            }
        }
        return false;
    }

    private void manageMyConnectedSocket(){

        while(mmSocket.isConnected()) {
            byte[] mmBuffer = new byte[1024];
            int numBytes;
            try {
                if(isCancelled()){
                    Log.d(Konstante.BT_TAG,"Uspješno otkazano");
                    //onPostExecute("kraj");
                }
                else {
                    InputStream inputStream = mmSocket.getInputStream();
                    numBytes = inputStream.read(mmBuffer);
                    String readMessage = new String(mmBuffer, 0, numBytes);
                    Log.d(Konstante.BT_TAG, "Poruka: " + readMessage);
                    publishProgress(readMessage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        Log.d(Konstante.BT_TAG,"IDEMOOO");

    }

    @Override
    protected void onProgressUpdate(String... values) {
        delegate.progressPublished(values);
    }

    @Override
    protected void onPostExecute(String s) {
        delegate.processFinish(s);
    }

    @Override
    protected void onCancelled(String s) {
        try {
            mmSocket.close();
            Log.d(Konstante.BT_TAG,"Uspješno otkazano2");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
