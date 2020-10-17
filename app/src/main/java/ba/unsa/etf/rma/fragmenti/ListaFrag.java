package ba.unsa.etf.rma.fragmenti;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Objects;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.customKlase.CustomAdapter;
import ba.unsa.etf.rma.customKlase.KvizoviViewModel;
import ba.unsa.etf.rma.modeli.Kategorija;

public class ListaFrag extends Fragment {

    private KvizoviViewModel model;

    private ListView listaKategorija;
    private CustomAdapter kategorijaAdapter = null;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        kategorije = new ArrayList<>(Objects.requireNonNull(getArguments().getParcelableArrayList("kategorije")));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        kategorijaAdapter = new CustomAdapter(Objects.requireNonNull(getActivity()), kategorije);
        listaKategorija.setAdapter(kategorijaAdapter);

        model = ViewModelProviders.of(getActivity()).get(KvizoviViewModel.class);

        listaKategorija.setOnItemClickListener((parent, view, position, id) -> {
            Objects.requireNonNull(getActivity())
                    .findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            model.setKategorija((Kategorija) parent.getItemAtPosition(position));
        });

        model.setKategorija((Kategorija) listaKategorija.getItemAtPosition(0));
    }

    public void azurirajKategorije(ArrayList<Kategorija> noveKategorije) {
        kategorije.clear();
        kategorije.addAll(noveKategorije);
        kategorijaAdapter.notifyDataSetChanged();
    }

    public void refreshujSpinner() {
        ((KvizoviAkt) Objects.requireNonNull(getActivity()))
                .intentServiceFiltriranje((Kategorija) kategorijaAdapter.getItem(0));
        Objects.requireNonNull(getActivity())
                .findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
    }
}
