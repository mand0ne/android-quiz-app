package ba.unsa.etf.rma.aktivnosti;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.firestore.FirestoreResultReceiver;
import ba.unsa.etf.rma.modeli.Pitanje;

import static ba.unsa.etf.rma.firestore.FirestoreIntentService.AZURIRAJ_PITANJA;

public class DodajPitanjeAkt extends AppCompatActivity implements FirestoreResultReceiver.Receiver {

    private Context context;
    private EditText etNaziv;
    private EditText etOdgovor;
    private Button btnDodajTacan;

    private ArrayAdapter<String> adapterOdgovori;

    private ArrayList<String> odgovori = new ArrayList<>();
    private Pitanje novoPitanje;

    private FirestoreResultReceiver receiver;
    // Firestore access token
    private String TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje_akt);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); // (°͜ʖ°)

        context = this;
        receiver = new FirestoreResultReceiver(new Handler());
        receiver.setReceiver(this);

        etNaziv = findViewById(R.id.etNaziv);
        etOdgovor = findViewById(R.id.etOdgovor);
        btnDodajTacan = findViewById(R.id.btnDodajTacan);

        ListView lvOdgovori = findViewById(R.id.lvOdgovori);
        Button btnDodajOdgovor = findViewById(R.id.btnDodajOdgovor);
        Button btnDodajPitanje = findViewById(R.id.btnDodajPitanje);

        final Intent intent = getIntent();
        TOKEN = intent.getStringExtra("token");

        adapterOdgovori = (new ArrayAdapter<String>(this, R.layout.element_odgovora, R.id.odgovor, odgovori) {
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

        lvOdgovori.setAdapter(adapterOdgovori);

        novoPitanje = new Pitanje(null, null, null);

        btnDodajOdgovor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etOdgovor.getText().length() != 0) {
                    String odgovor = etOdgovor.getText().toString();
                    if (novoPitanje.nePostojiOdgovor(odgovor)) {
                        novoPitanje.dodajOdgovor(odgovor);

                        odgovori.add(odgovor);
                        adapterOdgovori.notifyDataSetChanged();
                        etOdgovor.setText("");
                    } else
                        Toast.makeText(DodajPitanjeAkt.this, "Odgovor već postoji!", Toast.LENGTH_LONG).show();
                } else
                    etOdgovor.setError("Unesite odgovor!");
            }
        });

        btnDodajTacan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etOdgovor.getText().length() != 0) {
                    String odgovor = etOdgovor.getText().toString();
                    if (novoPitanje.nePostojiOdgovor(odgovor)) {
                        novoPitanje.dodajOdgovor(odgovor);
                        novoPitanje.setTacan(odgovor);

                        btnDodajTacan.setEnabled(false);
                        btnDodajTacan.getBackground().setColorFilter(0xFFB79D9D, PorterDuff.Mode.MULTIPLY);

                        odgovori.add(odgovor);
                        adapterOdgovori.notifyDataSetChanged();
                        etOdgovor.setText("");
                    } else
                        Toast.makeText(DodajPitanjeAkt.this, "Odgovor već postoji!", Toast.LENGTH_LONG).show();
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
                adapterOdgovori.notifyDataSetChanged();
            }
        });

        btnDodajPitanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etNaziv.getText().length() == 0)
                    etNaziv.setError("Unesite ime pitanja!");
                else if (novoPitanje.getTacan() == null)
                    etOdgovor.setError("Potrebno je unijeti bar jedan, tačan odgovor!");
                else
                    firestoreRequest();
            }
        });
    }

    private void firestoreRequest() {
        String naziv = etNaziv.getText().toString();
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, DodajPitanjeAkt.this, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", FirestoreIntentService.VALIDNO_PITANJE);
        intent.putExtra("nazivPitanja", naziv);
        startService(intent);
    }

    private void azurirajPitanjeDokumentFirestore(Pitanje novoPitanje) {
        final Intent intent = new Intent(Intent.ACTION_SEND, null, context, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", AZURIRAJ_PITANJA);
        intent.putExtra("pitanje", novoPitanje);
        startService(intent);
    }

    private void dodajPitanje(String nazivPitanja) {
        novoPitanje.setNaziv(nazivPitanja);
        novoPitanje.setTekstPitanja(etNaziv.getText().toString());
        azurirajPitanjeDokumentFirestore(novoPitanje);

        final Intent intent = new Intent();
        intent.putExtra("novoPitanje", novoPitanje);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void izbaciAlert(String naziv) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setNeutralButton("U redu", null);
        alertDialog.setMessage("Pitanje sa nazivom: \"" + naziv + "\" već postoji!");
        alertDialog.create();
        alertDialog.show();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == FirestoreIntentService.VALIDNO_PITANJE) {
            if (resultData.getBoolean("postojiPitanje"))
                izbaciAlert(resultData.getString("nazivPitanja"));
            else
                dodajPitanje(resultData.getString("nazivPitanja"));
        }
    }

}
