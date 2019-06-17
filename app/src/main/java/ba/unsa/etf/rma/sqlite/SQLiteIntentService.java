package ba.unsa.etf.rma.sqlite;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import ba.unsa.etf.rma.modeli.Kategorija;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.Pitanje;
import ba.unsa.etf.rma.modeli.RangListaKviz;

import static ba.unsa.etf.rma.firestore.FirestoreIntentService.DOHVATI_RANG_LISTU;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.FILTRIRAJ_KVIZOVE;


// NE PITAJTE ME NISTA!
public class SQLiteIntentService extends IntentService {

    public static final int AZURIRAJ_LOKALNU_BAZU = 401;
    public static final int AZURIRAJ_LOKALNE_RANGLISTE = 402;
    public static final int SINKRONIZUJ_RANG_LISTE = 403;

    private static final String TAG = "SQLiteIntentService";

    public SQLiteIntentService() {
        super(TAG);
    }

    public SQLiteIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        final ResultReceiver resultReceiver = intent.getParcelableExtra("receiver");
        final int request = intent.getIntExtra("request", 0);
        Bundle bundle = new Bundle();

        if (request == AZURIRAJ_LOKALNU_BAZU) {
            try {
                azurirajLokalneKategorije(intent.getParcelableArrayListExtra("kategorije"));
                azurirajLokalneKvizove(intent.getParcelableArrayListExtra("kvizovi"));
                azurirajLokalnaPitanja(intent.getParcelableArrayListExtra("pitanja"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (request == FILTRIRAJ_KVIZOVE) {
            try {
                ArrayList<Kategorija> kategorije = AppDbHelper.getInstance(getApplicationContext()).dajSveKategorije();
                bundle.putParcelableArrayList("kategorije", kategorije);
                bundle.putParcelableArrayList("kvizovi", AppDbHelper.getInstance(getApplicationContext())
                        .dajSpecificneKvizove(intent.getStringExtra("kategorijaFirestoreId")));
                resultReceiver.send(FILTRIRAJ_KVIZOVE, bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (request == AZURIRAJ_LOKALNE_RANGLISTE){
            try{
                AppDbHelper.getInstance(getApplicationContext()).azurirajRangListu(
                        intent.getParcelableExtra("rangListaKviz")
                );
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        else if (request == DOHVATI_RANG_LISTU){
            try{
                RangListaKviz rangListaKviz = AppDbHelper.getInstance(getApplicationContext()).dajRangListu(
                        intent.getStringExtra("kvizFirestoreId"));
                bundle.putParcelable("rangListaKviz", rangListaKviz);
                bundle.putString("nickname", intent.getStringExtra("nickname"));
                bundle.putDouble("skor", intent.getDoubleExtra("skor", 0.0) * 100.0);
                resultReceiver.send(DOHVATI_RANG_LISTU, bundle);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        else if(request == SINKRONIZUJ_RANG_LISTE){
            try{
                Kviz igraniKviz = intent.getParcelableExtra("igraniKviz");
                RangListaKviz rangListaKviz = AppDbHelper.getInstance(getApplicationContext()).dajRangListu(igraniKviz.firestoreId());
                bundle.putParcelable("rangListaKviz", rangListaKviz);
                resultReceiver.send(SINKRONIZUJ_RANG_LISTE, bundle);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void azurirajLokalneKategorije(ArrayList<Kategorija> kategorije) {
        for (Kategorija kategorija : kategorije)
            AppDbHelper.getInstance(getApplicationContext()).azurirajKategoriju(kategorija);
    }

    private void azurirajLokalneKvizove(ArrayList<Kviz> kvizovi) {
        for (Kviz kviz : kvizovi)
            AppDbHelper.getInstance(getApplicationContext()).azurirajKviz(kviz);
    }

    private void azurirajLokalnaPitanja(ArrayList<Pitanje> pitanja) {
        for (Pitanje pitanje : pitanja)
            AppDbHelper.getInstance(getApplicationContext()).azurirajPitanje(pitanje);
    }
}