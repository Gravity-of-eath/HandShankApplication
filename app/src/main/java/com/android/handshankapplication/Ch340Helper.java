package com.android.handshankapplication;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.nio.charset.Charset;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

public class Ch340Helper {
    private static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";
    public static final String TAG = "Ch340Helper";
    private Context context;
    private CH34xUARTDriver ch34xUARTDriver;
    OnMessageReceiverListener listener;
    private int baudRate = 9600;
    private byte stopBit = 1;
    private byte dataBit = 8;
    private byte parity = 0;
    byte flowControl = 0;
    private boolean isConfiged;

    public void setListener(OnMessageReceiverListener listener) {
        this.listener = listener;
    }

    public Ch340Helper(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        ch34xUARTDriver = new CH34xUARTDriver(
                (UsbManager) context.getSystemService(Context.USB_SERVICE), context,
                ACTION_USB_PERMISSION);
        int open = open();
        if (open == 0) {
            configDef();
        }
    }


    public int open() {
        int i = ch34xUARTDriver.ResumeUsbPermission();
        if (i == 0) {
            int retval = ch34xUARTDriver.ResumeUsbPermission();
            if (retval == 0) {
                //Resume usb device list
                retval = ch34xUARTDriver.ResumeUsbList();
                if (retval == -1)// ResumeUsbList方法用于枚举CH34X设备以及打开相关设备
                {
                    Toast.makeText(context, "Open failed!",
                            Toast.LENGTH_SHORT).show();
                    ch34xUARTDriver.CloseDevice();
                    return -1;
                } else if (retval == 0) {
                    if (ch34xUARTDriver.mDeviceConnection != null) {
                        if (!ch34xUARTDriver.UartInit()) {//对串口设备进行初始化操作
                            Toast.makeText(context, "Initialization failed!",
                                    Toast.LENGTH_SHORT).show();
                            return -1;
                        }
                        Toast.makeText(context, "Device opened",
                                Toast.LENGTH_SHORT).show();
                        if (!ch34xUARTDriver.SetConfig(baudRate, dataBit, stopBit, parity,//配置串口波特率，函数说明可参照编程手册
                                flowControl)) {
                            Toast.makeText(context, "Config failed!",
                                    Toast.LENGTH_SHORT).show();
                            return -1;
                        }
                        new readThread().start();//开启读线程读取串口接收的数据
                    } else {
                        Toast.makeText(context, "Open failed!",
                                Toast.LENGTH_SHORT).show();
                        return -1;
                    }
                }
            }
        } else {
            return -1;
        }
        return 0;
    }

    public int SendData(byte[] bytes) {
        if (null == ch34xUARTDriver) {
            Toast.makeText(context, "ch34xUARTDriver is null!", Toast.LENGTH_SHORT).show();
            return -1;
        }
        if (!isConfiged) {
            Toast.makeText(context, "no configed !", Toast.LENGTH_SHORT).show();
            return -1;
        }
        int retval = ch34xUARTDriver.WriteData(bytes, bytes.length);//写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
        if (retval < 0)
            Toast.makeText(context, "Write failed!", Toast.LENGTH_SHORT).show();
        return retval;
    }

    public void configDef() {
        if (!ch34xUARTDriver.isConnected()) {
            Toast.makeText(context, "NO connected devices", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ch34xUARTDriver.SetConfig(baudRate, dataBit, stopBit, parity,//配置串口波特率，函数说明可参照编程手册
                flowControl)) {
            Toast.makeText(context, "Config successfully", Toast.LENGTH_SHORT).show();
            isConfiged = true;
        } else {
            Toast.makeText(context, "Config failed!",
                    Toast.LENGTH_SHORT).show();
        }
    }


    public void config(int baudRate, byte stopBit, byte dataBit, byte parity) {
        if (ch34xUARTDriver == null || !ch34xUARTDriver.isConnected()) {
            Toast.makeText(context, "Config failed",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (ch34xUARTDriver.SetConfig(baudRate, dataBit, stopBit, parity,//配置串口波特率，函数说明可参照编程手册
                flowControl)) {
            Toast.makeText(context, "Config successfully",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Config failed!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class readThread extends Thread {
        public void run() {
            byte[] buffer = new byte[2048];
            while (true) {
                int length = ch34xUARTDriver.ReadData(buffer, 2048);
                if (length > 0) {
                    byte[] recvb = new byte[length];
                    System.arraycopy(buffer, 0, recvb, 0, length);
                    if (null != listener) {
                        listener.onMessageReceived(recvb);
                    }
                    String recv = new String(recvb, Charset.forName("ascii"));        //以字符串形式输出
                    Log.e(TAG, "readThread====  " + recv);
                }
            }
        }
    }


    public void onRelease() {
        if (ch34xUARTDriver != null) {
            ch34xUARTDriver.CloseDevice();
        }
    }

    public interface OnMessageReceiverListener {
        void onMessageReceived(byte[] msg);
    }

}
