package ba.unsa.etf.rma.firestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import ba.unsa.etf.rma.modeli.Igrac;
import ba.unsa.etf.rma.modeli.Kategorija;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.Pitanje;
import ba.unsa.etf.rma.modeli.RangListaKviz;

class FirestoreJsonParser {

    private final boolean filter;

    FirestoreJsonParser() {
        filter = false;
    }

    FirestoreJsonParser(boolean b) {
        filter = b;
    }

    ArrayList<Kategorija> parsirajKategorije(String kategorijeFirebase) {
        ArrayList<Kategorija> listaKategorija = new ArrayList<>();

        try {
            JSONObject dokumentObjekat = new JSONObject(kategorijeFirebase);
            JSONArray dokumenti = dokumentObjekat.getJSONArray("documents");

            for (int i = 0; i < dokumenti.length(); i++) {
                try {
                    Kategorija novaKategorija = parsirajDokumentKategorije(dokumenti.getJSONObject(i));
                    if (novaKategorija != null)
                        listaKategorija.add(novaKategorija);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        listaKategorija.sort((o1, o2) -> o1.getNaziv().compareTo(o2.getNaziv()));
        return listaKategorija;
    }

    Kategorija parsirajDokumentKategorije(JSONObject kategorija) throws JSONException {
        String name = kategorija.getString("name");
        int index = name.indexOf("Kategorije");
        String firebaseId = name.substring(index + 11);
        String naziv;
        int idIkonice;

        JSONObject fields = kategorija.getJSONObject("fields");
        JSONObject nazivKategorije = fields.getJSONObject("naziv");
        naziv = nazivKategorije.getString("stringValue");

        JSONObject jsonId = fields.getJSONObject("idIkonice");
        idIkonice = jsonId.getInt("integerValue");

        if (naziv == null)
            return null;

        return new Kategorija(naziv, idIkonice, firebaseId);
    }

    ArrayList<Pitanje> parsirajPitanja(String pitanjaFirebase) {
        ArrayList<Pitanje> listaPitanja = new ArrayList<>();

        try {
            JSONObject dokumentObjekat = new JSONObject(pitanjaFirebase);
            JSONArray dokumenti = dokumentObjekat.getJSONArray("documents");

            for (int i = 0; i < dokumenti.length(); i++) {
                try {
                    Pitanje novoPitanje = parsirajDokumentPitanje(dokumenti.getJSONObject(i));
                    if (novoPitanje != null)
                        listaPitanja.add(novoPitanje);
                } catch (Exception ignored) {
                }
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
        String naziv;
        int indexTacnog;
        ArrayList<String> odgovori = new ArrayList<>();


        JSONObject fields = pitanje.getJSONObject("fields");
        JSONObject nazivPitanja = fields.getJSONObject("naziv");
        naziv = nazivPitanja.getString("stringValue");

        JSONObject jsonIndex = fields.getJSONObject("indexTacnog");
        indexTacnog = jsonIndex.getInt("integerValue");

        JSONObject jsonOdgovori = fields.getJSONObject("odgovori");
        JSONObject jsonOdgovori2 = jsonOdgovori.getJSONObject("arrayValue");
        JSONArray jsonOdgovoriArray = jsonOdgovori2.getJSONArray("values");

        for (int i = 0; i < jsonOdgovoriArray.length(); i++)
            odgovori.add(jsonOdgovoriArray.getJSONObject(i).getString("stringValue"));

        if (naziv == null || odgovori.isEmpty())
            return null;

        return new Pitanje(naziv, odgovori, odgovori.get(indexTacnog), firebaseId);
    }

    ArrayList<Kviz> parsirajKvizove(String kvizoviFirebase, ArrayList<Kategorija> kategorije, ArrayList<Pitanje> pitanja) {
        ArrayList<Kviz> listaKvizova = new ArrayList<>();

        try {
            JSONObject dokumentObjekat = new JSONObject(kvizoviFirebase);
            JSONArray dokumenti = dokumentObjekat.getJSONArray("documents");

            for (int i = 0; i < dokumenti.length(); i++) {
                JSONObject jsonObjectKviz = dokumenti.getJSONObject(i);
                if (filter)
                    jsonObjectKviz = jsonObjectKviz.getJSONObject("document");

                try {
                    Kviz noviKviz = parsirajDokumentKviz(jsonObjectKviz, kategorije, pitanja);
                    listaKvizova.add(noviKviz);
                } catch (Exception ignored) {
                }
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
        String naziv, idKategorije;
        ArrayList<String> idPitanja = new ArrayList<>();

        JSONObject fields = kviz.getJSONObject("fields");
        JSONObject nazivKategorije = fields.getJSONObject("naziv");
        naziv = nazivKategorije.getString("stringValue");

        JSONObject jsonId = fields.getJSONObject("idKategorije");
        idKategorije = jsonId.getString("stringValue");


        try {
            JSONObject jsonPitanja = fields.getJSONObject("pitanja");
            JSONObject jsonPitanjaArrayValue = jsonPitanja.getJSONObject("arrayValue");
            JSONArray jsonPitanjaArray = jsonPitanjaArrayValue.getJSONArray("values");

            for (int i = 0; i < jsonPitanjaArray.length(); i++)
                idPitanja.add(jsonPitanjaArray.getJSONObject(i).getString("stringValue"));
        } catch (Exception ignored) {

        }

        Kategorija kategorijaKviza = null;
        ArrayList<Pitanje> pitanjaKviza = new ArrayList<>();

        for (Kategorija k : kategorije)
            if (k.firestoreId().equals(idKategorije))
                kategorijaKviza = k;
        if (kategorijaKviza == null)
            kategorijaKviza = new Kategorija("Svi", -1);


        for (Pitanje p : pitanja) {
            for (String pitanjeID : idPitanja) {
                if (p.firestoreId().equals(pitanjeID))
                    pitanjaKviza.add(p);
            }
        }

        return new Kviz(naziv, pitanjaKviza, kategorijaKviza, firebaseId);
    }

    RangListaKviz parsirajRangListu(String rangListaFirebase) {
        RangListaKviz rangLista = new RangListaKviz(null, null);
        ArrayList<Igrac> lista = new ArrayList<>();

        try {
            JSONObject dokumentObjekat = new JSONObject(rangListaFirebase);

            JSONObject fields = dokumentObjekat.getJSONObject("fields");
            JSONObject jsonLista = fields.getJSONObject("lista").getJSONObject("mapValue").getJSONObject("fields");

            int pozicija = 1;
            boolean loop = true;
            while (loop) {
                try {
                    JSONObject rezultat = jsonLista.getJSONObject(String.valueOf(pozicija)).getJSONObject("mapValue")
                            .getJSONObject("fields");

                    JSONArray imena = rezultat.names();
                    final String imeIgraca = imena.toString().replaceAll("\\[\"", "").replaceAll("\"]", "");
                    JSONObject igrac = rezultat.getJSONObject(imeIgraca);
                    final double skor = igrac.getDouble("doubleValue");

                    lista.add(new Igrac(imeIgraca, skor));
                    pozicija++;
                } catch (Exception e) {
                    loop = false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        rangLista.setLista(lista);
        return rangLista;
    }
}