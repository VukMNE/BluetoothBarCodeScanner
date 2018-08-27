package bluetooth.navira.me.bluetoothskener.util;

public interface Konstante {
    // Message types sent from the BluetoothChatService Handler
    public static final int PORUKA_PROMIJENJENO_STANJE = 1;
    public static final int PORUKA_CITAJ = 2;
    public static final int PORUKA_ŠALJI = 3;
    public static final int PORUKA_UREĐAJ = 4;
    public static final int PORUKA_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String NAZIV_UREĐAJA = "device_name";
    public static final String TOAST = "toast";
    public static final String BT_TAG = "BT27";

    // Constants that indicate the current connection state
    public static final int STANJE_NULA = 0;       // we're doing nothing
    public static final int STANJE_SLUŠANJE = 1;     // now listening for incoming connections
    public static final int STANJE_POVEZIVANJE = 2; // now initiating an outgoing connection
    public static final int STANJE_POVEZANI = 3;  // now connected to a remote device

}
