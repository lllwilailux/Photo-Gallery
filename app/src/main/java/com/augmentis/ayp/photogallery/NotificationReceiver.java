package com.augmentis.ayp.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by Wilailux on 9/5/2016.
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    public NotificationReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Notification calling");

        //
        if(getResultCode() != Activity.RESULT_OK){
            return;
        }

        Notification notification = (Notification)
                intent.getParcelableExtra(PollService.NOTIFICATION);

        int requestCode = intent.getIntExtra(PollService.REQUESTCODE, 0);

        NotificationManagerCompat.from(context).notify(requestCode, notification);

        Log.i(TAG, "Notify new item displayed! ");
    }
}
