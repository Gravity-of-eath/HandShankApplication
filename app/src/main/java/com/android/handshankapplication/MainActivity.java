package com.android.handshankapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.android.handshankapplication.sender.CH340Sender;
import com.android.handshankapplication.sender.DeviceFinder;
import com.android.handshankapplication.sender.NativeSerialSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android_serialport_api.SerialPortFinder;

public class MainActivity extends AppCompatActivity {
    ArrayList list = new ArrayList<Integer>();
    ControlEventManager manager = new ControlEventManager();
    public static final String TAG = "MainActivity";
    private UsbManager usbmanager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager.setSender(new CH340Sender(this));
//        getDevices();
        usbmanager = (UsbManager) getSystemService(Context.USB_SERVICE);
        init();
    }

    private void init() {
        HashMap<String, UsbDevice> deviceList = usbmanager.getDeviceList();
        Iterator<Map.Entry<String, UsbDevice>> iterator = deviceList.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("devices：\n");
        while (iterator.hasNext()) {
            Map.Entry<String, UsbDevice> next = iterator.next();
            String key = next.getKey();
            UsbDevice value = next.getValue();
            String deviceName = value.getDeviceName();
            String manufacturerName = value.getManufacturerName();
            int deviceId = value.getDeviceId();
            String productName = value.getProductName();
            String ss = "  name=" + key + "  deviceName=" + deviceName + "  manufacturerName=" + manufacturerName + "  productName=" + productName + "  deviceId=" + deviceId + "\n";
            stringBuffer.append(ss);
            Log.d(TAG, ss);
        }
        TextView view = findViewById(R.id.port_list);
        view.setText(stringBuffer);
    }


    private void getDevices() {
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int i : deviceIds) {
            InputDevice device = InputDevice.getDevice(i);
//            device.getVibratorManager().getDefaultVibrator().vibrate(new long[100],2);
            if (device.getSources() == InputDevice.SOURCE_JOYSTICK) {
                list.add(i);
            } else if (device.getSources() == InputDevice.SOURCE_GAMEPAD) {
                list.add(i);
            } else if (device.getSources() == InputDevice.SOURCE_CLASS_JOYSTICK) {
                list.add(i);
            } else {
            }
        }
    }

    void getAlllSerial() {//need root
        SerialPortFinder finder = new SerialPortFinder();
        String[] allDevices = finder.getAllDevices();
        StringBuffer stringBuffer = new StringBuffer();
        Log.d(TAG, "allDevices.length=" + allDevices.length);
        for (String n : allDevices) {
            stringBuffer.append(n).append("\n");
            Log.d(TAG, "allDevices.name=" + n);
        }
        TextView view = findViewById(R.id.port_list);
        view.setText(stringBuffer);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        manager.analysisEvent(ev);
        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        manager.analysisEvent(event);
        return super.dispatchKeyEvent(event);
    }
}