package com.android.handshankapplication;

import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.android.handshankapplication.fragment.ByteProtocolConstant;

import java.util.ArrayList;
import java.util.List;


public class ControlEventManager {
    private static final String TAG = "ControlEventManager";
    private List<ActionListener> listeners = new ArrayList<>();
    private Dpad dpad = new Dpad();
    private float axisValueX;
    private float axisValueY;
    private float axisValueZ;
    private float axisValueRX;
    private float axisValueRY;
    private float axisValueRZ;

    private static ControlEventManager manager;

    public static ControlEventManager getInstance() {
        if (manager == null) {
            manager = new ControlEventManager();
        }
        return manager;
    }

    public void setActionListener(ActionListener listener) {
        this.listeners.add(listener);
    }

    private ControlEventManager() {
    }

    public void analysisEvent(InputEvent event) {
        if (event instanceof MotionEvent) {
            /*
             * 处理双控制器摇杆。 许多游戏控制器都有左右两个操纵杆。对于左摇杆，Android 会将水平移动报告为 AXIS_X 事件，将垂直移动报告为 AXIS_Y 事件。对于右摇杆，Android 会将水平移动报告为 AXIS_Z 事件，将垂直移动报告为 AXIS_RZ 事件。请确保在您的代码中处理这两个控制器摇杆。*/
            if ((event.getSource()) == InputDevice.SOURCE_JOYSTICK) {
                MotionEvent motionEvent = (MotionEvent) event;
                float axisValueX = getCenteredAxis(motionEvent, motionEvent.getDevice(), MotionEvent.AXIS_X, -1);
                float axisValueY = getCenteredAxis(motionEvent, motionEvent.getDevice(), MotionEvent.AXIS_Y, -1);
                float axisValueZ = getCenteredAxis(motionEvent, motionEvent.getDevice(), MotionEvent.AXIS_Z, -1);

                float axisValueRX = getCenteredAxis(motionEvent, motionEvent.getDevice(), MotionEvent.AXIS_RX, -1);
                float axisValueRY = getCenteredAxis(motionEvent, motionEvent.getDevice(), MotionEvent.AXIS_RY, -1);
                float axisValueRZ = getCenteredAxis(motionEvent, motionEvent.getDevice(), MotionEvent.AXIS_RZ, -1);

                if (listeners != null) {
                    if (axisValueX != this.axisValueX) {
                        for (ActionListener l : listeners) {
                            if (l != null) {
                                l.onAction(ByteProtocolConstant.EventType.TYPE_LEFT_JOYSTICK_X, axisValueX);
                            }
                        }
                        this.axisValueX = axisValueX;
                    }
                    if (axisValueY != this.axisValueY) {
                        for (ActionListener l : listeners) {
                            if (l != null) {
                                l.onAction(ByteProtocolConstant.EventType.TYPE_LEFT_JOYSTICK_Y, axisValueY);
                            }
                        }
                        this.axisValueY = axisValueY;
                    }
                    if (axisValueZ != this.axisValueZ) {
                        for (ActionListener l : listeners) {
                            if (l != null) {
                                l.onAction(ByteProtocolConstant.EventType.TYPE_LEFT_JOYSTICK_Z, axisValueZ);
                            }
                        }
                        this.axisValueZ = axisValueZ;
                    }
                    if (axisValueRX != this.axisValueRX) {
                        for (ActionListener l : listeners) {
                            if (l != null) {
                                l.onAction(ByteProtocolConstant.EventType.TYPE_RIGHT_JOYSTICK_X, axisValueRX);
                            }
                        }
                        this.axisValueRX = axisValueRX;
                    }
                    if (axisValueRY != this.axisValueRY) {
                        for (ActionListener l : listeners) {
                            if (l != null) {
                                l.onAction(ByteProtocolConstant.EventType.TYPE_RIGHT_JOYSTICK_Y, axisValueRY);
                            }
                        }
                        this.axisValueRY = axisValueRY;
                    }
                    if (axisValueRZ != this.axisValueRZ) {
                        for (ActionListener l : listeners) {
                            if (l != null) {
                                l.onAction(ByteProtocolConstant.EventType.TYPE_RIGHT_JOYSTICK_Z, axisValueRZ);
                            }
                        }
                        this.axisValueRZ = axisValueRZ;
                    }
                }
            }

        } else if (event instanceof KeyEvent) {
            if (Dpad.isDpadDevice(event)) {
//                int directionPressed = dpad.getDirectionPressed(event);
//                int action = ((KeyEvent) event).getAction();
//                for (ActionListener l : listeners) {
//                    if (l != null) {
//                        l.onAction(directionPressed, action == KeyEvent.ACTION_DOWN ? 0f : 1f);
//                    }
//                }
            } else {
                int keyCode = ((KeyEvent) event).getKeyCode();
                Log.d(TAG, "analysisEvent: keyCode=" + keyCode);
                /*
                KEYCODE_BUTTON_THUMBL 左摇杆按键
                KEYCODE_BUTTON_THUMBR 右摇杆按键
                KEYCODE_BUTTON_X
                * */
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BUTTON_THUMBL:
                    case KeyEvent.KEYCODE_BUTTON_THUMBR:
                    case KeyEvent.KEYCODE_BUTTON_X:
                    case KeyEvent.KEYCODE_BUTTON_Y:
                    case KeyEvent.KEYCODE_BUTTON_A:
                    case KeyEvent.KEYCODE_BUTTON_B:
                    case KeyEvent.KEYCODE_BUTTON_START:
                }
            }
        } else {

        }

    }

    private static float getCenteredAxis(MotionEvent event,
                                         InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis) :
                            event.getHistoricalAxisValue(axis, historyPos);
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    public interface ActionListener {
        void onAction(int type, Number number);
    }

}
