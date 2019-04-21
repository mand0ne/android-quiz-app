package ba.unsa.etf.rma.klase;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class KvizoviViewModel extends ViewModel {

    private final MutableLiveData<Kategorija> kategorija = new MutableLiveData<>();

    public void setKategorija(Kategorija kategorija){
        this.kategorija.setValue(kategorija);
    }

    public MutableLiveData<Kategorija> getKategorija() {
        return kategorija;
    }
}

