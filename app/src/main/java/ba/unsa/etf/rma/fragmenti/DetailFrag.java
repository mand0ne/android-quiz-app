package ba.unsa.etf.rma.fragmenti;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.CustomAdapter;
import ba.unsa.etf.rma.klase.GridViewAdapter;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.KvizoviViewModel;

public class DetailFrag extends Fragment {


    private KvizoviViewModel model;

    private GridView gridKvizovi;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> sviKvizovi = new ArrayList<>();
    private ArrayList<Kviz> prikazaniKvizovi = new ArrayList<>();

    private GridViewAdapter kvizAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        kategorije = getArguments().getParcelableArrayList("kategorije");
        sviKvizovi = getArguments().getParcelableArrayList("kvizovi");
        prikazaniKvizovi.addAll(sviKvizovi);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gridKvizovi = getView().findViewById(R.id.gridKvizovi);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        kvizAdapter = new GridViewAdapter(getContext(), prikazaniKvizovi);
        gridKvizovi.setAdapter(kvizAdapter);

        model = ViewModelProviders.of(getActivity()).get(KvizoviViewModel.class);
        model.getKategorija().observe(getViewLifecycleOwner(), new Observer<Kategorija>() {
            @Override
            public void onChanged(@Nullable Kategorija kategorija) {
                if (kategorija != null) {
                    if (kategorija.getId().equals("-1")) {
                        prikazaniKvizovi.clear();
                        prikazaniKvizovi.addAll(sviKvizovi);
                    } else {
                        prikazaniKvizovi.clear();
                        for (Kviz k : sviKvizovi)
                            if (k.getKategorija() != null && k.getKategorija().getNaziv().equals(kategorija.getNaziv())
                                    || kategorija.getId().equals("-1"))
                                prikazaniKvizovi.add(k);
                    }

                    kvizAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
