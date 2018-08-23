package bluetooth.navira.me.bluetoothskener;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Set;

import bluetooth.navira.me.bluetoothskener.model.AsyncParamBT;
import bluetooth.navira.me.bluetoothskener.service.*;
import bluetooth.navira.me.bluetoothskener.util.Konstante;


public class BlueToothPairedDevices extends AppCompatActivity implements BlueToothUredjajAdapter.ListItemClickListener,
BluetoothAsyncResponse{
    int REQUEST_ENABLE_BT = 2702;
    public BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    public BluetoothDevice btDevice;
    private RecyclerView recyclerView;
    BlueToothUredjajAdapter btAdapter;
    BluetoothCommunication bc = new BluetoothCommunication();
    String[] konektovaniUređaji;
    int indeks = 0;
    int REQUEST_BACK_TO_MAIN = 2000;


    private boolean connectionExists = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_paired_devices);
        //Podesavanje velicine prozora
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width*0.8), (int) (height*0.5));

        recyclerView = (RecyclerView) findViewById(R.id.rv_uredjaji);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(false);

        bc.delegate = this;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        konektovaniUređaji = b.getStringArray("povezani");

        for(int i = 0; i < konektovaniUređaji.length; i++){
            if(konektovaniUređaji[i] != null){
                if(konektovaniUređaji[i].length() > 0){
                    Log.d(Konstante.BT_TAG, "#2 Uredjaj: " + konektovaniUređaji[i]);
                }
            }
        }

        blueToothSetUp(bluetoothAdapter);


        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);



    }


    private void blueToothSetUp(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            AlertDialog.Builder builder = new AlertDialog.Builder(BlueToothPairedDevices.this);
            builder.setTitle("Uređaj ne podržava Bluetooth");
            builder.setMessage("Ovaj uređaj ne podržava Bluetooth. Potrebno je instalirati aplikaciju na drugom uređaju.");
            builder.setIcon(R.drawable.ic_offline);
            AlertDialog alert1 = builder.create();
            alert1.show();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            pairedDevices = bluetoothAdapter.getBondedDevices();
            konektovaniUređaji = new String [pairedDevices.size()];
            btAdapter = new BlueToothUredjajAdapter(bluetoothAdapter, this, getIntent().getStringArrayExtra("povezani"));
            recyclerView.setAdapter(btAdapter);
            /*
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                   // txtRez.setText(deviceName);
                }
            }*/
        }
    }


    @Override
    public void listItemClicked(int position) {
      //  BlueToothUredjaj btUredjaj = btAdapter.getBluetoothUredjaj(position); // u slucaju servera
      //   ConnectThread cThread = new ConnectThread(btUredjaj, bluetoothAdapter);// u slucaju SERVERA
        Log.d(Konstante.BT_TAG, "Status AsyncTask-a: " + bc.getStatus().name());
        /*if(bc.getStatus() == AsyncTask.Status.RUNNING){
            bc.cancel(true);
            Log.d(Konstante.BT_TAG,"Otkazuje se");

        }*/
        btDevice = btAdapter.getBluetoothDevice(position);
        Log.d(Konstante.BT_TAG, "POS: " + position);
        bc.execute(new AsyncParamBT(bluetoothAdapter, btDevice));

    }

    @Override
    public void progressPublished(String[] progres) {
        Log.d(Konstante.BT_TAG, progres[0]);
        connectionExists = true;
        if(progres[0].indexOf("konektovan") > 0){
            startMainActivity();
            MainActivity.bluetoothDeviceConnectionState(true);
        }
        else {
            MainActivity.citajSkenirano(progres[0]);
        }
    }

    @Override
    public void processFinish(String output) {
        //Konekcija izgubljena
        if(output == "kraj"){
            Log.d(Konstante.BT_TAG,"Uspješno otkazano");

        }
        else {
            connectionExists = false;
            MainActivity.bluetoothDeviceConnectionState(false);
            Log.d(Konstante.BT_TAG, "Uređaj diskonektovan");
        }

    }

    private void startMainActivity(){
        Intent mainActivity = new Intent(BlueToothPairedDevices.this, MainActivity.class);
        mainActivity.putExtra("konekcija", true);

        Bundle bundle = new Bundle();
        bundle.putStringArray("povezani", konektovaniUređaji);
        mainActivity.putExtras(bundle);

        mainActivity.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        startActivity(mainActivity);
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(connectionExists== false) {
            MainActivity.deviceConnected = false;
            MainActivity.bluetoothDeviceConnectionState(false);
        }
        Bundle b = new Bundle();
        b.putStringArray("povezani", konektovaniUređaji);
        Intent intent = new Intent();
        setResult(REQUEST_BACK_TO_MAIN, intent);
        super.onBackPressed();
    }


    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            //Device is now connected
                Log.d(Konstante.BT_TAG, "Ovo radiiii: "+ device.getName());
                konektovaniUređaji[indeks] = device.getName();
                indeks++;
            }

            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                for(int i = 0; i < konektovaniUređaji.length; i++){
                    if(konektovaniUređaji[i].equals(device.getName())){
                        konektovaniUređaji[i] = null;
                    }
                }
            }

        }
    };
}
