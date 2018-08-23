package bluetooth.navira.me.bluetoothskener;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import bluetooth.navira.me.bluetoothskener.util.Konstante;

public class MainActivity extends AppCompatActivity {

    public Handler mHandler;
    public static EditText txtRez;
    private static TextView lblStatusKon;
    private static MenuItem menuItem;
    StringBuilder poruke;
    int REQUEST_ENABLE_BT = 2702;
    public static boolean deviceConnected;
    String[] konektovaniUređaji = new String[100];
    int REQUEST_BT_LIST_ACTIVITY = 1052;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtRez = (EditText) findViewById(R.id.txtRez);
        lblStatusKon = (TextView) findViewById(R.id.lblStatusKon);
        poruke = new StringBuilder();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menuItem = menu.findItem(R.id.ic_bluetooth);

        Intent intent = getIntent();
        boolean deviceConnected = intent.getBooleanExtra("konekcija", false);
        Log.d(Konstante.BT_TAG, "Uređaj je povezan: " + deviceConnected);
        MainActivity.this.bluetoothDeviceConnectionState(deviceConnected);

        Bundle b= intent.getExtras();
        if(b != null) {
            konektovaniUređaji = b.getStringArray("povezani");
        }
        for(int i = 0; i < konektovaniUređaji.length; i++){
            if(konektovaniUređaji[i] != null){
                if(konektovaniUređaji[i].length() > 0){
                    Log.d(Konstante.BT_TAG, "#1 Uredjaj: " + konektovaniUređaji[i]);
                }
            }
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== R.id.ic_bluetooth){
            Intent intent = new Intent(this, BlueToothPairedDevices.class);

            Bundle b = new Bundle();
            b.putStringArray("povezani", konektovaniUređaji);

            intent.putExtras(b);
            startActivityForResult(intent, REQUEST_BT_LIST_ACTIVITY);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1052 && resultCode == 2000){

            this.bluetoothDeviceConnectionState(imaLiKonektovanih());
            if(data.getExtras() != null) {
                konektovaniUređaji = data.getExtras().getStringArray("povezani");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void citajSkenirano(String sken){
        txtRez.setText(txtRez.getText() + "\n" + sken);
        /*ConnectThread cThread = new ConnectThread(BlueToothPairedDevices.btDevice, BlueToothPairedDevices.bluetoothAdapter,  mHandler);
        cThread.run();*/
    }

    public static void bluetoothDeviceConnectionState(boolean state){
        if(state == true){
            menuItem.setIcon(R.drawable.bluetooth_green);
            lblStatusKon.setText("Bar-kod čitač povezan");
        }
        else {
            menuItem.setIcon(R.drawable.bluetooth_gray);
            lblStatusKon.setText("Bar-kod čitač nije povezan");
        }
    }

    private boolean imaLiKonektovanih(){
        for(int t = 0; t < konektovaniUređaji.length; t++){
            if(konektovaniUređaji[t] != null){
                if(konektovaniUređaji.length > 0 ){
                    return true;
                }
            }
        }
        return false;
    }



}
