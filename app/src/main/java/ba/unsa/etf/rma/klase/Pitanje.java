package ba.unsa.etf.rma.klase;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;

public class Pitanje implements Parcelable {
    private String naziv;
    private String tekstPitanja;
    private ArrayList<String> odgovori = new ArrayList<>();
    private String tacan;

    public Pitanje(String naziv, String tekstPitanja, String tacan) {
        this.naziv = naziv;
        this.tekstPitanja = tekstPitanja;
        odgovori.add(tacan);
        this.tacan = tacan;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getTekstPitanja() {
        return tekstPitanja;
    }

    public void setTekstPitanja(String tekstPitanja) {
        this.tekstPitanja = tekstPitanja;
    }

    public ArrayList<String> getOdgovori() {
        return odgovori;
    }

    public void setOdgovori(ArrayList<String> odgovori) {
        this.odgovori = odgovori;
    }

    public String getTacan() {
        return tacan;
    }

    public void setTacan(String tacan) {
        this.tacan = tacan;
    }

    public ArrayList<String> dajRandomOdgovore(){
        ArrayList<String> randomOdgovori = new ArrayList<>();
        Collections.copy(randomOdgovori, odgovori);
        Collections.shuffle(randomOdgovori);
        return randomOdgovori;
    }

    public boolean postojiOdgovor(String odgovor){
        return odgovori.contains(odgovor);
    }

    public void dodajOdgovor(String odgovor){
        odgovori.add(odgovor);
    }

    @Override
    public String toString() {
        return naziv;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.naziv);
        dest.writeString(this.tekstPitanja);
        dest.writeStringList(this.odgovori);
        dest.writeString(this.tacan);
    }

    protected Pitanje(Parcel in) {
        this.naziv = in.readString();
        this.tekstPitanja = in.readString();
        this.odgovori = in.createStringArrayList();
        this.tacan = in.readString();
    }

    public static final Parcelable.Creator<Pitanje> CREATOR = new Parcelable.Creator<Pitanje>() {
        @Override
        public Pitanje createFromParcel(Parcel source) {
            return new Pitanje(source);
        }

        @Override
        public Pitanje[] newArray(int size) {
            return new Pitanje[size];
        }
    };
}
