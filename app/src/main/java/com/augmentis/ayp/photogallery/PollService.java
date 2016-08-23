package com.augmentis.ayp.photogallery;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Wilailux on 8/22/2016.
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";

    public static Intent newIntent(Context context) {

        return new Intent(context,PollService.class);
    }


    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
