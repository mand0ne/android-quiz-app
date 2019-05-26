package ba.unsa.etf.rma.klase;

import java.util.ArrayList;
import java.util.List;

class FirestoreJsonParser {

    ArrayList<Kategorija> parsirajKategorije(String kategorijeFirebase) {
        ArrayList<Kategorija> listaKategorija = new ArrayList<>();

        String[] kategorijeJson = kategorijeFirebase.split("\\{\n\"name\": ");

        for (int i = 1; i < kategorijeJson.length; i++) {
            Kategorija novaKategorija = parsirajDokumentKategorije(kategorijeJson[i].split("\n"));
            if (novaKategorija != null)
                listaKategorija.add(novaKategorija);
        }

        return listaKategorija;
    }

    Kategorija parsirajDokumentKategorije(String[] kategorija) {
        String firebaseId = kategorija[0].substring(kategorija[0].indexOf("/CAT") + 1, kategorija[0].length() - 2);
        String naziv = null, idIkonice = null;
        try {

            for (int j = 1; j<kategorija.length; j++) {
                if (kategorija[j].contains("\"naziv\": "))
                    naziv = kategorija[j + 1].substring(16, kategorija[j + 1].length() - 1);

                if (kategorija[j].contains("\"idIkonice\": "))
                    idIkonice = kategorija[j + 1].substring(17, kategorija[j + 1].length() - 1);
            }

            if(naziv == null || idIkonice == null)
                return null;

        } catch (Exception e) {
            return null;
        }

        return new Kategorija(naziv, idIkonice, firebaseId);
    }

    ArrayList<Pitanje> parsirajPitanja(String pitanjaFirebase){
        ArrayList<Pitanje> listaPitanja = new ArrayList<>();

        String[] pitanjaJson = pitanjaFirebase.split("\\{\n\"name\": ");

        for (int i = 1; i < pitanjaJson.length; i++) {
            Pitanje novoPitanje = parsirajDokumentPitanje(pitanjaJson[i].split("\n"));
            if (novoPitanje != null)
                listaPitanja.add(novoPitanje);
        }

        return listaPitanja;
    }

    Pitanje parsirajDokumentPitanje(String[] pitanje) {
        String firebaseId = pitanje[0].substring(pitanje[0].indexOf("/QUES") + 1, pitanje[0].length() - 2);
        String naziv = null;
        Integer indexTacnog = null;
        ArrayList<String> odgovori = new ArrayList<>();

        try {

            for (int j = 1; j < pitanje.length; j++) {

                if (pitanje[j].contains("\"naziv\": "))
                    naziv = pitanje[j + 1].substring(16, pitanje[j + 1].length() - 1);

                if (pitanje[j].contains("\"indexTacnog\": "))
                    indexTacnog = Integer.parseInt(pitanje[j + 1].substring(17, pitanje[j + 1].length() - 1));

                if (pitanje[j].contains("\"arrayValue\": ")) {
                    if (pitanje[j+1].contains("values")) {
                        while (!pitanje[j].equals("]")) {
                            if (pitanje[j].contains("\"stringValue\": "))
                                odgovori.add(pitanje[j].substring(16, pitanje[j].length()-1));
                            j++;
                        }
                    }
                }
            }

            if(naziv == null || indexTacnog == null || odgovori.isEmpty())
                return null;

        } catch (Exception e) {
            return null;
        }

        return new Pitanje(naziv, naziv, odgovori, odgovori.get(indexTacnog), firebaseId);
    }

    ArrayList<Kviz> parsirajKvizove(String kvizoviFirebase, ArrayList<Kategorija> kategorije, ArrayList<Pitanje> pitanja){
        ArrayList<Kviz> listaKvizova = new ArrayList<>();

        String[] kvizoviJson = kvizoviFirebase.split("\\{\n\"name\": ");

        for (int i = 1; i < kvizoviJson.length; i++) {
            Kviz noviKviz = parsirajDokumentKviz(kvizoviJson[i].split("\n"), kategorije, pitanja);
            if (noviKviz != null)
                listaKvizova.add(noviKviz);
        }

        return listaKvizova;
    }

    Kviz parsirajDokumentKviz(String[] kviz, ArrayList<Kategorija> kategorije, ArrayList<Pitanje> pitanja){
        String firebaseId = kviz[0].substring(kviz[0].indexOf("/QUES") + 1, kviz[0].length() - 2);
        String naziv = null, idKategorije = null;
        ArrayList<String> idPitanja = new ArrayList<>();

        for (int j = 1; j < kviz.length; j++) {
            if (kviz[j].contains("\"naziv\": "))
                naziv = kviz[j + 1].substring(16, kviz[j + 1].length() - 1);

            if (kviz[j].contains("\"idKategorije\": "))
                idKategorije = kviz[j + 1].substring(16, kviz[j + 1].length() - 1);

            if (kviz[j].contains("\"arrayValue\": ")) {
                if (kviz[j + 1].contains("values")) {
                    while (!kviz[j].equals("]")) {
                        if (kviz[j].contains("\"stringValue\": "))
                            idPitanja.add(kviz[j].substring(16, kviz[j].length()-1));
                        j++;
                    }
                }
            }
        }

        Kategorija kategorijaKviza = null;
        ArrayList<Pitanje> pitanjaKviza = new ArrayList<>();

        for (Kategorija k : kategorije)
            if (k.firebaseId().equals(idKategorije))
                kategorijaKviza = k;
        if (kategorijaKviza == null)
            kategorijaKviza = new Kategorija("Svi", "-1");


        for (Pitanje p : pitanja) {
            for (String pitanjeID : idPitanja) {
                if (p.firebaseId().equals(pitanjeID))
                    pitanjaKviza.add(p);
            }
        }

        return new Kviz(naziv, pitanjaKviza, kategorijaKviza, firebaseId);
    }
}