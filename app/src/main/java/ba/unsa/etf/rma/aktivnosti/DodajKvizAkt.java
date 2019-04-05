package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.CustomAdapter;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class DodajKvizAkt extends AppCompatActivity {

    private ListView lvDodanaPitanja, lvMogucaPitanja;
    private View ldFooterView;
    private Spinner spKategorije;
    private Button btnDodajKviz;
    private EditText etNaziv;

    private ArrayList<Kviz> kvizovi;
    private ArrayList<Kategorija> kategorije;

    private ArrayList<Pitanje> dodana = new ArrayList<>();
    private ArrayList<Pitanje> moguca = new ArrayList<>();

    private CustomAdapter adapterDodana;
    private ArrayAdapter<Pitanje> adapterMoguca;

    private Kviz trenutniKviz;

    ArrayAdapter<Kategorija> sAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        final Intent intent = getIntent();

        kvizovi = intent.getParcelableArrayListExtra("kvizovi");
        kategorije = intent.getParcelableArrayListExtra("kategorije");

        lvDodanaPitanja = findViewById(R.id.lvDodanaPitanja);
        lvMogucaPitanja = findViewById(R.id.lvMogucaPitanja);
        spKategorije = findViewById(R.id.spKategorije);
        btnDodajKviz = findViewById(R.id.btnDodajKviz);
        etNaziv = findViewById(R.id.etNaziv);

        trenutniKviz = (Kviz) intent.getParcelableExtra("kviz");

        if (trenutniKviz == null)
            trenutniKviz = new Kviz(null, null);

        sAdapter = new ArrayAdapter<Kategorija>(this, android.R.layout.simple_spinner_item, kategorije);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kategorije.add(new Kategorija("Dodaj kategoriju", "CADD00"));
        spKategorije.setAdapter(sAdapter);


        initialize();

        popuniMoguca();
        adapterMoguca = new ArrayAdapter<Pitanje>(this, R.layout.element_liste, R.id.naziv, moguca);
        lvMogucaPitanja.setAdapter(adapterMoguca);

        if (intent.getStringExtra("mode").equals("change")) {
            spKategorije.setSelection(intent.getIntExtra("kategorijaIndex", kategorije.size()) - 1);

            etNaziv.setText(trenutniKviz.getNaziv());

            if (trenutniKviz.getPitanja() != null)
                dodana = new ArrayList<>(trenutniKviz.getPitanja());

            adapterDodana = new CustomAdapter(this, dodana, getResources());
            lvDodanaPitanja.setAdapter(adapterDodana);
        }

        btnDodajKviz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validanUnos()) {
                    Intent i = new Intent();
                    if (intent.getStringExtra("mode").equals("add")) {
                        Kviz k = new Kviz(etNaziv.getText().toString(), (Kategorija) spKategorije.getSelectedItem());
                        i.putExtra("kviz", k);
                        i.putExtra("mode", "add");
                    } else {
                        trenutniKviz.setNaziv(etNaziv.getText().toString());
                        trenutniKviz.setKategorija((Kategorija) spKategorije.getSelectedItem());
                        i.putExtra("kviz", trenutniKviz);
                        i.putExtra("mode", "change");
                    }
                    i.putExtra("index", intent.getIntExtra("index", 0));
                    setResult(1, i);
                    finish();
                } else {
                    if (etNaziv.getText().toString().trim().equalsIgnoreCase(""))
                        etNaziv.setError("Unesite naziv kviza!");

                    if (spKategorije.getSelectedItemPosition() == kategorije.size() - 1) {
                        TextView errorText = (TextView) spKategorije.getSelectedView();
                        errorText.setError("");
                        errorText.setTextColor(Color.RED);
                        spKategorije.setSelection(0);
                        Toast.makeText(DodajKvizAkt.this, "Izaberite/Unesite kategoriju", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });

        // Klik na zadnji element liste koji je za dodavanje novog pitanja
        ldFooterView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
                                                intent.putExtra("kviz", trenutniKviz);
                                                startActivityForResult(intent, 2);
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

        spKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == kategorije.size() - 1) {
                    Intent i = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
                    i.putExtra("kviz", trenutniKviz);
                    startActivityForResult(i, 3);
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
        ldFooterView = adapterDodana.getFooterView(lvDodanaPitanja, "Dodaj pitanje");
        lvDodanaPitanja.setAdapter(adapterDodana);
        lvDodanaPitanja.addFooterView(ldFooterView);
    }

    private void popuniMoguca() {
        ArrayList<Kviz> sviKvizovi = getIntent().getParcelableArrayListExtra("kvizovi");

        for (Kviz k : sviKvizovi) {
            if ((trenutniKviz != null && k.equals(trenutniKviz)) || k.getPitanja() == null)
                continue;
            for (Pitanje p : k.getPitanja())
                moguca.add(p);
        }
    }

    private boolean validanUnos() {
        return (etNaziv.getText() != null && etNaziv.getText().length() != 0
                && spKategorije.getSelectedItemPosition() != kategorije.size() - 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null) {
            trenutniKviz = (Kviz) data.getParcelableExtra("kviz");

            if (trenutniKviz.getPitanja() != null)
                dodana = new ArrayList<>(trenutniKviz.getPitanja());

            if(requestCode == 3){
                kategorije.add(kategorije.size() -1, trenutniKviz.getKategorija());
                sAdapter.notifyDataSetChanged();
                spKategorije.setSelection(kategorije.size() - 2);
            }

            adapterDodana.setList(dodana);
            adapterDodana.notifyDataSetChanged();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

