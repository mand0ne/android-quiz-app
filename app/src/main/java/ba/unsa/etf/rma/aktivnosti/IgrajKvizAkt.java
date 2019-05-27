package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.fragmenti.RangLista;
import ba.unsa.etf.rma.klase.HttpGetRequest;
import ba.unsa.etf.rma.klase.Kviz;

public class IgrajKvizAkt extends AppCompatActivity {

    FragmentManager fragmentManager;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz_akt);
        context = this;
        Intent intent = getIntent();
        Kviz trenutniKviz = intent.getParcelableExtra("kviz");

        fragmentManager = getSupportFragmentManager();

        InformacijeFrag iFrag = new InformacijeFrag();
        PitanjeFrag pFrag = new PitanjeFrag();

        Bundle b = new Bundle();
        b.putParcelable("kviz", trenutniKviz);
        pFrag.setArguments(b);
        iFrag.setArguments(b);

        fragmentManager.beginTransaction().replace(R.id.informacijePlace, iFrag).commit();
        fragmentManager.beginTransaction().replace(R.id.pitanjePlace, pFrag).commit();
    }

    public void azurirajRangListuIPrikazi() {
/*

        // Prvo azuriaj. U onPostExecute nakon azuriranja pozovi prikazivanje.
        String url = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Rangliste/" + igrac.getIdDokumenta() + "?access_token=";
        String dokument = "{\"fields\": { \"nazivKviza\": {\"stringValue\": \"" + igrac.getNazivKviza() + "\"}," +
                "\"lista\": {\"mapValue\": {\"fields\": { \"pozicija\": { \"integerValue\": \"" + pozicija + "\"}, " +
                "\"informacije\": {\"mapValue\": {\"fields\": {\"imeIgraca\": {\"stringValue\": \"" + igrac.getImeIgraca() + "\"}," +
                "\"procenatTacnih\": {\"doubleValue\": " + igrac.getProcenatTacnih() + "}}}}}}}}}";

*/
        fragmentManager.beginTransaction().replace(R.id.pitanjePlace, new RangLista()).commit();
    }

    /*private static class dohvatiRangListu extends AsyncTask<String, Void, Void> {
        private WeakReference<Activity> activityWeakReference;
        private String TAG = getClass().getSimpleName();

        dohvatiRangListu(Activity activityWeakReference) {
            this.activityWeakReference = new WeakReference<>(activityWeakReference);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            KvizoviAkt kvizoviAkt = (KvizoviAkt) activityWeakReference.get();
            if(kvizoviAkt == null || kvizoviAkt.isFinishing()){
                this.cancel(true);
                return;
            }
        }

        protected Void doInBackground(String... params) {
            try {
                IgrajKvizAkt igrajKvizAkt = (IgrajKvizAkt) activityWeakReference.get();

            } catch (Exception e) {
                Log.d(TAG, "doInBackground: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            KvizoviAkt kvizoviAkt = (KvizoviAkt) activityWeakReference.get();
            new HttpGetRequest(kvizoviAkt).execute("ALL", kvizoviAkt.TOKEN);
        }
    }
    */
}
