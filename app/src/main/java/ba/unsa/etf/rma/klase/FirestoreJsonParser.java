package ba.unsa.etf.rma.klase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class FirestoreJsonParser {

    private boolean filter;

    public FirestoreJsonParser(){
        filter = false;
    }

    public FirestoreJsonParser(boolean b){
        filter = b;
    }

    ArrayList<Kategorija> parsirajKategorije(String kategorijeFirebase) {
        ArrayList<Kategorija> listaKategorija = new ArrayList<>();

        try {
            JSONObject dokumentObjekat = new JSONObject(kategorijeFirebase);
            JSONArray dokumenti = dokumentObjekat.getJSONArray("documents");

            for (int i = 0; i < dokumenti.length(); i++) {
                try{
                    Kategorija novaKategorija = parsirajDokumentKategorije(dokumenti.getJSONObject(i));
                    if (novaKategorija != null)
                        listaKategorija.add(novaKategorija);
                }catch(Exception e){}
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listaKategorija;
    }

    Kategorija parsirajDokumentKategorije(JSONObject kategorija) throws JSONException {
        String name = kategorija.getString("name");
        int index = name.indexOf("Kategorije");
        String firebaseId = name.substring(index + 11);
        String naziv = null, idIkonice = null;

        JSONObject fields = kategorija.getJSONObject("fields");
        JSONObject nazivKategorije = fields.getJSONObject("naziv");
        naziv = nazivKategorije.getString("stringValue");

        JSONObject jsonId = fields.getJSONObject("idIkonice");
        idIkonice = jsonId.getString("integerValue");

        if(naziv == null || idIkonice == null)
            return null;

        return new Kategorija(naziv, idIkonice, firebaseId);
    }

    ArrayList<Pitanje> parsirajPitanja(String pitanjaFirebase) {
        ArrayList<Pitanje> listaPitanja = new ArrayList<>();

        try {
            JSONObject dokumentObjekat = new JSONObject(pitanjaFirebase);
            JSONArray dokumenti = dokumentObjekat.getJSONArray("documents");

            for (int i = 0; i < dokumenti.length(); i++) {
                Pitanje novoPitanje = parsirajDokumentPitanje(dokumenti.getJSONObject(i));
                if (novoPitanje != null)
                    listaPitanja.add(novoPitanje);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listaPitanja;
    }

    Pitanje parsirajDokumentPitanje(JSONObject pitanje) throws JSONException {
        String name = pitanje.getString("name");
        int index = name.indexOf("Pitanja");
        String firebaseId = name.substring(index + 8);
        String naziv = null;
        Integer indexTacnog = null;
        ArrayList<String> odgovori = new ArrayList<>();


        JSONObject fields = pitanje.getJSONObject("fields");
        JSONObject nazivPitanja = fields.getJSONObject("naziv");
        naziv = nazivPitanja.getString("stringValue");

        JSONObject jsonIndex = fields.getJSONObject("indexTacnog");
        indexTacnog = jsonIndex.getInt("integerValue");

        JSONObject jsonOdgovori = fields.getJSONObject("odgovori");
        JSONObject jsonOdgovori2 = jsonOdgovori.getJSONObject("arrayValue");
        JSONArray jsonOdgovoriArray = jsonOdgovori2.getJSONArray("values");

        for(int i = 0; i < jsonOdgovoriArray.length(); i++)
            odgovori.add(jsonOdgovoriArray.getJSONObject(i).getString("stringValue"));

        if (naziv == null || indexTacnog == null || odgovori.isEmpty())
            return null;

        return new Pitanje(naziv, naziv, odgovori, odgovori.get(indexTacnog), firebaseId);
    }

    ArrayList<Kviz> parsirajKvizove(String kvizoviFirebase, ArrayList<Kategorija> kategorije, ArrayList<Pitanje> pitanja) {
        ArrayList<Kviz> listaKvizova = new ArrayList<>();

        try {
            JSONObject dokumentObjekat = new JSONObject(kvizoviFirebase);
            JSONArray dokumenti = dokumentObjekat.getJSONArray("documents");

            for (int i = 0; i < dokumenti.length(); i++) {
                JSONObject jsonObjectKviz = dokumenti.getJSONObject(i);
                if(filter)
                    jsonObjectKviz = jsonObjectKviz.getJSONObject("document");

                Kviz noviKviz = parsirajDokumentKviz(jsonObjectKviz, kategorije, pitanja);
                if (noviKviz != null)
                    listaKvizova.add(noviKviz);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listaKvizova;
    }

    Kviz parsirajDokumentKviz(JSONObject kviz, ArrayList<Kategorija> kategorije, ArrayList<Pitanje> pitanja) throws JSONException {

        String name = kviz.getString("name");
        int index = name.indexOf("Kvizovi");
        String firebaseId = name.substring(index + 8);
        String naziv = null, idKategorije = null;
        ArrayList<String> idPitanja = new ArrayList<>();

        JSONObject fields = kviz.getJSONObject("fields");
        JSONObject nazivKategorije = fields.getJSONObject("naziv");
        naziv = nazivKategorije.getString("stringValue");

        JSONObject jsonId = fields.getJSONObject("idKategorije");
        idKategorije = jsonId.getString("stringValue");


        try {
            JSONObject jsonPitanja = fields.getJSONObject("pitanja");
            JSONObject jsonPitanjaArrayValue= jsonPitanja.getJSONObject("arrayValue");
            JSONArray jsonPitanjaArray = jsonPitanjaArrayValue.getJSONArray("values");

            for(int i = 0; i < jsonPitanjaArray.length(); i++)
                idPitanja.add(jsonPitanjaArray.getJSONObject(i).getString("stringValue"));
        }catch (Exception e){

        }

        Kategorija kategorijaKviza = null;
        ArrayList<Pitanje> pitanjaKviza = new ArrayList<>();

        for (Kategorija k : kategorije)
            if (k.firebaseId().equals(idKategorije))
                kategorijaKviza = k;
        if (kategorijaKviza == null)
            kategorijaKviza = new Kategorija("Svi", "-1");


        for (Pitanje p : pitanja) {
            for (String pitanjeID : idPitanja) {
                if (p.firebaseId().equals(pitanjeID))
                    pitanjaKviza.add(p);
            }
        }

        return new Kviz(naziv, pitanjaKviza, kategorijaKviza, firebaseId);
    }
}