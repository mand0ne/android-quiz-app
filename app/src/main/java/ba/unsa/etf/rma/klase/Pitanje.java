package ba.unsa.etf.rma.klase;

import java.util.ArrayList;
import java.util.Collections;

public class Pitanje {
    private String naziv;
    private String tekstPitanja;
    private ArrayList<String> odgovori = new ArrayList<>();
    private String tacan;

    public Pitanje(String naziv, String tekstPitanja, String tacan) {
        this.naziv = naziv;
        this.tekstPitanja = tekstPitanja;
        odgovori.add(tacan);
        this.tacan = tacan;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getTekstPitanja() {
        return tekstPitanja;
    }

    public void setTekstPitanja(String tekstPitanja) {
        this.tekstPitanja = tekstPitanja;
    }

    public ArrayList<String> getOdgovori() {
        return odgovori;
    }

    public void setOdgovori(ArrayList<String> odgovori) {
        this.odgovori = odgovori;
    }

    public String getTacan() {
        return tacan;
    }

    public void setTacan(String tacan) {
        this.tacan = tacan;
    }

    public ArrayList<String> dajRandomOdgovore(){
        ArrayList<String> randomOdgovori = new ArrayList<>();
        Collections.copy(randomOdgovori, odgovori);
        Collections.shuffle(randomOdgovori);
        return randomOdgovori;
    }

    public boolean postojiOdgovor(String odgovor){
        return odgovori.contains(odgovor);
    }

    public void dodajOdgovor(String odgovor){
        odgovori.add(odgovor);
    }

    @Override
    public String toString() {
        return naziv;
    }
}
