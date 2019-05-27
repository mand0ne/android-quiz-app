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
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.SharedViewModel;

public class RangLista extends Fragment {

    private SharedViewModel model;
    private Kviz trenutniKviz;
    private int brojTacnihPitanja;
    private int brojPreostalihPitanja;
    private double procenatTacnih;
    private TextView infNazivKviza, infBrojTacnihPitanja;
    private TextView infBrojPreostalihPitanja, infProcenatTacni;
    private Button btnKraj;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trenutniKviz = (Kviz) getArguments().get("kviz");
        azurirajBrojPreostalih(false);
        brojTacnihPitanja = 0;
        procenatTacnih = 0.0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_informacije, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        infNazivKviza = getView().findViewById(R.id.infNazivKviza);
        infBrojTacnihPitanja = getView().findViewById(R.id.infBrojTacnihPitanja);
        infBrojPreostalihPitanja = getView().findViewById(R.id.infBrojPreostalihPitanja);
        infProcenatTacni = getView().findViewById(R.id.infProcenatTacni);
        btnKraj = getView().findViewById(R.id.btnKraj);
        infNazivKviza.setText(trenutniKviz.getNaziv());
        azuriraj();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.getOdgovor().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean odgovor) {
                assert odgovor != null;
                if(odgovor)
                    brojTacnihPitanja++;

                procenatTacnih = (double)brojTacnihPitanja / (trenutniKviz.getPitanja().size() - brojPreostalihPitanja);
                azurirajBrojPreostalih(true);

                azuriraj();
            }
        });

        btnKraj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }


    void azurirajBrojPreostalih(boolean decrement){
        if(decrement)
            brojPreostalihPitanja--;
        else
            brojPreostalihPitanja = trenutniKviz.getPitanja().size() - 1;
        if(brojPreostalihPitanja < 0)
            brojPreostalihPitanja = 0;
    }

    private void azuriraj(){
        infBrojPreostalihPitanja.setText(String.valueOf(brojPreostalihPitanja));
        infBrojTacnihPitanja.setText(String.valueOf(brojTacnihPitanja));
        BigDecimal bd = new BigDecimal(procenatTacnih * 100.0);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        infProcenatTacni.setText(String.valueOf(bd.doubleValue())  + " %");
    }
}
