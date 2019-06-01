package ba.unsa.etf.rma.fragmenti;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.Pitanje;
import ba.unsa.etf.rma.customKlase.IgraViewModel;

public class PitanjeFrag extends Fragment {

    private TextView tekstPitanja;
    private ListView odgovoriPitanja;
    private IgraViewModel model;
    private ArrayList<Pitanje> kvizPitanja = new ArrayList<>();
    private Pitanje trenutnoPitanje = null;
    private ArrayList<String> odgovori = new ArrayList<>();
    private ArrayAdapter<String> oAdapter = null;
    private boolean kliknutOdgovor;
    private String odabraniOdgovor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        kvizPitanja.addAll(((Kviz) Objects.requireNonNull(getArguments().getParcelable("kviz"))).getPitanja());
        if (kvizPitanja.size() > 0) {
            trenutnoPitanje = kvizPitanja.remove(new Random().nextInt(kvizPitanja.size()));
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        odgovoriPitanja = Objects.requireNonNull(getView()).findViewById(R.id.odgovoriPitanja);
        tekstPitanja = getView().findViewById(R.id.tekstPitanja);

        if (trenutnoPitanje != null)
            tekstPitanja.setText(trenutnoPitanje.getTekstPitanja());
        else
            tekstPitanja.setText("Kviz je završen!");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        oAdapter = new ArrayAdapter<String>(Objects.requireNonNull(getContext()), R.layout.element_odgovora, R.id.odgovor, odgovori) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, null, parent);

                if (kliknutOdgovor) {
                    String item = getItem(position);

                    if (trenutnoPitanje.getTacan().equals(item))
                        row.setBackgroundColor(getResources().getColor(R.color.zelena));

                    if (odabraniOdgovor.equals(item) && !trenutnoPitanje.getTacan().equals(odabraniOdgovor))
                        row.setBackgroundColor(getResources().getColor(R.color.crvena));
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

        odgovoriPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                kliknutOdgovor = true;
                odabraniOdgovor = (String) parent.getItemAtPosition(position);

                if (odabraniOdgovor.equals(trenutnoPitanje.getTacan()))
                    model.setOdgovor(true);
                else
                    model.setOdgovor(false);

                odgovoriPitanja.invalidateViews();

                if (kvizPitanja.size() > 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            odgovori.clear();
                            trenutnoPitanje = kvizPitanja.remove(new Random().nextInt(kvizPitanja.size()));
                            odgovori.addAll(trenutnoPitanje.dajRandomOdgovore());
                            tekstPitanja.setText(trenutnoPitanje.getTekstPitanja());

                            oAdapter.notifyDataSetChanged();
                            kliknutOdgovor = false;
                            odabraniOdgovor = "";
                        }
                    }, 2000);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            odgovori.clear();
                            oAdapter.notifyDataSetChanged();
                            tekstPitanja.setText("Kviz je završen!");
                            unosRangLista();
                        }
                    }, 2000);
                }
            }
        });
    }

    private void unosRangLista() {
        final EditText input = new EditText(getContext());
        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle("Kviz završen!")
                .setMessage("Unesite nickname:")
                .setView(input)
                .setPositiveButton("OK", null)
                .create();

        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button buttonOk = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                if (buttonOk != null) {
                    buttonOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (input.getText() != null && !input.getText().toString().trim().isEmpty()) {
                                dialog.cancel();
                                ((IgrajKvizAkt) Objects.requireNonNull(getActivity())).azurirajRangListuIPrikazi(input.getText().toString(), model.getSkor().getValue());
                            } else
                                input.setError("Morate unijeti ime i prezime");
                        }
                    });
                }
            }
        });

        alert.show();
    }
}
