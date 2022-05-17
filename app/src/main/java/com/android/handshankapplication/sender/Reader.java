package com.android.handshankapplication.sender;

import android.content.Context;

import com.android.handshankapplication.ByteArrayBuffer;
import com.android.handshankapplication.ImageDecoder;
import com.android.handshankapplication.OnDataAvailableListener;
import com.android.handshankapplication.Utils;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Reader extends Thread implements UsbSerialInterface.UsbReadCallback {
    OnDataAvailableListener listener;
    private ImageDecoder imageDecoder;
    private FileInputStream fileOutputStream;
    private boolean run = true;

    public Reader(Context context, OnDataAvailableListener listener) {
        this.listener = listener;
        imageDecoder = new ImageDecoder(listener);
        File file = new File(context.getCacheDir(), "mjpeg.stream");
        try {
            fileOutputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (run) {
            try {
                Utils.findHead(fileOutputStream);
                byte[] bytes1 = new byte[4];
                int read = fileOutputStream.read(bytes1);
                int i = Utils.byteArrayToInt(bytes1, 0);
                byte[] bytes = new byte[i];
                int read1 = fileOutputStream.read(bytes);
                imageDecoder.add(bytes);
                Thread.sleep(150);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReceivedData(byte[] data) {
    }
}