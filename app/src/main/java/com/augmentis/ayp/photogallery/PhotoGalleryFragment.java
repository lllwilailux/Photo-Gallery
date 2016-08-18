package com.augmentis.ayp.photogallery;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wilailux on 8/16/2016.
 */
public class PhotoGalleryFragment extends Fragment{

    public static PhotoGalleryFragment newInstance() {

        Bundle args = new Bundle();
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mRecyclerView;
    private FlickrFetcher mFlickrFetcher;
    private PhotoGalleryAdapter mAdapter;
    private List<GalleryItem> mItems;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery,container,false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.photo_gallery_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        mItems = new ArrayList<>();
        mRecyclerView.setAdapter(new PhotoGalleryAdapter(mItems));

        mFlickrFetcher = new FlickrFetcher();
        new FetcherTask().execute();// run another thread
        return v;
    }

    class PhotoGalleryVH extends RecyclerView.ViewHolder {

//        TextView mText;
        ImageView mPhoto;

        public PhotoGalleryVH(View itemView) {
            super(itemView);
            mPhoto = (ImageView) itemView.findViewById(R.id.image_photo);
        }

        public void bindDrawable(@NonNull Drawable drawable) {
            mPhoto.setImageDrawable(drawable);
        }

//        public void bindGalleryItem(GalleryItem galleryItem) {
//            mText.setText(galleryItem.getTitle());
//        }
    }

    class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoGalleryVH> {

        List<GalleryItem> mGalleryItemList;

        PhotoGalleryAdapter(List<GalleryItem> galleryItems) {
            mGalleryItemList = galleryItems;
        }

        @Override
        public PhotoGalleryVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_photo,parent,false);
            return new PhotoGalleryVH(v);
        }

        @Override
        public void onBindViewHolder(PhotoGalleryVH holder, int position) {
//            holder.bindGalleryItem(mGalleryItemList.get(position));
            Drawable smileyDrawable = ResourcesCompat.getDrawable(getResources(),R.drawable.bear,null);

            GalleryItem galleryItem = mGalleryItemList.get(position);
            Log.d(TAG,"bind position # " + position + " , url " + galleryItem.getUrl());

            holder.bindDrawable(smileyDrawable);
        }

        @Override
        public int getItemCount() {
            return mGalleryItemList.size();
        }
    }

    class FetcherTask extends AsyncTask<Void,Void,List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            Log.d(TAG,"Start Fetcher task ");
            List<GalleryItem> itemList = new ArrayList<>();
            mFlickrFetcher.fetchItems(itemList);
            Log.d(TAG,"Fetcher task finished");
            return itemList;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            String formatString = getResources().getString(R.string.photo_progress_loaded);
            Snackbar.make(mRecyclerView,formatString,Snackbar.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
//                mAdapter = new PhotoGalleryAdapter(galleryItems);
            mItems = galleryItems;
            mRecyclerView.setAdapter(new PhotoGalleryAdapter(mItems));

        }
    }
}
