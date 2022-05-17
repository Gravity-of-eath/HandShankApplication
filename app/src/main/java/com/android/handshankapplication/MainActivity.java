package com.android.handshankapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.android.handshankapplication.fragment.ByteProtocolConstant;
import com.android.handshankapplication.sender.CH340xSerialHelper;
import com.android.handshankapplication.sender.DeviceFinder;
import com.android.handshankapplication.sender.Reader;
import com.android.handshankapplication.view.JoystickView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class MainActivity extends AppCompatActivity implements OnDataAvailableListener {
    ArrayList list = new ArrayList<Integer>();
    ControlEventManager manager = ControlEventManager.getInstance();
    public static final String TAG = "MainActivity";
    private UsbManager usbmanager;
    private SurfaceView screen;
    SurfaceHolder surfaceHolder;
    //    private DeviceFinder deviceFinder;
    private CH340xSerialHelper xSerialHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbmanager = (UsbManager) getSystemService(Context.USB_SERVICE);
//        deviceFinder = new DeviceFinder(this, this);
        xSerialHelper = new CH340xSerialHelper(this, this);
        findViewById(R.id.start).setOnClickListener(listener);
        findViewById(R.id.stop).setOnClickListener(listener);
        findViewById(R.id.play_back).setOnClickListener(listener);
//        init();
        screen = findViewById(R.id.screen);
        screen.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                MainActivity.this.surfaceHolder = surfaceHolder;
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

            }
        });
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
            String manufacturerName = value.getManufacturerName();
            int deviceId = value.getDeviceId();
            String productName = value.getProductName();
            int productId = value.getProductId();
            int vendorId = value.getVendorId();
            String ss = "  name=" + key + "  productId=" + productId + "     vendorId=" + vendorId + "  manufacturerName=" + manufacturerName + "  productName=" + productName + "  deviceId=" + deviceId + "\n";
            stringBuffer.append(ss);
            Log.d(TAG, ss);
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


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.start) {
                xSerialHelper.sendMsg(Utils.intToByteArray(1));
            } else if (v.getId() == R.id.stop) {
                xSerialHelper.sendMsg(Utils.intToByteArray(2));
            } else if (v.getId() == R.id.play_back) {
                new Reader(MainActivity.this, MainActivity.this).start();
            } else {
            }
        }
    };

    @Override
    public void onDataAvailable(int dataType, byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Log.d(TAG, "onImageMessageReceived:dataType= " + dataType + " msg.length=" + data.length);
        if (bitmap != null) {
            Log.d(TAG, "onImageAvailable: bitmap");
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawBitmap(bitmap, 0, 0, new Paint());
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void onImageAvailable(int index, Bitmap data) {
        if (data != null) {
            Log.d(TAG, "onImageAvailable: bitmap");
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawBitmap(data, 0, 0, new Paint());
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }
}