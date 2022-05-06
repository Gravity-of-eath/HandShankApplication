package com.android.handshankapplication;

import android.content.Context;

import com.android.handshankapplication.fragment.MsgSender;

public class SerialSender implements MsgSender, Ch340Helper.OnMessageReceiverListener {
    private Ch340Helper helper;

    public SerialSender(Context context) {
        helper = new Ch340Helper(context);
        helper.setListener(this);
    }

    @Override
    public void sendMsg(byte[] msg) {
        int length = msg.length;
        byte[] bytes = new byte[length + 2];
        bytes[0] = MsgSender.MSG_HEAD;
        bytes[1] = (byte) length;
        System.arraycopy(msg, 0, bytes, 2, msg.length - 1);
        helper.SendData(bytes);
    }

    @Override
    public void onMessageReceived(byte[] msg) {

    }
}
