package ba.unsa.etf.rma.aktivnosti;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.CustomAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kvizovi_akt);
        context = KvizoviAkt.this;

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
                                                Intent intent = new Intent(context, DodajKvizAkt.class);
                                                intent.putParcelableArrayListExtra("kategorije", kategorije);
                                                intent.putParcelableArrayListExtra("kvizovi", sviKvizovi);
                                                intent.putExtra("requestCode", DODAJ_KVIZ);
                                                startActivityForResult(intent, DODAJ_KVIZ);
                                            }
                                        }
        );

        // Klik na kviz za uredivanje
        lvKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, DodajKvizAkt.class);
                intent.putParcelableArrayListExtra("kategorije", kategorije);
                intent.putParcelableArrayListExtra("kvizovi", sviKvizovi);
                intent.putExtra("kviz", (Kviz) parent.getItemAtPosition(position));
                intent.putExtra("requestCode", PROMIJENI_KVIZ);
                startActivityForResult(intent, PROMIJENI_KVIZ);
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

                spPostojeceKategorije.setSelection(spPostojeceKategorije.getSelectedItemPosition());
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
        sAdapter.notifyDataSetChanged();
    }

    private void initialize() {
        // Za Spinner
        kategorije.add(new Kategorija("Svi", "-1"));

        // Za ListView
        adapter = new CustomAdapter(context, prikazaniKvizovi, getResources());
        lvKvizovi.setAdapter(adapter);
        lvKvizovi.addFooterView(lvFooterView = adapter.getFooterView(lvKvizovi, "Dodaj kviz"));
    }
}
