package ba.unsa.etf.rma.aktivnosti;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

import java.util.ArrayList;
import java.util.Objects;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.firestore.FirestoreResultReceiver;
import ba.unsa.etf.rma.modeli.Kategorija;

import static ba.unsa.etf.rma.firestore.FirestoreIntentService.AZURIRAJ_KATEGORIJE;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.VALIDNA_KATEGORIJA;

public class DodajKategorijuAkt extends AppCompatActivity implements IconDialog.Callback, FirestoreResultReceiver.Receiver {

    private Context context;
    private EditText etNaziv;
    private EditText etIkona;
    private Icon[] selectedIcons;

    private FirestoreResultReceiver receiver;
    // Firestore access token
    private String TOKEN;

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

        btnDodajIkonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconDialog.setSelectedIcons(selectedIcons);
                iconDialog.show(getSupportFragmentManager(), "icon_dialog");
            }
        });

        btnDodajKategoriju.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etNaziv.getText().toString().length() == 0)
                    etNaziv.setError("Unesite naziv kategorije!");
                else if (etIkona.getText().toString().length() == 0)
                    etIkona.setError("Izaberite ikonu!");
                else
                    firestoreRequest();
            }
        });
    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        selectedIcons = icons;
        etIkona.setText(String.valueOf(selectedIcons[0].getId()));
    }

    private void firestoreRequest() {
        String uneseniNaziv = etNaziv.getText().toString();
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, DodajKategorijuAkt.this, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", FirestoreIntentService.VALIDNA_KATEGORIJA);
        intent.putExtra("nazivKategorije", uneseniNaziv);
        startService(intent);
    }

    private void azurirajKategorijaDokumentFirestore(Kategorija kategorija) {
        final Intent intent = new Intent(Intent.ACTION_SEND, null, context, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", AZURIRAJ_KATEGORIJE);
        intent.putExtra("kategorija", kategorija);
        startService(intent);
    }

    private void dodajKategoriju(String nazivKategorije, ArrayList<Kategorija> noveKategorije) {
        Kategorija novaKategorija = new Kategorija(nazivKategorije, etIkona.getText().toString());
        azurirajKategorijaDokumentFirestore(novaKategorija);
        noveKategorije.add(novaKategorija);

        final Intent intent = new Intent();
        intent.putExtra("noveKategorije", noveKategorije);
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
                dodajKategoriju(resultData.getString("nazivKategorije"),
                        Objects.requireNonNull(resultData.<Kategorija>getParcelableArrayList("noveKategorije")));
        }
    }
}