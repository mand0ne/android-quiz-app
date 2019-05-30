package ba.unsa.etf.rma.klase;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class FirebaseResultReceiver extends ResultReceiver {

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    private Receiver receiver;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public FirebaseResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if(receiver != null)
            receiver.onReceiveResult(resultCode, resultData);
    }
}
