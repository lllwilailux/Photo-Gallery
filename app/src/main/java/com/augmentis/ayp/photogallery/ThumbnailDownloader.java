<<<<<<< Updated upstream
package com.augmentis.ayp.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Wilailux on 8/18/2016.
=======

        package ayp.aug.photogallery;

        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.os.Handler;
        import android.os.HandlerThread;
        import android.os.Message;
        import android.util.Log;

        import com.augmentis.ayp.photogallery.FlickrFetcher;

        import java.io.IOException;
        import java.util.concurrent.ConcurrentHashMap;
        import java.util.concurrent.ConcurrentMap;

/**
 * Created by Waraporn on 8/18/2016.
>>>>>>> Stashed changes
 */
public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
<<<<<<< Updated upstream

    private static final int DOWNLOAD_FILE = 2018;

    private Handler mRequestHandler;
    private final ConcurrentMap<T,String> mRequestUrlMap = new ConcurrentHashMap<>();
=======
    private static final int DOWNLOAD_FILE = 2018;

    private Handler mRequestHandler;
    private final ConcurrentMap<T, String> mRequestUrlMap = new ConcurrentHashMap<>();
>>>>>>> Stashed changes

    private Handler mResponseHandler;
    private ThumbnailDownloaderListener<T> mThumbnailDownloaderListener;

    interface ThumbnailDownloaderListener<T> {
<<<<<<< Updated upstream
        void onThumbnailDownloaded(T target, Bitmap thumbnail,String url);
=======
        void onThumbnailDownloaded(T target, Bitmap thumbnail, String url);
>>>>>>> Stashed changes
    }

    public void setmThumbnailDownloaderListener(ThumbnailDownloaderListener<T> mThumbnailDownloaderListener) {
        this.mThumbnailDownloaderListener = mThumbnailDownloaderListener;
    }

    public ThumbnailDownloader(Handler mUIHandler) {
        super(TAG);

        mResponseHandler = mUIHandler;
<<<<<<< Updated upstream

    }

    /**
     *
     * new Handler() คือเอา Looper ที่กำลังรันอยู่ แต่ถ้า new Handler(Loop1) คือให้เอา Loop1 มาทำงน
     *
     * onLooperPrepared ถูกเรียกเมื่อ Looper เริ่มทำงาน
     */
=======
    }

>>>>>>> Stashed changes
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
<<<<<<< Updated upstream
                // work in the queue
=======
                //work in the queue
>>>>>>> Stashed changes
                if (msg.what == DOWNLOAD_FILE) {
                    T target = (T) msg.obj;

                    String url = mRequestUrlMap.get(target);
<<<<<<< Updated upstream
                    Log.i(TAG,"Got message from queue: pls download this URL: " + url);

                    handleRequestDownload(target,url);
=======
                    Log.i(TAG, "Got message from queue: pls download this url: " + url);

                    handleRequestdownload(target, url);
>>>>>>> Stashed changes
                }
            }
        };
    }

<<<<<<< Updated upstream
    public void clearQueue() {
        mRequestHandler.removeMessages(DOWNLOAD_FILE);
    }

    private void handleRequestDownload(final T target,final String url) {
=======
    public void clearQueue(){
        mRequestHandler.removeMessages(DOWNLOAD_FILE);
    }

    private void handleRequestdownload(final T target, final String url){
>>>>>>> Stashed changes
        try {
            if (url == null) {
                return;
            }

            byte[] bitMapBytes = new FlickrFetcher().getUrlBytes(url);
<<<<<<< Updated upstream
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitMapBytes,0,bitMapBytes.length);
=======
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitMapBytes, 0, bitMapBytes.length);
>>>>>>> Stashed changes

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    String currentUrl = mRequestUrlMap.get(target);
<<<<<<< Updated upstream
                    if (currentUrl != null && !currentUrl.equals(url)){
                        return;
                    }

                    // url is ok(the same one)
                    mRequestUrlMap.remove(target);
                    mThumbnailDownloaderListener.onThumbnailDownloaded(target,bitmap,url);
                }
            });

            Log.i(TAG,"Bitmap URL downloaded: ");

        } catch (IOException ioe) {
            Log.e(TAG,"Error downloading: ", ioe);
        }
    }

    public void queueThumbnailDownloader(T target, String url) {
        Log.i(TAG,"Got url: " + url);
=======

                    if (currentUrl != null && !currentUrl.equals(url)) {
                        return;
                    }

                    //url is ok (the same one)
                    mRequestUrlMap.remove(target);
                    mThumbnailDownloaderListener.onThumbnailDownloaded(target, bitmap, url);
                }
            });

            Log.i(TAG, "Bitmap url downloaded: ");
        } catch (IOException e) {
            Log.e(TAG, "Error downloading...");
        }
    }
    public void queueThumbnailDownload(T target, String url) {
        Log.i(TAG, "Got url: " + url);
>>>>>>> Stashed changes

        if (null == url) {
            mRequestUrlMap.remove(target);
        } else {
<<<<<<< Updated upstream
            mRequestUrlMap.put(target,url);

            Message msg =  mRequestHandler.obtainMessage(DOWNLOAD_FILE,target);// get message from handler
            msg.sendToTarget();//send to handler to queue
        }


    }
}
=======
            mRequestUrlMap.put(target, url);
        }

        Message message = mRequestHandler.obtainMessage(DOWNLOAD_FILE, target); //get msg. from handler
        message.sendToTarget(); //sent to handler
    }
}
>>>>>>> Stashed changes
