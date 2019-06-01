package ba.unsa.etf.rma.modeli;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class RangListaKviz implements Parcelable {

    private String nazivKviza;
    private String kvizFirebaseId;
    private ArrayList<Pair<String, Double>> lista = new ArrayList<>();

    public RangListaKviz(String nazivKviza, String kvizFirebaseId) {
        this.nazivKviza = nazivKviza;
        this.kvizFirebaseId = kvizFirebaseId;
    }

    public RangListaKviz(String nazivKviza, String kvizFirebaseId, ArrayList<Pair<String, Double>> lista) {
        this.nazivKviza = nazivKviza;
        this.kvizFirebaseId = kvizFirebaseId;
        this.lista = lista;
    }

    public String firebaseId(){
        return "RANK[" + kvizFirebaseId + "]";
    }

    public String getNazivKviza() {
        return nazivKviza;
    }

    public void setNazivKviza(String nazivKviza) {
        this.nazivKviza = nazivKviza;
    }

    public void setKvizFirebaseId(String firebaseId){
        this.kvizFirebaseId = firebaseId;
    }

    public ArrayList<Pair<String, Double>> getLista() {
        return lista;
    }

    public void setLista(ArrayList<Pair<String, Double>> lista) {
        this.lista = lista;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.nazivKviza);
        dest.writeString(this.kvizFirebaseId);
        dest.writeSerializable(this.lista);
    }

    protected RangListaKviz(Parcel in) {
        this.nazivKviza = in.readString();
        this.kvizFirebaseId = in.readString();
        this.lista = (ArrayList<Pair<String, Double>>) in.readSerializable();
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
