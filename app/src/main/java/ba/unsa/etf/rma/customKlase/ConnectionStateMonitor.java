package ba.unsa.etf.rma.customKlase;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ConnectionStateMonitor extends ConnectivityManager.NetworkCallback {

    public static final int CONNECTION_LOST = 2;

    public interface NetworkAwareActivity {
        void onNetworkLost();

        void onNetworkAvailable();

        boolean isFinishing();
    }

    private final NetworkRequest networkRequest;
    private final NetworkAwareActivity networkAwareActivity;
    private final ConnectivityManager connectivityManager;

    public ConnectionStateMonitor(NetworkAwareActivity networkAwareActivity, ConnectivityManager connectivityManager) {
        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();

        this.networkAwareActivity = networkAwareActivity;
        this.connectivityManager = connectivityManager;
    }

    public void registerNetworkCallback() {
        connectivityManager.registerNetworkCallback(networkRequest, this);
    }

    public void unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(this);
    }

    @Override
    public void onAvailable(Network network) {
        new CheckIfConnected(networkAwareActivity).execute();
    }

    @Override
    public void onLost(Network network) {
        networkAwareActivity.onNetworkLost();
    }

    private static class CheckIfConnected extends AsyncTask<String, Void, Boolean> {
        private final WeakReference<NetworkAwareActivity> activityWeakReference;

        CheckIfConnected(NetworkAwareActivity networkAwareActivity) {
            this.activityWeakReference = new WeakReference<>(networkAwareActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (activityWeakReference.get() == null || activityWeakReference.get().isFinishing())
                this.cancel(true);
        }

        protected Boolean doInBackground(String... params) {
            try {
                int timeoutMs = 1500;
                Socket sock = new Socket();
                SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);
                sock.connect(sockaddr, timeoutMs);
                sock.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean connected) {
            if (connected)
                activityWeakReference.get().onNetworkAvailable();
        }
    }
}
