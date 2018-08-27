package bluetooth.navira.me.bluetoothskener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import bluetooth.navira.me.bluetoothskener.model.BlueToothUredjaj;
import bluetooth.navira.me.bluetoothskener.util.Konstante;

/**
 * Created by Vuk on 29.5.2018..
 */

public class BlueToothUredjajAdapter extends RecyclerView.Adapter<BlueToothUredjajAdapter.BtUredjajViewHolder> {
    private ListItemClickListener listItemClickedListener;
    private ArrayList<BlueToothUredjaj> listaUredjaja;
    private ArrayList<BluetoothDevice> listaBluetoothDevices;
    private Set<BluetoothDevice> bluetoothDevices;
    private BluetoothAdapter adapter;
    private String[] povezaniUredjaji;

    public BlueToothUredjajAdapter(BluetoothAdapter bluetoothAdapter, ListItemClickListener listener, String[] povezaniUredjaji){
        this.adapter = bluetoothAdapter;
        listItemClickedListener = listener;
        this.bluetoothDevices = bluetoothAdapter.getBondedDevices();
        this.povezaniUredjaji = povezaniUredjaji;

        if(povezaniUredjaji != null) {
            for (int i = 0; i < povezaniUredjaji.length; i++) {
                if (povezaniUredjaji[i] != null) {
                    if (povezaniUredjaji[i].length() > 0) {
                        Log.d("BT27", "#3 Uredjaj: " + povezaniUredjaji[i]);
                    }
                }
            }
        }
        popuniListu(bluetoothDevices);
        this.notifyDataSetChanged();
    }

    @Override
    public BtUredjajViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.bt_device_list_item, parent, false);
        BtUredjajViewHolder viewHolder = new BtUredjajViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(BtUredjajViewHolder holder, int position){
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    public interface ListItemClickListener {
        public void listItemClicked(int position);
    }

    public BluetoothDevice getBluetoothDevice(int position){
        return listaBluetoothDevices.get(position);
    }

    public BlueToothUredjaj getBluetoothUredjaj(int position){
        return listaUredjaja.get(position);
    }

    public int getPozicijaUredjaja(String naziv){
        for(int i = 0; i < listaUredjaja.size(); i++){
            Log.d(Konstante.BT_TAG, "U adapteru: " + listaUredjaja.get(i).getNaziv() + "=" + naziv);
            if(listaUredjaja.get(i).getNaziv().equals(naziv)){
                return i;
            }
        }

        return -1;
    }


    private void popuniListu(Set<BluetoothDevice> bluetoothDevices){
        listaUredjaja = new ArrayList<BlueToothUredjaj>();
        listaBluetoothDevices = new ArrayList<BluetoothDevice>();
        Log.d("BT27", "Broj uredjaja: " + bluetoothDevices.size());
        if (bluetoothDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : bluetoothDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("BT27", "Naziv uredjaja: " + deviceName);
                Log.d("BT27", "MAC Adresa uredjaja: " + deviceHardwareAddress);
                //Log.d("BT27", "Detalji uredjaj1: " + device.getBluetoothClass().getMajorDeviceClass());
                //Log.d("BT27", "Detalji uredjaj2: " + device.getBluetoothClass().getDeviceClass());
                //Log.d("BT27", "Detalji uredjaj2: " + device.getType());
                final BlueToothUredjaj uredjaj = new BlueToothUredjaj(deviceName, deviceHardwareAddress);
                if(povezaniUredjaji != null) {
                    for (int j = 0; j < povezaniUredjaji.length; j++) {
                        if (povezaniUredjaji[j] != null) {
                            Log.d("BT27", povezaniUredjaji[j] + "=" + uredjaj.getNaziv());
                            if (povezaniUredjaji[j].equals(uredjaj.getNaziv())) {
                                uredjaj.setStatusVeze(true);
                                break;
                            }
                        }
                    }
                }
                listaUredjaja.add(uredjaj);
                listaBluetoothDevices.add(device);
                // txtRez.setText(deviceName);
            }
        }
        Log.d("BT27", "Lista: " + listaUredjaja.size());
    }
    class BtUredjajViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView lblUredjajIme;
        TextView lblMACAdresa;
        TextView lblStatus;

        public BtUredjajViewHolder(View itemView) {
            super(itemView);
            lblUredjajIme = (TextView) itemView.findViewById(R.id.lblUredjajIme);
            lblMACAdresa = (TextView) itemView.findViewById(R.id.lblUredjajMACAdresa);
            lblStatus = (TextView) itemView.findViewById(R.id.lblUredjajStatus);
            itemView.setOnClickListener(this);
        }


        void bind(int position)  {
            //Ova metoda zapravo povezuje izvoriste podataka sa elementom liste, ali se poziva iz Adaptera na onBindViewHolder
            lblUredjajIme.setText(listaUredjaja.get(position).getNaziv());
            lblMACAdresa.setText(listaUredjaja.get(position).getAdresaMAC());
            if(listaUredjaja.get(position).isStatusVeze()){
                lblStatus.setText("Povezan");
                lblStatus.setTextColor(Color.parseColor("#11b200"));
                lblStatus.setTypeface(null, Typeface.BOLD);
                ConstraintLayout layout = (ConstraintLayout) lblStatus.getParent();
                layout.setBackgroundColor(Color.parseColor("#d5fcd4"));
            }
            else{
                lblStatus.setText("Nije povezan");
            }
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            listItemClickedListener.listItemClicked(clickedPosition);
        }
    }
}
