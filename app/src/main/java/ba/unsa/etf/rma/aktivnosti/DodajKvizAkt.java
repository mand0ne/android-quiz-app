package ba.unsa.etf.rma.aktivnosti;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.customKlase.ConnectionStateMonitor;
import ba.unsa.etf.rma.customKlase.CustomAdapter;
import ba.unsa.etf.rma.customKlase.CustomSpinner;
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.firestore.FirestoreResultReceiver;
import ba.unsa.etf.rma.modeli.Kategorija;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.Pitanje;

import static ba.unsa.etf.rma.aktivnosti.KvizoviAkt.PROMIJENI_KVIZ;
import static ba.unsa.etf.rma.customKlase.ConnectionStateMonitor.CONNECTION_LOST;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.AZURIRAJ_KATEGORIJE;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.AZURIRAJ_KVIZOVE;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.AZURIRAJ_PITANJA;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.DODAJ_KVIZ_AKT;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.DOHVATI_KATEGORIJE;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.DOHVATI_PITANJA;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.VALIDAN_IMPORT;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.VALIDAN_KVIZ;

public class DodajKvizAkt extends AppCompatActivity implements FirestoreResultReceiver.Receiver, ConnectionStateMonitor.NetworkAwareActivity {

    static final int DODAJ_PITANJE = 40;
    static final int DODAJ_KATEGORIJU = 41;
    private static final int IMPORTUJ_KVIZ = 42;

    private Context context;
    private CustomSpinner spinnerKategorije;
    private EditText editTextNaziv;

    private CustomAdapter adapterDodanaPitanja;
    private ArrayAdapter<Pitanje> adapterMogucaPitanja;
    private ArrayAdapter<Kategorija> spinnerAdapter;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Pitanje> dodanaPitanja = new ArrayList<>();
    private ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();

    private Kviz trenutniKviz;
    private String originalnoImeTrenutnogKviza;
    private Kategorija kategorijaKviza = new Kategorija("Svi", -1);

