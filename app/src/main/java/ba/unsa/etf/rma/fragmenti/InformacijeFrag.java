package ba.unsa.etf.rma.fragmenti;

import android.annotation.SuppressLint;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.customKlase.IgraViewModel;
import ba.unsa.etf.rma.modeli.Kviz;

public class InformacijeFrag extends Fragment {

    private TextView infBrojTacnihPitanja;
    private TextView infBrojPreostalihPitanja, infProcenatTacni;
    private Button btnKraj;
    private IgraViewModel model;

    private Kviz trenutniKviz;
    private int brojTacnihPitanja;
    private int brojPreostalihPitanja;
    private double procenatTacnih;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        trenutniKviz = (Kviz) getArguments().get("kviz");
        azurirajBrojPreostalih(false);
        brojTacnihPitanja = 0;
        procenatTacnih = 0.0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_informacije, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView) view.findViewById(R.id.infNazivKviza)).setText(trenutniKviz.getNaziv());
        infBrojTacnihPitanja = view.findViewById(R.id.infBrojTacnihPitanja);
        infBrojPreostalihPitanja = view.findViewById(R.id.infBrojPreostalihPitanja);
        infProcenatTacni = view.findViewById(R.id.infProcenatTacni);
        btnKraj = view.findViewById(R.id.btnKraj);
        azurirajSkor();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isAdded()) {
            model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(IgraViewModel.class);
            model.getOdgovor().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean odgovor) {
                    assert odgovor != null;
                    if (odgovor)
                        brojTacnihPitanja++;

                    procenatTacnih = (double) brojTacnihPitanja / (trenutniKviz.getPitanja().size() - brojPreostalihPitanja);
                    model.setSkor(procenatTacnih);
                    azurirajBrojPreostalih(true);

                    azurirajSkor();
                }
            });

            btnKraj.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Objects.requireNonNull(getActivity()).finish();
                }
            });
        }
    }


    void azurirajBrojPreostalih(boolean decrement) {
        if (decrement)
            brojPreostalihPitanja--;
        else
            brojPreostalihPitanja = trenutniKviz.getPitanja().size() - 1;

        if (brojPreostalihPitanja < 0)
            brojPreostalihPitanja = 0;
    }

    @SuppressLint("SetTextI18n")
    private void azurirajSkor() {
        infBrojPreostalihPitanja.setText(String.valueOf(brojPreostalihPitanja));
        infBrojTacnihPitanja.setText(String.valueOf(brojTacnihPitanja));

        BigDecimal bd = new BigDecimal(procenatTacnih * 100.0);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        infProcenatTacni.setText(bd.doubleValue() + " %");
    }
}
