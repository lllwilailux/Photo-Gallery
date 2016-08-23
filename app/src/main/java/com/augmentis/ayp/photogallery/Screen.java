package com.augmentis.ayp.photogallery;

import android.content.Context;
import android.os.PowerManager;

/**
 * Created by Wilailux on 8/23/2016.
 */
public class Screen {
    private final String SCREEN_CLASS_TAG = "Screen";
    public Screen() {}

    public void on(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        if (!powerManager.isScreenOn()) {
            PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP | powerManager.ON_AFTER_RELEASE,
                    SCREEN_CLASS_TAG);
            wl.acquire();
        }
    }
}
