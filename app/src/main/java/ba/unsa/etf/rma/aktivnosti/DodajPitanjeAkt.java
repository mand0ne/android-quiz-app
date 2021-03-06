package ba.unsa.etf.rma.aktivnosti;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Objects;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.customKlase.ConnectionStateMonitor;
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.firestore.FirestoreResultReceiver;
import ba.unsa.etf.rma.modeli.Pitanje;

import static ba.unsa.etf.rma.customKlase.ConnectionStateMonitor.CONNECTION_LOST;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.AZURIRAJ_PITANJA;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.VALIDNO_PITANJE;

public class DodajPitanjeAkt extends AppCompatActivity implements FirestoreResultReceiver.Receiver, ConnectionStateMonitor.NetworkAwareActivity {

    private Context context;
    private EditText etNaziv;
    private EditText etOdgovor;
    private Button btnDodajTacan;

    private ArrayAdapter<String> adapterOdgovori;
    private Pitanje novoPitanje;

    private FirestoreResultReceiver receiver;
    // Firestore access token
    private String TOKEN;
    private ConnectionStateMonitor connectionStateMonitor;

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
        novoPitanje = new Pitanje(null, null);

        connectionStateMonitor = new ConnectionStateMonitor(this, (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE));
        connectionStateMonitor.registerNetworkCallback();

        adapterOdgovori = (new ArrayAdapter<String>(this, R.layout.element_odgovora, R.id.odgovor, novoPitanje.getOdgovori()) {
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

        btnDodajOdgovor.setOnClickListener(v -> {
            if (etOdgovor.getText().length() != 0) {
                String odgovor = etOdgovor.getText().toString();
                if (novoPitanje.nePostojiOdgovor(odgovor)) {
                    novoPitanje.dodajOdgovor(odgovor);
                    adapterOdgovori.notifyDataSetChanged();
                    etOdgovor.setText("");
                } else
                    Toast.makeText(DodajPitanjeAkt.this, "Odgovor već postoji!", Toast.LENGTH_LONG).show();
            } else
                etOdgovor.setError("Unesite odgovor!");
        });

        btnDodajTacan.setOnClickListener(v -> {
            if (etOdgovor.getText().length() != 0) {
                String odgovor = etOdgovor.getText().toString();
                if (novoPitanje.nePostojiOdgovor(odgovor)) {
                    novoPitanje.dodajOdgovor(odgovor);
                    novoPitanje.setTacan(odgovor);

                    btnDodajTacan.setEnabled(false);
                    btnDodajTacan.getBackground().setColorFilter(0xFFB79D9D, PorterDuff.Mode.MULTIPLY);

                    adapterOdgovori.notifyDataSetChanged();
                    etOdgovor.setText("");
                } else
                    Toast.makeText(DodajPitanjeAkt.this, "Odgovor već postoji!", Toast.LENGTH_LONG).show();
            } else
                etOdgovor.setError("Unesite odgovor!");
        });

        lvOdgovori.setOnItemClickListener((parent, view, position, id) -> {
            String obrisani = novoPitanje.getOdgovori().remove(position);

            if (obrisani.equals(novoPitanje.getTacan())) {
                novoPitanje.setTacan(null);
                btnDodajTacan.setEnabled(true);
                btnDodajTacan.getBackground().clearColorFilter();
            }

            adapterOdgovori.notifyDataSetChanged();
        });

        btnDodajPitanje.setOnClickListener(v -> {
            if (etNaziv.getText().length() == 0)
                etNaziv.setError("Unesite ime pitanja!");
            else if (novoPitanje.getTacan() == null)
                etOdgovor.setError("Potrebno je unijeti bar jedan, tačan odgovor!");
            else
                firestoreRequest();
        });
    }

    private Intent kreirajFirestoreIntent(int request) {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, context, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", request);
        return intent;
    }

    private void firestoreRequest() {
        Intent intent = kreirajFirestoreIntent(VALIDNO_PITANJE);
        String nazivPitanja = etNaziv.getText().toString();
        intent.putExtra("nazivPitanja", nazivPitanja);
        startService(intent);
    }

    void azurirajPitanjeDokumentFirestore(Pitanje novoPitanje) {
        final Intent intent = kreirajFirestoreIntent(AZURIRAJ_PITANJA);
        intent.putExtra("pitanje", novoPitanje);
        startService(intent);
    }

    private void dodajPitanje(String nazivPitanja) {
        novoPitanje.setNaziv(nazivPitanja);
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

    @Override
    public void onBackPressed() {
        final Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onNetworkLost() {
        Log.wtf("DodajPitanjeAkt: ", "onNetworkLost");
        Toast.makeText(context, "Connection lost!", Toast.LENGTH_SHORT).show();
        setResult(CONNECTION_LOST, new Intent());
        finish();
    }

    @Override
    public void onNetworkAvailable() {
        Log.wtf("DodajPitanjeAkt: ", "onNetworkAvailable");
        Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        connectionStateMonitor.unregisterNetworkCallback();
        super.onDestroy();
    }
}
