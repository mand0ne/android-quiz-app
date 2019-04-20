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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.SharedViewModel;

public class InformacijeFrag extends Fragment {

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
        brojPreostalihPitanja = trenutniKviz.getPitanja().size();
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
                Toast.makeText(getContext(), "USO", Toast.LENGTH_SHORT).show();
                brojPreostalihPitanja--;
                if(odgovor)
                    brojTacnihPitanja++;

                procenatTacnih = (double)brojTacnihPitanja / (trenutniKviz.getPitanja().size() - brojPreostalihPitanja);
                azuriraj();
            }
        });

    }

    private void azuriraj(){
        infBrojPreostalihPitanja.setText(String.valueOf(brojPreostalihPitanja));
        infBrojTacnihPitanja.setText(String.valueOf(brojTacnihPitanja));
        infProcenatTacni.setText(String.valueOf(Math.round(procenatTacnih*100))  + " %");
    }
}
