package ba.unsa.etf.rma.klase;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class FirebaseIntentService extends IntentService {
    private static final String TAG = "FirebaseIntentService";

    private String databaseUrl =
            "https://firestore.googleapis.com/v1/projects/rma19mandalanel18088/databases/(default)/documents";
    private String token;
    private URL url;
    private HttpURLConnection connection;

    public static final int DOHVATI_KATEGORIJE = 100;
    public static final int DOHVATI_KVIZOVE = 101;
    public static final int DOHVATI_PITANJA = 102;
    public static final int FILTRIRAJ_KVIZOVE = 103;

    public static final int VALIDNA_KATEGORIJA = 200;
    public static final int VALIDAN_KVIZ = 201;
    public static final int VALIDNO_PITANJE = 202;

    public FirebaseIntentService() {
        super(TAG);
    }

    public FirebaseIntentService(String name) {
        super(name);
        setIntentRedelivery(false);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final ResultReceiver resultReceiver = intent.getParcelableExtra("receiver");
        token = intent.getStringExtra("token");
        final int request = intent.getIntExtra("request", 0);
        Bundle bundle = new Bundle();

        switch (request) {
            case DOHVATI_KATEGORIJE:
                try {
                    bundle.putParcelableArrayList("kategorije", importCategories());
                    resultReceiver.send(RESULT_OK, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DOHVATI_KVIZOVE:
                try {
                    bundle.putParcelableArrayList("kvizovi", importQuizzes(importCategories(), importQuestions()));
                    resultReceiver.send(RESULT_OK, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DOHVATI_PITANJA:
                try {
                    bundle.putParcelableArrayList("pitanja", importQuestions());
                    resultReceiver.send(DOHVATI_PITANJA, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case FILTRIRAJ_KVIZOVE:
                try {
                    ArrayList<Kategorija> kategorije = importCategories();
                    bundle.putParcelableArrayList("kategorije", kategorije);
                    bundle.putParcelableArrayList("kvizovi",
                            importSpecificQuizzes(kategorije, intent.getStringExtra("kategorijaId")));
                    resultReceiver.send(RESULT_OK, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case VALIDAN_KVIZ:
                try {
                    String nazivKviza = intent.getStringExtra("nazivKviza");

                    ArrayList<Kategorija> kategorije = importCategories();
                    ArrayList<Kviz> kvizovi = importQuizzes(kategorije, importQuestions());

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
            case VALIDNA_KATEGORIJA:
                try {
                    String nazivKategorije = intent.getStringExtra("nazivKategorije");
                    bundle.putBoolean("postojiKategorija", postojiKategorija(nazivKategorije));
                    bundle.putString("nazivKategorije", nazivKategorije);
                    bundle.putParcelableArrayList("noveKategorije", importCategories());
                    resultReceiver.send(VALIDNA_KATEGORIJA, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

    }

    private void openConnection(boolean query) throws Exception {
        connection = (HttpURLConnection) url.openConnection();
        if (query) {
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
        } else
            connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
    }

    private String getResponse() throws Exception {
        String inputLine;

        InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
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

    private ArrayList<Kategorija> importCategories() throws Exception {
        String connectionUrl = databaseUrl + "/Kategorije?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection(false);

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaKategorija = getResponse();
        return firestoreJsonParser.parsirajKategorije(listaKategorija);
    }

    private ArrayList<Pitanje> importQuestions() throws Exception {
        String connectionUrl = databaseUrl + "/Pitanja?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection(false);

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaPitanja = getResponse();
        return firestoreJsonParser.parsirajPitanja(listaPitanja);
    }

    private ArrayList<Kviz> importQuizzes(ArrayList<Kategorija> kategorije, ArrayList<Pitanje> pitanja) throws Exception {
        String connectionUrl = databaseUrl + "/Kvizovi?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection(false);

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaKvizova = getResponse();
        return firestoreJsonParser.parsirajKvizove(listaKvizova, kategorije, pitanja);
    }

    private ArrayList<Kviz> importSpecificQuizzes(ArrayList<Kategorija> kategorije, final String kategorijaFirebaseId) throws Exception {
        if (kategorijaFirebaseId.equals("CAT[-ALL-]"))
            return importQuizzes(kategorije, importQuestions());

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
        openConnection(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = structuredQuery.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser(true);
        String listaKvizova = getResponse();
        listaKvizova = "{\n\"documents\":" + listaKvizova + "}";
        return firestoreJsonParser.parsirajKvizove(listaKvizova, importCategories(), importQuestions());
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
        openConnection(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = structuredQuery.getBytes("utf-8");
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
        openConnection(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = structuredQuery.getBytes("utf-8");
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
        openConnection(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = structuredQuery.getBytes("utf-8");
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
}
