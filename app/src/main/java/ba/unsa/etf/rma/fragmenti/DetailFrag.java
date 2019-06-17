package ba.unsa.etf.rma.fragmenti;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Objects;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.customKlase.GridViewAdapter;
import ba.unsa.etf.rma.customKlase.KvizoviViewModel;
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
        kvizoviFragment = new ArrayList<>(Objects.requireNonNull(getArguments().getParcelableArrayList("kvizovi")));
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
        model.getKategorija().observe(getViewLifecycleOwner(), kategorija -> {
            if (kategorija != null) {
                trenutnaKategorija = kategorija;
                ((KvizoviAkt) Objects.requireNonNull(getActivity())).intentServiceFiltriranje(model.getKategorija().getValue());
            }
        });

        gridKvizovi.setOnItemLongClickListener((parent, view, position, id) -> {
            Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
            if (kliknutiKviz.getNaziv().equals("Dodaj kviz"))
                ((KvizoviAkt) Objects.requireNonNull(getActivity())).dodajAzurirajKvizAktivnost(null);
            else
                ((KvizoviAkt) Objects.requireNonNull(getActivity())).dodajAzurirajKvizAktivnost(kliknutiKviz);

            return true;
        });

        gridKvizovi.setOnItemClickListener((parent, view, position, id) -> {
            Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
            if (!(kliknutiKviz.getNaziv().equals("Dodaj kviz")))
                ((KvizoviAkt) Objects.requireNonNull(getActivity())).igrajKvizAktivnost(kliknutiKviz);
            else
                ((KvizoviAkt) Objects.requireNonNull(getActivity())).dodajAzurirajKvizAktivnost(null);
        });
    }

    public void azurirajKvizove(ArrayList<Kviz> azuriraniKvizovi) {
        kvizoviFragment.clear();
        kvizoviFragment.addAll(azuriraniKvizovi);
        kvizoviFragment.add(new Kviz("Dodaj kviz", new Kategorija("-100", -100), "QUIZ[-ADD-]"));
        kvizAdapter.notifyDataSetChanged();
    }
}
