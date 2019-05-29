package ba.unsa.etf.rma.fragmenti;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.CustomAdapter;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.KvizoviViewModel;

public class ListaFrag extends Fragment {

    private KvizoviViewModel model;

    private ListView listaKategorija;
    private CustomAdapter kategorijaAdapter = null;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        kategorije = new ArrayList<>(getArguments().<Kategorija>getParcelableArrayList("kategorije"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lista, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listaKategorija = view.findViewById(R.id.listaKategorija);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        kategorijaAdapter = new CustomAdapter(getActivity(), kategorije);
        listaKategorija.setAdapter(kategorijaAdapter);

        model = ViewModelProviders.of(getActivity()).get(KvizoviViewModel.class);

        listaKategorija.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                model.setKategorija((Kategorija) parent.getItemAtPosition(position));
            }
        });

        model.setKategorija((Kategorija) listaKategorija.getItemAtPosition(0));
    }

    public void azurirajKategorije(ArrayList<Kategorija> noveKategorije) {
        kategorije.clear();
        kategorije.addAll(noveKategorije);
        kategorijaAdapter.notifyDataSetChanged();
    }
}
