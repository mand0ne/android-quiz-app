package ba.unsa.etf.rma.klase;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.DodajKvizAkt;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;

public class HttpGetRequest extends AsyncTask<String, Void, String> {
    private WeakReference<Activity> activityWeakReference;
    private String databaseUrl =
            "https://firestore.googleapis.com/v1/projects/rma19mandalanel18088/databases/(default)/documents/";

    //private String request;
    private String token;
    private URL url;
    private HttpURLConnection connection;
    private String request;
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Pitanje> pitanja = new ArrayList<>();

    public HttpGetRequest(Activity activityWeakReference) {
        this.activityWeakReference = new WeakReference<>(activityWeakReference);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (activityWeakReference.get() == null ||activityWeakReference.get().isFinishing())
            this.cancel(true);
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            request = strings[0];
            token = strings[1];

            switch (request) {
                case "ALL":
                    importEverything();
                    break;
                case "QUESTIONS":
                    pitanja = importQuestions();
                    break;
                case "QUIZ-FILTER":
                    kvizovi = quizFilter(strings[2]);
                    break;
                case "QUIZ-VALID":
                    kvizovi = quizFilter(strings[2]);
                    ((IActivity)activityWeakReference.get()).azurirajKvizove(kvizovi);
                default:
                    break;
            }

        } catch (Exception e) {
            Log.wtf("HttpGetRequest [doInBackground]: ", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<Kviz> quizFilter(final String kategorijaFirebaseId) throws Exception {
        importEverything();

        if(kategorijaFirebaseId.equals("CAT[-ALL-]"))
            return kvizovi;

        ArrayList<Kviz> filtriraniKvizovi = new ArrayList<>();
        for(Kviz k : kvizovi){
            if(k.getKategorija().firebaseId().equals(kategorijaFirebaseId))
                filtriraniKvizovi.add(k);
        }

        return filtriraniKvizovi;
    }

    private String getResponse() throws Exception {
        String inputLine;

        //Create a new InputStreamReader, buffered reader and String Builder
        InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(streamReader);
        StringBuilder stringBuilder = new StringBuilder();

        //Check if the line we are reading is not null
        while ((inputLine = reader.readLine()) != null) {
            stringBuilder.append(inputLine.trim());
            stringBuilder.append("\n");
        }

        //Close our InputStream and Buffered reader
        reader.close();
        streamReader.close();

        //Set our result equal to our stringBuilder
        return stringBuilder.toString();
    }

    private void openConnection() throws Exception {
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
    }

    private void importEverything() throws Exception {
        if (isCancelled())
            return;

        kategorije = importCategories();
        pitanja = importQuestions();
        kvizovi = importQuizzes(kategorije, pitanja);
    }

    private ArrayList<Kviz> importQuizzes(ArrayList<Kategorija> kategorije, ArrayList<Pitanje> pitanja) throws Exception {
        String connectionUrl = databaseUrl + "Kvizovi?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection();

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaKvizova = getResponse();
        return firestoreJsonParser.parsirajKvizove(listaKvizova, kategorije, pitanja);
    }

    private ArrayList<Pitanje> importQuestions() throws Exception {
        String connectionUrl = databaseUrl + "Pitanja?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection();

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaPitanja = getResponse();
        return firestoreJsonParser.parsirajPitanja(listaPitanja);
    }

    private ArrayList<Kategorija> importCategories() throws Exception {
        String connectionUrl = databaseUrl + "Kategorije?fields=documents(fields%2Cname)" + "&access_token=";
        url = new URL(connectionUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection();

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaKategorija = getResponse();
        return firestoreJsonParser.parsirajKategorije(listaKategorija);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (activityWeakReference.get() == null || activityWeakReference.get().isFinishing())
            return;


        switch (request) {
            case "ALL":
                KvizoviAkt kvizoviAkt = (KvizoviAkt) activityWeakReference.get();
                kvizoviAkt.findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);
                kvizoviAkt.setKategorije(kategorije);
                kvizoviAkt.azurirajKvizove(kvizovi);
                kvizoviAkt.start();
                break;
            case "QUESTIONS":
                DodajKvizAkt dodajKvizAkt = (DodajKvizAkt) activityWeakReference.get();
                dodajKvizAkt.azurirajMoguca(pitanja);
                break;
            case "QUIZ-FILTER":
                IActivity iActivity = (IActivity) activityWeakReference.get();
                ((IActivity)activityWeakReference.get()).azurirajKvizove(kvizovi);
                if(iActivity instanceof KvizoviAkt)
                    activityWeakReference.get().findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
    }


}