package com.android.handshankapplication.fragment;

import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.android.handshankapplication.R;
import com.android.handshankapplication.view.JoystickView;

public class ScreenFragment extends BaseFragment {

    private JoystickView left_joystick, right_joystick;
    private TextView debug_info;
    private float x, y;
    private SurfaceView screen;

    @Override
    int getLayoutResource() {
        return R.layout.fragment_screen;
    }

    @Override
    void initView(View root) {
        screen = root.findViewById(R.id.screen);
        debug_info = root.findViewById(R.id.debug_info);
        left_joystick = root.findViewById(R.id.left_joystick);
        right_joystick = root.findViewById(R.id.right_joystick);
        left_joystick.setListener(new JoystickView.JoystickActionListener() {
            @Override
            public void onJoystickAction(float currentX, float currentY) {
                x = currentX;
                y = currentY;
                debug_info.setText(generalDebugInfo());
                right_joystick.setCurrentX(currentX);
                right_joystick.setCurrentY(currentY);
            }
        });
    }

    @Override
    void initData() {

    }

    private String generalDebugInfo() {
        return String.format("Left:X= %.3f   Y=%.3f", x, y);
    }
}
