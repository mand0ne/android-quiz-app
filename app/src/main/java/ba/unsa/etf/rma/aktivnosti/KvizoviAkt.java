package ba.unsa.etf.rma.aktivnosti;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.customKlase.ConnectionStateMonitor;
import ba.unsa.etf.rma.customKlase.CustomAdapter;
import ba.unsa.etf.rma.customKlase.CustomSpinner;
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.firestore.FirestoreResultReceiver;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.fragmenti.ListaFrag;
import ba.unsa.etf.rma.modeli.Kategorija;
import ba.unsa.etf.rma.modeli.Kviz;

import static ba.unsa.etf.rma.firestore.FirestoreIntentService.FILTRIRAJ_KVIZOVE;

public class KvizoviAkt extends AppCompatActivity implements FirestoreResultReceiver.Receiver, ConnectionStateMonitor.NetworkAwareActivity {

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
    private ConnectionStateMonitor connectionStateMonitor;
    private boolean connected = false;

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

        connectionStateMonitor = new ConnectionStateMonitor(this, (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE));
        connectionStateMonitor.registerNetworkCallback();
        start();
    }

    public void start() {
        if (kategorije.isEmpty())
            kategorije.add(new Kategorija("Svi", "-1"));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dpwidth = displayMetrics.widthPixels / displayMetrics.density;

        if (dpwidth >= 550) {
            Bundle b = new Bundle();
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
                                                      if (connected)
                                                          dodajAzurirajKvizAktivnost(null);
                                                      else
                                                          izbaciAlertZaKonekciju();
                                                  }
                                              }
            );

            // Dugi klik na zadnji element liste za dodavanje novog kviza
            opcijaNoviKviz.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (connected)
                        dodajAzurirajKvizAktivnost(null);
                    else
                        izbaciAlertZaKonekciju();

                    return true;
                }
            });


            // Dugi klik na kviz za uredivanje
            listViewKvizovi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (connected) {
                        Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                        dodajAzurirajKvizAktivnost(kliknutiKviz);
                    }
                    else
                        izbaciAlertZaKonekciju();

                    return true;
                }
            });

            // Klik na kviz za igranje
            listViewKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                    String event = postojiEvent(kliknutiKviz.getPitanja().size() * 30000);
                    if (event == null) {
                        igrajKvizAktivnost(kliknutiKviz);
                    } else {
                        new AlertDialog.Builder(context)
                                .setTitle("Igranje kviza onemogućeno!")
                                .setMessage(event)
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.ok, null)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .show();
                    }
                }
            });


            // Filtriranje kvizova
            spinnerKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Kategorija selektovanaKategorija = (Kategorija) spinnerKategorije.getSelectedItem();

                    if (selektovanaKategorija != null) {
                        if(connected)
                            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                        intentServiceFiltriranje(selektovanaKategorija);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    spinnerKategorije.setSelection(0);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Povratkom na aktivnost ponovo dohvacamo sve iz baze i stavljamo kategoriju "Svi"
        if (dpwidth < 550)
            spinnerKategorije.setSelection(0);
        else
            listaFrag.refreshujSpinner();
    }

    private String postojiEvent(long vrijemeIgranjaKviza) {
        // SELECT _ID, TITLE, EVENT_LOCATION, DTSTART, DTEND
        // FROM Events
        // WHERE (dtstart >= trenutnoVrijeme AND dtstart <= trenutnoVrijeme + vrijemeIgranjaKviza)
        //       OR (dtstart <= trenutnoVrijeme AND trenutnoVrijeme + vrijemeIgranjaKviza <= dtend)
        // ORDER BY dtstart ASC;

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 420);

        String[] projection = {
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
        };

        String selectionClause = "(" + CalendarContract.Events.DTSTART + ">= ? AND "
                + CalendarContract.Events.DTSTART + "<= ?)"
                + "OR (" + CalendarContract.Events.DTSTART + " <= ? AND "
                + CalendarContract.Events.DTEND + ">= ?)";

        String[] selectionArgs = {"", "", "", ""};
        selectionArgs[0] = String.valueOf(Calendar.getInstance().getTimeInMillis());
        selectionArgs[1] = String.valueOf(Calendar.getInstance().getTimeInMillis() + vrijemeIgranjaKviza);
        selectionArgs[2] = selectionArgs[0];
        selectionArgs[3] = selectionArgs[0];

        try {
            Cursor cursor = getContentResolver().query(
                    CalendarContract.Events.CONTENT_URI,
                    projection,
                    selectionClause,
                    selectionArgs,
                    "DTSTART,DTEND ASC");

            if (cursor == null || cursor.getCount() < 1)
                return null;
            else {
                cursor.moveToFirst();
                String[] info = new String[4];
                info[0] = cursor.getString(1);
                info[1] = cursor.getString(2);
                info[1] = info[1] == null ? "Nedefinisano" : info[1];

                SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy  HH:mm:ss", Locale.getDefault());
                info[2] = sdfDate.format(new Date(cursor.getLong(3)));
                info[3] = sdfDate.format(new Date(cursor.getLong(4)));

                String event = "Imate sljedeći događaj:\n\n" +
                        "Naziv: " + info[0] + "\n" +
                        "Lokacija: " + info[1] + "\n" +
                        "Počinje: " + info[2] + "\n" +
                        "Završava: " + info[3] + "\n";

                cursor.close();
                return event;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    public void dodajAzurirajKvizAktivnost(Kviz kviz) {
        final Intent intent = new Intent(context, DodajKvizAkt.class);
        intent.putExtra("token", TOKEN);

        final int activity;

        if (kviz != null) {
            activity = PROMIJENI_KVIZ;
            intent.putExtra("kvizFirebaseId", kviz.firebaseId());
        } else
            activity = DODAJ_KVIZ;

        intent.putExtra("activity", activity);
        startActivity(intent);
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

    public FirestoreResultReceiver getReceiver() {
        return receiver;
    }

    public void intentServiceFiltriranje(Kategorija kategorija) {
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", FILTRIRAJ_KVIZOVE);
        intent.putExtra("kategorijaFirebaseId", kategorija.firebaseId());
        startService(intent);
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
    public void onBackPressed() {
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectionStateMonitor.unregisterNetworkCallback();
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

    private void izbaciAlertZaKonekciju(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setTitle("Onemogućeno!")
                .setMessage("Niste povezani na internet!");

        alertDialog.create();
        alertDialog.show();
    }

    @Override
    public void onNetworkLost() {
        Log.wtf("KvizoviAkt: ", "onNetworkLost");
        Toast.makeText(context, "Connection lost!", Toast.LENGTH_SHORT).show();
        connected = false;
    }

    @Override
    public void onNetworkAvailable() {
        Log.wtf("KvizoviAkt: ", "onNetworkAvailable");
        Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show();
        connected = true;
        if(spinnerKategorije != null)
            spinnerKategorije.setSelection(0);
    }
}
