package ba.unsa.etf.rma.klase;

import android.os.Parcel;
import android.os.Parcelable;

public class Kategorija implements Parcelable {
    private String naziv;
    private String id;

    public Kategorija(String naziv, String id){
        this.naziv = naziv;
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return (id + naziv).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.naziv.equals(((Kategorija)obj).naziv) && this.id.equals(((Kategorija)obj).id);
    }

    public void setId(String id) {
        this.id = id;
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
        dest.writeString(this.id);
    }

    protected Kategorija(Parcel in) {
        this.naziv = in.readString();
        this.id = in.readString();
    }

    public static final Parcelable.Creator<Kategorija> CREATOR = new Parcelable.Creator<Kategorija>() {
        @Override
        public Kategorija createFromParcel(Parcel source) {
            return new Kategorija(source);
        }

        @Override
        public Kategorija[] newArray(int size) {
            return new Kategorija[size];
        }
    };
}
