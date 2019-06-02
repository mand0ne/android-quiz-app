package ba.unsa.etf.rma.aktivnosti;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.customKlase.CustomAdapter;
import ba.unsa.etf.rma.customKlase.CustomSpinner;
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.firestore.FirestoreResultReceiver;
import ba.unsa.etf.rma.modeli.Kategorija;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.Pitanje;

import static ba.unsa.etf.rma.aktivnosti.KvizoviAkt.PROMIJENI_KVIZ;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.AZURIRAJ_KVIZOVE;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.DOHVATI_PITANJA;

public class DodajKvizAkt extends AppCompatActivity implements FirestoreResultReceiver.Receiver {

    static final int DODAJ_PITANJE = 40;
    static final int DODAJ_KATEGORIJU = 41;
    private static final int IMPORT_QUIZ = 42;

    private Context context;
    private CustomSpinner spinnerKategorije;
    private EditText editTextNaziv;

    private CustomAdapter adapterDodanaPitanja;
    private ArrayAdapter<Pitanje> adapterMogucaPitanja;
    private ArrayAdapter<Kategorija> spinnerAdapter;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Pitanje> dodanaPitanja = new ArrayList<>();
    private ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();

    private Kviz trenutniKviz;
    private String originalnoImeTrenutnogKviza;
    private Kategorija kategorijaKviza = new Kategorija("Svi", "-1");
    private boolean dodavanjeKviza = true;

