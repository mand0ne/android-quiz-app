package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.HttpGetRequest;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;

public class DodajKategorijuAkt extends AppCompatActivity implements IconDialog.Callback {

    private EditText etNaziv;
    private EditText etIkona;
    private Icon[] selectedIcons;
    private ArrayList<Kategorija> postojeceKategorije;
    private String TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kategoriju_akt);

        Button btnDodajKategoriju = findViewById(R.id.btnDodajKategoriju);
        Button btnDodajIkonu = findViewById(R.id.btnDodajIkonu);
        etNaziv = findViewById(R.id.etNaziv);
        etIkona = findViewById(R.id.etIkona);
        etIkona.setEnabled(false);

        Intent intent = getIntent();
        final IconDialog iconDialog = new IconDialog();
        postojeceKategorije = intent.getParcelableArrayListExtra("kategorije");
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
                if (validanUnos()) {
                    if (!postojiKategorija()) {
                        Kategorija novaKategorija = new Kategorija(etNaziv.getText().toString(), etIkona.getText().toString());
                        Intent i = new Intent();

                        postojeceKategorije.add(novaKategorija);
                        i.putParcelableArrayListExtra("kategorije", postojeceKategorije);
                        setResult(RESULT_OK, i);
                        finish();
                    } else
                        Toast.makeText(DodajKategorijuAkt.this, "Kategorija sa istim imenom već postoji!", Toast.LENGTH_SHORT).show();
                } else {
                    if (etNaziv.getText().toString().length() == 0)
                        etNaziv.setError("Unesite naziv kategorije!");

                    if (etIkona.getText().toString().length() == 0)
                        etIkona.setError("Izaberite ikonu!");
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putParcelableArrayListExtra("kategorije", postojeceKategorije);
        setResult(RESULT_CANCELED, i);
        finish();
    }

    private boolean postojiKategorija() {
        String naziv = etNaziv.getText().toString();

        try {
            new HttpGetRequest(DodajKategorijuAkt.this).execute("CATEGORY-VALID", TOKEN).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Kategorija ka : postojeceKategorije)
            if (ka.getNaziv().equals(naziv))
                return true;

        return false;
    }

    private boolean validanUnos() {
        return etIkona.getText().toString().length() != 0
                && etNaziv.getText().toString().length() != 0;
    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        selectedIcons = icons;
        etIkona.setText(String.valueOf(selectedIcons[0].getId()));
    }

    public void azurirajKategorije(ArrayList<Kategorija> kategorije) {
        postojeceKategorije = kategorije;
    }
}