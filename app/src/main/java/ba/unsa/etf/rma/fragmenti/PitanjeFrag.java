package ba.unsa.etf.rma.fragmenti;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.SharedViewModel;

public class PitanjeFrag extends Fragment {

    private SharedViewModel model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pitanje, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.getPitanje().observe(getViewLifecycleOwner(), new Observer<Pitanje>() {
            @Override
            public void onChanged(@Nullable Pitanje pitanje) {

            }
        });

        final int interval = 2000; // 1 Second
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                Toast.makeText(getActivity(), "C'Mom no hands!", Toast.LENGTH_SHORT).show();
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }
}
