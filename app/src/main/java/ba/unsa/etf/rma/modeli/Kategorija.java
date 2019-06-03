package ba.unsa.etf.rma.modeli;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

public class Kategorija implements Parcelable {
    private String naziv;
    private String id;
    private String firebaseId;

    public Kategorija(String naziv, String id, String firebaseId) {
        this.naziv = naziv;
        this.id = id;
        this.firebaseId = firebaseId;
    }

    public Kategorija(String naziv, String id) {
        this.naziv = naziv;
        this.id = id;
        generisiFirebaseId();
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

    public void setId(String id) {
        this.id = id;
    }

    private void generisiFirebaseId() {
        if (naziv.equals("Svi") && id.equals("-1"))
            firebaseId = "CAT[-ALL-]";
        else
            firebaseId = "CAT[" + UUID.randomUUID().toString().toUpperCase() + "]";
    }

    public String firebaseId() {
        return firebaseId;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return naziv;
    }

    @Override
    public int hashCode() {
        return (id + naziv).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass())
            return false;

        return this.naziv.equals(((Kategorija) obj).naziv) && this.id.equals(((Kategorija) obj).id);
    }

    public Kategorija(Parcel in) {
        this.naziv = in.readString();
        this.id = in.readString();
        this.firebaseId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.naziv);
        dest.writeString(this.id);
        dest.writeString(this.firebaseId);
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
