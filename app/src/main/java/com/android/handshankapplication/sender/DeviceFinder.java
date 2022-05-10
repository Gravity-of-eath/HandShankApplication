package com.android.handshankapplication.sender;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DeviceFinder {
    public static final String TAG = "DeviceFinder";

    private UsbManager manager;

    public DeviceFinder(UsbManager manager) {
        Log.d(TAG, "init");
        this.manager = manager;
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
        }

    }


}
