package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.klase.Kviz;

public class IgrajKvizAkt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz_akt);

        Intent intent = getIntent();
        Kviz trenutniKviz = intent.getParcelableExtra("kviz");

        FragmentManager fragmentManager = getSupportFragmentManager();
        InformacijeFrag iFrag = new InformacijeFrag();
        PitanjeFrag pFrag = new PitanjeFrag();

        Bundle b = new Bundle();
        b.putParcelable("kviz", trenutniKviz);
        pFrag.setArguments(b);
        iFrag.setArguments(b);

        fragmentManager.beginTransaction().replace(R.id.informacijePlace, iFrag).commit();
        fragmentManager.beginTransaction().replace(R.id.pitanjePlace, pFrag).commit();
    }
}
