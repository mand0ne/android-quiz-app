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
import ba.unsa.etf.rma.klase.CustomAdapter;
import ba.unsa.etf.rma.klase.FirebaseIntentService;
import ba.unsa.etf.rma.klase.FirebaseResultReceiver;
import ba.unsa.etf.rma.klase.HttpPostRequest;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.NDSpinner;
import ba.unsa.etf.rma.klase.Pitanje;

import static ba.unsa.etf.rma.aktivnosti.KvizoviAkt.PROMIJENI_KVIZ;
import static ba.unsa.etf.rma.klase.FirebaseIntentService.DOHVATI_PITANJA;

public class DodajKvizAkt extends AppCompatActivity implements FirebaseResultReceiver.Receiver {

    static final int DODAJ_PITANJE = 40;
    static final int DODAJ_KATEGORIJU = 41;
    private static final int IMPORT_QUIZ = 42;

    private Context context;
    private NDSpinner spinnerKategorije;
    private EditText editTextNaziv;

    private CustomAdapter adapterDodanaPitanja;
    private ArrayAdapter<Pitanje> adapterMogucaPitanja;
    private ArrayAdapter<Kategorija> spinnerAdapter;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();

    private ArrayList<Pitanje> dodana = new ArrayList<>();
    private ArrayList<Pitanje> moguca = new ArrayList<>();

    private Kviz trenutniKviz;
    private String originalnoIme;
    private boolean addMode = true;

    private String TOKEN = "";
    public FirebaseResultReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        context = this;
        receiver = new FirebaseResultReceiver(new Handler());
        receiver.setReceiver(this);

        spinnerKategorije = findViewById(R.id.spKategorije);
        editTextNaziv = findViewById(R.id.etNaziv);
        ListView lvDodanaPitanja = findViewById(R.id.lvDodanaPitanja);
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

        dodana = new ArrayList<>(trenutniKviz.getPitanja());
        adapterDodanaPitanja = new CustomAdapter(this, dodana);
        lvDodanaPitanja.setAdapter(adapterDodanaPitanja);
        View ldFooterView = adapterDodanaPitanja.getFooterView(lvDodanaPitanja, "Dodaj pitanje");
        lvDodanaPitanja.addFooterView(ldFooterView);

        adapterMogucaPitanja = new ArrayAdapter<>(this, R.layout.element_liste, R.id.naziv, moguca);
        ListView lvMogucaPitanja = findViewById(R.id.lvMogucaPitanja);
        lvMogucaPitanja.setAdapter(adapterMogucaPitanja);

        firestoreRequest(DOHVATI_PITANJA);

        if (intent.getIntExtra("requestCode", 0) == PROMIJENI_KVIZ) {
            addMode = false;
            originalnoIme = trenutniKviz.getNaziv();
            spinnerKategorije.setSelection(spinnerAdapter.getPosition(trenutniKviz.getKategorija()));
            editTextNaziv.setText(trenutniKviz.getNaziv());
        }

        lvDodanaPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                exchange(dodana, moguca, position);
            }
        });

        lvMogucaPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                exchange(moguca, dodana, position);
            }
        });

        // Klik na zadnji element liste koji je za dodavanje novog pitanja
        ldFooterView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
                                                intent.putParcelableArrayListExtra("dodana", dodana);
                                                intent.putParcelableArrayListExtra("moguca", moguca);
                                                intent.putExtra("token", TOKEN);
                                                startActivityForResult(intent, DODAJ_PITANJE);
                                            }
                                        }
        );

        // Klik na zadnji element spinnera za dodavanje kategorije
        spinnerKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == kategorije.size() - 1) {
                    Intent intent = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
                    kategorije.remove(0);
                    kategorije.remove(kategorije.size() - 1);
                    intent.putParcelableArrayListExtra("kategorije", kategorije);
                    intent.putExtra("token", TOKEN);
                    startActivityForResult(intent, DODAJ_KATEGORIJU);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerKategorije.setSelection(0);
            }
        });

        btnDodajKviz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validanUnos()) {
                    firestoreRequest(FirebaseIntentService.VALIDAN_KVIZ);
                } else {
                    if (editTextNaziv.getText().length() == 0)
                        editTextNaziv.setError("Unesite naziv kviza!");

                    if (spinnerKategorije.getSelectedItemPosition() == kategorije.size() - 1) {
                        TextView errorText = (TextView) spinnerKategorije.getSelectedView();
                        errorText.setError("");
                        errorText.setTextColor(Color.RED);
                        errorText.setText(getString(R.string.categoryError));
                    }
                }
            }
        });
    }

    private void exchange(ArrayList<Pitanje> source, ArrayList<Pitanje> destination, int position) {
        Pitanje p = source.get(position);
        destination.add(p);
        source.remove(position);
        adapterDodanaPitanja.notifyDataSetChanged();
        adapterMogucaPitanja.notifyDataSetChanged();
    }

    private void firestoreRequest(int request) {
        String naziv = editTextNaziv.getText().toString();

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, FirebaseIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", request);
        intent.putExtra("nazivKviza", naziv);
        startService(intent);
    }

    private boolean validanUnos() {
        return (editTextNaziv.getText() != null && editTextNaziv.getText().length() != 0
                && spinnerKategorije.getSelectedItemPosition() != kategorije.size() - 1);
    }

    private int index() {
        for (int i = 0; i < kvizovi.size(); i++) {
            if (kvizovi.get(i).getNaziv().equals(originalnoIme))
                return i;
        }
        return -1;
    }

    private void dodajKviz(String naziv) {
        trenutniKviz.setNaziv(naziv);
        trenutniKviz.setKategorija((Kategorija) spinnerKategorije.getSelectedItem());
        trenutniKviz.setPitanja(dodana);
        patchQuizDocumentOnFirebase(trenutniKviz);

        if (addMode)
            kvizovi.add(trenutniKviz);
        else {
            try {
                kvizovi.set(index(), trenutniKviz);
            } catch (Exception e) {

            }
        }

        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("kvizovi", kvizovi);

        if (kategorije.size() >= 2) {
            kategorije.remove(0);
            kategorije.remove(kategorije.size() - 1);
        }

        intent.putParcelableArrayListExtra("kategorije", kategorije);
        setResult(RESULT_OK, intent);

        finish();
    }

    private void patchCategoryDocumentOnFirebase(Kategorija novaKategorija) {
        String dokument = "{\"fields\": { \"naziv\": {\"stringValue\": \"" + novaKategorija.getNaziv() + "\"}," +
                "\"idIkonice\": {\"integerValue\": \"" + novaKategorija.getId() + "\"}}}";

        new HttpPostRequest().execute("Kategorije", TOKEN, dokument, novaKategorija.firebaseId());
    }

    private void patchQuizDocumentOnFirebase(Kviz noviKviz) {
        StringBuilder dokument = new StringBuilder("{\"fields\": { \"naziv\": {\"stringValue\": \"" + noviKviz.getNaziv() + "\"}," +
                "\"idKategorije\": {\"stringValue\": \"" + noviKviz.getKategorija().firebaseId() + "\"}," +
                "\"pitanja\": {\"arrayValue\": { \"values\": [");

        ArrayList<Pitanje> pitanja = noviKviz.getPitanja();
        for (int i = 0; i < pitanja.size(); i++) {
            dokument.append("{\"stringValue\": \"");
            dokument.append(pitanja.get(i).firebaseId());
            dokument.append("\"}");
            if (i < pitanja.size() - 1)
                dokument.append(",");
        }

        dokument.append("]}}}}");
        new HttpPostRequest().execute("Kvizovi", TOKEN, dokument.toString(), trenutniKviz.firebaseId());
    }

    private void patchQeustionDocumentOnFirebase(Pitanje novoPitanje) {
        StringBuilder dokument = new StringBuilder("{\"fields\": { \"naziv\": {\"stringValue\": \"" + novoPitanje.getNaziv() + "\"}," +
                "\"odgovori\": {\"arrayValue\": {\"values\": [");

        int indexTacnog = 0;

        ArrayList<String> odgovori = novoPitanje.getOdgovori();
        for (int i = 0; i < odgovori.size(); i++) {
            dokument.append("{\"stringValue\": \"");
            dokument.append(odgovori.get(i));
            dokument.append("\"}");
            if (i < odgovori.size() - 1)
                dokument.append(",");
            if (odgovori.get(i).equals(novoPitanje.getTacan()))
                indexTacnog = i;
        }

        dokument.append("]}}, \"indexTacnog\": {\"integerValue\": \"");
        dokument.append(indexTacnog);
        dokument.append("\"}}}");

        new HttpPostRequest().execute("Pitanja", TOKEN, dokument.toString(), novoPitanje.firebaseId());
    }

    private void throwAlert(String poruka) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setNeutralButton("U redu", null);
        alertDialog.setMessage(poruka);
        alertDialog.create();
        alertDialog.show();
    }

    public void azurirajMoguca(ArrayList<Pitanje> mogucaPitanja) {
        moguca.clear();
        moguca.addAll(mogucaPitanja);
        moguca.removeAll(dodana);
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
                    Pitanje novoPitanje = data.getParcelableExtra("novoPitanje");
                    patchQeustionDocumentOnFirebase(novoPitanje);
                    dodana.add(novoPitanje);
                    adapterDodanaPitanja.notifyDataSetChanged();
                    firestoreRequest(DOHVATI_PITANJA);
                }

                if (requestCode == DODAJ_KATEGORIJU) {
                    ArrayList<Kategorija> noveKategorije = data.<Kategorija>getParcelableArrayListExtra("noveKategorije");
                    Kategorija novaKategorija = noveKategorije.get(noveKategorije.size() - 1);
                    patchCategoryDocumentOnFirebase(novaKategorija);
                    azurirajKategorije(noveKategorije);
                    spinnerKategorije.setSelection(spinnerAdapter.getPosition(novaKategorija));
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
        if (resultCode == FirebaseIntentService.VALIDAN_KVIZ) {
            boolean postojiKviz = resultData.getBoolean("postojiKviz");
            azurirajKategorije(resultData.<Kategorija>getParcelableArrayList("kategorije"));

            if (!postojiKviz || originalnoIme.equals(resultData.getString("nazivKviza"))) {
                kvizovi = resultData.getParcelableArrayList("kvizovi");
                dodajKviz(resultData.getString("nazivKviza"));
            } else
                throwAlert("Kviz sa imenom: \"" + resultData.getString("nazivKviza") + "\" već postoji!");

        } else if (resultCode == DOHVATI_PITANJA) {
            azurirajMoguca(resultData.<Pitanje>getParcelableArrayList("pitanja"));
        }
    }
}


/*
    // Import quiz button onClick
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
        dodana.clear();
        dodana.addAll(importovanaPitanja);
        adapterDodanaPitanja.notifyDataSetChanged();
    }

    */
