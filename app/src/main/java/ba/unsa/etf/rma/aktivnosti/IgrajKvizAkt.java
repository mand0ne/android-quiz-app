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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.firestore.FirestoreIntentService;
import ba.unsa.etf.rma.firestore.FirestoreResultReceiver;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.fragmenti.RangLista;
import ba.unsa.etf.rma.modeli.IgraPair;
import ba.unsa.etf.rma.modeli.Kviz;
import ba.unsa.etf.rma.modeli.RangListaKviz;

import static ba.unsa.etf.rma.firestore.FirestoreIntentService.AZURIRAJ_RANG_LISTU;
import static ba.unsa.etf.rma.firestore.FirestoreIntentService.DOHVATI_RANG_LISTU;

public class IgrajKvizAkt extends AppCompatActivity implements FirestoreResultReceiver.Receiver {

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

        InformacijeFrag iFrag = new InformacijeFrag();
        PitanjeFrag pFrag = new PitanjeFrag();

        Bundle b = new Bundle();
        b.putParcelable("kviz", igraniKviz);

        pFrag.setArguments(b);
        iFrag.setArguments(b);


        getSupportFragmentManager().beginTransaction().replace(R.id.informacijePlace, iFrag).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, pFrag).commit();
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

    private Intent kreirajIntent(int request) {
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, FirestoreIntentService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra("token", TOKEN);
        intent.putExtra("request", request);

        return intent;
    }

    public void dohvatiRangListuZaPrikaz(String nickname, Double skor) {
        final Intent intent = kreirajIntent(DOHVATI_RANG_LISTU);
        intent.putExtra("nazivKviza", igraniKviz.getNaziv());
        intent.putExtra("kvizFirebaseId", igraniKviz.firestoreId());
        intent.putExtra("nickname", nickname);
        intent.putExtra("skor", skor);
        startService(intent);
    }

    private void azurirajRangListuFirestore(RangListaKviz rangListaKviz) {
        final Intent intent = kreirajIntent(AZURIRAJ_RANG_LISTU);
        intent.putExtra("rangListaKviz", rangListaKviz);
        startService(intent);
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
                .setPositiveButton("Nazad", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                }).create();

        alert.show();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == DOHVATI_RANG_LISTU) {
            RangListaKviz rangListaKviz = resultData.getParcelable("rangListaKviz");
            assert rangListaKviz != null;

            rangListaKviz.getLista().add(new IgraPair(resultData.getString("nickname"), resultData.getDouble("skor")));
            Collections.sort(rangListaKviz.getLista(), new Comparator<IgraPair>() {
                @Override
                public int compare(IgraPair o1, IgraPair o2) {
                    if (o2.second().equals(o1.second()))
                        return o2.first().compareTo(o2.first());

                    return o2.second().compareTo(o1.second());
                }
            });

            azurirajRangListuFirestore(rangListaKviz);

            Bundle bundle = new Bundle();
            bundle.putSerializable("rangPair", rangListaKviz.getLista());
            RangLista rangLista = new RangLista();
            rangLista.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, rangLista).commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(alarmReceiver);
        if (ringtone != null)
            ringtone.stop();
    }
}


