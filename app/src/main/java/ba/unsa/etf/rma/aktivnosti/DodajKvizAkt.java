package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.CustomAdapter;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.NDSpinner;
import ba.unsa.etf.rma.klase.Pitanje;

import static ba.unsa.etf.rma.aktivnosti.KvizoviAkt.PROMIJENI_KVIZ;

public class DodajKvizAkt extends AppCompatActivity {

    static final int DODAJ_PITANJE = 1200;
    static final int DODAJ_KATEGORIJU = 1300;
    private static final int READ_REQUEST_CODE = 42;

    private ListView lvDodanaPitanja;
    private View ldFooterView;
    private NDSpinner spKategorije;
    private EditText etNaziv;

    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Kategorija> kategorije = new ArrayList<>();

    private ArrayList<Pitanje> dodana = new ArrayList<>();
    private ArrayList<Pitanje> moguca = new ArrayList<>();

    private CustomAdapter adapterDodana;
    private ArrayAdapter<Pitanje> adapterMoguca;

    private Kviz trenutniKviz;
    private String staroImeKviza = null;

    private ArrayAdapter<Kategorija> sAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        final Intent intent = getIntent();

        kvizovi = intent.getParcelableArrayListExtra("kvizovi");
        kategorije = intent.getParcelableArrayListExtra("kategorije");

        lvDodanaPitanja = findViewById(R.id.lvDodanaPitanja);
        ListView lvMogucaPitanja = findViewById(R.id.lvMogucaPitanja);
        spKategorije = findViewById(R.id.spKategorije);
        Button btnDodajKviz = findViewById(R.id.btnDodajKviz);
        etNaziv = findViewById(R.id.etNaziv);

        trenutniKviz = intent.getParcelableExtra("kviz");

        if (trenutniKviz == null)
            trenutniKviz = new Kviz(null, null);

        sAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategorije);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kategorije.add(new Kategorija("Dodaj kategoriju", "-2"));
        spKategorije.setAdapter(sAdapter);

        initialize();

        dodana = new ArrayList<>(trenutniKviz.getPitanja());
        adapterDodana = new CustomAdapter(this, dodana, getResources());
        lvDodanaPitanja.setAdapter(adapterDodana);

        adapterMoguca = new ArrayAdapter<>(this, R.layout.element_liste, R.id.naziv, moguca);
        lvMogucaPitanja.setAdapter(adapterMoguca);

        if (intent.getIntExtra("requestCode", 0) == PROMIJENI_KVIZ) {
            staroImeKviza = trenutniKviz.getNaziv();
            spKategorije.setSelection(sAdapter.getPosition(trenutniKviz.getKategorija()));
            etNaziv.setText(trenutniKviz.getNaziv());
        }

        btnDodajKviz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validanUnos()) {
                    if (!postojiKviz()) {
                        Intent i = new Intent();
                        trenutniKviz.setNaziv(etNaziv.getText().toString());
                        trenutniKviz.setKategorija((Kategorija) spKategorije.getSelectedItem());
                        trenutniKviz.setPitanja(dodana);
                        i.putExtra("kviz", trenutniKviz);
                        if (intent.getIntExtra("requestCode", 0) == PROMIJENI_KVIZ)
                            i.putExtra("staroImeKviza", staroImeKviza);

                        i.putParcelableArrayListExtra("kategorije", kategorije);
                        setResult(RESULT_OK, i);
                        finish();
                    } else
                        Toast.makeText(DodajKvizAkt.this, "Kviz sa navedenim imenom već postoji!", Toast.LENGTH_SHORT).show();
                } else {
                    if (etNaziv.getText().length() == 0)
                        etNaziv.setError("Unesite naziv kviza!");

                    if (spKategorije.getSelectedItemPosition() == kategorije.size() - 1) {
                        TextView errorText = (TextView) spKategorije.getSelectedView();
                        errorText.setError("");
                        errorText.setTextColor(Color.RED);
                        errorText.setText(getString(R.string.categoryError));
                    }
                }
            }
        });

        // Klik na zadnji element liste koji je za dodavanje novog pitanja
        ldFooterView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
                                                intent.putParcelableArrayListExtra("dodana", dodana);
                                                intent.putParcelableArrayListExtra("moguca", moguca);
                                                startActivityForResult(intent, DODAJ_PITANJE);
                                            }
                                        }
        );

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


        // Klik na zadnji element spinnera za dodavanje kategorije
        spKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == kategorije.size() - 1) {
                    Intent i = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
                    i.putParcelableArrayListExtra("kategorije", kategorije);
                    startActivityForResult(i, DODAJ_KATEGORIJU);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spKategorije.setSelection(0);
            }
        });
    }

    private void exchange(ArrayList<Pitanje> source, ArrayList<Pitanje> destination, int position) {
        Pitanje p = source.get(position);
        destination.add(p);
        source.remove(position);
        adapterDodana.notifyDataSetChanged();
        adapterMoguca.notifyDataSetChanged();
    }

    private void initialize() {
        adapterDodana = new CustomAdapter(this, dodana, getResources());

        lvDodanaPitanja.setAdapter(adapterDodana);
        lvDodanaPitanja.addFooterView(ldFooterView = adapterDodana.getFooterView(lvDodanaPitanja, "Dodaj pitanje"));
    }

    private boolean postojiKviz() {
        boolean changeMode = getIntent().getIntExtra("requestCode", 0) == PROMIJENI_KVIZ;
        String naziv = etNaziv.getText().toString();

        if (!changeMode || !naziv.equalsIgnoreCase(staroImeKviza))
            for (Kviz k : kvizovi)
                if (k.getNaziv().equalsIgnoreCase(naziv)) {
                    etNaziv.setError("Kviz sa istim imenom već postoji!");
                    return true;
                }

        return false;
    }

    private boolean validanUnos() {
        return (etNaziv.getText() != null && etNaziv.getText().length() != 0
                && spKategorije.getSelectedItemPosition() != kategorije.size() - 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (data != null) {

                if (requestCode == DODAJ_PITANJE) {
                    Pitanje novoPitanje = data.getParcelableExtra("novoPitanje");
                    dodana.add(novoPitanje);
                    // trenutniKviz.setPitanja(dodana);
                    adapterDodana.notifyDataSetChanged();
                }

                if (requestCode == DODAJ_KATEGORIJU) {
                    Kategorija novaKategorija = data.getParcelableExtra("novaKategorija");
                    kategorije.add(kategorije.size() - 1, novaKategorija);
                    // trenutniKviz.setKategorija(novaKategorija);
                    sAdapter.notifyDataSetChanged();
                    spKategorije.setSelection(kategorije.size() - 2);
                }
            }
        }

        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                try {
                    Uri uri = data.getData();
                    validateImport(loadImportIntoArray(uri));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putParcelableArrayListExtra("kategorije", kategorije);
        setResult(RESULT_CANCELED, i);
        finish();
    }

    public void performFileSearch(View v) {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("text/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }


    private void validateImport(ArrayList<String> importovanKviz) {

        if (importovanKviz.isEmpty()) {
            throwAlert("Datoteka kviza kojeg importujete nema ispravan format!");
            return;
        }

        String kvizPodaci = importovanKviz.get(0);
        Kategorija kategorijaKviza = null;
        ArrayList<Pitanje> importovanaPitanja = new ArrayList<>();

        if (StringUtils.countMatches(kvizPodaci, ',') != 2) {
            throwAlert("Datoteka kviza kojeg importujete nema ispravan format!");
            return;
        }

        StringTokenizer razdvajac = new StringTokenizer(kvizPodaci, ",");

        String imeKviza = razdvajac.nextToken();
        String nazivKategorijeKviza = razdvajac.nextToken();;
        int brojPitanjaKviza = Integer.parseInt(razdvajac.nextToken());


        for (Kviz k : kvizovi)
            if (k.getNaziv().equals(imeKviza)){
                throwAlert("Kviz kojeg importujete već postoji!");
                return;
            }


        for (Kategorija k : kategorije)
            if (k.getNaziv().equals(nazivKategorijeKviza)) {
                kategorijaKviza = k;
            }

        if (kategorijaKviza == null)
            kategorijaKviza = new Kategorija(nazivKategorijeKviza, "-1");


        if (brojPitanjaKviza != importovanKviz.size() - 1) {
            throwAlert("Kviz kojeg imporujete ima neispravan broj pitanja!");
            return;
        }

        for (int j = 1; j < importovanKviz.size(); j++) {
            String pitanjeInfo = importovanKviz.get(j);

            if (pitanjeInfo.isEmpty()) {
                throwAlert("Datoteka kviza kojeg importujete nema ispravan format!");
                return;
            }

            StringTokenizer tokenizerPitanja = new StringTokenizer(pitanjeInfo, ",");

            int k = 0;
            String nazivPitanja = "";
            int brojOdgovora = -1, indeksTacnog = -1;

            ArrayList<String> odgovori = new ArrayList<>();

            while (tokenizerPitanja.hasMoreTokens()) {
                if (k == 0)
                    nazivPitanja = tokenizerPitanja.nextToken();
                if (k == 1)
                        brojOdgovora = Integer.parseInt(tokenizerPitanja.nextToken());
                if (k == 2)
                        indeksTacnog = Integer.parseInt(tokenizerPitanja.nextToken());

                if (k > 2) {
                    String odgovor = tokenizerPitanja.nextToken();

                    for (String o : odgovori) {
                        if (odgovor.equals(o))
                            throwAlert("Kviz kojeg importujete nije ispravan postoji ponavljanje odgovora!");
                    }

                    odgovori.add(odgovor);
                }

                k++;
            }

            for (Pitanje p : importovanaPitanja) {
                if (p.getNaziv().equals(nazivPitanja))
                    throwAlert("Kviz nije ispravan postoje dva pitanja sa istim nazivom!");
            }

            if (brojOdgovora != odgovori.size() || brojOdgovora == 0)
                throwAlert("Kviz kojeg importujete ima neispravan broj odgovora!");

            if (indeksTacnog < 0 || indeksTacnog >= odgovori.size())
                throwAlert("Kviz kojeg importujete ima neispravan index tačnog odgovora!");

            Pitanje p = new Pitanje(nazivPitanja, nazivPitanja, odgovori.get(indeksTacnog));
            p.setOdgovori(odgovori);

            importovanaPitanja.add(p);
        }


        etNaziv.setText(imeKviza);
        kategorije.add(kategorijaKviza);
        dodana.addAll(importovanaPitanja);
        adapterDodana.notifyDataSetChanged();
    }

    
    private void throwAlert(String poruka) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(poruka);
        alertDialog.create();
        alertDialog.show();
    }
    
}

