package com.android.handshankapplication.sender;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.handshankapplication.MsgSender;

public class CH340Sender implements MsgSender, Ch340Helper.OnMessageReceiverListener {
    private Ch340Helper helper;
    private onImageListener listener;

    public onImageListener getListener() {
        return listener;
    }

    public void setListener(onImageListener listener) {
        this.listener = listener;
    }

    public CH340Sender(Context context) {
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

    private static final String TAG = "CH340Sender";

    @Override
    public void onImageMessageReceived(byte[] msg) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(msg, 0, msg.length);
        Log.d(TAG, "onImageMessageReceived: msg.length=" + msg.length);
        if (bitmap != null && listener != null) {
            listener.onImageAvailable(bitmap);
            Log.d(TAG, "onImageAvailable: bitmap");
        }
    }

    public interface onImageListener {
        void onImageAvailable(Bitmap bitmap);
    }
}
