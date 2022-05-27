package com.android.handshankapplication.sender;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.android.handshankapplication.ImageDecoder;
import com.android.handshankapplication.MsgSender;
import com.android.handshankapplication.OnDataAvailableListener;
import com.android.handshankapplication.Utils;
import com.android.handshankapplication.fragment.ByteProtocolConstant;
import com.felhr.usbserial.CH34xSerialDevice;
import com.felhr.usbserial.SerialInputStream;
import com.felhr.usbserial.SerialOutputStream;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceFinder implements MsgSender {
    public static final String TAG = "DeviceFinder";
    private final int BAUD_RATE = 115200;
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public static final byte[] HEAD = "-1234567890987654321-".getBytes();
    private Context context;
    private UsbManager manager;
    private UsbDevice targetDevice;
    private static boolean run = true;
    OnDataAvailableListener listener;
    private UsbSerialDevice usbSerialDevice;
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                targetDevice = arg1.getExtras().getParcelable(UsbManager.EXTRA_DEVICE);
                if (granted) // User accepted our USB connection. Try to open the device as a serial port
                {
                    UsbDeviceConnection usbDeviceConnection = manager.openDevice(targetDevice);
                    UsbSerialDevice usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(targetDevice, usbDeviceConnection);
//                    usbSerialDevice = CH34xSerialDevice.createUsbSerialDevice(targetDevice, usbDeviceConnection);
//
                    if (usbSerialDevice.syncOpen()) {
                        usbSerialDevice.setBaudRate(BAUD_RATE);
                        usbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
                        usbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);
                        usbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);
                        usbSerialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                        new Reader(usbSerialDevice, listener).start();
                    }
                } else // User not accepted our USB connection. Send an Intent to the Main Activity
                {
                    Log.e(TAG, "onReceive: EXTRA_PERMISSION_GRANTED not granted");
                }
            } else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {
                run = false;
            } else if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
            } else {
            }
        }
    };

    public DeviceFinder(Context context, OnDataAvailableListener listener) {
        Log.d(TAG, "init  HEAD=" + bytesToHexString(HEAD));
        this.context = context;
        this.listener = listener;
        manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_ATTACHED);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        context.registerReceiver(usbReceiver, filter);
        init();
    }

    private void init() {
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<Map.Entry<String, UsbDevice>> iterator = deviceList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, UsbDevice> next = iterator.next();
            String key = next.getKey();
            UsbDevice value = next.getValue();
            String deviceName = value.getDeviceName();
            String manufacturerName = value.getManufacturerName();
            int deviceId = value.getDeviceId();
            String productName = value.getProductName();
            Log.d(TAG, "  name=" + key + "  deviceName=" + deviceName + "  manufacturerName=" + manufacturerName + "  productName=" + productName + "  deviceId=" + deviceId);
            int interfaceCount = value.getInterfaceCount();
            UsbInterface targetInterface = null;
            for (int i = 0; i < interfaceCount; i++) {
                UsbInterface anInterface = value.getInterface(i);
                if (anInterface.getInterfaceClass() == UsbConstants.USB_CLASS_VENDOR_SPEC
                        && anInterface.getInterfaceSubclass() == UsbConstants.USB_INTERFACE_SUBCLASS_BOOT
                        && anInterface.getInterfaceProtocol() == UsbConstants.USB_CLASS_COMM) {
                    targetInterface = anInterface;
                    targetDevice = value;
                    break;
                }
            }
            if (targetInterface != null) {
                requestUserPermission();
            }
        }

    }

    private void requestUserPermission() {
        Log.d(TAG, String.format("requestUserPermission(%X:%X)", targetDevice.getVendorId(), targetDevice.getProductId()));
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        manager.requestPermission(targetDevice, mPendingIntent);
    }

    @Override
    public void sendMsg(byte[] msg) {
        outputStream.write(msg);
    }

    private SerialOutputStream outputStream;

    private class Reader extends Thread {
        UsbSerialDevice usbSerialDevice;
        OnDataAvailableListener listener;
        private ImageDecoder imageDecoder;
        //        private final BufferedInputStream bufferedInputStream;
        private final SerialInputStream inputStream;

        public Reader(UsbSerialDevice usbSerialDevice, OnDataAvailableListener listener) {
            this.usbSerialDevice = usbSerialDevice;
            this.listener = listener;
            inputStream = usbSerialDevice.getInputStream();
            inputStream.setTimeout(1000);
//            bufferedInputStream = new BufferedInputStream(inputStream);
            outputStream = usbSerialDevice.getOutputStream();
//            outputStream.setTimeout(9914100);
            imageDecoder = new ImageDecoder(listener, 3);
        }

        @Override
        public void run() {
            while (run) {
                try {
                    byte[] head = new byte[1];
                    int read = inputStream.read();
                    if (read == HEAD[0]) {
                        Log.d(TAG, "run: HEAD-----finddddddddd");
                        boolean flag = false;
                        for (int i = 1; i < HEAD.length; i++) {
                            byte[] b = new byte[1];
                            inputStream.mark(HEAD.length);
                            int i1 = inputStream.read();
                            if (i1 == HEAD[i]) {
                                flag = true;
                            } else {
                                Log.d(TAG, "run: break i=" + i);
                                flag = false;
                                break;
                            }
                        }
                        Log.d(TAG, "run: flag---------- " + flag);
                        if (flag) {
                            int available = inputStream.available();
                            if (available > 4) {

                            }
                            byte[] bytes = new byte[4];
                            int ret = inputStream.read(bytes, 0, 4);
                            Log.d(TAG, "run: ret ---------- " + ret);
                            if (ret != -1) {
                                int dataLens = Utils.byteArrayToInt(bytes, 0);
                                Log.d(TAG, "run: dataLens ---------- " + dataLens);
                                if (dataLens > 0) {
                                    byte[] data = new byte[dataLens];
                                    inputStream.read(data);
                                }
                            }
                        } else {
                            inputStream.reset();
                        }
                    } else {

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int findSequence(byte[] data, byte[] mSequence) {
        if (mSequence == null || mSequence.length == 0 || data == null | data.length <= 0 || data.length < mSequence.length) {
            return -1;
        }
        int index = -1;
        for (int i = 0; i < data.length - mSequence.length; i++) {
            index++;
            if (data[i] == mSequence[0]) {
                boolean flag = false;
                for (int j = 1; j < mSequence.length; j++) {
                    if (mSequence[j] == data[i + j]) {
                        flag = true;
                    } else {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    return index;
                }
            }
        }
        return index;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String byteArrToHex(byte... bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToHexString(byte... src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


}
