package ba.unsa.etf.rma.firestore;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

import ba.unsa.etf.rma.modeli.Igrac;
import ba.unsa.etf.rma.modeli.Kategorija;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.Pitanje;
import ba.unsa.etf.rma.modeli.RangListaKviz;
import ba.unsa.etf.rma.sqlite.AppDbHelper;
import ba.unsa.etf.rma.sqlite.SQLiteIntentService;

import static ba.unsa.etf.rma.sqlite.SQLiteIntentService.SINKRONIZUJ_RANG_LISTE;

public class FirestoreIntentService extends IntentService {

    public enum TIP_ZAHTJEVA {
        GET("GET"), POST("POST"), PATCH("PATCH");

        private final String text;

        TIP_ZAHTJEVA(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static final int DODAJ_KVIZ_AKT = 1;

    public static final int DOHVATI_KATEGORIJE = 101;
    public static final int DOHVATI_PITANJA = 102;
    public static final int DOHVATI_RANG_LISTU = 103;
    public static final int FILTRIRAJ_KVIZOVE = 110;

    public static final int VALIDNA_KATEGORIJA = 200;
    public static final int VALIDAN_KVIZ = 201;
    public static final int VALIDNO_PITANJE = 202;
    public static final int VALIDAN_IMPORT = 210;

    public static final int AZURIRAJ_KATEGORIJE = 300;
    public static final int AZURIRAJ_KVIZOVE = 301;
    public static final int AZURIRAJ_PITANJA = 302;
    public static final int AZURIRAJ_RANG_LISTU = 303;

    public static final int AZURIRAJ_LOKALNU_BAZU = 400;

    private static final String TAG = "FirestoreIntentService";
    private String databaseUrl =
            "https://firestore.googleapis.com/v1/projects/rma19mandalanel18088/databases/(default)/documents";
    private String token;
    private URL url;
    private HttpURLConnection connection;

    public FirestoreIntentService() {
        super(TAG);
    }

    public FirestoreIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        final ResultReceiver resultReceiver = intent.getParcelableExtra("receiver");
        token = intent.getStringExtra("token");
        final int request = intent.getIntExtra("request", 0);
        Bundle bundle = new Bundle();

        switch (request) {
            case DODAJ_KVIZ_AKT:
                try {
                    ArrayList<Kategorija> kategorije = dohvatiKategorije();
                    ArrayList<Pitanje> pitanja = dohvatiPitanja();
                    String kvizFirestoreId = intent.getStringExtra("kvizFirestoreId");
                    if (kvizFirestoreId != null) {
                        Kviz trenutniKviz = dohvatiKviz(kvizFirestoreId, kategorije, pitanja);
                        bundle.putParcelable("trenutniKviz", trenutniKviz);
                    }
                    bundle.putParcelableArrayList("kategorije", kategorije);
                    bundle.putParcelableArrayList("pitanja", pitanja);
                    resultReceiver.send(DODAJ_KVIZ_AKT, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DOHVATI_KATEGORIJE:
                try {
                    bundle.putParcelableArrayList("kategorije", dohvatiKategorije());
                    resultReceiver.send(DOHVATI_KATEGORIJE, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DOHVATI_PITANJA:
                try {
                    bundle.putParcelableArrayList("pitanja", dohvatiPitanja());
                    resultReceiver.send(DOHVATI_PITANJA, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DOHVATI_RANG_LISTU:
                try {
                    String nazivKviza = intent.getStringExtra("nazivKviza");
                    String kvizFirestoreId = intent.getStringExtra("kvizFirestoreId");
                    bundle.putParcelable("rangListaKviz", dohvatiRangListu(nazivKviza, kvizFirestoreId));
                    bundle.putString("nickname", intent.getStringExtra("nickname"));
                    bundle.putDouble("skor", intent.getDoubleExtra("skor", 0.0) * 100.0);
                    resultReceiver.send(DOHVATI_RANG_LISTU, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case FILTRIRAJ_KVIZOVE:
                try {
                    ArrayList<Kategorija> kategorije = dohvatiKategorije();
                    bundle.putParcelableArrayList("kategorije", kategorije);
                    bundle.putParcelableArrayList("kvizovi",
                            dohvatiSpecificneKvizove(kategorije, intent.getStringExtra("kategorijaFirestoreId")));
                    resultReceiver.send(FILTRIRAJ_KVIZOVE, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case VALIDNA_KATEGORIJA:
                try {
                    String nazivKategorije = intent.getStringExtra("nazivKategorije");
                    bundle.putBoolean("postojiKategorija", postojiKategorija(nazivKategorije));
                    bundle.putString("nazivKategorije", nazivKategorije);
                    resultReceiver.send(VALIDNA_KATEGORIJA, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case VALIDAN_KVIZ:
                try {
                    String nazivKviza = intent.getStringExtra("nazivKviza");
                    bundle.putBoolean("postojiKviz", postojiKviz(nazivKviza));
                    bundle.putString("nazivKviza", nazivKviza);
                    resultReceiver.send(VALIDAN_KVIZ, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case VALIDNO_PITANJE:
                try {
                    String nazivPitanja = intent.getStringExtra("nazivPitanja");
                    bundle.putBoolean("postojiPitanje", postojiPitanje(nazivPitanja));
                    bundle.putString("nazivPitanja", nazivPitanja);
                    resultReceiver.send(VALIDNO_PITANJE, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case VALIDAN_IMPORT:
                try {
                    boolean validan = true;
                    String nazivKvizaImport = intent.getStringExtra("nazivKvizaImport");
                    String nazivKategorijeImport = intent.getStringExtra("nazivKategorijeImport");

                    if (!nazivKategorijeImport.contains("null-index:"))
                        validan = !postojiKategorija(nazivKategorijeImport);

                    validan = validan && !postojiKviz(nazivKvizaImport);

                    ArrayList<Pitanje> pitanjaImport = intent.getParcelableArrayListExtra("pitanjaImport");
                    for (Pitanje pitanje : pitanjaImport)
                        validan = validan && !postojiPitanje(pitanje.getNaziv());

                    if (validan) {
                        bundle.putString("nazivKvizaImport", nazivKvizaImport);
                        bundle.putString("nazivKategorijeImport", nazivKategorijeImport);
                        bundle.putParcelableArrayList("pitanjaImport", pitanjaImport);
                    }
                    bundle.putBoolean("validanImport", validan);
                    resultReceiver.send(VALIDAN_IMPORT, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            case AZURIRAJ_KATEGORIJE:
                try {
                    azurirajKategorije(intent.getParcelableExtra("kategorija"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case AZURIRAJ_KVIZOVE:
                try {
                    azurirajKvizove(intent.getParcelableExtra("kviz"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case AZURIRAJ_PITANJA:
                try {
                    azurirajPitanja(intent.getParcelableExtra("pitanje"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case AZURIRAJ_RANG_LISTU:
                try {
                    azurirajRangListu(intent.getParcelableExtra("rangListaKviz"));
                    AppDbHelper.getInstance(getApplicationContext()).azurirajRangListu(intent.getParcelableExtra("rangListaKviz"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case AZURIRAJ_LOKALNU_BAZU:
                try {
                    azurirajLokalnuBazu();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case SINKRONIZUJ_RANG_LISTE:
                try {
                    RangListaKviz rangListaKviz = intent.getParcelableExtra("rangListaKviz");
                    sinkronizujRangLisu(rangListaKviz);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            default:
                break;
        }

    }

    private void uspostaviVezu(TIP_ZAHTJEVA query) throws Exception {
        connection = (HttpURLConnection) url.openConnection();

        if (query == TIP_ZAHTJEVA.POST || query == TIP_ZAHTJEVA.PATCH)
            connection.setDoOutput(true);

        connection.setRequestMethod(query.toString());
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
    }

    private String dajOdgovorServera() throws Exception {
        String inputLine;

        if (connection.getResponseCode() == 404)
            throw new FileNotFoundException("Nema dokumenta!");

        InputStreamReader streamReader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        StringBuilder stringBuilder = new StringBuilder();

        while ((inputLine = reader.readLine()) != null) {
            stringBuilder.append(inputLine.trim());
            stringBuilder.append("\n");
        }

        reader.close();
        streamReader.close();

        return stringBuilder.toString();
    }


    private Kviz dohvatiKviz(String kvizFirestoreId, ArrayList<Kategorija> kategorije, ArrayList<Pitanje> pitanja) throws Exception {
        String connectionUrl = databaseUrl + "/Kvizovi/" + kvizFirestoreId + "?fields=fields%2Cname&access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.GET);

        String kvizJson = dajOdgovorServera();
        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser(false);
        return firestoreJsonParser.parsirajDokumentKviz(new JSONObject(kvizJson), kategorije, pitanja);
    }


    // Sljedece metode dohvacaju odgovarajuci dokumente iz baze
    private ArrayList<Kategorija> dohvatiKategorije() throws Exception {
        String connectionUrl = databaseUrl + "/Kategorije?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.GET);

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaKategorija = dajOdgovorServera();
        return firestoreJsonParser.parsirajKategorije(listaKategorija);
    }

    private ArrayList<Pitanje> dohvatiPitanja() throws Exception {
        String connectionUrl = databaseUrl + "/Pitanja?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.GET);

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaPitanja = dajOdgovorServera();
        return firestoreJsonParser.parsirajPitanja(listaPitanja);
    }

    private ArrayList<Kviz> dohvatiKvizove(ArrayList<Kategorija> kategorije, ArrayList<Pitanje> pitanja) throws Exception {
        String connectionUrl = databaseUrl + "/Kvizovi?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.GET);

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaKvizova = dajOdgovorServera();
        return firestoreJsonParser.parsirajKvizove(listaKvizova, kategorije, pitanja);
    }

    private ArrayList<Kviz> dohvatiSpecificneKvizove(ArrayList<Kategorija> kategorije, final String kategorijaFirestoreId) throws Exception {
        if (kategorijaFirestoreId.equals("CAT[-ALL-]"))
            return dohvatiKvizove(kategorije, dohvatiPitanja());

        String structuredQuery = "{\n" +
                " \"structuredQuery\": {\n" +
                "  \"select\": {\n" +
                "   \"fields\": [\n" +
                "    {\n" +
                "     \"fieldPath\": \"idKategorije\"\n" +
                "    },\n" +
                "    {\n" +
                "     \"fieldPath\": \"naziv\"\n" +
                "    },\n" +
                "    {\n" +
                "     \"fieldPath\": \"pitanja\"\n" +
                "    }\n" +
                "   ]\n" +
                "  },\n" +
                "  \"from\": [\n" +
                "   {\n" +
                "    \"collectionId\": \"Kvizovi\"\n" +
                "   }\n" +
                "  ],\n" +
                "  \"where\": {\n" +
                "   \"fieldFilter\": {\n" +
                "    \"field\": {\n" +
                "     \"fieldPath\": \"idKategorije\"\n" +
                "    },\n" +
                "    \"op\": \"EQUAL\",\n" +
                "    \"value\": {\n" +
                "     \"stringValue\": \"" + kategorijaFirestoreId + "\"\n" +
                "    }\n" +
                "   }\n" +
                "  },\n" +
                " }\n" +
                "}\n" +
                "\n";

        String connectionUrl = databaseUrl + ":runQuery?fields=document(fields%2Cname)&access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.POST);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = structuredQuery.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser(true);
        String listaKvizova = dajOdgovorServera();
        listaKvizova = "{\n\"documents\":" + listaKvizova + "}";
        return firestoreJsonParser.parsirajKvizove(listaKvizova, kategorije, dohvatiPitanja());
    }

    private RangListaKviz dohvatiRangListu(String nazivKviza, String kvizFirestoreId) throws Exception {
        String connectionUrl = databaseUrl + "/Rangliste/RANK[" + kvizFirestoreId + "]?fields=fields%2Cname&access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.GET);

        RangListaKviz rangListaKviz;
        try {
            String rangListaJson = dajOdgovorServera();
            FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser(false);
            rangListaKviz = firestoreJsonParser.parsirajRangListu(rangListaJson);
            rangListaKviz.setNazivKviza(nazivKviza);
            rangListaKviz.setKvizFirestoreId(kvizFirestoreId);
        } catch (FileNotFoundException e) {
            rangListaKviz = new RangListaKviz(nazivKviza, kvizFirestoreId);
        }

        return rangListaKviz;
    }


    // Sljedece metode testiraju da li postoji odgovarajuci podatak u bazi
    private boolean postojiKviz(String nazivKviza) throws Exception {
        String structuredQuery = "{\n" +
                " \"structuredQuery\": {\n" +
                "  \"select\": {\n" +
                "   \"fields\": [\n" +
                "    {\n" +
                "     \"fieldPath\": \"naziv\"\n" +
                "    }\n" +
                "   ]\n" +
                "  },\n" +
                "  \"from\": [\n" +
                "   {\n" +
                "    \"collectionId\": \"Kvizovi\"\n" +
                "   }\n" +
                "  ],\n" +
                "  \"where\": {\n" +
                "   \"fieldFilter\": {\n" +
                "    \"field\": {\n" +
                "     \"fieldPath\": \"naziv\"\n" +
                "    },\n" +
                "    \"op\": \"EQUAL\",\n" +
                "    \"value\": {\n" +
                "     \"stringValue\": \"" + nazivKviza + "\"\n" +
                "    }\n" +
                "   }\n" +
                "  }\n" +
                " }\n" +
                "}";

        String connectionUrl = databaseUrl + ":runQuery?fields=document(fields%2Cname)&access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.POST);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = structuredQuery.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        String kviz = dajOdgovorServera();

        try {
            JSONArray jo = new JSONArray(kviz);
            jo.getJSONObject(0).getJSONObject("document");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean postojiPitanje(String nazivPitanja) throws Exception {
        String structuredQuery = "{\n" +
                " \"structuredQuery\": {\n" +
                "  \"select\": {\n" +
                "   \"fields\": [\n" +
                "    {\n" +
                "     \"fieldPath\": \"naziv\"\n" +
                "    }\n" +
                "   ]\n" +
                "  },\n" +
                "  \"from\": [\n" +
                "   {\n" +
                "    \"collectionId\": \"Pitanja\"\n" +
                "   }\n" +
                "  ],\n" +
                "  \"where\": {\n" +
                "   \"fieldFilter\": {\n" +
                "    \"field\": {\n" +
                "     \"fieldPath\": \"naziv\"\n" +
                "    },\n" +
                "    \"op\": \"EQUAL\",\n" +
                "    \"value\": {\n" +
                "     \"stringValue\": \"" + nazivPitanja + "\"\n" +
                "    }\n" +
                "   }\n" +
                "  }\n" +
                " }\n" +
                "}";

        String connectionUrl = databaseUrl + ":runQuery?fields=document(fields%2Cname)&access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.POST);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = structuredQuery.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        String pitanje = dajOdgovorServera();

        try {
            JSONArray jo = new JSONArray(pitanje);
            jo.getJSONObject(0).getJSONObject("document");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean postojiKategorija(String nazivKategorije) throws Exception {
        String structuredQuery = "{\n" +
                " \"structuredQuery\": {\n" +
                "  \"select\": {\n" +
                "   \"fields\": [\n" +
                "    {\n" +
                "     \"fieldPath\": \"naziv\"\n" +
                "    }\n" +
                "   ]\n" +
                "  },\n" +
                "  \"from\": [\n" +
                "   {\n" +
                "    \"collectionId\": \"Kategorije\"\n" +
                "   }\n" +
                "  ],\n" +
                "  \"where\": {\n" +
                "   \"fieldFilter\": {\n" +
                "    \"field\": {\n" +
                "     \"fieldPath\": \"naziv\"\n" +
                "    },\n" +
                "    \"op\": \"EQUAL\",\n" +
                "    \"value\": {\n" +
                "     \"stringValue\": \"" + nazivKategorije + "\"\n" +
                "    }\n" +
                "   }\n" +
                "  }\n" +
                " }\n" +
                "}";

        String connectionUrl = databaseUrl + ":runQuery?fields=document(fields%2Cname)&access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.POST);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = structuredQuery.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        String kategorija = dajOdgovorServera();

        try {
            JSONArray jo = new JSONArray(kategorija);
            jo.getJSONObject(0).getJSONObject("document");
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    // Sljedece metode salju PATCH request i azuriraju specificni dokument u bazi
    private void azurirajKategorije(Kategorija kategorija) throws Exception {
        String connectionUrl = databaseUrl + "/Kategorije/" + kategorija.firestoreId() + "?access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.PATCH);

        String dokument = "{\"fields\": { \"naziv\": {\"stringValue\": \"" + kategorija.getNaziv() + "\"}," +
                "\"idIkonice\": {\"integerValue\": \"" + kategorija.getIdIkonice() + "\"}}}";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = dokument.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        dajOdgovorServera();
    }

    private void azurirajKvizove(Kviz kviz) throws Exception {
        String connectionUrl = databaseUrl + "/Kvizovi/" + kviz.firestoreId() + "?access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.PATCH);

        StringBuilder dokument = new StringBuilder("{\"fields\": { \"naziv\": {\"stringValue\": \"" + kviz.getNaziv() + "\"}," +
                "\"idKategorije\": {\"stringValue\": \"" + kviz.getKategorija().firestoreId() + "\"}," +
                "\"pitanja\": {\"arrayValue\": { \"values\": [");

        ArrayList<Pitanje> pitanja = kviz.getPitanja();
        for (int i = 0; i < pitanja.size(); i++) {
            dokument.append("{\"stringValue\": \"");
            dokument.append(pitanja.get(i).firestoreId());
            dokument.append("\"}");
            if (i < pitanja.size() - 1)
                dokument.append(",");
        }

        dokument.append("]}}}}");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = dokument.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        dajOdgovorServera();
    }

    private void azurirajPitanja(Pitanje pitanje) throws Exception {
        String connectionUrl = databaseUrl + "/Pitanja/" + pitanje.firestoreId() + "?access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.PATCH);

        StringBuilder dokument = new StringBuilder("{\"fields\": { \"naziv\": {\"stringValue\": \"" + pitanje.getNaziv() + "\"}," +
                "\"odgovori\": {\"arrayValue\": {\"values\": [");

        int indexTacnog = 0;

        ArrayList<String> odgovori = pitanje.getOdgovori();
        for (int i = 0; i < odgovori.size(); i++) {
            dokument.append("{\"stringValue\": \"");
            dokument.append(odgovori.get(i));
            dokument.append("\"}");
            if (i < odgovori.size() - 1)
                dokument.append(",");
            if (odgovori.get(i).equals(pitanje.getTacan()))
                indexTacnog = i;
        }

        dokument.append("]}}, \"indexTacnog\": {\"integerValue\": \"");
        dokument.append(indexTacnog);
        dokument.append("\"}}}");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = dokument.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        dajOdgovorServera();
    }

    private void azurirajRangListu(RangListaKviz rangListaKviz) throws Exception {
        String connectionUrl = databaseUrl + "/Rangliste/" + rangListaKviz.firestoreId() + "?access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        uspostaviVezu(TIP_ZAHTJEVA.PATCH);

        StringBuilder dokument = new StringBuilder("{\n" +
                " \"fields\": {\n" +
                "  \"nazivKviza\": {\n" +
                "   \"stringValue\": \"" + rangListaKviz.getNazivKviza() + "\"\n" +
                "  },\n" +
                "  \"lista\": {\n" +
                "   \"mapValue\": {\n" +
                "    \"fields\": {\n");

        ArrayList<Igrac> pairs = rangListaKviz.getLista();

        for (int i = 0; i < pairs.size(); i++) {
            dokument.append("\"").append(i + 1).append("\": { \n");
            dokument.append("\"mapValue\": {\n");
            dokument.append("\"fields\": {\n");
            dokument.append("\"").append(pairs.get(i).nickname()).append("\": {\n");
            dokument.append("\"doubleValue\": ").append(pairs.get(i).score()).append("\n");
            dokument.append("}\n}\n}\n}\n");
            if (i < pairs.size() - 1)
                dokument.append(",");
        }

        dokument.append("}\n}\n}\n}\n}");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = dokument.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        dajOdgovorServera();
    }


    private void azurirajLokalnuBazu() throws Exception {
        ArrayList<Kategorija> kategorije = dohvatiKategorije();
        ArrayList<Pitanje> pitanja = dohvatiPitanja();
        ArrayList<Kviz> kvizovi = dohvatiKvizove(kategorije, pitanja);

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, getApplicationContext(), SQLiteIntentService.class);
        intent.putExtra("request", SQLiteIntentService.AZURIRAJ_LOKALNU_BAZU);
        intent.putParcelableArrayListExtra("kategorije", kategorije);
        intent.putParcelableArrayListExtra("kvizovi", kvizovi);
        intent.putParcelableArrayListExtra("pitanja", pitanja);

        startService(intent);
    }

    private void sinkronizujRangLisu(RangListaKviz rangListaKviz) throws Exception {
        RangListaKviz firestoreRangLista = dohvatiRangListu(rangListaKviz.getNazivKviza(), rangListaKviz.getKvizFirestoreId());

        TreeSet<Igrac> lista = new TreeSet<>(firestoreRangLista.getLista());
        lista.addAll(rangListaKviz.getLista());

        ArrayList<Igrac> novaLista = new ArrayList<>(lista);
        Collections.sort(novaLista, Collections.reverseOrder());
        firestoreRangLista.setLista(novaLista);
        azurirajRangListu(firestoreRangLista);
    }
}

