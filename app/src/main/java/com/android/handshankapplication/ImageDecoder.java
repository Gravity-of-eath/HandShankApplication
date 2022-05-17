package com.android.handshankapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class ImageDecoder {
    private static final String TAG = "ImageDecoder";

    private LinkedBlockingQueue<byte[]> deque = new LinkedBlockingQueue<>();
    private int index = 0;
    private OnDataAvailableListener listener;
    private boolean run = true;
    private Decoder decoderA, decoderB;

    public ImageDecoder(OnDataAvailableListener listener) {
        this.listener = listener;
        decoderA = new Decoder("decoderA");
        decoderA.start();
        decoderB = new Decoder("decoderB");
        decoderB.start();
    }

    public void add(byte[] data) {
        deque.add(data);
        Log.e(TAG, "add: data.len=" + data.length);
    }

    private Pair<Integer, byte[]> takeData() {
        try {
            index++;
            Log.e(TAG, "takeData: index=" + index);
            return new Pair<>(index, deque.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class Decoder extends Thread {
        private int pIndex;

        public Decoder(@NonNull String name) {
            super(name);
        }

        @Override
        public void run() {
            while (run) {
                Pair<Integer, byte[]> data = takeData();
                if (data.second != null) {
                    pIndex = data.first;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data.second, 0, data.second.length);
                    if (bitmap != null && listener != null) {
                        listener.onImageAvailable(pIndex, bitmap);
                        Log.d(TAG, "run: decodeByteArray success!");
                    } else {
                        Log.e(TAG, "run: BitmapFactory.decodeByteArray fail..");
                    }
                }
            }
        }
    }

}
