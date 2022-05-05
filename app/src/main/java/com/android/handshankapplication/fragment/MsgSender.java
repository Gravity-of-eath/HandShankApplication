package com.android.handshankapplication.fragment;

public interface MsgSender {
    void sendMsg(byte[] msg);

    public static final byte MSG_HEAD = 0x23;
}