    private FirestoreResultReceiver receiver;
    // Firestore access token
    private String TOKEN;
    private ConnectionStateMonitor connectionStateMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); // (°͜ʖ°)

        context = this;
        receiver = new FirestoreResultReceiver(new Handler());
        receiver.setReceiver(this);

        TOKEN = getIntent().getStringExtra("token");

        spinnerKategorije = findViewById(R.id.spKategorije);
        editTextNaziv = findViewById(R.id.etNaziv);

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategorije);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategorije.setAdapter(spinnerAdapter);

        adapterMogucaPitanja = new ArrayAdapter<>(this, R.layout.element_liste, R.id.naziv, mogucaPitanja);

        connectionStateMonitor = new ConnectionStateMonitor(this, (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE));
        connectionStateMonitor.registerNetworkCallback();

        firestoreRequest(DODAJ_KVIZ_AKT);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        connectionStateMonitor.unregisterNetworkCallback();
        super.onDestroy();
    }

    private Intent kreirajFirestoreIntent(int request) {
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", request);

        return intent;
    }

    private void firestoreRequest(int request) {
        final Intent intent = kreirajFirestoreIntent(request);
        if (request == VALIDAN_KVIZ)
            intent.putExtra("nazivKviza", editTextNaziv.getText().toString());
        else if (request == DODAJ_KVIZ_AKT)
            intent.putExtra("kvizFirestoreId", getIntent().getStringExtra("kvizFirestoreId"));
        startService(intent);
    }

    public void start() {
        originalnoImeTrenutnogKviza = trenutniKviz.getNaziv();

        if (getIntent().getIntExtra("activity", 30) == PROMIJENI_KVIZ) {
            spinnerKategorije.setSelection(spinnerAdapter.getPosition(trenutniKviz.getKategorija()));
            editTextNaziv.setText(trenutniKviz.getNaziv());
        }

        ListView lvDodanaPitanja = findViewById(R.id.lvDodanaPitanja);
        ListView lvMogucaPitanja = findViewById(R.id.lvMogucaPitanja);

        dodanaPitanja = new ArrayList<>(trenutniKviz.getPitanja());
        adapterDodanaPitanja = new CustomAdapter(this, dodanaPitanja);
        View opcijaNovoPitanje = adapterDodanaPitanja.getFooterView(lvDodanaPitanja, "Dodaj pitanje");
        lvDodanaPitanja.addFooterView(opcijaNovoPitanje);

        lvDodanaPitanja.setAdapter(adapterDodanaPitanja);
        lvMogucaPitanja.setAdapter(adapterMogucaPitanja);

        lvDodanaPitanja.setOnItemClickListener((parent, view, position, id) -> prebaciPitanje(dodanaPitanja, mogucaPitanja, position));

        lvMogucaPitanja.setOnItemClickListener((parent, view, position, id) -> prebaciPitanje(mogucaPitanja, dodanaPitanja, position));

        // Klik na zadnji element liste za dodavanje novog pitanja
        opcijaNovoPitanje.setOnClickListener(v -> {
                    final Intent intent = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
                    intent.putExtra("token", TOKEN);
                    startActivityForResult(intent, DODAJ_PITANJE);
                }
        );

        // Klik na zadnji element spinnera za dodavanje nove kategorije
        spinnerKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == kategorije.size() - 1) {
                    Intent intent = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
                    intent.putExtra("token", TOKEN);
                    startActivityForResult(intent, DODAJ_KATEGORIJU);
                } else
                    kategorijaKviza = (Kategorija) spinnerKategorije.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerKategorije.setSelection(0);
                kategorijaKviza = (Kategorija) spinnerKategorije.getSelectedItem();
            }
        });

        findViewById(R.id.btnDodajKviz).setOnClickListener(v -> {
            if (editTextNaziv.getText().length() == 0)
                editTextNaziv.setError("Unesite naziv kviza!");
            else if (spinnerKategorije.getSelectedItemPosition() == kategorije.size() - 1) {
                TextView errorText = (TextView) spinnerKategorije.getSelectedView();
                errorText.setError("");
                errorText.setTextColor(Color.RED);
                errorText.setText(getString(R.string.categoryError));
            } else
                firestoreRequest(FirestoreIntentService.VALIDAN_KVIZ);

        });
    }

    private void prebaciPitanje(ArrayList<Pitanje> izvor, ArrayList<Pitanje> destinacija, int pozicija) {
        destinacija.add(izvor.get(pozicija));
        izvor.remove(pozicija);
        adapterDodanaPitanja.notifyDataSetChanged();
        adapterMogucaPitanja.notifyDataSetChanged();
    }

    private void dodajKviz(String naziv) {
        trenutniKviz.setNaziv(naziv);
        trenutniKviz.setKategorija(kategorijaKviza);
        trenutniKviz.setPitanja(dodanaPitanja);

        azurirajKvizDokumentFirestore(trenutniKviz);
        finish();
    }

    private void izbaciAlert(String poruka) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setNeutralButton("U redu", null)
                .setMessage(poruka)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create()
                .show();
    }

    public void azurirajMogucaPitanja(ArrayList<Pitanje> mogucaPitanja) {
        this.mogucaPitanja.clear();
        this.mogucaPitanja.addAll(mogucaPitanja);
        this.mogucaPitanja.removeAll(dodanaPitanja);
        adapterMogucaPitanja.notifyDataSetChanged();
    }

    public void azurirajKategorije(ArrayList<Kategorija> kategorije) {
        this.kategorije.clear();
        this.kategorije.add(new Kategorija("Svi", -1));
        this.kategorije.addAll(kategorije);
        this.kategorije.add(new Kategorija("Dodaj kategoriju", -2));
        spinnerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNetworkLost() {
        Log.wtf("DodajKvizAkt: ", "onNetworkLost");
        Toast.makeText(context, "Connection lost!", Toast.LENGTH_SHORT).show();

        finish();
    }

    @Override
    public void onNetworkAvailable() {
        Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null) {
            if (requestCode == DODAJ_PITANJE) {
                if (resultCode == CONNECTION_LOST)
                    return;

                if (resultCode == RESULT_OK) {
                    dodanaPitanja.add(data.getParcelableExtra("novoPitanje"));
                    adapterDodanaPitanja.notifyDataSetChanged();
                }
                Log.wtf("POVRATAK PITAJNA", "onActivityResult: JEBOTE");
                firestoreRequest(DOHVATI_PITANJA);
            } else if (requestCode == DODAJ_KATEGORIJU) {
                if (resultCode == CONNECTION_LOST)
                    return;

                if (resultCode == RESULT_OK)
                    kategorijaKviza = data.getParcelableExtra("novaKategorija");
                firestoreRequest(DOHVATI_KATEGORIJE);
            } else if (requestCode == IMPORTUJ_KVIZ) {
                try {
                    procesirajImport(ucitajImportUNiz(data.getData()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == DODAJ_KVIZ_AKT) {
            trenutniKviz = resultData.getParcelable("trenutniKviz");
            if (trenutniKviz == null)
                trenutniKviz = new Kviz("", kategorijaKviza);
            azurirajKategorije(resultData.getParcelableArrayList("kategorije"));
            start();
            azurirajMogucaPitanja(resultData.getParcelableArrayList("pitanja"));
        }
        if (resultCode == FirestoreIntentService.VALIDAN_KVIZ) {
            if (!resultData.getBoolean("postojiKviz") || originalnoImeTrenutnogKviza.equals(resultData.getString("nazivKviza")))
                dodajKviz(resultData.getString("nazivKviza"));
            else
                izbaciAlert("Kviz sa imenom: \"" + resultData.getString("nazivKviza") + "\" već postoji!");
        } else if (resultCode == DOHVATI_PITANJA)
            azurirajMogucaPitanja(resultData.getParcelableArrayList("pitanja"));
        else if (resultCode == DOHVATI_KATEGORIJE) {
            azurirajKategorije(resultData.getParcelableArrayList("kategorije"));
            spinnerKategorije.setSelection(spinnerAdapter.getPosition(kategorijaKviza));
        } else if (resultCode == VALIDAN_IMPORT) {
            if (resultData.getBoolean("validanImport")) {
                String nazivKvizaImport = resultData.getString("nazivKvizaImport");
                String nazivKategorijeImport = resultData.getString("nazivKategorijeImport");
                ArrayList<Pitanje> pitanja = resultData.getParcelableArrayList("pitanjaImport");

                int spinnerIndex;
                assert nazivKategorijeImport != null;
                if (nazivKategorijeImport.contains("null-index:"))
                    spinnerIndex = Integer.parseInt(nazivKategorijeImport.substring(11));
                else {
                    kategorijaKviza = new Kategorija(nazivKategorijeImport, -3);
                    azurirajKategorijaDokumentFirestore(kategorijaKviza);
                    kategorije.add(kategorije.size() - 1, kategorijaKviza);
                    spinnerIndex = kategorije.size() - 2;
                }

                assert pitanja != null;
                for (Pitanje p : pitanja)
                    azurirajPitanjeDokumentFirestore(p);

                editTextNaziv.setText(nazivKvizaImport);
                spinnerAdapter.notifyDataSetChanged();
                spinnerKategorije.setSelection(spinnerIndex);

                dodanaPitanja.clear();
                dodanaPitanja.addAll(pitanja);
                adapterDodanaPitanja.notifyDataSetChanged();
                firestoreRequest(DOHVATI_PITANJA);
            } else
                izbaciAlert("Kviz/Kategorija/Pitanje već postoji!");
        }
    }

    // "Importuj kviz" button
    public void importujKviz(View view) {
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, IMPORTUJ_KVIZ);
    }

    ArrayList<String> ucitajImportUNiz(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null)
            return null;

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<String> kviz = new ArrayList<>();

        String lineInFile;
        while ((lineInFile = bufferedReader.readLine()) != null)
            kviz.add(lineInFile);

        inputStream.close();
        return kviz;
    }

    // Najvjerovatnije najgora funkcija ikad napisana, ali sta je tu je
    private void procesirajImport(ArrayList<String> importovanKviz) {

        if (importovanKviz.isEmpty()) {
            izbaciAlert("Datoteka kviza kojeg importujete nije ispravnog formata!");
            return;
        }

        String kvizPodaci = importovanKviz.get(0);
        Kategorija kategorijaKviza = null;
        ArrayList<Pitanje> importovanaPitanja = new ArrayList<>();

        if (StringUtils.countMatches(kvizPodaci, ',') != 2) {
            izbaciAlert("Datoteka kviza kojeg importujete nije ispravnog formata!");
            return;
        }

        StringTokenizer razdvajac = new StringTokenizer(kvizPodaci, ",");

        String imeKviza = razdvajac.nextToken();
        String nazivKategorijeKviza = razdvajac.nextToken();
        int brojPitanjaKviza;

        try {
            brojPitanjaKviza = Integer.parseInt(razdvajac.nextToken());
        } catch (Exception e) {
            izbaciAlert("Datoteka kviza kojeg importujete nije ispravnog formata!");
            return;
        }

        if (brojPitanjaKviza != importovanKviz.size() - 1) {
            izbaciAlert("Kviz kojeg imporujete ima neispravan broj pitanja!");
            return;
        }

        for (int i = 1; i < importovanKviz.size(); i++) {
            String pitanje = importovanKviz.get(i);

            if (pitanje.isEmpty() || StringUtils.countMatches(pitanje, ',') < 3) {
                izbaciAlert("Datoteka kviza kojeg importujete nema ispravan format!");
                return;
            }

            StringTokenizer razdvajacPitanja = new StringTokenizer(pitanje, ",");

            String nazivPitanja = razdvajacPitanja.nextToken();
            int brojOdgovora, indexTacnogOdgovora;
            try {
                brojOdgovora = Integer.parseInt(razdvajacPitanja.nextToken());
                indexTacnogOdgovora = Integer.parseInt(razdvajacPitanja.nextToken());
            } catch (Exception e) {
                izbaciAlert("Datoteka kviza kojeg importujete nije ispravnog formata!");
                return;
            }

            ArrayList<String> odgovori = new ArrayList<>();

            while (razdvajacPitanja.hasMoreTokens()) {
                String odgovor = razdvajacPitanja.nextToken();

                for (String o : odgovori)
                    if (odgovor.equals(o)) {
                        izbaciAlert("Kviz kojeg importujete nije ispravan, postoji ponavljanje odgovora!");
                        return;
                    }

                odgovori.add(odgovor);
            }

            if (brojOdgovora == 0 || brojOdgovora != odgovori.size()) {
                izbaciAlert("Kviz kojeg importujete ima pitanje sa neispravanim brojem odgovora!");
                return;
            }

            if (indexTacnogOdgovora < 0 || indexTacnogOdgovora >= odgovori.size()) {
                izbaciAlert("Kviz kojeg importujete ima pitanje neispravanim indexom tačnog odgovora!");
                return;
            }

            for (Pitanje p : importovanaPitanja)
                if (p.getNaziv().equals(nazivPitanja)) {
                    izbaciAlert("Kviz nije ispravan, postoje pitanja sa istim nazivom!");
                    return;
                }

            Pitanje p = new Pitanje(nazivPitanja, odgovori.get(indexTacnogOdgovora));
            p.setOdgovori(odgovori);
            importovanaPitanja.add(p);
        }

        int indexKategorijeSpiner = 0;
        for (int i = 0; i < kategorije.size() - 1; i++) {
            Kategorija ka = kategorije.get(i);
            if (ka.getNaziv().equals(nazivKategorijeKviza)) {
                indexKategorijeSpiner = i;
                kategorijaKviza = ka;
                break;
            }
        }

        nazivKategorijeKviza = (kategorijaKviza == null) ? nazivKategorijeKviza : "null-index:" + indexKategorijeSpiner;
        validanImport(imeKviza, nazivKategorijeKviza, importovanaPitanja);
    }

    private void validanImport(String nazivKvizaImport, String nazivKategorijeImport, ArrayList<Pitanje> pitanjaImport) {
        final Intent intent = kreirajFirestoreIntent(VALIDAN_IMPORT);
        intent.putExtra("nazivKvizaImport", nazivKvizaImport);
        intent.putExtra("nazivKategorijeImport", nazivKategorijeImport);
        intent.putParcelableArrayListExtra("pitanjaImport", pitanjaImport);
        startService(intent);
    }

    private void azurirajKvizDokumentFirestore(Kviz azuriraniKviz) {
        final Intent intent = kreirajFirestoreIntent(AZURIRAJ_KVIZOVE);
        intent.putExtra("kviz", azuriraniKviz);
        startService(intent);
    }

    private void azurirajKategorijaDokumentFirestore(Kategorija kategorija) {
        final Intent intent = kreirajFirestoreIntent(AZURIRAJ_KATEGORIJE);
        intent.putExtra("kategorija", kategorija);
        startService(intent);
    }

    private void azurirajPitanjeDokumentFirestore(Pitanje novoPitanje) {
        final Intent intent = kreirajFirestoreIntent(AZURIRAJ_PITANJA);
        intent.putExtra("pitanje", novoPitanje);
        startService(intent);
    }
}