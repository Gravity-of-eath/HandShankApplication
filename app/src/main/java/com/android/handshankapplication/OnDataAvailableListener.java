package com.android.handshankapplication;

import android.graphics.Bitmap;

public interface OnDataAvailableListener {
    default void onDataAvailable(int dataType, byte[] data) {
    }


    default void onImageAvailable(int dataType, Bitmap data) {
    }

}
