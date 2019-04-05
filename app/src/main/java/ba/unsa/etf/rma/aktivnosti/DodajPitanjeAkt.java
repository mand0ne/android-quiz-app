package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class DodajPitanjeAkt extends AppCompatActivity {

    private EditText etNaziv;
    private EditText etOdgovor;
    private ListView lvOdgovori;
    private Button btnDodajOdgovor, btnDodajTacan, btnDodajPitanje;
    private Kviz trenutniKviz;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> odgovori = new ArrayList<>();
    private Pitanje novoPitanje = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje_akt);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        etNaziv = findViewById(R.id.etNaziv);
        etOdgovor = findViewById(R.id.etOdgovor);
        lvOdgovori = findViewById(R.id.lvOdgovori);
        btnDodajOdgovor = findViewById(R.id.btnDodajOdgovor);
        btnDodajTacan = findViewById(R.id.btnDodajTacan);
        btnDodajPitanje = findViewById(R.id.btnDodajPitanje);


        Intent intent = getIntent();
        trenutniKviz = (Kviz) intent.getSerializableExtra("kviz");

        adapter = new ArrayAdapter<String>(this, R.layout.element_odgovora, R.id.odgovor, odgovori);

        lvOdgovori.setAdapter(adapter);

        novoPitanje = new Pitanje(etNaziv.getText().toString(), etNaziv.getText().toString(), null);

        btnDodajOdgovor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validanOdgovor()) {
                    String odgovor = etOdgovor.getText().toString();
                    if (!novoPitanje.postojiOdgovor(odgovor)) {
                        odgovori.add(odgovor);
                        adapter.notifyDataSetChanged();
                        novoPitanje.dodajOdgovor(odgovor);
                    }
                } else {
                    etOdgovor.setError("Unesite odgovor!");
                }
            }
        });

        btnDodajTacan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validanOdgovor()) {
                    String odgovor = etOdgovor.getText().toString();
                    odgovori.add(0, odgovor);
                    adapter.notifyDataSetChanged();
                    novoPitanje.dodajOdgovor(odgovor);
                    novoPitanje.setTacan(odgovor);
                    btnDodajTacan.setEnabled(false);
                    btnDodajTacan.getBackground().setColorFilter(0xFFb79d9d, PorterDuff.Mode.MULTIPLY);

                    lvOdgovori.post(new Runnable() {
                        @Override
                        public void run() {
                            lvOdgovori.getChildAt(0).setBackgroundColor(Color.GREEN);
                        }
                    });
                } else {
                    etOdgovor.setError("Unesite odgovor!");
                }
            }
        });

        lvOdgovori.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String obrisani = odgovori.remove(position);
                etNaziv.setText(obrisani);
                novoPitanje.getOdgovori().remove(obrisani);
                adapter.notifyDataSetChanged();
            }
        });

        btnDodajPitanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validnoPitanje()) {
                    Intent i = new Intent();
                    novoPitanje.setNaziv(etNaziv.getText().toString());
                    novoPitanje.setTekstPitanja(etNaziv.getText().toString());
                    trenutniKviz.getPitanja().add(novoPitanje);
                    i.putExtra("kviz", trenutniKviz);
                    setResult(2, i);
                    finish();
                } else {
                    etNaziv.setError("Unesite ime pitanja!");
                }
            }
        });
    }

    boolean validanOdgovor() {
        return etOdgovor.getText() != null && etOdgovor.getText().length() != 0;
    }

    boolean validnoPitanje() {
        return !etNaziv.getText().toString().trim().equalsIgnoreCase("")
                && !trenutniKviz.sadrziPitanje(novoPitanje);
    }
}
