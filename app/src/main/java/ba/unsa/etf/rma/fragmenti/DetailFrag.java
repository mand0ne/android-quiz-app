package ba.unsa.etf.rma.fragmenti;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.GridViewAdapter;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.KvizoviViewModel;

public class DetailFrag extends Fragment {


    private KvizoviViewModel model;

    private GridView gridKvizovi;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> sviKvizoviFragment = new ArrayList<>();
    private ArrayList<Kviz> prikazaniKvizoviFragment = new ArrayList<>();
    private Kategorija trenutnaKategorija;

    private GridViewAdapter kvizAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        kategorije = new ArrayList<>(getArguments().<Kategorija>getParcelableArrayList("kategorije"));
        trenutnaKategorija = kategorije.get(0);
        sviKvizoviFragment = new ArrayList<>(getArguments().<Kviz>getParcelableArrayList("kvizovi"));
        sviKvizoviFragment.add(new Kviz("Dodaj kviz", new Kategorija("-100", "-100"), null));
        prikazaniKvizoviFragment.addAll(sviKvizoviFragment);
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

        kvizAdapter = new GridViewAdapter(getContext(), prikazaniKvizoviFragment);
        gridKvizovi.setAdapter(kvizAdapter);

        model = ViewModelProviders.of(getActivity()).get(KvizoviViewModel.class);
        model.getKategorija().observe(getViewLifecycleOwner(), new Observer<Kategorija>() {
            @Override
            public void onChanged(@Nullable Kategorija kategorija) {
                if (kategorija != null) {
                    trenutnaKategorija = kategorija;
                    filtrirajKvizove();
                    kvizAdapter.notifyDataSetChanged();
                }
            }
        });

        gridKvizovi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                if (kliknutiKviz.getNaziv().equals("Dodaj kviz"))
                    ((KvizoviAkt) getActivity()).aktivnostDodajKviz();
                else
                    ((KvizoviAkt) getActivity()).aktivnostUrediKviz(kliknutiKviz);

                return true;
            }
        });

        gridKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                if (!(kliknutiKviz.getNaziv().equals("Dodaj kviz")))
                    ((KvizoviAkt) getActivity()).aktivnostIgrajKviz(kliknutiKviz);
            }
        });
    }

    private void filtrirajKvizove() {
        if (trenutnaKategorija.getId().equals("-1")) {
            prikazaniKvizoviFragment.clear();
            prikazaniKvizoviFragment.addAll(sviKvizoviFragment);
        } else {
            prikazaniKvizoviFragment.clear();
            for (Kviz k : sviKvizoviFragment)
                if (k.getKategorija() != null && k.getKategorija().getNaziv().equals(trenutnaKategorija.getNaziv())
                        || trenutnaKategorija.getId().equals("-1") || k.getNaziv().equals("Dodaj kviz"))
                    prikazaniKvizoviFragment.add(k);
        }
    }

    public void azurirajKvizove(ArrayList<Kviz> azuriraniKvizovi) {
        sviKvizoviFragment.clear();
        sviKvizoviFragment.addAll(azuriraniKvizovi);
        sviKvizoviFragment.add(new Kviz("Dodaj kviz", new Kategorija("-100", "-100"), null));
        filtrirajKvizove();
        kvizAdapter.notifyDataSetChanged();
    }
}
