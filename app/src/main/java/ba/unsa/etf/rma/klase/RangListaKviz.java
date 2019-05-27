package ba.unsa.etf.rma.klase;

import java.util.HashMap;
import java.util.UUID;

class RangListaKviz {
    String firebaseId;
    String nazivKviza;
    String kvizFirebaseId;
    HashMap<String, HashMap<String, Double>> lista;

    public RangListaKviz(String nazivKviza, String kvizFirebaseId) {
        this.firebaseId = firebaseId;
        this.nazivKviza = nazivKviza;
        generisiFirebaseId();
    }

    public RangListaKviz(String nazivKviza, String kvizFirebaseId, String firebaseId, HashMap<String, HashMap<String, Double>> lista) {
        this.firebaseId = firebaseId;
        this.nazivKviza = nazivKviza;
        this.kvizFirebaseId = kvizFirebaseId;
        this.lista = lista;
    }

    public RangListaKviz(String firebaseId, String nazivKviza, HashMap<String, HashMap<String, Double>> lista) {
        this.firebaseId = firebaseId;
        this.nazivKviza = nazivKviza;
        this.lista = lista;
    }

    public void generisiFirebaseId(){
        firebaseId = "RANK[" + kvizFirebaseId + "]";
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getNazivKviza() {
        return nazivKviza;
    }

    public void setNazivKviza(String nazivKviza) {
        this.nazivKviza = nazivKviza;
    }

    public HashMap<String, HashMap<String, Double>> getLista() {
        return lista;
    }

    public void setLista(HashMap<String, HashMap<String, Double>> lista) {
        this.lista = lista;
    }


}
