package bluetooth.navira.me.bluetoothskener.model;

/**
 * Created by Vuk on 29.5.2018..
 */

public class BlueToothUredjaj {
    private String naziv;
    private String adresaMAC;
    private boolean statusVeze;

    public BlueToothUredjaj(String naziv, String mac){
        this.naziv = naziv;
        this.adresaMAC = mac;
        this.statusVeze = false;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getAdresaMAC() {
        return adresaMAC;
    }

    public void setAdresaMAC(String adresaMAC) {
        this.adresaMAC = adresaMAC;
    }

    public boolean isStatusVeze() {
        return statusVeze;
    }

    public void setStatusVeze(boolean statusVeze) {
        this.statusVeze = statusVeze;
    }


}
