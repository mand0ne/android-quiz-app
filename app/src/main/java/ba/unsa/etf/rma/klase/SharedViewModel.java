package ba.unsa.etf.rma.klase;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Pitanje> selected = new MutableLiveData<>();

    public void setPitanje(Pitanje p) {
        selected.setValue(p);
    }

    public LiveData<Pitanje> getPitanje() {
        return selected;
    }
}

