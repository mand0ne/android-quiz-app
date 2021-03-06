package ba.unsa.etf.rma.aktivnosti;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.customKlase.ConnectionStateMonitor;
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.firestore.FirestoreResultReceiver;
import ba.unsa.etf.rma.modeli.Kategorija;

import static ba.unsa.etf.rma.customKlase.ConnectionStateMonitor.CONNECTION_LOST;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.AZURIRAJ_KATEGORIJE;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.VALIDNA_KATEGORIJA;

public class DodajKategorijuAkt extends AppCompatActivity implements IconDialog.Callback, FirestoreResultReceiver.Receiver, ConnectionStateMonitor.NetworkAwareActivity {

    private Context context;
    private EditText etNaziv;
    private EditText etIkona;
    private Icon[] selectedIcons;

    private FirestoreResultReceiver receiver;
    // Firestore access token
    private String TOKEN;
    private ConnectionStateMonitor connectionStateMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kategoriju_akt);

        context = this;
        receiver = new FirestoreResultReceiver(new Handler());
        receiver.setReceiver(this);

        etNaziv = findViewById(R.id.etNaziv);
        etIkona = findViewById(R.id.etIkona);
        etIkona.setEnabled(false);

        Button btnDodajKategoriju = findViewById(R.id.btnDodajKategoriju);
        Button btnDodajIkonu = findViewById(R.id.btnDodajIkonu);

        final Intent intent = getIntent();
        final IconDialog iconDialog = new IconDialog();
        TOKEN = intent.getStringExtra("token");

        connectionStateMonitor = new ConnectionStateMonitor(this, (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE));
        connectionStateMonitor.registerNetworkCallback();

        btnDodajIkonu.setOnClickListener(v -> {
            iconDialog.setSelectedIcons(selectedIcons);
            iconDialog.show(getSupportFragmentManager(), "icon_dialog");
        });

        btnDodajKategoriju.setOnClickListener(v -> {
            if (etNaziv.getText().toString().length() == 0)
                etNaziv.setError("Unesite naziv kategorije!");
            else if (etIkona.getText().toString().length() == 0)
                etIkona.setError("Izaberite ikonu!");
            else
                firestoreRequest();
        });
    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        selectedIcons = icons;
        etIkona.setText(String.valueOf(selectedIcons[0].getId()));
    }

    private Intent kreirajFirestoreIntent(int request) {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, context, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", request);
        return intent;
    }

    private void firestoreRequest() {
        final Intent intent = kreirajFirestoreIntent(VALIDNA_KATEGORIJA);
        String uneseniNaziv = etNaziv.getText().toString();
        intent.putExtra("nazivKategorije", uneseniNaziv);
        startService(intent);
    }

    private void azurirajKategorijaDokumentFirestore(Kategorija kategorija) {
        final Intent intent = kreirajFirestoreIntent(AZURIRAJ_KATEGORIJE);
        intent.putExtra("kategorija", kategorija);
        startService(intent);
    }

    private void dodajKategoriju(String nazivKategorije) {
        Kategorija novaKategorija = new Kategorija(nazivKategorije, Integer.parseInt(etIkona.getText().toString()));
        azurirajKategorijaDokumentFirestore(novaKategorija);

        final Intent intent = new Intent();
        intent.putExtra("novaKategorija", novaKategorija);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void izbaciAlert(String naziv) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setNeutralButton("U redu", null);
        alertDialog.setMessage("Kategorija sa nazivom: \"" + naziv + "\" već postoji!");
        alertDialog.create();
        alertDialog.show();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == VALIDNA_KATEGORIJA) {
            if (resultData.getBoolean("postojiKategorija"))
                izbaciAlert(resultData.getString("nazivKategorije"));
            else
                dodajKategoriju(resultData.getString("nazivKategorije"));
        }
    }

    @Override
    protected void onDestroy() {
        connectionStateMonitor.unregisterNetworkCallback();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        final Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onNetworkLost() {
        Log.wtf("DodajKategorijuAkt: ", "onNetworkLost");
        Toast.makeText(context, "Connection lost!", Toast.LENGTH_SHORT).show();
        setResult(CONNECTION_LOST, new Intent());
        finish();
    }

    @Override
    public void onNetworkAvailable() {
        Log.wtf("DodajKategorijuAkt: ", "onNetworkAvailable");
        Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show();
    }
}