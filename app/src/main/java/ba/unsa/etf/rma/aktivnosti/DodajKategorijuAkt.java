package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import ba.unsa.etf.rma.R;

public class DodajKategorijuAkt extends AppCompatActivity {

    private Button btnDodajKategoriju;
    private Button btnDodajIkonu;
    private EditText etNaziv;
    private EditText etIkona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kategoriju_akt);

        btnDodajKategoriju = findViewById(R.id.btnDodajKategoriju);
        btnDodajIkonu = findViewById(R.id.btnDodajIkonu);
        etNaziv = findViewById(R.id.etNaziv);
        etIkona = findViewById(R.id.etIkona);

        Intent intent = getIntent();

    }
}