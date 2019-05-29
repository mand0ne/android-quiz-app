package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.fragmenti.ListaFrag;
import ba.unsa.etf.rma.klase.CustomAdapter;
import ba.unsa.etf.rma.klase.FirebaseIntentService;
import ba.unsa.etf.rma.klase.FirebaseResultReceiver;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.NDSpinner;

public class KvizoviAkt extends AppCompatActivity implements FirebaseResultReceiver.Receiver {

    static final int DODAJ_KVIZ = 30;
    static final int PROMIJENI_KVIZ = 31;

    private Context context;
    private ListView listViewKvizovi;
    private View listViewFooter;
    private NDSpinner spinnerKategorije;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();

    private CustomAdapter listViewAdapter = null;
    private ArrayAdapter<Kategorija> spinnerAdapter = null;

    private double dpwidth = 0.0;
    private ListaFrag listaFrag;
    private DetailFrag detailFrag;

    public String getTOKEN() {
        return TOKEN;
    }

    // FIRESTORE Access Token
    private String TOKEN = "";
    public FirebaseResultReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kvizovi_akt);
        context = KvizoviAkt.this;

        receiver = new FirebaseResultReceiver(new Handler());
        receiver.setReceiver(this);

        if (savedInstanceState != null) {
            kvizovi = savedInstanceState.getParcelableArrayList("kvizovi");
            kategorije = savedInstanceState.getParcelableArrayList("kategorije");

            TOKEN = savedInstanceState.getString("token");
            if (TOKEN == null) {
                try {
                    new getAccessToken(this).execute().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);
            start();
        } else {
            promptConnection(context);
            try {
                new getAccessToken(this).execute().get();
                // ovdje jednostavno mora get.. mislim ne mora
                // ali nema smisla komplikovati previse na prvom ucitavanju aplikacije...
                start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dpwidth = displayMetrics.widthPixels / displayMetrics.density;

        if (kategorije.isEmpty())
            kategorije.add(new Kategorija("Svi", "-1"));

        if (dpwidth >= 550) {
            Intent intent = getIntent();
            Kviz trenutniKviz = intent.getParcelableExtra("kviz");

            FragmentManager fragmentManager = getSupportFragmentManager();

            Bundle b = new Bundle();
            b.putParcelable("kviz", trenutniKviz);
            b.putParcelableArrayList("kvizovi", kvizovi);
            b.putParcelableArrayList("kategorije", kategorije);

            listaFrag = new ListaFrag();
            detailFrag = new DetailFrag();

            listaFrag.setArguments(b);
            detailFrag.setArguments(b);

            fragmentManager.beginTransaction().replace(R.id.listPlace, listaFrag).commit();
            fragmentManager.beginTransaction().replace(R.id.detailPlace, detailFrag).commit();
        } else {
            listViewKvizovi = findViewById(R.id.lvKvizovi);
            spinnerKategorije = findViewById(R.id.spPostojeceKategorije);

            initializeAdapters();

            // Klik na zadnji element liste koji je za dodavanje novog kviza
            listViewFooter.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      dodajKvizAktivnost(null);
                                                  }
                                              }
            );

            listViewFooter.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    dodajKvizAktivnost(null);
                    return true;
                }
            });


            // Klik na kviz za uredivanje
            listViewKvizovi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                    dodajKvizAktivnost(kliknutiKviz);
                    return true;
                }
            });

            listViewKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                    aktivnostIgrajKviz(kliknutiKviz);
                }
            });


            spinnerKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Kategorija selektovanaKategorija = (Kategorija) spinnerKategorije.getSelectedItem();

                    if (selektovanaKategorija != null) {
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, FirebaseIntentService.class);
                        intent.putExtra("receiver", receiver);
                        intent.putExtra("token", TOKEN);
                        intent.putExtra("request", FirebaseIntentService.FILTRIRAJ_KVIZOVE);
                        intent.putExtra("kategorijaId", selektovanaKategorija.firebaseId());
                        startService(intent);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    spinnerKategorije.setSelection(0);
                }
            });
        }
    }

    private void initializeAdapters() {
        listViewAdapter = new CustomAdapter(context, kvizovi);
        listViewKvizovi.setAdapter(listViewAdapter);
        listViewKvizovi.addFooterView(listViewFooter = listViewAdapter.getFooterView(listViewKvizovi, "Dodaj kviz"));

        spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, kategorije);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategorije.setAdapter(spinnerAdapter);
    }

    public void dodajKvizAktivnost(Kviz kviz) {
        Intent intent = new Intent(context, DodajKvizAkt.class);
        intent.putParcelableArrayListExtra("kategorije", kategorije);
        intent.putParcelableArrayListExtra("kvizovi", kvizovi);
        intent.putExtra("token", TOKEN);

        if (kviz != null) {
            intent.putExtra("requestCode", PROMIJENI_KVIZ);
            intent.putExtra("kviz", kviz);
            startActivityForResult(intent, PROMIJENI_KVIZ);
        } else {
            intent.putExtra("requestCode", DODAJ_KVIZ);
            startActivityForResult(intent, DODAJ_KVIZ);
        }
    }

    public void aktivnostIgrajKviz(Kviz kliknutiKviz) {
        Intent intent = new Intent(context, IgrajKvizAkt.class);
        intent.putExtra("kviz", kliknutiKviz);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null) {
            if (resultCode == RESULT_OK) {
                ArrayList<Kviz> noviKvizovi = data.getParcelableArrayListExtra("kvizovi");

                azurirajKvizove(noviKvizovi);

                if (dpwidth < 550)
                    spinnerKategorije.setSelection(spinnerKategorije.getSelectedItemPosition());
                else
                    detailFrag.azurirajKvizove(kvizovi);
            }

            azurirajKategorije(data);
        }
    }

    private int dajIndexKviza(String staroImeKviza) {
        for (int i = 0; i < kvizovi.size(); i++)
            if (kvizovi.get(i).getNaziv().equals(staroImeKviza))
                return i;

        return kvizovi.size() - 1;
    }

    public void azurirajKvizove(ArrayList<Kviz> noviKvizovi) {
        kvizovi.clear();
        kvizovi.addAll(noviKvizovi);
        if (listViewAdapter != null)
            listViewAdapter.notifyDataSetChanged();
    }

    public void azurirajKategorije(@NonNull Intent data) {
        kategorije.clear();
        kategorije.addAll(data.<Kategorija>getParcelableArrayListExtra("kategorije"));
        kategorije.remove(kategorije.size() - 1);

        if (dpwidth < 550)
            spinnerAdapter.notifyDataSetChanged();
        else
            listaFrag.azurirajKategorije(kategorije);

    }

    public void setKategorije(ArrayList<Kategorija> kategorije) {
        this.kategorije.clear();
        this.kategorije.addAll(kategorije);
    }

    // U slucaju privremenog destroy-a, recimo rotacija ekrana
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("kvizovi", kvizovi);
        outState.putParcelableArrayList("kategorije", kategorije);
        outState.putString("token", TOKEN);

        // Call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == RESULT_OK) {
            kategorije.clear();
            kvizovi.clear();

            ArrayList<Kategorija> noveKategorije = resultData.getParcelableArrayList("kategorije");
            ArrayList<Kviz> noviKvizovi = resultData.getParcelableArrayList("kvizovi");

            if (noveKategorije != null)
                kategorije.addAll(noveKategorije);
            if (noviKvizovi != null)
                kvizovi.addAll(noviKvizovi);

            findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);

            if (dpwidth < 550) {
                spinnerAdapter.notifyDataSetChanged();
                listViewAdapter.notifyDataSetChanged();
            } else {
                listaFrag.azurirajKategorije(kategorije);
                detailFrag.azurirajKvizove(kvizovi);
            }
        }
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

            KvizoviAkt kvizoviAkt = (KvizoviAkt) activityWeakReference.get();
            if (kvizoviAkt == null || kvizoviAkt.isFinishing())
                this.cancel(true);
        }

        protected Void doInBackground(String... params) {
            try {
                KvizoviAkt kvizoviAkt = (KvizoviAkt) activityWeakReference.get();
                InputStream is = kvizoviAkt.context.getResources().openRawResource(R.raw.secret);
                GoogleCredential credentials = GoogleCredential.fromStream(is)
                        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                kvizoviAkt.TOKEN = credentials.getAccessToken();
                Log.d(TAG, "TOKEN: " + kvizoviAkt.TOKEN);
            } catch (Exception e) {
                Log.d(TAG, "doInBackground: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public void promptConnection(final Context context) {
        boolean wifiConnection = false;
        boolean mobileConnection = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    wifiConnection = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    mobileConnection = true;
        }

        if (!wifiConnection && !mobileConnection)
            showDialog(context);
    }

    private void showDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connect to the internet or quit")
                .setCancelable(false)
                .setNegativeButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
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

}
