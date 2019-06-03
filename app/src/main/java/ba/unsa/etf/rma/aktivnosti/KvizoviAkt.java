package ba.unsa.etf.rma.aktivnosti;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.customKlase.CustomAdapter;
import ba.unsa.etf.rma.customKlase.CustomSpinner;
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.firestore.FirestoreResultReceiver;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.fragmenti.ListaFrag;
import ba.unsa.etf.rma.modeli.Kategorija;
import ba.unsa.etf.rma.modeli.Kviz;

import static ba.unsa.etf.rma.firestore.FirestoreIntentService.FILTRIRAJ_KVIZOVE;

public class KvizoviAkt extends AppCompatActivity implements FirestoreResultReceiver.Receiver {

    static final int DODAJ_KVIZ = 30;
    static final int PROMIJENI_KVIZ = 31;

    private Context context;
    private ListView listViewKvizovi;
    private View opcijaNoviKviz;

    private CustomSpinner spinnerKategorije;
    private CustomAdapter listViewAdapter = null;
    private ArrayAdapter<Kategorija> spinnerAdapter = null;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();

    private double dpwidth;
    private ListaFrag listaFrag;
    private DetailFrag detailFrag;

    private FirestoreResultReceiver receiver;
    // Firestore access token
    private String TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kvizovi_akt);

        context = KvizoviAkt.this;
        receiver = new FirestoreResultReceiver(new Handler());
        receiver.setReceiver(this);

        if (savedInstanceState != null) {
            kvizovi = savedInstanceState.getParcelableArrayList("kvizovi");
            kategorije = savedInstanceState.getParcelableArrayList("kategorije");
            TOKEN = savedInstanceState.getString("token");
        }

        if (TOKEN == null)
            TOKEN = getIntent().getStringExtra("token");

        start();
    }

    public void start() {
        if (kategorije.isEmpty())
            kategorije.add(new Kategorija("Svi", "-1"));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dpwidth = displayMetrics.widthPixels / displayMetrics.density;

        if (dpwidth >= 550) {
            Intent intent = getIntent();
            Kviz trenutniKviz = intent.getParcelableExtra("kviz");

            Bundle b = new Bundle();
            b.putParcelable("kviz", trenutniKviz);
            b.putParcelableArrayList("kvizovi", kvizovi);
            b.putParcelableArrayList("kategorije", kategorije);

            listaFrag = new ListaFrag();
            detailFrag = new DetailFrag();

            listaFrag.setArguments(b);
            detailFrag.setArguments(b);

            getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, listaFrag).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.detailPlace, detailFrag).commit();
        } else {
            listViewKvizovi = findViewById(R.id.lvKvizovi);
            spinnerKategorije = findViewById(R.id.spPostojeceKategorije);

            inicijalizirajAdaptere();

            // Klik na zadnji element liste za dodavanje novog kviza
            opcijaNoviKviz.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      dodajKvizAktivnost(null);
                                                  }
                                              }
            );

            // Dugi klik na zadnji element liste za dodavanje novog kviza
            opcijaNoviKviz.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    dodajKvizAktivnost(null);
                    return true;
                }
            });


            // Dugi klik na kviz za uredivanje
            listViewKvizovi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                    dodajKvizAktivnost(kliknutiKviz);
                    return true;
                }
            });


            // Klik na kviz za igranje
            listViewKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                    igrajKvizAktivnost(kliknutiKviz);
                }
            });


            // Filtriranje kvizova
            spinnerKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Kategorija selektovanaKategorija = (Kategorija) spinnerKategorije.getSelectedItem();

                    if (selektovanaKategorija != null) {
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, FirestoreIntentService.class);
                        intent.putExtra("receiver", receiver);
                        intent.putExtra("token", TOKEN);
                        intent.putExtra("request", FILTRIRAJ_KVIZOVE);
                        intent.putExtra("kategorijaFirebaseId", selektovanaKategorija.firebaseId());
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

    private void inicijalizirajAdaptere() {
        listViewAdapter = new CustomAdapter(context, kvizovi);
        listViewKvizovi.setAdapter(listViewAdapter);
        listViewKvizovi.addFooterView(opcijaNoviKviz = listViewAdapter.getFooterView(listViewKvizovi, "Dodaj kviz"));

        spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, kategorije);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategorije.setAdapter(spinnerAdapter);
    }

    public FirestoreResultReceiver getReceiver() {
        return receiver;
    }

    public void dodajKvizAktivnost(Kviz kviz) {
        final Intent intent = new Intent(context, DodajKvizAkt.class);
        intent.putExtra("token", TOKEN);

        kategorije.remove(0);
        intent.putParcelableArrayListExtra("kategorije", kategorije);
        intent.putParcelableArrayListExtra("kvizovi", kvizovi);

        final int requestCode;
        if (kviz != null) {
            requestCode = PROMIJENI_KVIZ;
            intent.putExtra("kviz", kviz);
        } else
            requestCode = DODAJ_KVIZ;

        intent.putExtra("requestCode", requestCode);
        startActivityForResult(intent, requestCode);
    }

    public void igrajKvizAktivnost(Kviz kliknutiKviz) {
        Intent intent = new Intent(context, IgrajKvizAkt.class);
        intent.putExtra("token", TOKEN);
        intent.putExtra("kviz", kliknutiKviz);
        startActivity(intent);
    }


    public void azurirajKategorije(ArrayList<Kategorija> noveKategorije, boolean jeNormalniLayout) {
        kategorije.clear();
        kategorije.add(new Kategorija("Svi", "-1"));
        kategorije.addAll(noveKategorije);

        if (jeNormalniLayout)
            spinnerAdapter.notifyDataSetChanged();
        else
            listaFrag.azurirajKategorije(kategorije);
    }

    public void azurirajKvizove(ArrayList<Kviz> noviKvizovi, boolean jeNormalniLayout) {
        kvizovi.clear();
        kvizovi.addAll(noviKvizovi);

        if (jeNormalniLayout)
            listViewAdapter.notifyDataSetChanged();
        else
            detailFrag.azurirajKvizove(kvizovi);
    }

    public String getTOKEN() {
        return TOKEN;
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null) {
            if (resultCode == RESULT_OK) {
                ArrayList<Kviz> noviKvizovi = data.getParcelableArrayListExtra("kvizovi");
                azurirajKvizove(noviKvizovi, dpwidth < 500);
            }
            ArrayList<Kategorija> noveKategorije = data.getParcelableArrayListExtra("kategorije");
            azurirajKategorije(noveKategorije, dpwidth < 550);
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == FILTRIRAJ_KVIZOVE) {
            ArrayList<Kategorija> noveKategorije = resultData.getParcelableArrayList("kategorije");
            ArrayList<Kviz> noviKvizovi = resultData.getParcelableArrayList("kvizovi");

            azurirajKategorije(noveKategorije, dpwidth < 550);
            azurirajKvizove(noviKvizovi, dpwidth < 500);

            findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);
        }
    }
}
