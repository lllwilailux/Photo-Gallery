package com.augmentis.ayp.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wilailux on 8/23/2016.
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";

    private static final int POLL_INTERVAL = 1000*60; //60 sec

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context c, boolean isOn){
        Intent i = PollService.newIntent(c);
        PendingIntent pi = PendingIntent.getService(c, 0, i, 0);

        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            //AlarmManager.RTC -> System.currentTimeMillis();
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,   //param1: Mode
                    SystemClock.elapsedRealtime(),                  //param2: Start
                    POLL_INTERVAL,                                  //param3: Interval
                    pi);                                            //param4: Pending action(intent)
        } else {
            am.cancel(pi); //cancel interval call
            pi.cancel(); // cancel pending intent call
        }
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Receive a call from intent: " + intent);
        if (!isNetworkAvailableAndConnected()) {
            return;
        }
        Log.i(TAG, "Active network!");


//        String query = PhotoGalleryPreference
//                .mySharedPref(this)
//                .getString(PhotoGalleryPreference.PREF_SEARCH_KEY,null);

        String query = PhotoGalleryPreference.getStoredSearchKey(this);
        String storedId = PhotoGalleryPreference.getStoredLastId(this);

        List<GalleryItem> galleryItemList = new ArrayList<>();

        FlickrFetcher flickrFetcher = new FlickrFetcher();
        if(query == null) {
            flickrFetcher.getRecentPhotos(galleryItemList);
        } else {
            flickrFetcher.searchPhotos(galleryItemList, query);
        }

        if (galleryItemList.size() == 0) {
            return;
        }

        Log.i(TAG, "Found search or te ited");

        String newestId = galleryItemList.get(0).getId(); // fetching first item

        if (newestId.equals(storedId)) {
            Log.i(TAG, "No new item");
        } else {
            Log.i(TAG, "New item found");

            Resources res = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

            //Build to build notification object
            NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this);
            notiBuilder.setTicker(res.getString(R.string.new_picture_arriving));
            notiBuilder.setSmallIcon(android.R.drawable.ic_menu_report_image);
            notiBuilder.setContentTitle(res.getString(R.string.new_picture_title));
            notiBuilder.setContentText(res.getString(R.string.new_picture_content));
            notiBuilder.setContentIntent(pi);
            notiBuilder.setAutoCancel(true);

            Notification notification = notiBuilder.build(); // Build notification from builder

            // Get notification manager
            NotificationManagerCompat nm = NotificationManagerCompat.from(this);
            nm.notify(0, notification);
        }
        PhotoGalleryPreference.setStoredLastId(this, newestId);
    }

    /**
     * check ว่า มี Network ให้ใช้มั้ย
     * ต้องสร้าง permission in AndroidManifest ด้วย
     * @return
     */
    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isActiveNetwork = cm.getActiveNetworkInfo() != null;
        boolean isActiveNetworkConnected = isActiveNetwork && cm.getActiveNetworkInfo().isConnected();
        return isActiveNetworkConnected;
    }
}
