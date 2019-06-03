package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Objects;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.customKlase.RangListaAdapter;
import ba.unsa.etf.rma.modeli.IgraPair;

public class RangLista extends Fragment {

    private ListView rangLista;
    private ArrayList<IgraPair> rangPairs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        rangPairs = getArguments().getParcelableArrayList("rangPair");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rang_lista, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rangLista = view.findViewById(R.id.rangLista);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RangListaAdapter rangListaAdapter = new RangListaAdapter(Objects.requireNonNull(getContext()), rangPairs);
        rangLista.setAdapter(rangListaAdapter);
    }
}
