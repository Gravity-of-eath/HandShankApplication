package com.android.handshankapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class JoystickView extends View {
    public static final String TAG = "JoystickView";
    private int width = 0, oX = 0;
    private int height = 0, oY = 0, radius;
    private int backgroundColor, radiusColor;
    private Paint paint;
    private float currentX = 0.0f, currentY = 0.0f;
    private boolean canTouch = true;
    private float startX;
    private float startY;
    private JoystickActionListener listener;

    public void setListener(JoystickActionListener listener) {
        this.listener = listener;
    }

    public float getCurrentX() {
        return currentX;
    }

    public void setCurrentX(float currentX) {
        if (currentX >= -1 && currentX <= 1) {
            this.currentX = currentX;
        }
        invalidate();
    }

    public float getCurrentY() {
        return currentY;
    }

    public void setCurrentY(float currentY) {
        if (currentY >= -1 && currentY <= 1) {
            this.currentY = currentY;
        }
        invalidate();
    }

    public JoystickView(Context context) {
        super(context);
        init();
    }

    public JoystickView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JoystickView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundColor = Color.parseColor("#20AAAAAA");
        radiusColor = Color.parseColor("#FFAAAAAA");
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (canTouch) {
            int action = event.getAction();
            Log.d(TAG, "onTouchEvent " + action);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    startY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    currentX = (event.getX() - startX) / radius;
                    currentY = (event.getY() - startY) / radius;
                    if (currentX > 1) {
                        currentX = 1;
                    }
                    if (currentX < -1) {
                        currentX = -1;
                    }
                    if (currentY > 1) {
                        currentY = 1;
                    }
                    if (currentY < -1) {
                        currentY = -1;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    currentX = 0;
                    currentY = 0;
                    break;
            }
            if (this.listener != null) {
                this.listener.onJoystickAction(currentX, currentY);
            }
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        oX = width / 2;
        oY = height / 2;
        radius = oX < oY ? oX : oY;
        radius *= 0.9;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(backgroundColor);
        canvas.drawCircle(oX, oY, radius, paint);
        paint.setColor(radiusColor);
        canvas.drawCircle(oX, oY, radius / 15, paint);
        float vx = currentX * radius;
        float vy = currentY * radius;
        if (Math.sqrt(vx * vx + vy * vy) < radius) {
            paint.setColor(Color.GREEN);
            canvas.drawCircle(oX + vx, oY + vy, radius / 8, paint);
        } else {
            paint.setColor(Color.RED);
            double tan = Math.abs(Math.atan(currentX / currentY));
            float x = (float) (Math.sin(tan) * radius);
            float y = (float) (Math.cos(tan) * radius);
            if (currentX < 0) {
                x *= -1;
            }
            if (currentY < 0) {
                y *= -1;
            }
            canvas.drawCircle(oX + x, oY + y, radius / 8, paint);
        }

    }

    public interface JoystickActionListener {
        void onJoystickAction(float currentX, float currentY);
    }
}
