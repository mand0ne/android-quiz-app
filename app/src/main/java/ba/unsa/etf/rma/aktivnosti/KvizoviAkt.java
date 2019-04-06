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

public class KvizoviAkt extends AppCompatActivity {

    static final int DODAJ_KVIZ = 1100;
    static final int PROMIJENI_KVIZ = 2100;

    private Context context;
    private ListView lvKvizovi;
    private Spinner spPostojeceKategorije;
    private View lvFooterView;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();

    private ArrayAdapter<Kategorija> sAdapter = null;
    private CustomAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kvizovi_akt);
        context = this;

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
                                                intent.putExtra("requestCode", DODAJ_KVIZ);
                                                intent.putParcelableArrayListExtra("kategorije", kategorije);
                                                intent.putParcelableArrayListExtra("kvizovi", kvizovi);
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
                intent.putParcelableArrayListExtra("kvizovi", kvizovi);
                intent.putExtra("kviz", kvizovi.get(position));

                // Zapamtiti TACNU poziciju u listView-u
                // koja se moze poremetiti zbog recikliranja u getView metodi.
                intent.putExtra("index", position);

                intent.putExtra("requestCode", PROMIJENI_KVIZ);
                startActivityForResult(intent, PROMIJENI_KVIZ);
            }
        });

        spPostojeceKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Kategorija ka = (Kategorija) spPostojeceKategorije.getSelectedItem();

                if (ka != null) {
                    if (ka.getId().equals("-1"))
                        adapter.setList(kvizovi);

                    else {
                        ArrayList<Kviz> newKvizovi = new ArrayList<>();
                        for (Kviz k : kvizovi)
                            if (k.getKategorija() != null && k.getKategorija().getNaziv().equals(ka.getNaziv())
                                    || ka.getId().equals("-1"))
                                newKvizovi.add(k);

                        adapter.setList(newKvizovi);
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
                if (requestCode == PROMIJENI_KVIZ)
                    kvizovi.set(data.getIntExtra("index", 0), novi);
                else if (requestCode == DODAJ_KVIZ)
                    kvizovi.add(novi);

                azurirajKategorije(data);
            }

            adapter.notifyDataSetChanged();
        }
        else if(resultCode == RESULT_CANCELED)
             azurirajKategorije(data);
    }

    public void azurirajKategorije(@Nullable Intent data){
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
        adapter = new CustomAdapter(context, kvizovi, getResources());
        lvKvizovi.setAdapter(adapter);
        lvKvizovi.addFooterView(lvFooterView = adapter.getFooterView(lvKvizovi, "Dodaj kviz"));
    }
}
