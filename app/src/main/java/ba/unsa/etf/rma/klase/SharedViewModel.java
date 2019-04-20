package ba.unsa.etf.rma.klase;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> odgovor = new MutableLiveData<>();

    public void setOdgovor(Boolean o) {
        odgovor.setValue(o);
    }

    public LiveData<Boolean> getOdgovor() {
        return odgovor;
    }
}

