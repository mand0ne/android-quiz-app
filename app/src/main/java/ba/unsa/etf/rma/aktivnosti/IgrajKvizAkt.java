package ba.unsa.etf.rma.aktivnosti;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Collections;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.customKlase.ConnectionStateMonitor;
import ba.unsa.etf.rma.firestore.AccessToken;
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.firestore.FirestoreResultReceiver;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.fragmenti.RangLista;
import ba.unsa.etf.rma.modeli.Igrac;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.RangListaKviz;
import ba.unsa.etf.rma.sqlite.SQLiteIntentService;

import static ba.unsa.etf.rma.firestore.FirestoreIntentService.AZURIRAJ_RANG_LISTU;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.DOHVATI_RANG_LISTU;
import static ba.unsa.etf.rma.sqlite.SQLiteIntentService.AZURIRAJ_LOKALNE_RANGLISTE;
import static ba.unsa.etf.rma.sqlite.SQLiteIntentService.SINKRONIZUJ_RANG_LISTE;

public class IgrajKvizAkt extends AppCompatActivity implements FirestoreResultReceiver.Receiver, ConnectionStateMonitor.NetworkAwareActivity {

    private class NotificationHelper extends ContextWrapper {
        public static final String channelID = "channelID";
        public static final String channelName = "Channel Name";

        private NotificationManager mManager;

        public NotificationHelper(Context base) {
            super(base);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel();
            }
        }

        @TargetApi(Build.VERSION_CODES.O)
        private void createChannel() {
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);

