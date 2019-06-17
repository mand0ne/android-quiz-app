package ba.unsa.etf.rma.fragmenti;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.customKlase.IgraViewModel;
import ba.unsa.etf.rma.modeli.Kviz;

import static android.graphics.Color.RED;

public class InformacijeFrag extends Fragment {

    private TextView infBrojTacnihPitanja;
    private TextView infBrojPreostalihPitanja, infProcenatTacni, infVrijeme;
    private IgraViewModel model;

    private Kviz trenutniKviz;
    private int brojTacnihPitanja;
    private int brojPreostalihPitanja;
    private double procenatTacnih;
    private CountDownTimer countDownTimer;
    private long timeLeftInMilis;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        trenutniKviz = (Kviz) getArguments().get("kviz");
        azurirajBrojPreostalih(false);
        brojTacnihPitanja = 0;
        procenatTacnih = 0.0;
        timeLeftInMilis = trenutniKviz.getPitanja().size() * 30000;
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
        infVrijeme = view.findViewById(R.id.infVrijeme);
        azurirajSkor();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isAdded()) {
            if (timeLeftInMilis != 0) {
                countDownTimer = new CountDownTimer(timeLeftInMilis + 200, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timeLeftInMilis = millisUntilFinished;
                        azurirajVrijeme();
                    }

                    @Override
                    public void onFinish() {
                        ((IgrajKvizAkt) Objects.requireNonNull(getActivity())).istekloVrijeme(brojPreostalihPitanja + 1);
                    }
                }.start();
            } else
                azurirajVrijeme();

            ((IgrajKvizAkt) Objects.requireNonNull(getActivity())).postaviAlarm(timeLeftInMilis);
            model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(IgraViewModel.class);
            model.getOdgovor().observe(getViewLifecycleOwner(), odgovor -> {
                assert odgovor != null;
                if (odgovor)
                    brojTacnihPitanja++;

                procenatTacnih = (double) brojTacnihPitanja / (trenutniKviz.getPitanja().size() - brojPreostalihPitanja);
                model.setSkor(procenatTacnih);
                azurirajBrojPreostalih(true);

                azurirajSkor();
            });
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null)
            countDownTimer.cancel();
    }

    private void azurirajVrijeme() {
        int minute = (int) (timeLeftInMilis / 1000) / 60;
        int sekunde = (int) (timeLeftInMilis / 1000) % 60;

        if (minute < 1 && sekunde <= 15)
            infVrijeme.setTextColor(RED);

        String timeLeft = String.format(Locale.getDefault(), "%02d:%02d", minute, sekunde);
        infVrijeme.setText(timeLeft);
    }

    void azurirajBrojPreostalih(boolean decrement) {
        if (decrement)
            brojPreostalihPitanja--;
        else
            brojPreostalihPitanja = trenutniKviz.getPitanja().size() - 1;

        if (brojPreostalihPitanja < 0) {
            brojPreostalihPitanja = 0;
            if (countDownTimer != null)
                countDownTimer.cancel();
        }
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
