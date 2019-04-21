package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;

public class DodajPitanjeAkt extends AppCompatActivity {

    private EditText etNaziv;
    private EditText etOdgovor;
    private Button btnDodajTacan;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> odgovori = new ArrayList<>();
    private Pitanje novoPitanje = null;

    private ArrayList<Pitanje> svaPitanja = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje_akt);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        etNaziv = findViewById(R.id.etNaziv);
        etOdgovor = findViewById(R.id.etOdgovor);
        ListView lvOdgovori = findViewById(R.id.lvOdgovori);
        Button btnDodajOdgovor = findViewById(R.id.btnDodajOdgovor);
        btnDodajTacan = findViewById(R.id.btnDodajTacan);
        Button btnDodajPitanje = findViewById(R.id.btnDodajPitanje);

        Intent intent = getIntent();

        // U sustini nema smisla slati citav Objekat Kviz, gubi se na performansama
        svaPitanja.addAll(intent.<Pitanje>getParcelableArrayListExtra("dodana"));
        svaPitanja.addAll(intent.<Pitanje>getParcelableArrayListExtra("moguca"));

        adapter = (new ArrayAdapter<String>(this, R.layout.element_odgovora, R.id.odgovor, odgovori) {
            @SuppressWarnings("NullableProblems")
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent);

                if (Objects.requireNonNull(getItem(position)).equals(novoPitanje.getTacan()))
                    row.setBackgroundColor(Color.GREEN);
                else
                    row.setBackgroundColor(0);

                return row;
            }
        });

        lvOdgovori.setAdapter(adapter);

        novoPitanje = new Pitanje(etNaziv.getText().toString(), etNaziv.getText().toString(), null);

        btnDodajOdgovor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validanOdgovor()) {
                    String odgovor = etOdgovor.getText().toString();
                    if (novoPitanje.nePostojiOdgovor(odgovor)) {
                        odgovori.add(odgovor);
                        adapter.notifyDataSetChanged();
                        novoPitanje.dodajOdgovor(odgovor);
                        etOdgovor.setText("");
                    } else
                        Toast.makeText(DodajPitanjeAkt.this, "Odgovor već postoji!", Toast.LENGTH_SHORT).show();
                } else
                    etOdgovor.setError("Unesite odgovor!");
            }
        });

        btnDodajTacan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validanOdgovor()) {
                    String odgovor = etOdgovor.getText().toString();
                    if (novoPitanje.nePostojiOdgovor(odgovor)) {
                        novoPitanje.dodajOdgovor(odgovor);
                        novoPitanje.setTacan(odgovor);
                        btnDodajTacan.setEnabled(false);
                        btnDodajTacan.getBackground().setColorFilter(0xFFB79D9D, PorterDuff.Mode.MULTIPLY);

                        odgovori.add(odgovor);
                        adapter.notifyDataSetChanged();
                        etOdgovor.setText("");
                    } else
                        Toast.makeText(DodajPitanjeAkt.this, "Odgovor već postoji!", Toast.LENGTH_SHORT).show();
                } else
                    etOdgovor.setError("Unesite odgovor!");
            }
        });

        lvOdgovori.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String obrisani = odgovori.remove(position);

                if (obrisani.equals(novoPitanje.getTacan())) {
                    novoPitanje.setTacan(null);
                    btnDodajTacan.setEnabled(true);
                    btnDodajTacan.getBackground().clearColorFilter();
                }

                novoPitanje.getOdgovori().remove(obrisani);
                adapter.notifyDataSetChanged();
            }
        });

        btnDodajPitanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validnoPitanje()) {
                    if (!postojiPitanje()) {
                        Intent i = new Intent();
                        novoPitanje.setNaziv(etNaziv.getText().toString());
                        novoPitanje.setTekstPitanja(etNaziv.getText().toString());
                        i.putExtra("novoPitanje", novoPitanje);
                        setResult(RESULT_OK, i);
                        finish();
                    } else
                        Toast.makeText(DodajPitanjeAkt.this, "Pitanje sa istim imenom već postoji!", Toast.LENGTH_SHORT).show();
                } else {
                    if (novoPitanje.getTacan() == null)
                        etOdgovor.setError("Potrebno je unijeti tačan odgovor!");
                    if (etNaziv.getText().length() == 0)
                        etNaziv.setError("Unesite ime pitanja!");
                }
            }
        });
    }

    boolean validanOdgovor() {
        return etOdgovor != null && !etOdgovor.getText().toString().isEmpty() && etOdgovor.getText() != null && etOdgovor.getText().length() != 0;
    }

    boolean postojiPitanje() {
        for (Pitanje p : svaPitanja)
            if (p.getNaziv().equals(etNaziv.getText().toString()))
                return true;

        return false;
    }

    boolean validnoPitanje() {
        return etNaziv.getText().length() != 0
                && novoPitanje.getTacan() != null;
    }
}
