package com.augmentis.ayp.photogallery;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.LruCache;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wilailux on 8/16/2016.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";
    private static final int REQUEST_SHOW_PHOTO_DETAIL = 123;
    private static final String DIALOG_SHOW_PHOTO_DETAIL = "SHOW_PHOTO_DETAIL";
    private static final int REQUEST_PERMISSION_LOCATION = 2928;

    /**
     * Method for make sure isPhotoGalleryFragment.
     *
     * @return
     */
    public static PhotoGalleryFragment newInstance() {
        Bundle args = new Bundle();
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;
    private FlickrFetcher mFlickrFetcher;
    private PhotoGalleryAdapter mAdapter;
    private List<GalleryItem> mItems;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloaderThread;
    private FetcherTask mFetcherTask;
    private String mSearchKey;
    private Boolean mUseGPS;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

    // Cache
    private LruCache<String, Bitmap> mMemoryCache;
    // Memory
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use 1/8th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 8;

    @SuppressWarnings("all")
    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    Log.i(TAG,"Google API connected");
                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    Log.i(TAG,"Last location" + mLocation);

                    if (mUseGPS) {
                        findLocation();
                        loadPhoto();
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.i(TAG,"Google API suspended");
                }
            };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG,"Got Location change : " + location.getLatitude() + "," + location.getLongitude() );

            mLocation = location;

            if (mUseGPS) {
                loadPhoto();
            }

            Toast.makeText(getActivity(),location.getLatitude() + "," + location.getLongitude(),
                    Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setRetainInstance(true);

        mUseGPS = PhotoGalleryPreference.getUseGps(getActivity());
        mSearchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());

        Log.d(TAG, "Memory size = " + maxMemory + " K ");

//        Intent i = PollService.newIntent(getActivity());
//        getActivity().startService(i);

//        PollJobService.start(getActivity());

