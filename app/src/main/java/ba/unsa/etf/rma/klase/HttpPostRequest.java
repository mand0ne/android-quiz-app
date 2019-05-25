package ba.unsa.etf.rma.klase;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpPostRequest extends AsyncTask<String, Void, String> {

    private String databaseUrl =
            "https://firestore.googleapis.com/v1/projects/rma19mandalanel18088/databases/(default)/documents/";

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected String doInBackground(String... strings) {

        try {
            databaseUrl += strings[0] + "/" + strings[3] + "?access_token=";
            String token = strings[1];

            URL url = new URL(databaseUrl + URLEncoder.encode(token, "UTF-8"));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PATCH");
            connection.setRequestProperty("Content-Type", "application/json"); //utf-8 je default encoding
            connection.setRequestProperty("Accept", "application/json");

            String dokument = strings[2];

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = dokument.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // int code = connection.getResponseCode();
            InputStream odgovor = connection.getInputStream();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(odgovor, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;

                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                    response.append("\n");
                }

                Log.wtf("ODGOVOR: ", response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }
}
