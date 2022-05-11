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

import com.android.handshankapplication.MsgSender;
import com.android.handshankapplication.OnDataAvailableListener;
import com.android.handshankapplication.fragment.ByteProtocolConstant;
import com.felhr.usbserial.CH34xSerialDevice;
import com.felhr.usbserial.SerialInputStream;
import com.felhr.usbserial.SerialOutputStream;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DeviceFinder implements OnDataAvailableListener, MsgSender {
    public static final String TAG = "DeviceFinder";

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private Context context;
    private UsbManager manager;
    private UsbDevice targetDevice;
    private static boolean run = true;
    OnDataAvailableListener listener;
    private SerialOutputStream outputStream;
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) // User accepted our USB connection. Try to open the device as a serial port
                {
                    UsbDeviceConnection usbDeviceConnection = manager.openDevice(targetDevice);
//                    UsbSerialDevice usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(targetDevice, usbDeviceConnection);
                    UsbSerialDevice usbSerialDevice = CH34xSerialDevice.createUsbSerialDevice(targetDevice, usbDeviceConnection);
                    usbSerialDevice.setBaudRate(115200);
                    usbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    usbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);
                    usbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);
                    if (usbSerialDevice.syncOpen()) {
                        outputStream = usbSerialDevice.getOutputStream();
                        new Reader(usbSerialDevice, DeviceFinder.this).start();
                    } else {
                        Log.e(TAG, "onReceive: syncOpen fail");
                    }
                } else // User not accepted our USB connection. Send an Intent to the Main Activity
                {
                    Log.e(TAG, "onReceive: EXTRA_PERMISSION_GRANTED not granted");
                }
            } else {
            }
        }
    };

    public DeviceFinder(Context context, OnDataAvailableListener listener) {
        Log.d(TAG, "init");
        this.context = context;
        this.listener = listener;
        manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
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
    public void onDataAvailable(int dataType, byte[] data) {
        this.listener.onDataAvailable(dataType, data);
    }

    @Override
    public void sendMsg(byte[] msg) {
        outputStream.write(msg);
    }


    private class Reader extends Thread {
        UsbSerialDevice usbSerialDevice;
        SerialInputStream inputStream;
        OnDataAvailableListener listener;

        public Reader(UsbSerialDevice usbSerialDevice, OnDataAvailableListener listener) {
            this.usbSerialDevice = usbSerialDevice;
            this.listener = listener;
        }

        @Override
        public void run() {
            inputStream = usbSerialDevice.getInputStream();
            while (run) {
                try {
//                    while (inputStream.available() > 0) {
                    int read = inputStream.read();
                    if (read == ByteProtocolConstant.HEAD) {
                        Log.d(TAG, "run: HEAD matching");
//                            while (inputStream.available() > 0) {
                        int type = inputStream.read();
                        switch (type) {
                            case ByteProtocolConstant.DataType.IMAGE:
                                Log.d(TAG, "run: type matching IMAGE");
                                break;
                            case ByteProtocolConstant.DataType.VIDEO:
                                Log.d(TAG, "run: type matching VIDEO");
                                int dataLen = inputStream.read();
                                Log.d(TAG, "run: dataLen=" + dataLen);
                                byte[] bytes = new byte[dataLen];
                                int read1 = inputStream.read(bytes, 0, dataLen);
                                if (listener != null) {
                                    listener.onDataAvailable(ByteProtocolConstant.DataType.VIDEO, bytes);
                                }
                                break;
                        }

//                            }
                    }
//                    }
                    Log.e(TAG, "run: read=" + read);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "run: -----------------------------------------------------");
            }
        }
    }

}