//        PollService.setServiceAlarm(getActivity(), true);


        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        // Move from onCreateView
        mFlickrFetcher = new FlickrFetcher();
        mFetcherTask = new FetcherTask();
        new FetcherTask().execute(); //run another thread.

        Handler responseUIHandler = new Handler();
        ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder> listener =
                new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail, String url) {

                        if (null == mMemoryCache.get(url)) {
                            mMemoryCache.put(url, thumbnail);
                        }
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        target.bindDrawable(drawable);
                    }
                };

        mThumbnailDownloaderThread = new ThumbnailDownloader<>(responseUIHandler);
        mThumbnailDownloaderThread.setmThumbnailDownloaderListener(listener);
        mThumbnailDownloaderThread.start();
        mThumbnailDownloaderThread.getLooper();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .build();

        Log.i(TAG, "Start background thread");
    }


    private void findLocation() {
        if (hasPermission()) {
            requestLocation();
        }
    }

    private boolean hasPermission() {
        int permissionStatus = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        requestPermissions(new String[] {
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_PERMISSION_LOCATION);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            }
        }
    }

    @SuppressWarnings("all")
    private void requestLocation() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) == ConnectionResult.SUCCESS) {

            LocationRequest request = LocationRequest.create();

            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setNumUpdates(50); // จำนวนที่มัน update ได้
            request.setInterval(1000); // แต่ละช่วงห่างกัน 1 วิ

            LocationAvailability availability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);

            Log.d(TAG,"Location Available:" + availability.isLocationAvailable());

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, mLocationListener);
        }
    }

    private void unFindLocation() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.list_menu_refresh, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQuery(mSearchKey, false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Query text submitted: " + query);
                mSearchKey = query;
                loadPhoto();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Query text changed: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery(mSearchKey, false);
            }
        });

        //render polling
        MenuItem mnuPolling = menu.findItem(R.id.mnu_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            mnuPolling.setTitle(R.string.stop_polling);
        } else {
            mnuPolling.setTitle(R.string.start_polling);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh: loadPhoto();
                return true;

            case R.id.mnu_toggle_polling:

                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());

                Log.d(TAG, (shouldStartAlarm ? " Start " : " Stop ") + " Intent Service ");

                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu(); //refresh menu
                return true;

            case R.id.menu_clear_search:
                mSearchKey = null;
                loadPhoto();
                return true;

            case  R.id.mnu_manual_check:
                Intent pollIntent = PollService.newIntent(getActivity());
                getActivity().startService(pollIntent);
                return true;

            case R.id.mnu_alarm_clock:
                return true;

            case R.id.mnu_setting:
                Intent i = SettingActivity.newIntent(getActivity());
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mThumbnailDownloaderThread.quit();
        Log.i(TAG, "Stop background thread");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mThumbnailDownloaderThread.clearQueue();
    }

    @Override
    public void onPause() {
        super.onPause();
        PhotoGalleryPreference.setStoredSearchKey(getActivity(), mSearchKey);
        unFindLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        String searchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());

        if (searchKey != null) {
            mSearchKey = searchKey;
        }

        mUseGPS = PhotoGalleryPreference.getUseGps(getActivity());
        if (!mUseGPS) {
           loadPhoto();
        }
        Log.d(TAG,"On resume complete, mSearchKey = " + mSearchKey + ", mUseGPS = " + mUseGPS);

    }

    /**
     *
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.photo_gallery_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        mSearchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());

        Log.d(TAG, "On create complete : ----- Loaded key -----" + mSearchKey);

        return v;
    }

    private void loadPhoto() {
        if (mFetcherTask == null) {
            mFetcherTask = new FetcherTask();

            if (mSearchKey != null) {
                mFetcherTask.execute(mSearchKey);
            } else {
                mFetcherTask.execute();
            }
        } else {
            Log.d(TAG,"Fetch task is running now");
        }
    }

    class PhotoHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        ImageView mPhoto;
        GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);

//            mText = (TextView) itemView;
            mPhoto = (ImageView) itemView.findViewById(R.id.image_photo);
            mPhoto.setOnClickListener(this);

            itemView.setOnCreateContextMenuListener(this); // itemView is which holder are holding
        }



        public void bindDrawable(@NonNull Drawable drawable) {
            mPhoto.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem){
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View v) {
            FragmentManager fm = getFragmentManager();
            PhotoDialog dD = PhotoDialog.newInstance(mGalleryItem.getBigSizeUrl());
            dD.setTargetFragment(PhotoGalleryFragment.this, REQUEST_SHOW_PHOTO_DETAIL);
            dD.show(fm, DIALOG_SHOW_PHOTO_DETAIL);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(mGalleryItem.getPhotoUri().toString());

            MenuItem menuItem = menu.add(0, 1, 0, R.string.open_with_external_browser);
            menuItem.setOnMenuItemClickListener(this);
            //
            MenuItem menuItem2 = menu.add(0, 2, 0, R.string.open_in_app_browser);
            menuItem2.setOnMenuItemClickListener(this);

            MenuItem menuItem3 = menu.add(0, 3, 0, R.string.open_in_map);
            menuItem3.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case 1:
                    Intent i = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoUri());
                    startActivity(i); // call external browser by implicit intent
//            Toast.makeText(getActivity(), mGalleryItem.getUrl(), Toast.LENGTH_LONG).show();
                    return true;

                case 2:
                    Intent i2 = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoUri());
                    startActivity(i2); // call internal activity by explicit intent
                    return true;

                case 3:
                    Location itemLoc = null;
                    if (mGalleryItem.isGeoCorrect()) {
                        itemLoc = new Location("");
                        itemLoc.setLatitude(Double.valueOf(mGalleryItem.getLat()));
                        itemLoc.setLongitude(Double.valueOf(mGalleryItem.getLon()));
                    }

                    Intent i3 = PhotoMapActivity.newIntent(getActivity(), mLocation,itemLoc,
                            mGalleryItem.getUrl());
                    startActivity(i3);
                return true;

                default:
            }
            return false;
        }
    }

    class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoHolder> {

        List<GalleryItem> mGalleryItemList;

        PhotoGalleryAdapter(List<GalleryItem> galleryItems) {
            mGalleryItemList = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(
                    R.layout.item_photo, parent, false);

            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
//            holder.bindGalleryItem(mGalleryItemList.get(position));
            Drawable smileyDrawable = ResourcesCompat
                    .getDrawable(getResources(), R.drawable.bear, null);

            GalleryItem galleryItem = mGalleryItemList.get(position);
            Log.d(TAG, "bind position #" + position + ", url: " + galleryItem.getUrl());

            holder.bindGalleryItem(galleryItem);
            holder.bindDrawable(smileyDrawable);

            Glide.with(getActivity()).load(galleryItem.getBigSizeUrl()).into(holder.mPhoto);

            /**
            if(mMemoryCache.get(galleryItem.getUrl()) != null) {
                Bitmap bitmap = mMemoryCache.get(galleryItem.getUrl());
                holder.bindDrawable(new BitmapDrawable(getResources(), bitmap));
            } else {
                mThumbnailDownloaderThread.queueThumbnailDownload(holder, galleryItem.getUrl());
            }**/
        }

        @Override
        public int getItemCount() {
            return mGalleryItemList.size();
        }
    }

    class FetcherTask extends AsyncTask<String, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(String... params) {

                Log.d(TAG, "Fetcher task finish");

                List<GalleryItem> itemList = new ArrayList<>();
                FlickrFetcher flickrFetcher = new FlickrFetcher();

                if (params.length > 0) {
                    if (mUseGPS && mLocation != null) {
                        flickrFetcher.searchPhotos(itemList, params[0],
                                String.valueOf(mLocation.getLatitude()),
                                String.valueOf(mLocation.getLongitude()));
                    } else {
                        mFlickrFetcher.searchPhotos(itemList, params[0]);
                    }

                } else {
                    mFlickrFetcher.getRecentPhotos(itemList);
                }

                Log.d(TAG, "Fetcher task finish");
                return itemList;
        }


        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems = galleryItems;
            mRecyclerView.setAdapter(new PhotoGalleryAdapter(mItems));

            mAdapter = new PhotoGalleryAdapter(galleryItems);
            mRecyclerView.setAdapter(mAdapter);
            String formatString = getResources().getString(R.string.photo_progress_loaded);
            Snackbar.make(mRecyclerView, formatString, Snackbar.LENGTH_SHORT).show();
            mFetcherTask = null;
        }
    }
}