            getManager().createNotificationChannel(channel);
        }

        public NotificationManager getManager() {
            if (mManager == null) {
                mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }

            return mManager;
        }

        public NotificationCompat.Builder getChannelNotification() {
            return new NotificationCompat.Builder(context, channelID)
                    .setContentTitle("Vrijeme isteklo!")
                    .setContentText("Kviz je završen.")
                    .setSmallIcon(R.drawable.applogo);
        }
    }

    private class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationHelper notificationHelper = new NotificationHelper(context);
            NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
            notificationHelper.getManager().notify(1, nb.build());

            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                if (alert == null)
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }

            ringtone = RingtoneManager.getRingtone(context, alert);
            if (ringtone != null)
                ringtone.play();

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                v.vibrate(VibrationEffect.createOneShot(750, VibrationEffect.DEFAULT_AMPLITUDE));
            else
                v.vibrate(750);     // API 26+ deprecation
        }
    }

    private Context context;
    private AlarmReceiver alarmReceiver;
    private Kviz igraniKviz;
    private Ringtone ringtone;

    private FirestoreResultReceiver receiver;
    // Firestore access token
    private String TOKEN;

    private ConnectionStateMonitor connectionStateMonitor;
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz_akt);

        context = this;
        receiver = new FirestoreResultReceiver(new Handler());
        receiver.setReceiver(this);

        alarmReceiver = new AlarmReceiver();
        this.registerReceiver(alarmReceiver,
                new IntentFilter("ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt$AlarmReceiver"));

        final Intent intent = getIntent();
        igraniKviz = intent.getParcelableExtra("kviz");
        TOKEN = intent.getStringExtra("token");
        connected = intent.getBooleanExtra("connected", false);

        connectionStateMonitor = new ConnectionStateMonitor(this, (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE));
        connectionStateMonitor.registerNetworkCallback();

        if (!connected)
            headsUpPlayer();

        InformacijeFrag iFrag = new InformacijeFrag();
        PitanjeFrag pFrag = new PitanjeFrag();

        Bundle b = new Bundle();
        b.putParcelable("kviz", igraniKviz);

        pFrag.setArguments(b);
        iFrag.setArguments(b);

        getSupportFragmentManager().beginTransaction().replace(R.id.informacijePlace, iFrag).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, pFrag).commit();
    }

    private void headsUpPlayer() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Stanje rang liste")
                .setMessage("Ukoliko niste povezani na internet, prikazati će se trenutno, lokalno stanje rang liste koje se može razlikovati od aktuelnog!")
                .setIcon(android.R.drawable.ic_dialog_info)
                .create()
                .show();
    }

    public void postaviAlarm(long miliSekunde) {
        Calendar time = Calendar.getInstance();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt$AlarmReceiver");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        if (miliSekunde != 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis() + miliSekunde + 200, pendingIntent);
    }

    public void iskljuciAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt$AlarmReceiver");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    public void istekloVrijeme(int brojPreostalihPitanja) {
        StringBuilder message = new StringBuilder("Preostalo je " + brojPreostalihPitanja);
        if (brojPreostalihPitanja % 10 == 1)
            message.append(" pitanje.\n");
        else
            message.append(" pitanja.\n");

        message.append("Pokušajte ponovo...");
        AlertDialog alert = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("Vrijeme isteklo!")
                .setMessage(message.toString())
                .setPositiveButton("Nazad", (dialog, which) -> {
                    dialog.cancel();
                    finish();
                }).create();

        alert.show();
    }

    private Intent kreirajIntent(int request, boolean sqlite) {
        final Intent intent;
        if (sqlite)
            intent = new Intent(Intent.ACTION_SYNC, null, context, SQLiteIntentService.class);
        else
            intent = new Intent(Intent.ACTION_SYNC, null, context, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", request);

        return intent;
    }

    public void dohvatiRangListuZaPrikaz(String nickname, Double skor) {
        if (connected) {
            final Intent intent = kreirajIntent(DOHVATI_RANG_LISTU, false);
            intent.putExtra("nazivKviza", igraniKviz.getNaziv());
            intent.putExtra("kvizFirestoreId", igraniKviz.firestoreId());
            intent.putExtra("nickname", nickname);
            intent.putExtra("skor", skor);
            startService(intent);
        } else {
            final Intent intent = kreirajIntent(DOHVATI_RANG_LISTU, true);
            intent.putExtra("nazivKviza", igraniKviz.getNaziv());
            intent.putExtra("kvizFirestoreId", igraniKviz.firestoreId());
            intent.putExtra("nickname", nickname);
            intent.putExtra("skor", skor);
            startService(intent);
        }
    }

    private void azurirajRangListuFirestore(RangListaKviz rangListaKviz) {
        if (connected) {
            final Intent intent = kreirajIntent(AZURIRAJ_RANG_LISTU, false);
            intent.putExtra("rangListaKviz", rangListaKviz);
            startService(intent);
        } else {
            final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, SQLiteIntentService.class);
            intent.putExtra("rangListaKviz", rangListaKviz);
            intent.putExtra("request", AZURIRAJ_LOKALNE_RANGLISTE);
            startService(intent);
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == DOHVATI_RANG_LISTU) {
            RangListaKviz rangListaKviz = resultData.getParcelable("rangListaKviz");

            if (rangListaKviz == null)
                rangListaKviz = new RangListaKviz(igraniKviz.getNaziv(), igraniKviz.firestoreId());

            Igrac igrac = new Igrac(resultData.getString("nickname"), resultData.getDouble("skor"));

            if (!rangListaKviz.getLista().contains(igrac))
                rangListaKviz.getLista().add(igrac);

            rangListaKviz.getLista().sort((o1, o2) -> {
                if (o2.score().equals(o1.score()))
                    return o2.nickname().compareTo(o2.nickname());

                return o2.score().compareTo(o1.score());
            });

            azurirajRangListuFirestore(rangListaKviz);

            Bundle bundle = new Bundle();
            bundle.putSerializable("rangPair", rangListaKviz.getLista());
            RangLista rangLista = new RangLista();
            rangLista.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, rangLista).commit();
        } else if (resultCode == SINKRONIZUJ_RANG_LISTE) {
            final Intent intent = kreirajIntent(SINKRONIZUJ_RANG_LISTE, false);
            intent.putExtra("rangListaKviz", (RangListaKviz) resultData.getParcelable("rangListaKviz"));
            startService(intent);
        }
    }

    // Dugme "Zavrsi kviz"
    public void zavrsiKviz(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(alarmReceiver);
        if (ringtone != null)
            ringtone.stop();

        connectionStateMonitor.unregisterNetworkCallback();
    }

    @Override
    public void onNetworkLost() {
        Log.wtf("IgrajKvizAkt: ", "onNetworkLost");
        Toast.makeText(context, "Connection lost!", Toast.LENGTH_SHORT).show();
        connected = false;
    }

    private void sinkronizujStanjaRangListi() {
        final Intent intent = kreirajIntent(SINKRONIZUJ_RANG_LISTE, true);
        intent.putExtra("igraniKviz", igraniKviz);
        startService(intent);
    }

    @Override
    public void onNetworkAvailable() {
        Log.wtf("IgrajKvizAkt: ", "onNetworkAvailable");
        Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show();

        if (TOKEN == null || TOKEN.isEmpty()) {
            try {
                TOKEN = new AccessToken(this).execute().get();      // Moramo pricekati, sta ces..
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        connected = true;
        sinkronizujStanjaRangListi();
    }
}


