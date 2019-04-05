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
import ba.unsa.etf.rma.klase.Pitanje;

public class KvizoviAkt extends AppCompatActivity {

    private Context context;
    private ListView lvKvizovi;
    private Spinner spPostojeceKategorije;
    private View lvFooterView;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();

    private ArrayAdapter<Kategorija> sAdapter = null;   // Basic adapter za spiner
    private CustomAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kvizovi_akt);
        context = this;

        lvKvizovi = findViewById(R.id.lvKvizovi);
        spPostojeceKategorije = findViewById(R.id.spPostojeceKategorije);

        initialize();
        popuniKategorije();
        popuniKvizove();

        sAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, kategorije);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPostojeceKategorije.setAdapter(sAdapter);



        // Klik na zadnji element liste koji je za dodavanje novog kviza
        lvFooterView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(context, DodajKvizAkt.class);
                                                intent.putExtra("mode", "add");
                                                intent.putExtra("kategorije", kategorije);
                                                intent.putExtra("kvizovi", kvizovi);
                                                startActivityForResult(intent, 1);
                                            }
                                        }
        );

        lvKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (lvKvizovi.getAdapter().getCount() > 0) {

                    Intent intent = new Intent(context, DodajKvizAkt.class);
                    intent.putExtra("mode", "change");
                    intent.putExtra("kategorije", kategorije);
                    intent.putExtra("kvizovi", kvizovi);
                    intent.putExtra("index", position);
                    intent.putExtra("kategorijaIndex", Integer.valueOf(kvizovi.get(position).getKategorija().getId().substring(4)));
                    intent.putExtra("kviz", kvizovi.get(position));

                    startActivityForResult(intent, 1);
                }
            }
        });

        spPostojeceKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Kategorija ka = (Kategorija) spPostojeceKategorije.getSelectedItem();

                if (ka != null) {
                    if (ka.getId().equals("CSVI00"))
                        adapter.setList(kvizovi);

                    else {
                        ArrayList<Kviz> newKvizovi = new ArrayList<>();
                        for (Kviz k : kvizovi)
                            if (k.getKategorija() != null && k.getKategorija().getNaziv().equals(ka.getNaziv())
                                    || ka.getId().equals("CSVI00"))
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
        if (data != null) {
            Kviz novi = (Kviz) data.getSerializableExtra("kviz");
            if(data.getStringExtra("mode").equals("change"))
                kvizovi.set(data.getIntExtra("index", 0), novi);
            else
                kvizovi.add(novi);
        }

        adapter.notifyDataSetChanged();

    }

    private void initialize() {
        // Za spiner
        kategorije.add(new Kategorija("Svi", "CSVI00")); // Univerzalna kategorija

        // Za ListView
        adapter = new CustomAdapter(context, kvizovi, getResources());
        lvKvizovi.setAdapter(adapter);
        lvKvizovi.addFooterView(lvFooterView = adapter.getFooterView(lvKvizovi, "Dodaj kviz"));
    }


    private void popuniKategorije() {
        kategorije.add(new Kategorija("Sport", "QSPO1"));
        kategorije.add(new Kategorija("Igre", "QIGR2"));
        /*kategorije.add(new Kategorija("Matematika","MAT3"));
        kategorije.add(new Kategorija("Historija","HIST4"));
        kategorije.add(new Kategorija("Geografija","GEO5"));*/
    }

    private void popuniKvizove() {
        ArrayList<Pitanje> pitanja = new ArrayList<>();
        pitanja.add(new Pitanje("Barcelona", "Barcelona klub iz?", "Spanija"));
        pitanja.get(0).getOdgovori().add("Spanija");
        pitanja.get(0).getOdgovori().add("Francuska");
        pitanja.get(0).getOdgovori().add("Italija");


        ArrayList<Pitanje> pitanja2 = new ArrayList<>();
        pitanja2.add(new Pitanje("Pacman", "Koje boje?", "Žuta"));
        pitanja2.get(0).getOdgovori().add("Žuta");
        pitanja2.get(0).getOdgovori().add("Zelena");
        pitanja2.get(0).getOdgovori().add("Crvena");


        Kviz k = new Kviz("Fudbal", pitanja, kategorije.get(1));
        kvizovi.add(k);

        Kviz k2 = new Kviz("Pacman", pitanja2, kategorije.get(2));
        kvizovi.add(k2);
    }


}
