package com.android.handshankapplication.sender;

import com.android.handshankapplication.MsgSender;

import java.io.File;
import java.io.IOException;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class NativeSerialSender implements MsgSender {

    private SerialPort serialPort;

    public NativeSerialSender() {
        try {
            serialPort = new SerialPort(new File("/dev/ttyUSB0"), 115200, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMsg(byte[] msg) {

    }
}
