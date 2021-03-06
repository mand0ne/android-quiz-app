package ba.unsa.etf.rma.customKlase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class IgraViewModel extends ViewModel {
    private final MutableLiveData<Boolean> odgovor = new MutableLiveData<>();
    private final MutableLiveData<Double> skor = new MutableLiveData<>();

    public LiveData<Boolean> getOdgovor() {
        return odgovor;
    }

    public void setOdgovor(Boolean o) {
        odgovor.setValue(o);
    }

    public LiveData<Double> getSkor() {
        return skor;
    }

    public void setSkor(Double d) {
        skor.setValue(d);
    }
}

