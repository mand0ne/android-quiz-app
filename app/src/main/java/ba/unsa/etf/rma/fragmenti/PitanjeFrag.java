package ba.unsa.etf.rma.fragmenti;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Random;
import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.SharedViewModel;

public class PitanjeFrag extends Fragment {

    private TextView tekstPitanja;
    private ListView odgovoriPitanja;
    private SharedViewModel model;
    private ArrayList<Pitanje> kvizPitanja = new ArrayList<>();
    private Pitanje trenutnoPitanje = null;
    private ArrayList<String> odgovori = new ArrayList<>();
    private ArrayAdapter<String> oAdapter = null;
    private boolean kliknutOdgovor;
    private String odabraniOdgovor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        kvizPitanja.addAll(((Kviz) getArguments().getParcelable("kviz")).getPitanja());
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
        odgovoriPitanja = getView().findViewById(R.id.odgovoriPitanja);
        tekstPitanja = getView().findViewById(R.id.tekstPitanja);
        if(trenutnoPitanje != null)
            tekstPitanja.setText(trenutnoPitanje.getTekstPitanja());
        else
            tekstPitanja.setText("Kviz je završen!");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        oAdapter = new ArrayAdapter<String>(getContext(), R.layout.element_odgovora, R.id.odgovor, odgovori) {
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
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        odgovoriPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                kliknutOdgovor = true;
                odabraniOdgovor = (String) parent.getItemAtPosition(position);;

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
                        }
                    }, 2000);
                }
            }
        });
    }
}
