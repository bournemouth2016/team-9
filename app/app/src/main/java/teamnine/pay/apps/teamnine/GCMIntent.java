package teamnine.pay.apps.teamnine;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by kenneth on 11/18/16.
 */

public class GCMIntent extends IntentService {

    PendingIntent resultPendingIntent;

    String indanger;
    String gps;

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

                if(extras.get("phone")!=null){
                    indanger = extras.get("phone").toString();
                    gps = extras.get("gps").toString();
                    sendDangerNotif(indanger, gps);
                }
                else{
                    sendNotif(extras.toString());
                }
            }

            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
        catch(Exception r){
            r.printStackTrace();
        }

    }

    public void sendNotif(String message){
        NotificationManager nm = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        long[] vibrate_ptn = {0, 100, 300, 500};

        Long timestamp = System.currentTimeMillis()/1000;

        System.out.println("Timestamp is: "+timestamp);

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getBaseContext())
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("Alert!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(vibrate_ptn);
                //.setOngoing(true);
                //.setAutoCancel(true)
        //mBuilder.setContentIntent(resultPendingIntent);
        nm.notify(Integer.parseInt(timestamp.toString()), mBuilder.build());
    }

    public void sendDangerNotif(String phonenum, String gps){
        NotificationManager nm = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        long[] vibrate_ptn = {0, 100, 300, 500};

        Long timestamp = System.currentTimeMillis()/1000;

        System.out.println("Timestamp is: "+timestamp);

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getBaseContext())
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("Alert!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Someone is in danger.\nPhone number: "+phonenum+
                "\nCoordinates: "+gps))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(vibrate_ptn);
                //.setOngoing(true);
        //.setAutoCancel(true)
        //mBuilder.setContentIntent(resultPendingIntent);
        nm.notify(Integer.parseInt(timestamp.toString()), mBuilder.build());
    }
}
