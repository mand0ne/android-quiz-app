package ba.unsa.etf.rma.klase;

import java.util.ArrayList;

public class Kviz implements Comparable<Kviz> {
    private String naziv;
    private ArrayList<Pitanje> pitanja = new ArrayList<>();
    private Kategorija kategorija;

    public Kviz(String naziv, Kategorija kategorija){
        this.naziv = naziv;
        this.kategorija = kategorija;
    }

    public Kviz(String naziv, ArrayList<Pitanje> pitanja, Kategorija kategorija) {
        this.naziv = naziv;
        this.pitanja = pitanja;
        this.kategorija = kategorija;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public ArrayList<Pitanje> getPitanja() {
        return pitanja;
    }

    public void setPitanja(ArrayList<Pitanje> pitanja) {
        this.pitanja = pitanja;
    }

    public Kategorija getKategorija() {
        return kategorija;
    }

    public void setKategorija(Kategorija kategorija) {
        this.kategorija = kategorija;
    }

    public void dodajPitanje(Pitanje p){
        pitanja.add(p);
    }

    public boolean sadrziPitanje(Pitanje p){
        return pitanja.contains(p);
    }

    @Override
    public boolean equals(Object ob) {
        return this.naziv.equals(((Kviz)ob).naziv);
    }

    @Override
    public int hashCode() {
        return naziv.hashCode()+kategorija.getId().hashCode();
    }

    @Override
    public int compareTo(Kviz o) {
        return this.getNaziv().compareTo(o.getNaziv());
    }
}
