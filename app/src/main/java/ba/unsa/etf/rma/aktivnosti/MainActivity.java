package ba.unsa.etf.rma.aktivnosti;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.firestore.AccessToken;

public class MainActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        String TOKEN = null;
        try {
            TOKEN = new AccessToken(this).execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        prikaziAlert(TOKEN);
    }

    private void prikaziAlert(String TOKEN) {
        new Handler().postDelayed(() -> new AlertDialog.Builder(context)
                .setTitle("Svjež početak?")
                .setMessage("Restartovati lokalnu bazu?")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> pokreniAplikaciju(TOKEN, true))
                .setNegativeButton(android.R.string.no, (dialog, which) -> pokreniAplikaciju(TOKEN, false))
                .setIcon(android.R.drawable.ic_dialog_dialer)
                .show(), 1000);
    }

    private void pokreniAplikaciju(String TOKEN, boolean restartDatabase) {
        Intent intent = new Intent(context, KvizoviAkt.class);
        intent.putExtra("restartDatabase", restartDatabase);
        intent.putExtra("token", TOKEN);
        startActivity(intent);
        finish();
    }
}
