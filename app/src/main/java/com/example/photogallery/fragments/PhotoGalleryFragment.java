package com.example.photogallery.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photogallery.FlickrFetchr;
import com.example.photogallery.GalleryItem;
import com.example.photogallery.R;
import com.example.photogallery.ThumbnailDownloader;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";
    private List<GalleryItem> mItems = new ArrayList<>();
    RecyclerView mPhotoRecyclerView;
    private int currentPage = 0;
    private int recyclerViewWidth;
    private GridLayoutManager mGridLayoutManager;
    private PhotoAdapter photoAdapter;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery,container,false);

        mGridLayoutManager = new GridLayoutManager(getActivity(),3);
        mPhotoRecyclerView = view.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(mGridLayoutManager);
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                Log.v(TAG,"dx " + dx + "\ndy" + dy);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!recyclerView.canScrollVertically(1)){
                    new FetchItemsTask().execute(String.valueOf(++currentPage));
                    setupAdapter();
                }
            }
        });

        ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recyclerViewWidth = mPhotoRecyclerView.getWidth();
                Log.v(TAG,"recyclerview width " + recyclerViewWidth);
                if(recyclerViewWidth > 1080){
                    mGridLayoutManager.setSpanCount(6);
                }
            }
        };

        photoAdapter = new PhotoAdapter(mItems);
        mPhotoRecyclerView.setAdapter(photoAdapter);
        mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

        setupAdapter();

        return view;

    }



    private void setupAdapter() {
        Log.v(TAG,"item count " + mItems.size());

        if(isAdded()){
            photoAdapter.notifyDataSetChanged();
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder{

        private ImageView mItemImage;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            mItemImage = itemView.findViewById(R.id.image_view);
        }

        public void bindDrawable(Drawable drawable){
            mItemImage.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

        List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            View v = inflater.inflate(R.layout.list_item_gallery,parent,false);

            return new PhotoHolder(v);

//            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {

            GalleryItem galleryItem = mGalleryItems.get(position);

            Drawable placeHolder = getResources().getDrawable(R.drawable.ic_face);

            holder.bindDrawable(placeHolder);

            mThumbnailDownloader.queueThumbnail(holder,galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setRetainInstance(true);
        new FetchItemsTask().execute(String.valueOf(currentPage));

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);

        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(),thumbnail);
                photoHolder.bindDrawable(drawable);
            }
        });

        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG,"Background thread started");
    }

    private class FetchItemsTask extends AsyncTask<String,Void,List<GalleryItem>>{
        @Override
        protected List<GalleryItem> doInBackground(String... params) {
            String pageNumber = params[0];
            return new FlickrFetchr().fetchItems(pageNumber);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems.addAll(galleryItems);
            setupAdapter();
            super.onPostExecute(galleryItems);
        }
    }

    @Override
    public void onDestroy() {
        mThumbnailDownloader.quit();
        Log.i(TAG,"background thread destroyed");
        super.onDestroy();
    }
}
