package com.android.handshankapplication;

import com.android.handshankapplication.fragment.MsgSender;

public class SerialSender implements MsgSender {


    @Override
    public void sendMsg(byte[] msg) {
        int length = msg.length;
        byte[] bytes = new byte[length + 2];
        bytes[0] = MsgSender.MSG_HEAD;
        bytes[0] = (byte) length;
        System.arraycopy(msg, 0, bytes, 2, msg.length - 1);
    }
}
