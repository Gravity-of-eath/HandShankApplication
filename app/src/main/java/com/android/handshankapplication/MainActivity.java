package com.android.handshankapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList list = new ArrayList<Integer>();
    ControlEventManager manager = new ControlEventManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager.setSender(new SerialSender(this));
//        getDevices();
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