package ba.unsa.etf.rma.klase;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import ba.unsa.etf.rma.R;

public class HttpGetRequest extends AsyncTask<String, Void, String> {
    private String databaseUrl =
            "https://firestore.googleapis.com/v1/projects/rma19mandalanel18088/databases/(default)/documents/";
    private String request;
    private String token;
    private URL url;
    private HttpURLConnection connection;

    @Override
    protected String doInBackground(String... strings) {


        try {
            request = strings[0];
            token = strings[1];

        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String getResponse() throws Exception {
        String inputLine;

        //Create a new InputStreamReader, buffered reader and String Builder
        InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(streamReader);
        StringBuilder stringBuilder = new StringBuilder();

        //Check if the line we are reading is not null
        while((inputLine = reader.readLine()) != null){
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
        connection =(HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
    }

    private void importEverything() throws Exception {

    }

    private void importQuestions() throws Exception{
        databaseUrl += "Pitanja?fields=documents(fields%2Cname)" + "&access_token";
        url = new URL(databaseUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection();

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaPitanja = getResponse();
        List<Pitanje> pitanja = firestoreJsonParser.parsirajPitanja(listaPitanja);
    }

    private ArrayList<Kategorija> importCategories() throws Exception {
        databaseUrl += "Kategorije?fields=documents(fields%2Cname)" + "&access_token";
        url = new URL(databaseUrl + URLEncoder.encode(token, "UTF-8"));
        openConnection();

        FirestoreJsonParser firestoreJsonParser = new FirestoreJsonParser();
        String listaKategorija = getResponse();
        return firestoreJsonParser.parsirajKategorije(listaKategorija);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }


}