package ba.unsa.etf.rma.firestore;

import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import ba.unsa.etf.rma.R;

public class AccessToken extends AsyncTask<String, Void, String> {
    private final WeakReference<AppCompatActivity> activityWeakReference;
    private final String TAG = getClass().getSimpleName();

    public AccessToken(AppCompatActivity activityWeakReference) {
        this.activityWeakReference = new WeakReference<>(activityWeakReference);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        AppCompatActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing())
            this.cancel(true);
    }

    protected String doInBackground(String... params) {
        try {
            InputStream is = activityWeakReference.get().getResources().openRawResource(R.raw.secret);
            GoogleCredential credentials = GoogleCredential.fromStream(is)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
            credentials.refreshToken();
            return credentials.getAccessToken();
        } catch (Exception e) {
            Log.d(TAG, "doInBackground: " + e.getMessage());
            return null;
        }
    }
}