package bluetooth.navira.me.bluetoothskener;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import bluetooth.navira.me.bluetoothskener.model.AsyncParamBT;
import bluetooth.navira.me.bluetoothskener.service.*;
import bluetooth.navira.me.bluetoothskener.util.Konstante;

import static bluetooth.navira.me.bluetoothskener.util.Konstante.BT_TAG;


public class BlueToothPairedDevices extends AppCompatActivity implements BlueToothUredjajAdapter.ListItemClickListener{
    int REQUEST_ENABLE_BT = 2702;
    public BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    public BluetoothDevice btDevice;
    private RecyclerView recyclerView;
    BlueToothUredjajAdapter btAdapter;
    BluetoothCommunication bc;
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

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        konektovaniUređaji = b.getStringArray("povezani");

        if(konektovaniUređaji == null){
            konektovaniUređaji = new String[10];
        }

        for(int i = 0; i < konektovaniUređaji.length; i++){
            if(konektovaniUređaji[i] != null){
                if(konektovaniUređaji[i].length() > 0){
                    Log.d(BT_TAG, "#2 Uredjaj: " + konektovaniUređaji[i]);
                }
            }
        }

        blueToothSetUp(bluetoothAdapter);

        }

    @Override
    protected void onStart() {
        if (bc == null){
            bc = new BluetoothCommunication(this, mHandler);
        }
        super.onStart();
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
            recyclerView.setAdapter(new BlueToothUredjajAdapter(bluetoothAdapter, this, getIntent().getStringArrayExtra("povezani")));

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
        /*if(bc.getStatus() == AsyncTask.Status.RUNNING){
            bc.cancel(true);
            Log.d(Konstante.BT_TAG,"Otkazuje se");

        }*/
        btDevice = btAdapter.getBluetoothDevice(position);
        Log.d(BT_TAG, "POS: " + position);
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        for(Thread thread: threadArray){
         //   Log.d(Konstante.BT_TAG, "Thread: " + thread.getName());
            if(thread.getName().equals("ConnectedThread")){
                thread.interrupt();
            }
        }
        if (bc != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bc.getStanje() == Konstante.STANJE_NULA) {
                // Start the Bluetooth chat services
                bc.connect(btDevice);

            }
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
    public void onBackPressed() {
        Intent intent = new Intent();
        if(connectionExists== false) {
            MainActivity.deviceConnected = false;
            MainActivity.bluetoothDeviceConnectionState(false);
            intent.putExtra("NO_CONN", true);
        }
        Bundle b = new Bundle();
        b.putStringArray("povezani", konektovaniUređaji);
        setResult(REQUEST_BACK_TO_MAIN, intent);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        super.onBackPressed();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Konstante.PORUKA_PROMIJENJENO_STANJE:
                    switch (msg.arg1) {
                        case Konstante.STANJE_POVEZANI:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            MainActivity.bluetoothDeviceConnectionState(true);
                            startMainActivity();
                            break;
                        case  Konstante.STANJE_POVEZIVANJE:
                            MainActivity.txtRez.setText("Povezivanje u toku");
                            break;
                        case Konstante.STANJE_SLUŠANJE:
                        case Konstante.STANJE_NULA:
                            //setStatus(R.string.title_not_connected);
                            MainActivity.bluetoothDeviceConnectionState(false);
                            break;
                    }
                    break;

                case Konstante.PORUKA_CITAJ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    MainActivity.citajSkenirano(readMessage);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Konstante.PORUKA_UREĐAJ:
                    // save the connected device's name
                    /*mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }*/
                    String mConnectedDeviceName = msg.getData().getString(Konstante.NAZIV_UREĐAJA);
                    konektovaniUređaji[indeks] = mConnectedDeviceName;
                    indeks++;
                    Log.d(BT_TAG, "Connected to " + mConnectedDeviceName);
                    break;
                case Konstante.PORUKA_TOAST:
                        Toast.makeText(getApplicationContext(), msg.getData().getString(Konstante.TOAST),
                                Toast.LENGTH_SHORT).show();
                        if(msg.getData().get(Konstante.NAZIV_UREĐAJA) != null) {
                            String nazivUredjaja = msg.getData().getString(Konstante.NAZIV_UREĐAJA);
                            int pozicija = btAdapter.getPozicijaUredjaja(nazivUredjaja);

                            Log.d(Konstante.BT_TAG, "Pozicija unutar recyclerView-a: " + pozicija);
                            TextView lblStatus = (TextView) recyclerView.findViewHolderForAdapterPosition(pozicija).itemView.findViewById(R.id.lblUredjajStatus);
                            lblStatus.setText("Nije povezan");
                            lblStatus.setTextColor(getResources().getColor(R.color.colorTextDark));
                            lblStatus.setTypeface(Typeface.DEFAULT);
                            ConstraintLayout con = (ConstraintLayout) lblStatus.getParent();
                            con.setBackgroundColor(Color.TRANSPARENT);
                            connectionExists = false;

                            Log.d(Konstante.BT_TAG, "Duzina niza je: " + konektovaniUređaji.length);
                            for(int s = 0; s < konektovaniUređaji.length; s++){
                                Log.d(Konstante.BT_TAG,"U handleru: konektovaniUređaji[" + s + "]=" + konektovaniUređaji[s]);
                                if (konektovaniUređaji[s] == nazivUredjaja) {
                                    Log.d(Konstante.BT_TAG,konektovaniUređaji[s] + " -> ''" );
                                    konektovaniUređaji[s] = "";
                                }
                            }

                        }

                    break;
            }
        }
    };
}
