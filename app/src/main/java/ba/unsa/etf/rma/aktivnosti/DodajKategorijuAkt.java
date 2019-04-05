package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;

public class DodajKategorijuAkt extends AppCompatActivity implements IconDialog.Callback {

    private Button btnDodajKategoriju;
    private Button btnDodajIkonu;
    private EditText etNaziv;
    private EditText etIkona;
    private Icon[] selectedIcons;
    private Kviz trenutniKviz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kategoriju_akt);

        btnDodajKategoriju = findViewById(R.id.btnDodajKategoriju);
        btnDodajIkonu = findViewById(R.id.btnDodajIkonu);
        etNaziv = findViewById(R.id.etNaziv);
        etIkona = findViewById(R.id.etIkona);

        Intent intent = getIntent();
        final IconDialog iconDialog = new IconDialog();
        trenutniKviz = intent.getParcelableExtra("kviz");

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
                if(validanUnos()){
                Kategorija novaKategorija = new Kategorija(etNaziv.getText().toString(), etIkona.getText().toString());
                trenutniKviz.setKategorija(novaKategorija);
                Intent i = new Intent();
                i.putExtra("kviz", trenutniKviz);
                setResult(RESULT_OK, i);
                finish();
                }
                else{
                    if(etNaziv.getText().toString().length() == 0)
                        etNaziv.setError("Unesite naziv kategorije!");

                    if(etIkona.getText().toString().length() == 0)
                        etNaziv.setError("Izaberite ikonu!");
                }
            }
        });

    }

    private boolean validanUnos(){
        return etIkona.getText().toString().length() != 0
                && etNaziv.getText().toString().length() != 0;
    }
    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        selectedIcons = icons;
        etIkona.setText(String.valueOf(selectedIcons[0].getId()));
    }
}