package com.augmentis.ayp.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by Wilailux on 9/5/2016.
 */
public class VisibleFragment extends Fragment {

    private static final String TAG = "VisibleFragment";

    public VisibleFragment() {
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "In application receiver, Cancel notification! ");

            setResultCode(Activity.RESULT_CANCELED);

        }
    };

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, intentFilter,
                PollService.PERMISSION_SHOW_NOTIF, null);

        //receiver permission "ayp.aug.photogallery.RECEIVE_SHOW_NOTIFICATION"
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(mOnShowNotification);
    }

}
