package ba.unsa.etf.rma.fragmenti;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.FirebaseIntentService;
import ba.unsa.etf.rma.klase.GridViewAdapter;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.KvizoviViewModel;

public class DetailFrag extends Fragment {


    private KvizoviViewModel model;

    private GridView gridKvizovi;

    private ArrayList<Kviz> kvizoviFragment = new ArrayList<>();
    private Kategorija trenutnaKategorija;

    private GridViewAdapter kvizAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        trenutnaKategorija = null;
        kvizoviFragment = new ArrayList<>(Objects.requireNonNull(getArguments().<Kviz>getParcelableArrayList("kvizovi")));
        kvizoviFragment.add(new Kviz("Dodaj kviz", new Kategorija("-100", "-100"), null));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gridKvizovi = Objects.requireNonNull(getView()).findViewById(R.id.gridKvizovi);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        kvizAdapter = new GridViewAdapter(getContext(), kvizoviFragment);
        gridKvizovi.setAdapter(kvizAdapter);

        model = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(KvizoviViewModel.class);
        model.getKategorija().observe(getViewLifecycleOwner(), new Observer<Kategorija>() {
            @Override
            public void onChanged(@Nullable Kategorija kategorija) {
                if (kategorija != null) {
                    trenutnaKategorija = kategorija;
                    filtrirajKvizove();
                    kvizAdapter.notifyDataSetChanged();
                }
            }
        });

        gridKvizovi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                if (kliknutiKviz.getNaziv().equals("Dodaj kviz"))
                    ((KvizoviAkt) Objects.requireNonNull(getActivity())).aktivnostDodajKviz();
                else
                    ((KvizoviAkt) Objects.requireNonNull(getActivity())).aktivnostUrediKviz(kliknutiKviz);

                return true;
            }
        });

        gridKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Kviz kliknutiKviz = (Kviz) parent.getItemAtPosition(position);
                if (!(kliknutiKviz.getNaziv().equals("Dodaj kviz")))
                    ((KvizoviAkt) Objects.requireNonNull(getActivity())).aktivnostIgrajKviz(kliknutiKviz);
                else
                    ((KvizoviAkt) Objects.requireNonNull(getActivity())).aktivnostDodajKviz();
            }
        });
    }

    private void filtrirajKvizove() {
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(), FirebaseIntentService.class);
        intent.putExtra("receiver", ((KvizoviAkt) Objects.requireNonNull(getActivity())).receiver);
        intent.putExtra("token", ((KvizoviAkt)getActivity()).getTOKEN());
        intent.putExtra("action", FirebaseIntentService.FILTRIRAJ_KVIZOVE);
        intent.putExtra("kategorijaId", trenutnaKategorija.firebaseId());
        getActivity().startService(intent);
    }

    public void azurirajKvizove(ArrayList<Kviz> azuriraniKvizovi) {
        kvizoviFragment.clear();
        kvizoviFragment.addAll(azuriraniKvizovi);
        kvizoviFragment.add(new Kviz("Dodaj kviz", new Kategorija("-100", "-100"), null));
        kvizAdapter.notifyDataSetChanged();
    }
}
