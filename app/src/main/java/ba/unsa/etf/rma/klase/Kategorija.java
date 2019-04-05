package ba.unsa.etf.rma.klase;

public class Kategorija {
    private String naziv;
    private String id;

    public Kategorija(String naziv, String id){
        this.naziv = naziv;
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return (id + naziv).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.naziv.equals(((Kategorija)obj).naziv) && this.id.equals(((Kategorija)obj).id);
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return naziv;
    }
}
