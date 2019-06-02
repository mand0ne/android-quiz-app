package ba.unsa.etf.rma.firestore;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import ba.unsa.etf.rma.modeli.IgraPair;
import ba.unsa.etf.rma.modeli.Kategorija;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.Pitanje;
import ba.unsa.etf.rma.modeli.RangListaKviz;

public class FirestoreIntentService extends IntentService {
    public enum REQUEST_TYPE {
        GET("GET"), POST("POST"), PATCH("PATCH");

        private final String text;

        REQUEST_TYPE(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static final int DOHVATI_PITANJA = 102;
    public static final int DOHVATI_RANG_LISTU = 103;
    public static final int FILTRIRAJ_KVIZOVE = 110;

    public static final int VALIDNA_KATEGORIJA = 200;
    public static final int VALIDAN_KVIZ = 201;
    public static final int VALIDNO_PITANJE = 202;

    public static final int AZURIRAJ_KATEGORIJE = 300;
    public static final int AZURIRAJ_KVIZOVE = 301;
    public static final int AZURIRAJ_PITANJA = 302;
    public static final int AZURIRAJ_RANG_LISTU = 303;

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
                    String kvizFirebaseId = intent.getStringExtra("kvizFirebaseId");
                    bundle.putParcelable("rangListaKviz", dohvatiRangListu(nazivKviza, kvizFirebaseId));
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
                            dohvatiSpecificneKvizove(kategorije, intent.getStringExtra("kategorijaFirebaseId")));
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
                    bundle.putParcelableArrayList("noveKategorije", dohvatiKategorije());
                    resultReceiver.send(VALIDNA_KATEGORIJA, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case VALIDAN_KVIZ:
                try {
                    String nazivKviza = intent.getStringExtra("nazivKviza");
                    ArrayList<Kategorija> kategorije = dohvatiKategorije();
                    ArrayList<Kviz> kvizovi = dohvatiKvizove(kategorije, dohvatiPitanja());
                    bundle.putBoolean("postojiKviz", postojiKviz(nazivKviza));
                    bundle.putString("nazivKviza", nazivKviza);
                    bundle.putParcelableArrayList("kategorije", kategorije);
                    bundle.putParcelableArrayList("kvizovi", kvizovi);
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
            case AZURIRAJ_KATEGORIJE:
                try {
                    azurirajKategorije((Kategorija) intent.getParcelableExtra("kategorija"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case AZURIRAJ_KVIZOVE:
                try {
                    azurirajKvizove((Kviz) intent.getParcelableExtra("kviz"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case AZURIRAJ_PITANJA:
                try {
                    azurirajPitanja((Pitanje) intent.getParcelableExtra("pitanje"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case AZURIRAJ_RANG_LISTU:
                try {
                    azurirajRangListu((RangListaKviz) intent.getParcelableExtra("rangListaKviz"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

    }

    private void openConnection(REQUEST_TYPE query) throws Exception {
        connection = (HttpURLConnection) url.openConnection();

        if (query == REQUEST_TYPE.POST || query == REQUEST_TYPE.PATCH)
            connection.setDoOutput(true);

        connection.setRequestMethod(query.toString());
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
    }

    private String getResponse() throws Exception {
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

    private ArrayList<Kategorija> dohvatiKategorije() throws Exception {
        String connectionUrl = databaseUrl + "/Kategorije?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection(REQUEST_TYPE.GET);

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaKategorija = getResponse();
        return firestoreJsonParser.parsirajKategorije(listaKategorija);
    }

    private ArrayList<Pitanje> dohvatiPitanja() throws Exception {
        String connectionUrl = databaseUrl + "/Pitanja?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection(REQUEST_TYPE.GET);

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaPitanja = getResponse();
        return firestoreJsonParser.parsirajPitanja(listaPitanja);
    }

    private ArrayList<Kviz> dohvatiKvizove(ArrayList<Kategorija> kategorije, ArrayList<Pitanje> pitanja) throws Exception {
        String connectionUrl = databaseUrl + "/Kvizovi?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection(REQUEST_TYPE.GET);

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaKvizova = getResponse();
        return firestoreJsonParser.parsirajKvizove(listaKvizova, kategorije, pitanja);
    }

    private ArrayList<Kviz> dohvatiSpecificneKvizove(ArrayList<Kategorija> kategorije, final String kategorijaFirebaseId) throws Exception {
        if (kategorijaFirebaseId.equals("CAT[-ALL-]"))
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
                "     \"stringValue\": \"" + kategorijaFirebaseId + "\"\n" +
                "    }\n" +
                "   }\n" +
                "  },\n" +
                "  \"limit\": 1000\n" +
                " }\n" +
                "}\n" +
                "\n";

        String connectionUrl = databaseUrl + ":runQuery?fields=document(fields%2Cname)&access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection(REQUEST_TYPE.POST);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = structuredQuery.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser(true);
        String listaKvizova = getResponse();
        listaKvizova = "{\n\"documents\":" + listaKvizova + "}";
        return firestoreJsonParser.parsirajKvizove(listaKvizova, dohvatiKategorije(), dohvatiPitanja());
    }

    private RangListaKviz dohvatiRangListu(String nazivKviza, String kvizFirebaseId) throws Exception {
        String connectionUrl = databaseUrl + "/Rangliste/RANK[" + kvizFirebaseId + "]?fields=fields%2Cname&access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection(REQUEST_TYPE.GET);

        RangListaKviz rangListaKviz;
        try {
            String rangListaJson = getResponse();
            FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser(false);
            rangListaKviz = firestoreJsonParser.parsirajRangListu(rangListaJson);
            rangListaKviz.setNazivKviza(nazivKviza);
            rangListaKviz.setKvizFirebaseId(kvizFirebaseId);
        } catch (FileNotFoundException e) {
            rangListaKviz = new RangListaKviz(nazivKviza, kvizFirebaseId);
        }

        return rangListaKviz;
    }

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
        openConnection(REQUEST_TYPE.POST);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = structuredQuery.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        String kviz = getResponse();

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
        openConnection(REQUEST_TYPE.POST);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = structuredQuery.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        String pitanje = getResponse();

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
        openConnection(REQUEST_TYPE.POST);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = structuredQuery.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        String kategorija = getResponse();

        try {
            JSONArray jo = new JSONArray(kategorija);
            jo.getJSONObject(0).getJSONObject("document");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void azurirajKategorije(Kategorija kategorija) throws Exception {
        String connectionUrl = databaseUrl + "/Kategorije/" + kategorija.firebaseId() + "?access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection(REQUEST_TYPE.PATCH);

        String dokument = "{\"fields\": { \"naziv\": {\"stringValue\": \"" + kategorija.getNaziv() + "\"}," +
                "\"idIkonice\": {\"integerValue\": \"" + kategorija.getId() + "\"}}}";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = dokument.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        getResponse();
    }

    private void azurirajKvizove(Kviz kviz) throws Exception {
        String connectionUrl = databaseUrl + "/Kvizovi/" + kviz.firebaseId() + "?access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection(REQUEST_TYPE.PATCH);

        StringBuilder dokument = new StringBuilder("{\"fields\": { \"naziv\": {\"stringValue\": \"" + kviz.getNaziv() + "\"}," +
                "\"idKategorije\": {\"stringValue\": \"" + kviz.getKategorija().firebaseId() + "\"}," +
                "\"pitanja\": {\"arrayValue\": { \"values\": [");

        ArrayList<Pitanje> pitanja = kviz.getPitanja();
        for (int i = 0; i < pitanja.size(); i++) {
            dokument.append("{\"stringValue\": \"");
            dokument.append(pitanja.get(i).firebaseId());
            dokument.append("\"}");
            if (i < pitanja.size() - 1)
                dokument.append(",");
        }

        dokument.append("]}}}}");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = dokument.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        getResponse();
    }

    private void azurirajPitanja(Pitanje pitanje) throws Exception {
        String connectionUrl = databaseUrl + "/Pitanja/" + pitanje.firebaseId() + "?access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection(REQUEST_TYPE.PATCH);

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

        getResponse();
    }

    private void azurirajRangListu(RangListaKviz rangListaKviz) throws Exception {
        String connectionUrl = databaseUrl + "/Rangliste/" + rangListaKviz.firebaseId() + "?access_token=";

        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection(REQUEST_TYPE.PATCH);

        StringBuilder dokument = new StringBuilder("{\n" +
                " \"fields\": {\n" +
                "  \"nazivKviza\": {\n" +
                "   \"stringValue\": \"" + rangListaKviz.getNazivKviza() + "\"\n" +
                "  },\n" +
                "  \"lista\": {\n" +
                "   \"mapValue\": {\n" +
                "    \"fields\": {\n");

        ArrayList<IgraPair> pairs = rangListaKviz.getLista();

        for (int i = 0; i < pairs.size(); i++) {
            dokument.append("\"" + (i + 1) + "\": { \n");
            dokument.append("\"mapValue\": {\n");
            dokument.append("\"fields\": {\n");
            dokument.append("\"" + pairs.get(i).first() + "\": {\n");
            dokument.append("\"doubleValue\": " + pairs.get(i).second() + "\n");
            dokument.append("}\n}\n}\n}\n");
            if (i < pairs.size() - 1)
                dokument.append(",");
        }

        dokument.append("}\n}\n}\n}\n}");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = dokument.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        getResponse();
    }
}
