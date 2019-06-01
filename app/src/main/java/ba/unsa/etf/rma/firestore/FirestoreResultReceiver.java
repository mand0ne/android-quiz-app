package ba.unsa.etf.rma.firestore;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class FirestoreResultReceiver extends ResultReceiver {

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    private Receiver receiver;

    public FirestoreResultReceiver(Handler handler) {
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
