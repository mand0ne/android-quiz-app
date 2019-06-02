package ba.unsa.etf.rma.modeli;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.UUID;

public class Kviz implements Comparable<Kviz>, Parcelable {
    private String naziv;
    private ArrayList<Pitanje> pitanja = new ArrayList<>();
    private Kategorija kategorija;
    private String firebaseId;

    public Kviz(String naziv, ArrayList<Pitanje> pitanja, Kategorija kategorija, String firebaseId) {
        this.naziv = naziv;
        this.pitanja = pitanja;
        this.kategorija = kategorija;
        this.firebaseId = firebaseId;
    }

    public Kviz(String naziv, Kategorija kategorija) {
        this.naziv = naziv;
        this.kategorija = kategorija;
        generisiFirebaseId();
    }


    public Kviz(String naziv, Kategorija kategorija, ArrayList<Pitanje> pitanja) {
        this.naziv = naziv;
        this.kategorija = kategorija;
        this.pitanja = pitanja;
        generisiFirebaseId();
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public ArrayList<Pitanje> getPitanja() {
        return pitanja;
    }

    public void setPitanja(ArrayList<Pitanje> pitanja) {
        this.pitanja = pitanja;
    }

    public Kategorija getKategorija() {
        return kategorija;
    }

    public void setKategorija(Kategorija kategorija) {
        this.kategorija = kategorija;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass())
            return false;

        return this.naziv.equals(((Kviz) obj).naziv);
    }

    private void generisiFirebaseId() {
        firebaseId = "QUIZ[" + UUID.randomUUID().toString().toUpperCase() + "]";
    }

    public String firebaseId() {
        return firebaseId;
    }

    @Override
    public int hashCode() {
        return (naziv + pitanja.size()).hashCode() + kategorija.hashCode();
    }

    @Override
    public int compareTo(Kviz o) {
        return this.getNaziv().compareTo(o.getNaziv());
    }


    public Kviz(Parcel in) {
        this.naziv = in.readString();
        this.pitanja = new ArrayList<>();
        in.readList(this.pitanja, Pitanje.class.getClassLoader());
        this.kategorija = in.readParcelable(Kategorija.class.getClassLoader());
        this.firebaseId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.naziv);
        dest.writeList(this.pitanja);
        dest.writeParcelable(this.kategorija, flags);
        dest.writeString(this.firebaseId);
    }

    public static final Parcelable.Creator<Kviz> CREATOR = new Parcelable.Creator<Kviz>() {
        @Override
        public Kviz createFromParcel(Parcel source) {
            return new Kviz(source);
        }

        @Override
        public Kviz[] newArray(int size) {
            return new Kviz[size];
        }
    };
}
