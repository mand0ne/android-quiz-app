package ba.unsa.etf.rma.customKlase;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ba.unsa.etf.rma.modeli.Kategorija;

public class KvizoviViewModel extends ViewModel {

    private final MutableLiveData<Kategorija> kategorija = new MutableLiveData<>();

    public MutableLiveData<Kategorija> getKategorija() {
        return kategorija;
    }

    public void setKategorija(Kategorija kategorija) {
        this.kategorija.setValue(kategorija);
    }
}

