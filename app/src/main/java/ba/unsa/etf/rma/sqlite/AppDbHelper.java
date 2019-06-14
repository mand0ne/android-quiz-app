package ba.unsa.etf.rma.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ba.unsa.etf.rma.modeli.Kategorija;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.Pitanje;

public class AppDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Kvizovi.db";
    private static AppDbHelper instance;

    public static class KategorijeEntry implements BaseColumns {
        static final String TABLE_NAME = "Kategorije";
        static final String COLUMN_NAME_NAZIV = "naziv";
        static final String COLUMN_NAME_ID_IKONICE = "ikonica_id";
        static final String COLUMN_NAME_FIRESTORE_ID = "firestore_id";
    }

    public static class KvizoviEntry implements BaseColumns {
        static final String TABLE_NAME = "Kvizovi";
        static final String COLUMN_NAME_NAZIV = "naziv";
        static final String COLUMN_NAME_ID_KATEGORIJE = "kategorija_id";
        static final String COLUMN_NAME_PITANJA = "ids_pitanja";
        static final String COLUMN_NAME_FIRESTORE_ID = "firestore_id";
    }

    public static class PitanjaEntry implements BaseColumns {
        static final String TABLE_NAME = "Pitanja";
        static final String COLUMN_NAME_NAZIV = "naziv";
        static final String COLUMN_NAME_TACAN_ODGOVOR = "tacan_odgovor";
        static final String COLUMN_NAME_ODGOVORI = "odgovori";
        static final String COLUMN_NAME_FIRESTORE_ID = "firestore_id";
    }

    public static class RanglisteEntry implements BaseColumns {
        static final String TABLE_NAME = "Rangliste";
        static final String COLUMN_NAME_NAZIV_KVIZA = "naziv_kviza";
        static final String COLUMN_NAME_LISTA = "lista";
        static final String COLUMN_NAME_FIRESTORE_ID = "firestore_id";
    }

    private static final String CREATE_TABLE_KATEGORIJE =
            "CREATE TABLE IF NOT EXISTS " + KategorijeEntry.TABLE_NAME + "(" +
                    KategorijeEntry._ID + " INTEGER PRIMARY KEY," +
                    KategorijeEntry.COLUMN_NAME_NAZIV + " TEXT," +
                    KategorijeEntry.COLUMN_NAME_ID_IKONICE + " INTEGER," +
                    KategorijeEntry.COLUMN_NAME_FIRESTORE_ID + " TEXT UNIQUE)";

    private static final String CREATE_TABLE_KVIZOVI =
            "CREATE TABLE IF NOT EXISTS " + KvizoviEntry.TABLE_NAME + "(" +
                    KvizoviEntry._ID + " INTEGER PRIMARY KEY," +
                    KvizoviEntry.COLUMN_NAME_NAZIV + " TEXT," +
                    KvizoviEntry.COLUMN_NAME_ID_KATEGORIJE + " TEXT," +
                    KvizoviEntry.COLUMN_NAME_PITANJA + " TEXT," +
                    KvizoviEntry.COLUMN_NAME_FIRESTORE_ID + " TEXT UNIQUE)";

    private static final String CREATE_TABLE_PITANJA =
            "CREATE TABLE IF NOT EXISTS " + PitanjaEntry.TABLE_NAME + "(" +
                    PitanjaEntry._ID + " INTEGER PRIMARY KEY," +
                    PitanjaEntry.COLUMN_NAME_NAZIV + " TEXT," +
                    PitanjaEntry.COLUMN_NAME_TACAN_ODGOVOR + " TEXT," +
                    PitanjaEntry.COLUMN_NAME_ODGOVORI + " TEXT," +
                    PitanjaEntry.COLUMN_NAME_FIRESTORE_ID + " TEXT UNIQUE)";

    private static final String CREATE_TABLE_RANGLISTE =
            "CREATE TABLE IF NOT EXISTS " + RanglisteEntry.TABLE_NAME + "(" +
                    RanglisteEntry._ID + " INTEGER PRIMARY KEY," +
                    RanglisteEntry.COLUMN_NAME_NAZIV_KVIZA + " TEXT," +
                    RanglisteEntry.COLUMN_NAME_LISTA + " TEXT," +
                    RanglisteEntry.COLUMN_NAME_FIRESTORE_ID + " TEXT UNIQUE)";

    private AppDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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

    /////////////////////////////////////////////////////////////////
    ///// ------------ Metode tabele "Kategorije" ------------ /////
    ////////////////////////////////////////////////////////////////

    public long azurirajKategoriju(Kategorija kategorija) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KategorijeEntry.COLUMN_NAME_NAZIV, kategorija.getNaziv());
        values.put(KategorijeEntry.COLUMN_NAME_ID_IKONICE, kategorija.getIdIkonice());
        values.put(KategorijeEntry.COLUMN_NAME_FIRESTORE_ID, kategorija.firestoreId());

        return db.replace(KategorijeEntry.TABLE_NAME, null, values);
    }

    private Kategorija dajKategoriju(String firestoreId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + KategorijeEntry.TABLE_NAME + " WHERE "
                + KategorijeEntry.COLUMN_NAME_FIRESTORE_ID + " = '" + firestoreId + "'";

        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
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

    public List<Kategorija> dajSveKategorije() {
        List<Kategorija> kategorije = new ArrayList<Kategorija>();
        String selectQuery = "SELECT  * FROM " + KategorijeEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
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

    public long azurirajKviz(Kviz kviz) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KvizoviEntry.COLUMN_NAME_NAZIV, kviz.getNaziv());
        values.put(KvizoviEntry.COLUMN_NAME_ID_KATEGORIJE, kviz.getKategorija().getIdIkonice());
        values.put(KvizoviEntry.COLUMN_NAME_FIRESTORE_ID, kviz.firestoreId());

        StringBuilder id_pitanja = new StringBuilder();

        for (Pitanje pitanje : kviz.getPitanja())
            id_pitanja.append(pitanje.firestoreId()).append(',');

        values.put(KvizoviEntry.COLUMN_NAME_PITANJA, id_pitanja.toString());

        return db.replace(KvizoviEntry.TABLE_NAME, null, values);
    }

    public Kviz dajKviz(String firestoreId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + KvizoviEntry.TABLE_NAME + " WHERE "
                + KvizoviEntry.COLUMN_NAME_FIRESTORE_ID + " = " + firestoreId;

        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            Kviz kviz = null;
            if (cursor.moveToFirst()) {
                kviz = new Kviz(
                        cursor.getString(cursor.getColumnIndex(KvizoviEntry.COLUMN_NAME_NAZIV)),
                        dajKategoriju(cursor.getString(cursor.getColumnIndex(KvizoviEntry.COLUMN_NAME_ID_KATEGORIJE)))
                );

                ArrayList<Pitanje> pitanjaKviza = new ArrayList<>();

                String[] pitanjaFirestoreId = cursor.getString(cursor.getColumnIndex(KvizoviEntry.COLUMN_NAME_PITANJA)).split(",");

                for (String id : pitanjaFirestoreId)
                    pitanjaKviza.add(dajPitanje(id));

                kviz.setPitanja(pitanjaKviza);
            }

            cursor.close();
            return kviz;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Kviz> dajSveKvizove() {
        List<Kviz> kvizovi = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + KategorijeEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Kviz kviz = new Kviz(
                            cursor.getString(cursor.getColumnIndex(KvizoviEntry.COLUMN_NAME_NAZIV)),
                            dajKategoriju(cursor.getString(cursor.getColumnIndex(KvizoviEntry.COLUMN_NAME_ID_KATEGORIJE)))
                    );

                    ArrayList<Pitanje> pitanjaKviza = new ArrayList<>();

                    String[] pitanjaFirestoreId = cursor.getString(cursor.getColumnIndex(KvizoviEntry.COLUMN_NAME_PITANJA)).split(",");

                    for (String id : pitanjaFirestoreId)
                        pitanjaKviza.add(dajPitanje(id));

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

    public long azurirajPitanje(Pitanje pitanje) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PitanjaEntry.COLUMN_NAME_NAZIV, pitanje.getNaziv());
        values.put(PitanjaEntry.COLUMN_NAME_FIRESTORE_ID, pitanje.firestoreId());
        values.put(PitanjaEntry.COLUMN_NAME_TACAN_ODGOVOR, pitanje.getTacan());

        StringBuilder odgovori = new StringBuilder();

        for (String odgovor : pitanje.getOdgovori())
            odgovori.append(odgovor).append(',');

        values.put(PitanjaEntry.COLUMN_NAME_ODGOVORI, odgovori.toString());

        return db.replace(PitanjaEntry.TABLE_NAME, null, values);
    }

    private Pitanje dajPitanje(String firestoreId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + PitanjaEntry.TABLE_NAME + " WHERE "
                + PitanjaEntry.COLUMN_NAME_FIRESTORE_ID + " = " + firestoreId;

        try {
            Cursor cursor = db.rawQuery(selectQuery, null);

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

    public List<Pitanje> dajSvaPitanja() {
        List<Pitanje> pitanja = new ArrayList<Pitanje>();
        String selectQuery = "SELECT  * FROM " + PitanjaEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Pitanje pitanje = new Pitanje(
                            cursor.getString(cursor.getColumnIndex(PitanjaEntry.COLUMN_NAME_NAZIV)),
                            cursor.getString(cursor.getColumnIndex(PitanjaEntry.COLUMN_NAME_TACAN_ODGOVOR)),
                            cursor.getString(cursor.getColumnIndex(PitanjaEntry.COLUMN_NAME_FIRESTORE_ID)));

                    String[] odgovori = cursor.getString(cursor.getColumnIndex(PitanjaEntry.COLUMN_NAME_ODGOVORI))
                            .split(",");

                    for (String odgovor : odgovori)
                        if (!odgovor.equals(pitanje.getTacan()))
                            pitanje.dodajOdgovor(odgovor);

                    pitanja.add(pitanje);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception ignored) {
        }

        return pitanja;
    }

    ////////////////////////////////////////////////////////////////
    ///// ------------ Metode tabele "Rangliste" ------------ /////
    ///////////////////////////////////////////////////////////////


}