    private FirestoreResultReceiver receiver;
    // Firestore access token
    private String TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); // (°͜ʖ°)

        context = this;
        receiver = new FirestoreResultReceiver(new Handler());
        receiver.setReceiver(this);

        spinnerKategorije = findViewById(R.id.spKategorije);
        editTextNaziv = findViewById(R.id.etNaziv);
        ListView lvDodanaPitanja = findViewById(R.id.lvDodanaPitanja);
        ListView lvMogucaPitanja = findViewById(R.id.lvMogucaPitanja);
        Button btnDodajKviz = findViewById(R.id.btnDodajKviz);

        final Intent intent = getIntent();

        kvizovi = intent.getParcelableArrayListExtra("kvizovi");
        trenutniKviz = intent.getParcelableExtra("kviz");
        if (trenutniKviz == null)
            trenutniKviz = new Kviz(null, null);

        TOKEN = intent.getStringExtra("token");

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategorije);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategorije.setAdapter(spinnerAdapter);
        azurirajKategorije(intent.<Kategorija>getParcelableArrayListExtra("kategorije"));

        dodanaPitanja = new ArrayList<>(trenutniKviz.getPitanja());
        adapterDodanaPitanja = new CustomAdapter(this, dodanaPitanja);
        lvDodanaPitanja.setAdapter(adapterDodanaPitanja);
        View opcijaNovoPitanje = adapterDodanaPitanja.getFooterView(lvDodanaPitanja, "Dodaj pitanje");
        lvDodanaPitanja.addFooterView(opcijaNovoPitanje);

        adapterMogucaPitanja = new ArrayAdapter<>(this, R.layout.element_liste, R.id.naziv, mogucaPitanja);
        lvMogucaPitanja.setAdapter(adapterMogucaPitanja);

        firestoreRequest(DOHVATI_PITANJA);

        if (intent.getIntExtra("requestCode", 30) == PROMIJENI_KVIZ) {
            dodavanjeKviza = false;
            originalnoImeTrenutnogKviza = trenutniKviz.getNaziv();
            spinnerKategorije.setSelection(spinnerAdapter.getPosition(trenutniKviz.getKategorija()));
            editTextNaziv.setText(trenutniKviz.getNaziv());
        }

        lvDodanaPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                prebaciPitanje(dodanaPitanja, mogucaPitanja, position);
            }
        });

        lvMogucaPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                prebaciPitanje(mogucaPitanja, dodanaPitanja, position);
            }
        });

        // Klik na zadnji element liste za dodavanje novog pitanja
        opcijaNovoPitanje.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View v) {
                                                     final Intent intent = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
                                                     intent.putExtra("token", TOKEN);
                                                     startActivityForResult(intent, DODAJ_PITANJE);
                                                 }
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

        btnDodajKviz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextNaziv.getText().length() == 0)
                    editTextNaziv.setError("Unesite naziv kviza!");
                else if (spinnerKategorije.getSelectedItemPosition() == kategorije.size() - 1) {
                    TextView errorText = (TextView) spinnerKategorije.getSelectedView();
                    errorText.setError("");
                    errorText.setTextColor(Color.RED);
                    errorText.setText(getString(R.string.categoryError));
                } else
                    firestoreRequest(FirestoreIntentService.VALIDAN_KVIZ);

            }
        });
    }

    private void prebaciPitanje(ArrayList<Pitanje> izvor, ArrayList<Pitanje> destinacija, int pozicija) {
        destinacija.add(izvor.get(pozicija));
        izvor.remove(pozicija);
        adapterDodanaPitanja.notifyDataSetChanged();
        adapterMogucaPitanja.notifyDataSetChanged();
    }

    private void firestoreRequest(int request) {
        String uneseniNaziv = editTextNaziv.getText().toString();

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", request);
        intent.putExtra("nazivKviza", uneseniNaziv);
        startService(intent);
    }

    private void azurirajKvizDokumentFirestore(Kviz noviDokument) {
        final Intent intent = new Intent(Intent.ACTION_SEND, null, context, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", AZURIRAJ_KVIZOVE);
        intent.putExtra("kviz", noviDokument);
        startService(intent);
    }

    private int index() {
        for (int i = 0; i < kvizovi.size(); i++) {
            if (kvizovi.get(i).getNaziv().equals(originalnoImeTrenutnogKviza))
                return i;
        }
        return -1;
    }

    private void dodajKviz(String naziv) {
        trenutniKviz.setNaziv(naziv);
        trenutniKviz.setKategorija(kategorijaKviza);
        trenutniKviz.setPitanja(dodanaPitanja);

        azurirajKvizDokumentFirestore(trenutniKviz);

        ArrayList<Kategorija> azuriraneKategorije = new ArrayList<>(kategorije);  // ne znam zasto...
        if (azuriraneKategorije.size() >= 2) {
            azuriraneKategorije.remove(azuriraneKategorije.size() - 1);
            azuriraneKategorije.remove(0);
        }

        if (dodavanjeKviza)
            kvizovi.add(trenutniKviz);
        else try {
            kvizovi.set(index(), trenutniKviz);
        } catch (Exception ignored) {
        }

        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("kvizovi", kvizovi);
        intent.putParcelableArrayListExtra("kategorije", azuriraneKategorije);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void izbaciAlert(String poruka) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setNeutralButton("U redu", null);
        alertDialog.setMessage(poruka);
        alertDialog.create();
        alertDialog.show();
    }

    public void azurirajMogucaPitanja(ArrayList<Pitanje> mogucaPitanja) {
        this.mogucaPitanja.clear();
        this.mogucaPitanja.addAll(mogucaPitanja);
        this.mogucaPitanja.removeAll(dodanaPitanja);
        adapterMogucaPitanja.notifyDataSetChanged();
    }

    public void azurirajKategorije(ArrayList<Kategorija> kategorije) {
        this.kategorije.clear();
        this.kategorije.add(new Kategorija("Svi", "-1"));
        this.kategorije.addAll(kategorije);
        this.kategorije.add(new Kategorija("Dodaj kategoriju", "-2"));
        spinnerAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (kategorije.size() >= 2) {
            kategorije.remove(0);
            kategorije.remove(kategorije.size() - 1);
        }
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("kategorije", kategorije);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null) {
            if (resultCode == RESULT_OK) {
                if (requestCode == DODAJ_PITANJE) {
                    dodanaPitanja.add((Pitanje) data.getParcelableExtra("novoPitanje"));
                    adapterDodanaPitanja.notifyDataSetChanged();
                    firestoreRequest(DOHVATI_PITANJA);
                }

                if (requestCode == DODAJ_KATEGORIJU) {
                    ArrayList<Kategorija> noveKategorije = data.getParcelableArrayListExtra("noveKategorije");
                    azurirajKategorije(noveKategorije);
                    spinnerKategorije.setSelection(spinnerAdapter.getPosition(noveKategorije.get(noveKategorije.size() - 1)));
                    kategorijaKviza = (Kategorija) spinnerKategorije.getSelectedItem();
                }

                /*if (requestCode == IMPORT_QUIZ) {
                    try {
                        Uri uri = data.getData();
                        validateImportAndAssign(loadImportIntoArray(uri));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }*/
            }
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == FirestoreIntentService.VALIDAN_KVIZ) {
            boolean postojiKviz = resultData.getBoolean("postojiKviz");
            azurirajKategorije(resultData.<Kategorija>getParcelableArrayList("kategorije"));

            if (!postojiKviz || originalnoImeTrenutnogKviza.equals(resultData.getString("nazivKviza"))) {
                kvizovi = resultData.getParcelableArrayList("kvizovi");
                dodajKviz(resultData.getString("nazivKviza"));
            } else
                izbaciAlert("Kviz sa imenom: \"" + resultData.getString("nazivKviza") + "\" već postoji!");

        } else if (resultCode == DOHVATI_PITANJA)
            azurirajMogucaPitanja(resultData.<Pitanje>getParcelableArrayList("pitanja"));
    }
}









 /*

    public void performFileSearch(View v) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, IMPORT_QUIZ);
    }


    ArrayList<String> loadImportIntoArray(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null)
            return null;

        BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream));

        ArrayList<String> quizData = new ArrayList<>();

        String lineInFile;

        while ((lineInFile = bReader.readLine()) != null)
            quizData.add(lineInFile);

        inputStream.close();
        return quizData;
    }


    private void validateImportAndAssign(ArrayList<String> importovanKviz) {

        if (importovanKviz.isEmpty()) {
            throwAlert("Datoteka kviza kojeg importujete nije ispravnog formata!");
            return;
        }

        String kvizPodaci = importovanKviz.get(0);
        Kategorija kategorijaKviza = null;
        ArrayList<Pitanje> importovanaPitanja = new ArrayList<>();

        if (StringUtils.countMatches(kvizPodaci, ',') != 2) {
            throwAlert("Datoteka kviza kojeg importujete nije ispravnog formata!");
            return;
        }

        StringTokenizer razdvajac = new StringTokenizer(kvizPodaci, ",");

        String imeKviza = razdvajac.nextToken();
        String nazivKategorijeKviza = razdvajac.nextToken();
        int brojPitanjaKviza;

        try {
            brojPitanjaKviza = Integer.parseInt(razdvajac.nextToken());
        } catch (Exception e) {
            throwAlert("Datoteka kviza kojeg importujete nije ispravnog formata!");
            return;
        }

        for (Kviz k : kvizovi)
            if (k.getNaziv().equals(imeKviza)) {
                throwAlert("Kviz kojeg importujete već postoji!");
                return;
            }

        if (brojPitanjaKviza != importovanKviz.size() - 1) {
            throwAlert("Kviz kojeg imporujete ima neispravan broj pitanja!");
            return;
        }

        for (int j = 1; j < importovanKviz.size(); j++) {
            String pitanje = importovanKviz.get(j);

            if (pitanje.isEmpty() || StringUtils.countMatches(pitanje, ',') < 3) {
                throwAlert("Datoteka kviza kojeg importujete nema ispravan format!");
                return;
            }

            StringTokenizer razdvajacPitanja = new StringTokenizer(pitanje, ",");

            String nazivPitanja = razdvajacPitanja.nextToken();
            int brojOdgovora, indexTacnogOdgovora;
            try {
                brojOdgovora = Integer.parseInt(razdvajacPitanja.nextToken());
                indexTacnogOdgovora = Integer.parseInt(razdvajacPitanja.nextToken());
            } catch (Exception e) {
                throwAlert("Datoteka kviza kojeg importujete nije ispravnog formata!");
                return;
            }

            ArrayList<String> odgovori = new ArrayList<>();

            while (razdvajacPitanja.hasMoreTokens()) {
                String odgovor = razdvajacPitanja.nextToken();

                for (String o : odgovori)
                    if (odgovor.equals(o)) {
                        throwAlert("Kviz kojeg importujete nije ispravan, postoji ponavljanje odgovora!");
                        return;
                    }

                odgovori.add(odgovor);
            }

            if (brojOdgovora == 0 || brojOdgovora != odgovori.size()) {
                throwAlert("Kviz kojeg importujete ima pitanje sa neispravanim brojem odgovora!");
                return;
            }

            if (indexTacnogOdgovora < 0 || indexTacnogOdgovora >= odgovori.size()) {
                throwAlert("Kviz kojeg importujete ima pitanje neispravanim indexom tačnog odgovora!");
                return;
            }

            for (Pitanje p : importovanaPitanja)
                if (p.getNaziv().equals(nazivPitanja)) {
                    throwAlert("Kviz nije ispravan, postoje pitanja sa istim nazivom!");
                    return;
                }

            Pitanje p = new Pitanje(nazivPitanja, nazivPitanja, odgovori.get(indexTacnogOdgovora));
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

        if (kategorijaKviza == null) {
            kategorijaKviza = new Kategorija(nazivKategorijeKviza, "-3");
            kategorije.add(kategorije.size() - 1, kategorijaKviza);
            indexKategorijeSpiner = kategorije.size() - 2;
            patchCategoryDocumentOnFirebase(kategorijaKviza);
        }

        for (Pitanje p : importovanaPitanja)
            patchQeustionDocumentOnFirebase(p);

        editTextNaziv.setText(imeKviza);
        spinnerAdapter.notifyDataSetChanged();
        spinnerKategorije.setSelection(indexKategorijeSpiner);
        dodanaPitanja.clear();
        dodanaPitanja.addAll(importovanaPitanja);
        adapterDodanaPitanja.notifyDataSetChanged();
    }*/