package ba.unsa.etf.rma.aktivnosti;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.FirebaseIntentService;
import ba.unsa.etf.rma.klase.FirebaseResultReceiver;
import ba.unsa.etf.rma.klase.Kategorija;

import static ba.unsa.etf.rma.klase.FirebaseIntentService.VALIDNA_KATEGORIJA;

public class DodajKategorijuAkt extends AppCompatActivity implements IconDialog.Callback, FirebaseResultReceiver.Receiver {

    private EditText etNaziv;
    private EditText etIkona;

    private Icon[] selectedIcons;

    private String TOKEN = "";
    public FirebaseResultReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kategoriju_akt);

        receiver = new FirebaseResultReceiver(new Handler());
        receiver.setReceiver(this);

        etNaziv = findViewById(R.id.etNaziv);
        etIkona = findViewById(R.id.etIkona);
        etIkona.setEnabled(false);

        Button btnDodajKategoriju = findViewById(R.id.btnDodajKategoriju);
        Button btnDodajIkonu = findViewById(R.id.btnDodajIkonu);

        Intent intent = getIntent();
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
                if (validanUnos())
                    firestoreRequest(VALIDNA_KATEGORIJA);
                else {
                    if (etNaziv.getText().toString().length() == 0)
                        etNaziv.setError("Unesite naziv kategorije!");

                    if (etIkona.getText().toString().length() == 0)
                        etIkona.setError("Izaberite ikonu!");
                }
            }
        });
    }


    private boolean validanUnos() {
        return etIkona.getText().length() != 0 && etNaziv.getText().length() != 0;
    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        selectedIcons = icons;
        etIkona.setText(String.valueOf(selectedIcons[0].getId()));
    }


    private void dodajKategoriju(String nazivKategorije, ArrayList<Kategorija> noveKategorije) {
        Intent intent = new Intent();
        if(nazivKategorije != null)
            noveKategorije.add(new Kategorija(nazivKategorije, etIkona.getText().toString()));

        intent.putExtra("noveKategorije", noveKategorije);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void firestoreRequest(int request) {
        String naziv = etNaziv.getText().toString();
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, DodajKategorijuAkt.this, FirebaseIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", request);
        intent.putExtra("nazivKategorije", naziv);
        startService(intent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == VALIDNA_KATEGORIJA) {
            if (resultData.getBoolean("postojiKategorija"))
                throwAlert(resultData.getString("nazivKategorije"));
            else {
                dodajKategoriju(resultData.getString("nazivKategorije"),
                        resultData.<Kategorija>getParcelableArrayList("noveKategorije"));
            }
        }
    }

    private void throwAlert(String naziv) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setNeutralButton("U redu", null);
        alertDialog.setMessage("Kategorija sa nazivom: \"" + naziv + "\" već postoji!");
        alertDialog.create();
        alertDialog.show();
    }
}