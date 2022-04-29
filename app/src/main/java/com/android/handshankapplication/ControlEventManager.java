package com.android.handshankapplication;

import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.android.handshankapplication.fragment.MsgSender;


public class ControlEventManager {

    private MsgSender sender;

    public void setSender(MsgSender sender) {
        this.sender = sender;
    }

    public ControlEventManager() {
    }

    public void analysisEvent(InputEvent event) {
        if (event instanceof MotionEvent) {
            if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
                switch (((MotionEvent) event).getAction()) {
                    case MotionEvent.AXIS_RELATIVE_X:
                        float axisValueX = ((MotionEvent) event).getAxisValue(MotionEvent.AXIS_RELATIVE_X);
                        break;
                    case MotionEvent.AXIS_RELATIVE_Y:
                        float axisValueY = ((MotionEvent) event).getAxisValue(MotionEvent.AXIS_RELATIVE_Y);
                        break;
                }
            }

        } else if (event instanceof KeyEvent) {

        } else {

        }

    }


}
