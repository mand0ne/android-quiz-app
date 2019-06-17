package ba.unsa.etf.rma.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import ba.unsa.etf.rma.modeli.Igrac;
import ba.unsa.etf.rma.modeli.Kategorija;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.Pitanje;
import ba.unsa.etf.rma.modeli.RangListaKviz;

public class AppDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "QuizApp.db";
    private static AppDbHelper instance;

    public static class KategorijeEntry {
        static final String TABLE_NAME = "Kategorije";
        static final String COLUMN_NAME_NAZIV = "naziv";
        static final String COLUMN_NAME_ID_IKONICE = "ikonica_id";
        static final String COLUMN_NAME_FIRESTORE_ID = "firestore_id";
    }

    public static class KvizoviEntry {
        static final String TABLE_NAME = "Kvizovi";
        static final String COLUMN_NAME_NAZIV = "naziv";
        static final String COLUMN_NAME_ID_KATEGORIJE = "kategorija_id";
        static final String COLUMN_NAME_PITANJA = "ids_pitanja";
        static final String COLUMN_NAME_FIRESTORE_ID = "firestore_id";
    }

    public static class PitanjaEntry {
        static final String TABLE_NAME = "Pitanja";
        static final String COLUMN_NAME_NAZIV = "naziv";
        static final String COLUMN_NAME_TACAN_ODGOVOR = "tacan_odgovor";
        static final String COLUMN_NAME_ODGOVORI = "odgovori";
        static final String COLUMN_NAME_FIRESTORE_ID = "firestore_id";
    }

    public static class RanglisteEntry {
        static final String TABLE_NAME = "Rangliste";
        static final String COLUMN_NAME_NAZIV_KVIZA = "naziv_kviza";
        static final String COLUMN_NAME_LISTA = "lista";
        static final String COLUMN_NAME_FIRESTORE_ID = "firestore_id";
    }

    private static final String CREATE_TABLE_KATEGORIJE =
            "CREATE TABLE IF NOT EXISTS " + KategorijeEntry.TABLE_NAME + "(" +
                    KategorijeEntry.COLUMN_NAME_FIRESTORE_ID + " TEXT PRIMARY KEY," +
                    KategorijeEntry.COLUMN_NAME_NAZIV + " TEXT," +
                    KategorijeEntry.COLUMN_NAME_ID_IKONICE + " INTEGER)";

    private static final String CREATE_TABLE_KVIZOVI =
            "CREATE TABLE IF NOT EXISTS " + KvizoviEntry.TABLE_NAME + "(" +
                    KvizoviEntry.COLUMN_NAME_FIRESTORE_ID + " TEXT PRIMARY KEY," +
                    KvizoviEntry.COLUMN_NAME_NAZIV + " TEXT," +
                    KvizoviEntry.COLUMN_NAME_ID_KATEGORIJE + " TEXT," +
                    KvizoviEntry.COLUMN_NAME_PITANJA + " TEXT)";

    private static final String CREATE_TABLE_PITANJA =
            "CREATE TABLE IF NOT EXISTS " + PitanjaEntry.TABLE_NAME + "(" +
                    PitanjaEntry.COLUMN_NAME_FIRESTORE_ID + " TEXT PRIMARY KEY," +
                    PitanjaEntry.COLUMN_NAME_NAZIV + " TEXT," +
                    PitanjaEntry.COLUMN_NAME_TACAN_ODGOVOR + " TEXT," +
                    PitanjaEntry.COLUMN_NAME_ODGOVORI + " TEXT)";

    private static final String CREATE_TABLE_RANGLISTE =
            "CREATE TABLE IF NOT EXISTS " + RanglisteEntry.TABLE_NAME + "(" +
                    RanglisteEntry.COLUMN_NAME_FIRESTORE_ID + " TEXT PRIMARY KEY," +
                    RanglisteEntry.COLUMN_NAME_NAZIV_KVIZA + " TEXT," +
                    RanglisteEntry.COLUMN_NAME_LISTA + " TEXT)";

    private SQLiteDatabase sqLiteDatabase = null;

    private AppDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        if (sqLiteDatabase == null)
            sqLiteDatabase = getWritableDatabase();
    }

    synchronized public static AppDbHelper getInstance(Context context) {
        if (instance == null)
            instance = new AppDbHelper(context);

        return instance;
    }


    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_KATEGORIJE);
        db.execSQL(CREATE_TABLE_KVIZOVI);
        db.execSQL(CREATE_TABLE_PITANJA);
        db.execSQL(CREATE_TABLE_RANGLISTE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over

        db.execSQL("DROP TABLE IF EXISTS " + KategorijeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + KvizoviEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PitanjaEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RanglisteEntry.TABLE_NAME);
        onCreate(db);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (sqLiteDatabase != null && sqLiteDatabase.isOpen())
            sqLiteDatabase.close();
    }

    /////////////////////////////////////////////////////////////////
    ///// ------------ Metode tabele "Kategorije" ------------ /////
    ////////////////////////////////////////////////////////////////

    public void azurirajKategoriju(Kategorija kategorija) {
        ContentValues values = new ContentValues();
        values.put(KategorijeEntry.COLUMN_NAME_NAZIV, kategorija.getNaziv());
        values.put(KategorijeEntry.COLUMN_NAME_ID_IKONICE, kategorija.getIdIkonice());
        values.put(KategorijeEntry.COLUMN_NAME_FIRESTORE_ID, kategorija.firestoreId());

        sqLiteDatabase.replace(KategorijeEntry.TABLE_NAME, null, values);
    }

    private Kategorija dajKategoriju(String firestoreId) {
        String selectQuery = "SELECT  * FROM " + KategorijeEntry.TABLE_NAME + " WHERE "
                + KategorijeEntry.COLUMN_NAME_FIRESTORE_ID + " = '" + firestoreId + "'";

        try {
            Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
            Kategorija kategorija = null;
            if (cursor.moveToFirst()) {
                kategorija = new Kategorija(
                        cursor.getString(cursor.getColumnIndex(KategorijeEntry.COLUMN_NAME_NAZIV)),
                        cursor.getInt(cursor.getColumnIndex(KategorijeEntry.COLUMN_NAME_ID_IKONICE)),
                        cursor.getString(cursor.getColumnIndex(KategorijeEntry.COLUMN_NAME_FIRESTORE_ID))
                );
            }

            cursor.close();
            return kategorija;
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<Kategorija> dajSveKategorije() {
        ArrayList<Kategorija> kategorije = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + KategorijeEntry.TABLE_NAME;

        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Kategorija kategorija = new Kategorija(
                            cursor.getString(cursor.getColumnIndex(KategorijeEntry.COLUMN_NAME_NAZIV)),
                            cursor.getInt(cursor.getColumnIndex(KategorijeEntry.COLUMN_NAME_ID_IKONICE)),
                            cursor.getString(cursor.getColumnIndex(KategorijeEntry.COLUMN_NAME_FIRESTORE_ID))
                    );

                    kategorije.add(kategorija);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception ignored) {
        }

        return kategorije;
    }


    //////////////////////////////////////////////////////////////
    ///// ------------ Metode tabele "Kvizovi" ------------ /////
    /////////////////////////////////////////////////////////////

    public void azurirajKviz(Kviz kviz) {
        ContentValues values = new ContentValues();
        values.put(KvizoviEntry.COLUMN_NAME_NAZIV, kviz.getNaziv());
        values.put(KvizoviEntry.COLUMN_NAME_ID_KATEGORIJE, kviz.getKategorija().firestoreId());
        values.put(KvizoviEntry.COLUMN_NAME_FIRESTORE_ID, kviz.firestoreId());

        StringBuilder id_pitanja = new StringBuilder();

        for (Pitanje pitanje : kviz.getPitanja())
            id_pitanja.append(pitanje.firestoreId()).append(',');

        values.put(KvizoviEntry.COLUMN_NAME_PITANJA, id_pitanja.toString());

        sqLiteDatabase.replace(KvizoviEntry.TABLE_NAME, null, values);
    }

    public ArrayList<Kviz> dajSpecificneKvizove(String kategorijaFirestoreId) {
        ArrayList<Kviz> kvizovi = new ArrayList<>();
        String selectQuery;
        if (kategorijaFirestoreId.equals("CAT[-ALL-]"))
            selectQuery = "SELECT  * FROM " + KvizoviEntry.TABLE_NAME;
        else
            selectQuery = "SELECT  * FROM " + KvizoviEntry.TABLE_NAME +
                    " WHERE " + KvizoviEntry.COLUMN_NAME_ID_KATEGORIJE + " = '" + kategorijaFirestoreId + "'";

        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Kviz kviz = new Kviz(
                            cursor.getString(cursor.getColumnIndex(KvizoviEntry.COLUMN_NAME_NAZIV)),
                            dajKategoriju(cursor.getString(cursor.getColumnIndex(KvizoviEntry.COLUMN_NAME_ID_KATEGORIJE))),
                            cursor.getString(cursor.getColumnIndex(KvizoviEntry.COLUMN_NAME_FIRESTORE_ID))
                    );

                    if (kviz.getKategorija() == null)
                        kviz.setKategorija(new Kategorija("Svi", -1));

                    ArrayList<Pitanje> pitanjaKviza = new ArrayList<>();

                    String[] pitanjaFirestoreId = cursor.getString(cursor.getColumnIndex(KvizoviEntry.COLUMN_NAME_PITANJA)).split(",");

                    for (String id : pitanjaFirestoreId) {
                        Pitanje pitanje = dajPitanje(id);
                        if (pitanje != null)
                            pitanjaKviza.add(pitanje);
                    }

                    kviz.setPitanja(pitanjaKviza);

                    kvizovi.add(kviz);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception ignored) {
        }

        return kvizovi;
    }


    //////////////////////////////////////////////////////////////
    ///// ------------ Metode tabele "Pitanja" ------------ /////
    /////////////////////////////////////////////////////////////

    public void azurirajPitanje(Pitanje pitanje) {
        ContentValues values = new ContentValues();
        values.put(PitanjaEntry.COLUMN_NAME_NAZIV, pitanje.getNaziv());
        values.put(PitanjaEntry.COLUMN_NAME_FIRESTORE_ID, pitanje.firestoreId());
        values.put(PitanjaEntry.COLUMN_NAME_TACAN_ODGOVOR, pitanje.getTacan());

        StringBuilder odgovori = new StringBuilder();

        for (String odgovor : pitanje.getOdgovori())
            odgovori.append(odgovor).append(',');

        values.put(PitanjaEntry.COLUMN_NAME_ODGOVORI, odgovori.toString());

        sqLiteDatabase.replace(PitanjaEntry.TABLE_NAME, null, values);
    }

    private Pitanje dajPitanje(String firestoreId) {
        String selectQuery = "SELECT * FROM " + PitanjaEntry.TABLE_NAME + " WHERE "
                + PitanjaEntry.COLUMN_NAME_FIRESTORE_ID + " = '" + firestoreId + "'";

        try {
            Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

            Pitanje pitanje = null;
            if (cursor.moveToFirst()) {
                pitanje = new Pitanje(
                        cursor.getString(cursor.getColumnIndex(PitanjaEntry.COLUMN_NAME_NAZIV)),
                        cursor.getString(cursor.getColumnIndex(PitanjaEntry.COLUMN_NAME_TACAN_ODGOVOR)),
                        cursor.getString(cursor.getColumnIndex(PitanjaEntry.COLUMN_NAME_FIRESTORE_ID)));

                String[] odgovori = cursor.getString(cursor.getColumnIndex(PitanjaEntry.COLUMN_NAME_ODGOVORI))
                        .split(",");

                for (String odgovor : odgovori)
                    if (!odgovor.equals(pitanje.getTacan()))
                        pitanje.dodajOdgovor(odgovor);
            }
            cursor.close();
            return pitanje;
        } catch (Exception e) {
            return null;
        }

    }

    ////////////////////////////////////////////////////////////////
    ///// ------------ Metode tabele "Rangliste" ------------ /////
    ///////////////////////////////////////////////////////////////

    public void azurirajRangListu(RangListaKviz rangListaKviz) {
        ContentValues values = new ContentValues();
        values.put(RanglisteEntry.COLUMN_NAME_FIRESTORE_ID, rangListaKviz.firestoreId());
        values.put(RanglisteEntry.COLUMN_NAME_NAZIV_KVIZA, rangListaKviz.getNazivKviza());


        StringBuilder lista = new StringBuilder();
        ArrayList<Igrac> igraci = rangListaKviz.getLista();

        for (Igrac igrac : igraci)
            lista.append('(').append(igrac.nickname()).append('_').append(igrac.score()).append("),");

        values.put(RanglisteEntry.COLUMN_NAME_LISTA, lista.toString());
        sqLiteDatabase.replace(RanglisteEntry.TABLE_NAME, null, values);
    }

    public RangListaKviz dajRangListu(String kvizFirestoreId) {
        String selectQuery = "SELECT * FROM " + RanglisteEntry.TABLE_NAME + " WHERE "
                + RanglisteEntry.COLUMN_NAME_FIRESTORE_ID + " = 'RANK[" + kvizFirestoreId + "]'";

        try {
            Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

            RangListaKviz rangListaKviz = null;
            if (cursor.moveToFirst())
                rangListaKviz = new RangListaKviz(
                        cursor.getString(cursor.getColumnIndex(RanglisteEntry.COLUMN_NAME_NAZIV_KVIZA)),
                        kvizFirestoreId,
                        dajIgraceIzStringa(cursor.getString(cursor.getColumnIndex(RanglisteEntry.COLUMN_NAME_LISTA))));

            cursor.close();
            return rangListaKviz;
        } catch (Exception e) {
            return null;
        }
    }

    private ArrayList<Igrac> dajIgraceIzStringa(String listaString) {
        String[] igraci = listaString.split(",");
        ArrayList<Igrac> lista = new ArrayList<>();

        try {
            for (String igrac : igraci) {
                int donja_crta = igrac.indexOf('_');
                String imeIgraca = igrac.substring(1, donja_crta);
                Double skor = Double.valueOf(igrac.substring(donja_crta + 1, igrac.indexOf(')')));

                lista.add(new Igrac(imeIgraca, skor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
}
