package ba.unsa.etf.rma.modeli;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

public class Kategorija implements Parcelable {
    private String naziv;
    private int idIkonice;
    private String firestoreId;

    public Kategorija(String naziv, int idIkonice, String firestoreId) {
        this.naziv = naziv;
        this.idIkonice = idIkonice;
        this.firestoreId = firestoreId;
    }

    public Kategorija(String naziv, int idIkonice) {
        this.naziv = naziv;
        this.idIkonice = idIkonice;
        generisiFirestoreId();
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public int getIdIkonice() {
        return idIkonice;
    }

    private void generisiFirestoreId() {
        if (naziv.equals("Svi") && idIkonice == -1)
            firestoreId = "CAT[-ALL-]";
        else
            firestoreId = "CAT[" + UUID.randomUUID().toString().toUpperCase() + "]";
    }

    public String firestoreId() {
        return firestoreId;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return naziv;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass())
            return false;

        return this.naziv.equals(((Kategorija) obj).naziv) && this.idIkonice == ((Kategorija) obj).idIkonice;
    }

    @Override
    public int hashCode() {
        return (idIkonice + naziv).hashCode();
    }

    public Kategorija(Parcel in) {
        this.naziv = in.readString();
        this.idIkonice = in.readInt();
        this.firestoreId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.naziv);
        dest.writeInt(this.idIkonice);
        dest.writeString(this.firestoreId);
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
