package ba.unsa.etf.rma.klase;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class FirebaseIntentService extends IntentService {
    private static final String TAG = "FirebaseIntentService";

    private String databaseUrl =
            "https://firestore.googleapis.com/v1/projects/rma19mandalanel18088/databases/(default)/documents/";
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
        final int action = intent.getIntExtra("action", 0);
        Bundle bundle = new Bundle();

        switch (action) {
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
                    resultReceiver.send(RESULT_OK, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case FILTRIRAJ_KVIZOVE:
                try {
                    ArrayList<Kategorija> kategorije = importCategories();
                    bundle.putParcelableArrayList("kategorije", kategorije);
                    bundle.putParcelableArrayList("kvizovi", quizFilter(kategorije, intent.getStringExtra("kategorijaId")));
                    resultReceiver.send(RESULT_OK, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

    }

    private void openConnection() throws Exception {
        connection = (HttpURLConnection) url.openConnection();
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
        String connectionUrl = databaseUrl + "Kategorije?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection();

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaKategorija = getResponse();
        return firestoreJsonParser.parsirajKategorije(listaKategorija);
    }

    private ArrayList<Pitanje> importQuestions() throws Exception {
        String connectionUrl = databaseUrl + "Pitanja?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection();

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaPitanja = getResponse();
        return firestoreJsonParser.parsirajPitanja(listaPitanja);
    }

    private ArrayList<Kviz> importQuizzes(ArrayList<Kategorija> kategorije, ArrayList<Pitanje> pitanja) throws Exception {
        String connectionUrl = databaseUrl + "Kvizovi?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection();

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaKvizova = getResponse();
        return firestoreJsonParser.parsirajKvizove(listaKvizova, kategorije, pitanja);
    }


    private ArrayList<Kviz> quizFilter(ArrayList<Kategorija> kategorije, final String kategorijaFirebaseId) throws Exception {
        ArrayList<Kviz> kvizovi;
        if(kategorije == null)
             kvizovi = importQuizzes(importCategories(), importQuestions());
        else
            kvizovi = importQuizzes(kategorije, importQuestions());

        if (kategorijaFirebaseId.equals("CAT[-ALL-]"))
            return kvizovi;

        ArrayList<Kviz> filtriraniKvizovi = new ArrayList<>();
        for (Kviz k : kvizovi) {
            if (k.getKategorija().firebaseId().equals(kategorijaFirebaseId))
                filtriraniKvizovi.add(k);
        }

        return filtriraniKvizovi;
    }
}
