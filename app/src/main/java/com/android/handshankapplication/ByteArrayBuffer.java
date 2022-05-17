package com.android.handshankapplication;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ByteArrayBuffer extends InputStream {
    private ArrayList<byte[]> bytesArray = new ArrayList<>(32);
    private int readPosition = 0;
    private int writIndex = 0;
    private static final String TAG = "ByteArrayBuffer";

    public void put(byte[] data) {
        synchronized (bytesArray) {
            ++writIndex;
            bytesArray.add(data);
            Log.d(TAG, "put: writIndex= " + writIndex);
        }
    }

    @Override
    public int read() throws IOException {
        byte[] bytes = new byte[4];
        int read = read(bytes);
        if (read == 4) {
            return Utils.byteArrayToInt(bytes, 0);
        } else {
            return -1;
        }
    }

    public void clear() {
        synchronized (bytesArray) {
            readPosition = 0;
            writIndex = 0;
            bytesArray.clear();
        }
    }

    @Override
    public int available() throws IOException {
        int count = 0;
        for (byte[] bbb : bytesArray) {
            count += bbb.length;
        }
        return (count - readPosition);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null || b.length <= 0 || off < 0 || len <= 0) {
            return -1;
        }
        while (bytesArray.size() < 1) ;
        synchronized (bytesArray) {
            byte[] bytes = bytesArray.get(0);
            int finalLen = 0;
            if (len <= b.length) {
                finalLen = len;
            } else {
                finalLen = b.length;
            }
            int surplusLen = bytes.length - readPosition - off;
            Log.d(TAG, "read: surplusLen= " + surplusLen + "  finalLen=" + finalLen);
            if (surplusLen > finalLen) {
                System.arraycopy(bytes, readPosition + off, b, 0, finalLen);
                readPosition += (off + finalLen);
            } else {
                int needOrder = finalLen - surplusLen;
                System.arraycopy(bytes, readPosition + off, b, 0, surplusLen);
                boolean flag = true;
                while (flag) {
                    bytesArray.remove(0);
                    while (bytesArray.size() < 1) ;
                    byte[] bytesNew = bytesArray.get(0);
                    if (needOrder < bytesNew.length) {
                        System.arraycopy(bytesNew, 0, b, surplusLen, needOrder);
                        readPosition = needOrder;
                        flag = false;
                    } else {
                        System.arraycopy(bytesNew, 0, b, surplusLen, bytesNew.length);
                        surplusLen += bytesNew.length;
                        needOrder = needOrder - bytesNew.length;
                        flag = true;
                    }
                }
            }
            Log.d(TAG, "read: readPosition= " + readPosition + "  finalLen=" + finalLen);
            return finalLen;
        }
    }
}
