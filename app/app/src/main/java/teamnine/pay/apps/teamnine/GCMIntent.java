package teamnine.pay.apps.teamnine;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by kenneth on 11/18/16.
 */

public class GCMIntent extends IntentService {

    PendingIntent resultPendingIntent;

    private static final String TAG = "GcmIntentService";
    public GCMIntent() {
        super("GCMIntent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        try {
            if (!extras.isEmpty()) {
                System.out.println(extras.toString());
            }

            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
        catch(Exception r){
            r.printStackTrace();
        }

    }
}
