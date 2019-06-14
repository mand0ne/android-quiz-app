package ba.unsa.etf.rma.modeli;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class RangListaKviz implements Parcelable {
    private String nazivKviza;
    private String kvizFirestoreId;
    private ArrayList<IgraPair> lista = new ArrayList<>();

    public RangListaKviz(String nazivKviza, String kvizFirestoreId) {
        this.nazivKviza = nazivKviza;
        this.kvizFirestoreId = kvizFirestoreId;
    }

    public RangListaKviz(String nazivKviza, String kvizFirestoreId, ArrayList<IgraPair> lista) {
        this.nazivKviza = nazivKviza;
        this.kvizFirestoreId = kvizFirestoreId;
        this.lista = lista;
    }

    public String getNazivKviza() {
        return nazivKviza;
    }

    public void setNazivKviza(String nazivKviza) {
        this.nazivKviza = nazivKviza;
    }

    public void setKvizFirestoreId(String firebaseId) {
        this.kvizFirestoreId = firebaseId;
    }

    public String firestoreId() {
        return "RANK[" + kvizFirestoreId + "]";
    }

    public ArrayList<IgraPair> getLista() {
        return lista;
    }

    public void setLista(ArrayList<IgraPair> lista) {
        this.lista = lista;
    }

    public RangListaKviz(Parcel in) {
        this.nazivKviza = in.readString();
        this.kvizFirestoreId = in.readString();
        this.lista = in.createTypedArrayList(IgraPair.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.nazivKviza);
        dest.writeString(this.kvizFirestoreId);
        dest.writeTypedList(this.lista);
    }

    public static final Parcelable.Creator<RangListaKviz> CREATOR = new Parcelable.Creator<RangListaKviz>() {
        @Override
        public RangListaKviz createFromParcel(Parcel source) {
            return new RangListaKviz(source);
        }

        @Override
        public RangListaKviz[] newArray(int size) {
            return new RangListaKviz[size];
        }
    };
}
