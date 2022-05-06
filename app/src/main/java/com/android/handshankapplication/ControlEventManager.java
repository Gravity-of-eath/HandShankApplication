package com.android.handshankapplication;

import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.android.handshankapplication.fragment.ByteProtocolConstant;


public class ControlEventManager {

    private MsgSender sender;
    Dpad dpad = new Dpad();

    public void setSender(MsgSender sender) {
        this.sender = sender;
    }

    public ControlEventManager() {
    }

    public void analysisEvent(InputEvent event) {
        if (event instanceof MotionEvent) {
            /*
            * 处理双控制器摇杆。 许多游戏控制器都有左右两个操纵杆。对于左摇杆，Android 会将水平移动报告为 AXIS_X 事件，将垂直移动报告为 AXIS_Y 事件。对于右摇杆，Android 会将水平移动报告为 AXIS_Z 事件，将垂直移动报告为 AXIS_RZ 事件。请确保在您的代码中处理这两个控制器摇杆。*/
            if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
                MotionEvent motionEvent = (MotionEvent) event;
                float axisValueX = getCenteredAxis(motionEvent, motionEvent.getDevice(), MotionEvent.AXIS_X, -1);
                float axisValueY = getCenteredAxis(motionEvent, motionEvent.getDevice(), MotionEvent.AXIS_Y, -1);
                float axisValueZ = getCenteredAxis(motionEvent, motionEvent.getDevice(), MotionEvent.AXIS_Z, -1);

                float axisValueRX = getCenteredAxis(motionEvent, motionEvent.getDevice(), MotionEvent.AXIS_RX, -1);
                float axisValueRY = getCenteredAxis(motionEvent, motionEvent.getDevice(), MotionEvent.AXIS_RY, -1);
                float axisValueRZ = getCenteredAxis(motionEvent, motionEvent.getDevice(), MotionEvent.AXIS_RZ, -1);
                if (sender != null) {
                    byte[] msgX = new byte[2];
                    msgX[0] = ByteProtocolConstant.EventType.TYPE_LEFT_JOYSTICK_X;
                    msgX[1] = (byte) (axisValueX * 100);
                    sender.sendMsg(msgX);

                    byte[] msgY = new byte[2];
                    msgY[0] = ByteProtocolConstant.EventType.TYPE_LEFT_JOYSTICK_Y;
                    msgY[1] = (byte) (axisValueY * 100);
                    sender.sendMsg(msgY);

                    byte[] msgZ = new byte[2];
                    msgZ[0] = ByteProtocolConstant.EventType.TYPE_LEFT_JOYSTICK_Z;
                    msgZ[1] = (byte) (axisValueZ * 100);
                    sender.sendMsg(msgZ);

                    byte[] msgRx = new byte[2];
                    msgRx[0] = ByteProtocolConstant.EventType.TYPE_RIGHT_JOYSTICK_X;
                    msgRx[1] = (byte) (axisValueRX * 100);
                    sender.sendMsg(msgRx);

                    byte[] msgRY = new byte[2];
                    msgRY[0] = ByteProtocolConstant.EventType.TYPE_RIGHT_JOYSTICK_Y;
                    msgRY[1] = (byte) (axisValueRY * 100);
                    sender.sendMsg(msgRY);

                    byte[] msgRZ = new byte[2];
                    msgRZ[0] = ByteProtocolConstant.EventType.TYPE_RIGHT_JOYSTICK_Z;
                    msgRZ[1] = (byte) (axisValueRZ * 100);
                    sender.sendMsg(msgRZ);
                }
            }

        } else if (event instanceof KeyEvent) {
            if (Dpad.isDpadDevice(event)) {
                int directionPressed = dpad.getDirectionPressed(event);
                int action = ((KeyEvent) event).getAction();
                byte[] keyMsg = new byte[2];
                keyMsg[0] = (byte) directionPressed;
                keyMsg[1] = (byte) action;
                sender.sendMsg(keyMsg);
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

}
