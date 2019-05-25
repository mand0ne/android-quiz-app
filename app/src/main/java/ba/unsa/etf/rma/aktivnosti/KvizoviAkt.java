package ba.unsa.etf.rma.aktivnosti;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.fragmenti.ListaFrag;
import ba.unsa.etf.rma.klase.CustomAdapter;
import ba.unsa.etf.rma.klase.HttpGetRequest;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.NDSpinner;

public class KvizoviAkt extends AppCompatActivity {

    static final int DODAJ_KVIZ = 30;
    static final int PROMIJENI_KVIZ = 31;

    private Context context;
    private ListView lvKvizovi;
    private NDSpinner spPostojeceKategorije;
    private View lvFooterView;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> sviKvizovi = new ArrayList<>();
    private ArrayList<Kviz> prikazaniKvizovi = new ArrayList<>();

    private ArrayAdapter<Kategorija> sAdapter = null;
    private CustomAdapter adapter = null;
    private ProgressBar progressBar;

    private double dpwidth = 0.0;
    private ListaFrag listaFrag;
    private DetailFrag detailFrag;
    private String TOKEN = ""; // firestore access_token

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kvizovi_akt);
        context = KvizoviAkt.this;

        new getAccessToken().execute();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dpwidth = displayMetrics.widthPixels / displayMetrics.density;

        kategorije.add(new Kategorija("Svi", "-1"));

        if (savedInstanceState != null) {
            sviKvizovi = savedInstanceState.getParcelableArrayList("kvizovi");
            kategorije = savedInstanceState.getParcelableArrayList("kategorije");
        }

        if (dpwidth >= 550) {
            Intent intent = getIntent();
            Kviz trenutniKviz = intent.getParcelableExtra("kviz");

            FragmentManager fragmentManager = getSupportFragmentManager();

            Bundle b = new Bundle();
            b.putParcelable("kviz", trenutniKviz);

            b.putParcelableArrayList("kvizovi", sviKvizovi);
            b.putParcelableArrayList("kategorije", kategorije);

            listaFrag = new ListaFrag();
            detailFrag = new DetailFrag();

            listaFrag.setArguments(b);
            detailFrag.setArguments(b);

            fragmentManager.beginTransaction().replace(R.id.listPlace, listaFrag).commit();
            fragmentManager.beginTransaction().replace(R.id.detailPlace, detailFrag).commit();
        } else {

            lvKvizovi = findViewById(R.id.lvKvizovi);
            spPostojeceKategorije = findViewById(R.id.spPostojeceKategorije);

            initialize();

            sAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, kategorije);
            sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spPostojeceKategorije.setAdapter(sAdapter);

            // Klik na zadnji element liste koji je za dodavanje novog kviza
            lvFooterView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    aktivnostDodajKviz();
                                                }
                                            }
            );

            // Klik na kviz za uredivanje
            lvKvizovi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                    aktivnostUrediKviz(kliknutiKviz);
                    return true;
                }
            });

            lvKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                    aktivnostIgrajKviz(kliknutiKviz);
                }
            });

            spPostojeceKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Kategorija ka = (Kategorija) spPostojeceKategorije.getSelectedItem();

                    if (ka != null) {
                        if (ka.getId().equals("-1")) {
                            prikazaniKvizovi.clear();
                            prikazaniKvizovi.addAll(sviKvizovi);
                        } else {
                            prikazaniKvizovi.clear();
                            for (Kviz k : sviKvizovi)
                                if (k.getKategorija() != null && k.getKategorija().getNaziv().equals(ka.getNaziv())
                                        || ka.getId().equals("-1"))
                                    prikazaniKvizovi.add(k);
                        }

                        adapter.notifyDataSetChanged();
                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    spPostojeceKategorije.setSelection(0);
                }
            });
        }

    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void aktivnostIgrajKviz(Kviz kliknutiKviz) {
        Intent intent = new Intent(context, IgrajKvizAkt.class);
        intent.putExtra("kviz", kliknutiKviz);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (data != null) {
                Kviz novi = data.getParcelableExtra("kviz");
                if (requestCode == PROMIJENI_KVIZ) {
                    int index = dajIndexKviza(data.getStringExtra("staroImeKviza"));
                    sviKvizovi.set(index, novi);
                } else if (requestCode == DODAJ_KVIZ)
                    sviKvizovi.add(novi);


                if (dpwidth < 550)
                    spPostojeceKategorije.setSelection(spPostojeceKategorije.getSelectedItemPosition());
                else
                    detailFrag.azurirajKvizove(sviKvizovi);

                azurirajKategorije(data);
            }
        } else if (resultCode == RESULT_CANCELED)
            azurirajKategorije(data);
    }


    private int dajIndexKviza(String staroImeKviza) {
        for (int i = 0; i < sviKvizovi.size(); i++)
            if (sviKvizovi.get(i).getNaziv().equals(staroImeKviza))
                return i;

        return sviKvizovi.size() - 1;
    }

    public void azurirajKategorije(@Nullable Intent data) {
        kategorije.clear();
        assert data != null;
        kategorije.addAll(data.<Kategorija>getParcelableArrayListExtra("kategorije"));
        kategorije.remove(kategorije.size() - 1);
        if (dpwidth < 550)
            sAdapter.notifyDataSetChanged();
        else
            listaFrag.azurirajKategorije(kategorije);

    }

    private void initialize() {
        // Za ListView
        adapter = new CustomAdapter(context, prikazaniKvizovi);
        lvKvizovi.setAdapter(adapter);
        lvKvizovi.addFooterView(lvFooterView = adapter.getFooterView(lvKvizovi, "Dodaj kviz"));
    }


    public void aktivnostDodajKviz() {
       /* Intent intent = new Intent(context, DodajKvizAkt.class);
        intent.putParcelableArrayListExtra("kategorije", kategorije);
        intent.putParcelableArrayListExtra("kvizovi", sviKvizovi);
        intent.putExtra("requestCode", DODAJ_KVIZ);
        intent.putExtra("token", TOKEN);
        startActivityForResult(intent, DODAJ_KVIZ);
    */
    }

    public void aktivnostUrediKviz(Kviz kviz) {
        Intent intent = new Intent(context, DodajKvizAkt.class);
        intent.putParcelableArrayListExtra("kategorije", kategorije);
        intent.putParcelableArrayListExtra("kvizovi", sviKvizovi);
        intent.putExtra("kviz", kviz);
        intent.putExtra("requestCode", PROMIJENI_KVIZ);
        intent.putExtra("token", TOKEN);
        startActivityForResult(intent, PROMIJENI_KVIZ);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("kvizovi", sviKvizovi);
        outState.putParcelableArrayList("kategorije", kategorije);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }


    class getAccessToken extends AsyncTask<String, Void, Void> {
        String TAG = getClass().getSimpleName();

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(String... params) {
            try {
                InputStream is = context.getResources().openRawResource(R.raw.secret);
                GoogleCredential credentials = GoogleCredential.fromStream(is)
                        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                TOKEN = credentials.getAccessToken();
                Log.d(TAG, "TOKEN: " + TOKEN);
            } catch (Exception e) {
                Log.d(TAG, "doInBackground: " + e.getMessage());
            }
            return null;
        }
    }


    class MyTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            int count = 1;
            for (; count <= params[0]; count++) {
                try {
                    Thread.sleep(1000);
                    publishProgress(count);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return "Task Completed.";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(context, s, Toast.LENGTH_LONG).show();
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }
}
