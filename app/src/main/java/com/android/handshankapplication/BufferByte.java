package com.android.handshankapplication;

import java.nio.ByteBuffer;

public class BufferByte {
    byte[] bb = new byte[65545];
    ByteBuffer buffer = ByteBuffer.wrap(bb);

    public BufferByte(byte[] bb) {
        this.bb = bb;

    }
}
