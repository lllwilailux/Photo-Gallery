package com.augmentis.ayp.photogallery;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.LruCache;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wilailux on 8/16/2016.
 */
public class PhotoGalleryFragment extends Fragment{

    private static final int REQUEST_PIC = 123;

    /**
     * create fragment and put create bundle to put data
     *
     * @return fragment
     */
    public static PhotoGalleryFragment newInstance() {

        Bundle args = new Bundle();
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mRecyclerView;
//    private FlickrFetcher mFlickrFetcher;
    private PhotoGalleryAdapter mAdapter;
    private List<GalleryItem> mItems;
    private ThumbnailDownloader<PhotoHolder>  mThumbnailDownloaderThread;
    private FetcherTask mFetcherTask;
    private String mSearchKey;
    private ImageView imgView;

    //cache
    private LruCache<String,Bitmap> mMemoryCache;
    // memory
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    //Use 1/8th of the available memory of this memory cache
    final int cacheSize = maxMemory /8;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setHasOptionsMenu(true);

        Log.d(TAG,"Memory size = " + maxMemory + " K ");

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount()/1024;
            }
        };

//        mFlickrFetcher = new FlickrFetcher();
        mFetcherTask = new FetcherTask();

        new FetcherTask().execute();// run another thread

        Handler responseUIHandler = new Handler();
<<<<<<< Updated upstream

        ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder> listener = new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail,String url) {

                if (null == mMemoryCache.get(url)) {
                    mMemoryCache.put(url,thumbnail);
                }

                Drawable drawable = new BitmapDrawable(getResources(),thumbnail);
                target.bindDrawable(drawable);
            }
        };

        mThumbnailDownloaderThread = new ThumbnailDownloader<>(responseUIHandler);
        mThumbnailDownloaderThread.setmThumbnailDownloaderListener(listener);
        mThumbnailDownloaderThread.start();
        mThumbnailDownloaderThread.getLooper();

        Log.i(TAG,"Start background thread");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mThumbnailDownloaderThread.quit();
        Log.i(TAG,"Stop background thread");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloaderThread.clearQueue();
    }

    @Override
    public void onPause() {
        super.onPause();

        PhotoGalleryPreference.setStoredSearchKey(getActivity(),mSearchKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        String searchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());

        if (searchKey != null) {
            mSearchKey = searchKey;
        }

        Log.d(TAG,"OnResume complete");
    }

    /**
     * Set Search,reload and clear search menu
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.mnu_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG,"Query text submitted: " + query);
                mSearchKey = query;
                loadPhotos();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG,"Query text changing: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery(mSearchKey,false);
            }
        });
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnu_reload:
                loadPhotos();
                return true;
            case R.id.mnu_clear_search:
                mSearchKey = null;
                loadPhotos();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadPhotos() {
        if (mFetcherTask == null || !mFetcherTask.isRunning()) {
            mFetcherTask = new FetcherTask();

=======

        ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder> listener = new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail,String url) {

                if (null == mMemoryCache.get(url)) {
                    mMemoryCache.put(url,thumbnail);
                }

                Drawable drawable = new BitmapDrawable(getResources(),thumbnail);
                target.bindDrawable(drawable);
            }
        };

        mThumbnailDownloaderThread = new ThumbnailDownloader<>(responseUIHandler);
        mThumbnailDownloaderThread.setmThumbnailDownloaderListener(listener);
        mThumbnailDownloaderThread.start();
        mThumbnailDownloaderThread.getLooper();

        Log.i(TAG,"Start background thread");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mThumbnailDownloaderThread.quit();
        Log.i(TAG,"Stop background thread");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloaderThread.clearQueue();
    }

    @Override
    public void onPause() {
        super.onPause();

        PhotoGalleryPreference.setStoredSearchKey(getActivity(),mSearchKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        String searchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());

        if (searchKey != null) {
            mSearchKey = searchKey;
        }

        Log.d(TAG,"OnResume complete");
    }

    /**
     * Set Search,reload and clear search menu
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.mnu_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG,"Query text submitted: " + query);
                mSearchKey = query;
                loadPhotos();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG,"Query text changing: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery(mSearchKey,false);
            }
        });
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnu_reload:
                loadPhotos();
                return true;
            case R.id.mnu_clear_search:
                mSearchKey = null;
                loadPhotos();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadPhotos() {
        if (mFetcherTask == null || !mFetcherTask.isRunning()) {
            mFetcherTask = new FetcherTask();

>>>>>>> Stashed changes
            if (mSearchKey != null) {
                mFetcherTask.execute(mSearchKey);
            } else {
                mFetcherTask.execute();
            }
        }
    }
    ///////////////////////////////////////////////////

    /**
     * map fragment layout and id
     * set layout to fragment
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery,container,false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.photo_gallery_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        mItems = new ArrayList<>();
        mRecyclerView.setAdapter(new PhotoGalleryAdapter(mItems));

        mSearchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());
        loadPhotos();

        Log.d(TAG,"OnCreate complete - loaded search key = null");




        return v;

    }

    /**
     * set image view
     */
    class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

//        TextView mText;
        ImageView mPhoto;
        String mBigUrl;

        public PhotoHolder(View itemView) {
            super(itemView);
            mPhoto = (ImageView) itemView.findViewById(R.id.image_photo);
        }

        public void bindDrawable(@NonNull Drawable drawable) {
            mPhoto.setImageDrawable(drawable);
        }

        public void setBigUrl(String bigUrl) {
            mBigUrl = bigUrl;
        }

        @Override
        public void onClick(View view) {


            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final ImageView imgView = new ImageView(getActivity());
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            builder.setView(imgView);
            builder.setPositiveButton("Close",null);


            new AsyncTask<String,Void,Bitmap>() {

                @Override
                protected Bitmap doInBackground(String... urls) {
                    FlickrFetcher flickrFetcher = new FlickrFetcher();
                    Bitmap bm = null;
                    try{
                        byte[] bytes = flickrFetcher.getUrlBytes(urls[0]);
                        bm = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    } catch (IOException ioe) {
                        Log.d(TAG,"error in reading Bitmap" + ioe);
                        return null;
                    }
                    return bm;
                }

                @Override
                protected void onPostExecute(Bitmap img) {
                    builder.create().show();
                    imgView.setImageDrawable(new BitmapDrawable(getResources(),img));
                }

<<<<<<< Updated upstream
            };

=======
            }.execute(mBigUrl);
>>>>>>> Stashed changes
        }


//        public void bindGalleryItem(GalleryItem galleryItem) {
//            mText.setText(galleryItem.getTitle());
//        }
    }

}
