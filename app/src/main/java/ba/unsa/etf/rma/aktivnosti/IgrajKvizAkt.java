package ba.unsa.etf.rma.aktivnosti;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Toast;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.firestore.FirestoreResultReceiver;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.fragmenti.RangLista;
import ba.unsa.etf.rma.modeli.Kategorija;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.RangListaKviz;

import static ba.unsa.etf.rma.firestore.FirestoreIntentService.AZURIRAJ_KATEGORIJE;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.DOHVATI_RANG_LISTU;

public class IgrajKvizAkt extends AppCompatActivity implements FirestoreResultReceiver.Receiver {

    private Kviz igraniKviz = null;

    private Context context;
    public FirestoreResultReceiver receiver;
    private String TOKEN = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz_akt);

        context = this;
        receiver = new FirestoreResultReceiver(new Handler());
        receiver.setReceiver(this);

        Intent intent = getIntent();
        igraniKviz = intent.getParcelableExtra("kviz");
        TOKEN = intent.getStringExtra("token");

        InformacijeFrag iFrag = new InformacijeFrag();
        PitanjeFrag pFrag = new PitanjeFrag();

        Bundle b = new Bundle();
        b.putParcelable("kviz", igraniKviz);

        pFrag.setArguments(b);
        iFrag.setArguments(b);

        getSupportFragmentManager().beginTransaction().replace(R.id.informacijePlace, iFrag).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, pFrag).commit();
    }

    private void firestoreRequest(String nickname, Double skor) {
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, IgrajKvizAkt.this, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("nazivKviza", igraniKviz.getNaziv());
        intent.putExtra("kvizFirebaseId", igraniKviz.firebaseId());
        intent.putExtra("nickname", nickname);
        intent.putExtra("skor", skor);
        intent.putExtra("request", FirestoreIntentService.DOHVATI_RANG_LISTU);
        startService(intent);
    }


    private void patchRankListDocumentOnFirebase(RangListaKviz rangListaKviz) {
        final Intent intent = new Intent(Intent.ACTION_SEND, null, context, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("rangLista", rangListaKviz);
        //intent.putExtra("request", AZURIRAJ_RANG_LISTU);//
        startService(intent);
    }

    public void azurirajRangListuIPrikazi(String nickname, Double skor) {
        firestoreRequest(nickname, skor);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if(resultCode == DOHVATI_RANG_LISTU){
            RangListaKviz rangListaKviz = resultData.getParcelable("rangLista");
            assert rangListaKviz != null;
            rangListaKviz.getLista().add(new Pair<>(resultData.getString("nickname"), resultData.getDouble("skor")));
            Collections.sort(rangListaKviz.getLista(), new Comparator<Pair<String, Double>>() {
                        @Override
                        public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                            return o1.second.compareTo(o2.second);
                        }
                    });


            Bundle bundle = new Bundle();
            bundle.putSerializable("rangLista", rangListaKviz.getLista());
            RangLista rangLista = new RangLista();
            rangLista.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, rangLista).commit();
        }
    }
}


