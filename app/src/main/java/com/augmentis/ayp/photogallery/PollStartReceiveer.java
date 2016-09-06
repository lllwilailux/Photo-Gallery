package com.augmentis.ayp.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Wilailux on 9/5/2016.
 */
public class PollStartReceiveer extends BroadcastReceiver {

    private static final String TAG = "PollStarterReceiver";

    public PollStartReceiveer() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Receive some message");

        Boolean isOn = PhotoGalleryPreference.getStoredIsAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);

        Log.d(TAG, "Status of service alarm is : " + isOn);
    }
}
