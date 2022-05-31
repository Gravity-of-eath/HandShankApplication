package com.android.handshankapplication;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ByteArrayBuffer extends InputStream {
    private static final String TAG = "ByteArrayBuffer";
    ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 4);

    public void put(byte[] data) {
        buffer.put(data);
        buffer.flip();
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
        buffer.clear();
    }

    @Override
    public synchronized void mark(int readlimit) {
        buffer.mark();
    }

    @Override
    public synchronized void reset() throws IOException {
        buffer.reset();
    }

    @Override
    public int available() throws IOException {
        return buffer.remaining();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        buffer.get(b, off, len);
        buffer.compact();
        return len;
    }

}
