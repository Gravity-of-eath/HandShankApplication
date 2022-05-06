package com.android.handshankapplication;

public interface MsgSender {
    void sendMsg(byte[] msg);

    public static final byte MSG_HEAD = 0x23;
}
