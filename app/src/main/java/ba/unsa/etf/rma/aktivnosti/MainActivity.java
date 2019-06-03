package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import ba.unsa.etf.rma.R;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private String TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

    }

    @Override
    protected void onStart() {
        super.onStart();
        promptConnection();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1337)
            promptConnection();
    }

    private void promptConnection(){
        if (isNetworkAvailable())
            new getAccessToken(this).execute();
        else
            showDialog();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connect to the internet or quit")
                .setCancelable(false)
                .setNegativeButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 1337);
                    }
                })
                .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((KvizoviAkt) context).finishAndRemoveTask();
                        System.exit(0);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void pokreniAplikaciju() {
        Toast.makeText(context, "Konektovani ste!", Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, KvizoviAkt.class);
                intent.putExtra("token", TOKEN);
                startActivity(intent);
                finish();

            }
        }, 1500);
    }

    private static class getAccessToken extends AsyncTask<String, Void, Void> {
        private WeakReference<Activity> activityWeakReference;
        private String TAG = getClass().getSimpleName();

        getAccessToken(Activity activityWeakReference) {
            this.activityWeakReference = new WeakReference<>(activityWeakReference);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            MainActivity mainActivity = (MainActivity) activityWeakReference.get();
            if (mainActivity == null || mainActivity.isFinishing())
                this.cancel(true);
        }

        protected Void doInBackground(String... params) {
            try {
                MainActivity mainActivity = (MainActivity) activityWeakReference.get();
                InputStream is = mainActivity.context.getResources().openRawResource(R.raw.secret);
                GoogleCredential credentials = GoogleCredential.fromStream(is)
                        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                mainActivity.TOKEN = credentials.getAccessToken();
                Log.d(TAG, "TOKEN: " + mainActivity.TOKEN);
            } catch (Exception e) {
                Log.d(TAG, "doInBackground: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            MainActivity mainActivity = (MainActivity) activityWeakReference.get();
            mainActivity.pokreniAplikaciju();
        }
    }
}
