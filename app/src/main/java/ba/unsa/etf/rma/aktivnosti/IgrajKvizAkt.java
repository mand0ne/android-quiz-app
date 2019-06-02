package ba.unsa.etf.rma.aktivnosti;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import java.util.Collections;
import java.util.Comparator;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.firestore.FirestoreResultReceiver;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.fragmenti.RangLista;
import ba.unsa.etf.rma.modeli.IgraPair;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.RangListaKviz;

import static ba.unsa.etf.rma.firestore.FirestoreIntentService.AZURIRAJ_RANG_LISTU;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.DOHVATI_RANG_LISTU;

public class IgrajKvizAkt extends AppCompatActivity implements FirestoreResultReceiver.Receiver {

    private Context context;
    private Kviz igraniKviz;

    private FirestoreResultReceiver receiver;
    // Firestore access token
    private String TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz_akt);

        context = this;
        receiver = new FirestoreResultReceiver(new Handler());
        receiver.setReceiver(this);

        final Intent intent = getIntent();
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

    private void azurirajRangListuFirestore(RangListaKviz rangListaKviz) {
        final Intent intent = new Intent(Intent.ACTION_SEND, null, context, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("rangListaKviz", rangListaKviz);
        intent.putExtra("request", AZURIRAJ_RANG_LISTU);
        startService(intent);
    }

    public void azurirajRangListuIPrikazi(String nickname, Double skor) {
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

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == DOHVATI_RANG_LISTU) {
            RangListaKviz rangListaKviz = resultData.getParcelable("rangListaKviz");
            assert rangListaKviz != null;

            rangListaKviz.getLista().add(new IgraPair(resultData.getString("nickname"), resultData.getDouble("skor")));
            Collections.sort(rangListaKviz.getLista(), new Comparator<IgraPair>() {
                @Override
                public int compare(IgraPair o1, IgraPair o2) {
                    if (o2.second().equals(o1.second()))
                        return o2.first().compareTo(o2.first());

                    return o2.second().compareTo(o1.second());
                }
            });

            azurirajRangListuFirestore(rangListaKviz);

            Bundle bundle = new Bundle();
            bundle.putSerializable("rangPair", rangListaKviz.getLista());
            RangLista rangLista = new RangLista();
            rangLista.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, rangLista).commit();
        }
    }
}


