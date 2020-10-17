package ba.unsa.etf.rma.fragmenti;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.customKlase.IgraViewModel;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.Pitanje;

public class PitanjeFrag extends Fragment {

    private TextView tekstPitanja;
    private ListView odgovoriPitanja;
    private IgraViewModel model;
    private ArrayAdapter<String> oAdapter = null;

    private final ArrayList<Pitanje> pitanjaKviza = new ArrayList<>();
    private Pitanje trenutnoPitanje = null;
    private ArrayList<String> odgovori = new ArrayList<>();
    private boolean kliknutOdgovor;
    private String odabraniOdgovor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        pitanjaKviza.addAll(((Kviz) Objects.requireNonNull(getArguments().getParcelable("kviz"))).getPitanja());
        if (pitanjaKviza.size() > 0) {
            trenutnoPitanje = pitanjaKviza.remove(new Random().nextInt(pitanjaKviza.size()));
            odgovori = trenutnoPitanje.dajRandomOdgovore();
        }
        kliknutOdgovor = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pitanje, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        odgovoriPitanja = Objects.requireNonNull(getView()).findViewById(R.id.odgovoriPitanja);
        tekstPitanja = getView().findViewById(R.id.tekstPitanja);

        if (trenutnoPitanje != null)
            tekstPitanja.setText(trenutnoPitanje.getNaziv());
        else
            tekstPitanja.setText("Kviz je završen!");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        oAdapter = new ArrayAdapter<String>(Objects.requireNonNull(getContext()), R.layout.element_odgovora, R.id.odgovor, odgovori) {
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, null, parent);

                if (kliknutOdgovor) {
                    String item = getItem(position);

                    if (trenutnoPitanje.getTacan().equals(item))
                        row.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.zelena));
                    if (odabraniOdgovor.equals(item) && !trenutnoPitanje.getTacan().equals(odabraniOdgovor))
                        row.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.crvena));
                } else
                    row.setBackgroundColor(0x000000);

                return row;
            }

            @Override
            public boolean isEnabled(int position) {
                return !kliknutOdgovor;
            }
        };

        odgovoriPitanja.setAdapter(oAdapter);
        model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(IgraViewModel.class);

        odgovoriPitanja.setOnItemClickListener((parent, view, position, id) -> {
            kliknutOdgovor = true;
            odabraniOdgovor = (String) parent.getItemAtPosition(position);

            model.setOdgovor(odabraniOdgovor.equals(trenutnoPitanje.getTacan()));

            odgovoriPitanja.invalidateViews();

            if (pitanjaKviza.size() > 0) {
                new Handler().postDelayed(() -> {
                    odgovori.clear();
                    trenutnoPitanje = pitanjaKviza.remove(new Random().nextInt(pitanjaKviza.size()));
                    odgovori.addAll(trenutnoPitanje.dajRandomOdgovore());
                    tekstPitanja.setText(trenutnoPitanje.getNaziv());

                    oAdapter.notifyDataSetChanged();
                    kliknutOdgovor = false;
                    odabraniOdgovor = "";
                }, 2000);
            } else {
                new Handler().postDelayed(() -> {
                    ((IgrajKvizAkt) Objects.requireNonNull(getActivity())).iskljuciAlarm();
                    odgovori.clear();
                    oAdapter.notifyDataSetChanged();
                    tekstPitanja.setText("Kviz je završen!");
                    unosRangLista();
                }, 1100);
            }
        });
    }

    private void unosRangLista() {
        final EditText input = new EditText(getContext());
        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("Kviz završen!")
                .setMessage("Unesite nickname za rang listu:")
                .setView(input)
                .setPositiveButton("Unesi i prikazi", null)
                .setNegativeButton("Nazad", null)
                .create();

        alert.setOnShowListener(dialog -> {
            Button buttonOk = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            Button buttonCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
            if (buttonOk != null) {
                buttonOk.setOnClickListener(view -> {
                    if (input.getText() != null && !input.getText().toString().trim().isEmpty()) {
                        dialog.cancel();
                        ((IgrajKvizAkt) Objects.requireNonNull(getActivity()))
                                .dohvatiRangListuZaPrikaz(input.getText().toString(), model.getSkor().getValue());
                    } else
                        input.setError("Morate unijeti nickname!");
                });
            }
            if (buttonCancel != null) {
                buttonCancel.setOnClickListener(view -> {
                    dialog.cancel();
                    Objects.requireNonNull(getActivity()).finish();
                });
            }
        });

        alert.show();
    }
}
