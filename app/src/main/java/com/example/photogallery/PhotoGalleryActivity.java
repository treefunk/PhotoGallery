package com.example.photogallery;

import android.os.Bundle;
import android.os.StrictMode;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.photogallery.fragments.PhotoGalleryFragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
}
