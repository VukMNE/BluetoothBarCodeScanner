package bluetooth.navira.me.bluetoothskener;

import org.json.JSONException;

public interface BluetoothAsyncResponse {
    void progressPublished(String[] progres);
    void processFinish(String output);


}
