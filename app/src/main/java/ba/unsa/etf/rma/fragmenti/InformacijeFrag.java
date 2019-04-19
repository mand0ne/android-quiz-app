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
import android.widget.TextView;
import android.widget.Toast;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.SharedViewModel;

public class InformacijeFrag extends Fragment {

    private SharedViewModel model;
    private int brojTacnihPitanja;
    private int brojPreostalihPitanja;
    private double procenatTacnih;

    private TextView infNazivKviza, infBrojTacnihPitanja;
    private TextView infBrojPreostalihPitanja, infProcenatTacni;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        brojTacnihPitanja = brojPreostalihPitanja = 0;
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

        infNazivKviza.setText("Test");
        infBrojPreostalihPitanja.setText(brojPreostalihPitanja);
        infBrojTacnihPitanja.setText(brojTacnihPitanja);
        infProcenatTacni.setText(String.valueOf(procenatTacnih));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.getPitanje().observe(getViewLifecycleOwner(), new Observer<Pitanje>() {
            @Override
            public void onChanged(@Nullable Pitanje pitanje) {
                Toast.makeText(getActivity(), "promjena", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
