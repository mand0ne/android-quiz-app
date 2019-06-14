package ba.unsa.etf.rma.fragmenti;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Objects;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.customKlase.GridViewAdapter;
import ba.unsa.etf.rma.customKlase.KvizoviViewModel;
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.modeli.Kategorija;
import ba.unsa.etf.rma.modeli.Kviz;

public class DetailFrag extends Fragment {

    private GridView gridKvizovi;
    private GridViewAdapter kvizAdapter;
    private KvizoviViewModel model;

    private ArrayList<Kviz> kvizoviFragment = new ArrayList<>();
    private Kategorija trenutnaKategorija;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        trenutnaKategorija = null;
        kvizoviFragment = new ArrayList<>(Objects.requireNonNull(getArguments().<Kviz>getParcelableArrayList("kvizovi")));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gridKvizovi = view.findViewById(R.id.gridKvizovi);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        kvizAdapter = new GridViewAdapter(getContext(), kvizoviFragment);
        gridKvizovi.setAdapter(kvizAdapter);

        model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(KvizoviViewModel.class);
        model.getKategorija().observe(getViewLifecycleOwner(), new Observer<Kategorija>() {
            @Override
            public void onChanged(@Nullable Kategorija kategorija) {
                if (kategorija != null) {
                    trenutnaKategorija = kategorija;
                    filtrirajKvizove();
                }
            }
        });

        gridKvizovi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                if (kliknutiKviz.getNaziv().equals("Dodaj kviz"))
                    ((KvizoviAkt) Objects.requireNonNull(getActivity())).dodajAzurirajKvizAktivnost(null);
                else
                    ((KvizoviAkt) Objects.requireNonNull(getActivity())).dodajAzurirajKvizAktivnost(kliknutiKviz);

                return true;
            }
        });

        gridKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                if (!(kliknutiKviz.getNaziv().equals("Dodaj kviz")))
                    ((KvizoviAkt) Objects.requireNonNull(getActivity())).igrajKvizAktivnost(kliknutiKviz);
                else
                    ((KvizoviAkt) Objects.requireNonNull(getActivity())).dodajAzurirajKvizAktivnost(null);
            }
        });
    }

    private void filtrirajKvizove() {
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(), FirestoreIntentService.class);
        intent.putExtra("receiver", ((KvizoviAkt) Objects.requireNonNull(getActivity())).getReceiver());
        intent.putExtra("token", ((KvizoviAkt) getActivity()).getTOKEN());
        intent.putExtra("request", FirestoreIntentService.FILTRIRAJ_KVIZOVE);
        intent.putExtra("kategorijaFirebaseId", trenutnaKategorija.firestoreId());
        getActivity().startService(intent);
    }

    public void azurirajKvizove(ArrayList<Kviz> azuriraniKvizovi) {
        kvizoviFragment.clear();
        kvizoviFragment.addAll(azuriraniKvizovi);
        kvizoviFragment.add(new Kviz("Dodaj kviz", new Kategorija("-100", -100), null));
        kvizAdapter.notifyDataSetChanged();
    }
}
