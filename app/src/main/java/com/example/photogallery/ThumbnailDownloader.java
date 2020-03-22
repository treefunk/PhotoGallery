package com.example.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    public ThumbnailDownloader() {
        super(TAG);
    }

    public void queueThumbnail(T target, String url){

        if(url == null){
            mRequestMap.remove(target);
        }else{
            mRequestMap.put(target,url);
        }

        mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();

        Log.i(TAG,"Got a URL: " + url);
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD){
                    T target = (T)msg.obj;
                    Log.i(TAG,"Got a request for url " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    private void handleRequest(final T target) {
        try{
            final String url = mRequestMap.get(target);

            if(url == null){
                return;
            }

            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);

        }catch (IOException io){
            Log.e(TAG,"input output exception ", io);
        }
    }

    @Override
    public boolean quit() {
        return super.quit();
    }
}
