package ba.unsa.etf.rma.modeli;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@SuppressWarnings("unused")
public class Pitanje implements Parcelable {
    private String naziv;
    private String tekstPitanja;
    private ArrayList<String> odgovori = new ArrayList<>();
    private String tacan = null;
    private String firebaseId;

    public Pitanje(String naziv, String tekstPitanja, ArrayList<String> odgovori, String tacan, String firebaseId) {
        this.naziv = naziv;
        this.tekstPitanja = tekstPitanja;
        this.odgovori = odgovori;
        this.tacan = tacan;
        this.firebaseId = firebaseId;
    }

    public Pitanje(String naziv, String tekstPitanja, String tacan) {
        this.naziv = naziv;
        this.tekstPitanja = tekstPitanja;

        if (tacan != null) {
            odgovori.add(tacan);
            this.tacan = tacan;
        }

        generisiFirebaseId();
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

    public ArrayList<String> dajRandomOdgovore() {
        ArrayList<String> randomOdgovori = new ArrayList<>(odgovori);
        Collections.shuffle(randomOdgovori);
        return randomOdgovori;
    }

    public boolean nePostojiOdgovor(String odgovor) {
        return !odgovori.contains(odgovor);
    }

    public void dodajOdgovor(String odgovor) {
        odgovori.add(odgovor);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return naziv;
    }

    private void generisiFirebaseId() {
        firebaseId = "QUES[" + UUID.randomUUID().toString().toUpperCase() + "]";
    }

    public String firebaseId() {
        return firebaseId;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass())
            return false;

        Pitanje p = (Pitanje) obj;
        return this.naziv.equals(p.naziv) && this.tacan.equals(p.tacan) && this.odgovori.size() == p.odgovori.size();
    }

    @Override
    public int hashCode() {
        return (naziv + tacan + odgovori.size()).hashCode();
    }


    public Pitanje(Parcel in) {
        this.naziv = in.readString();
        this.tekstPitanja = in.readString();
        this.odgovori = in.createStringArrayList();
        this.tacan = in.readString();
        this.firebaseId = in.readString();
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
        dest.writeString(this.firebaseId);
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
