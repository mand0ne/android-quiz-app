package ba.unsa.etf.rma.fragmenti;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Objects;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.customKlase.RangListaAdapter;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.customKlase.IgraViewModel;
import ba.unsa.etf.rma.modeli.RangListaKviz;

public class RangLista extends Fragment {

    private ListView rangLista;
    private RangListaAdapter rangListaAdapter;
    private ArrayList<Pair<String, Double>> rangPairs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        rangPairs = (ArrayList<Pair<String, Double>>) getArguments().getSerializable("rangLista");
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

       rangListaAdapter = new RangListaAdapter(Objects.requireNonNull(getContext()), rangPairs);
       rangLista.setAdapter(rangListaAdapter);
    }
}
