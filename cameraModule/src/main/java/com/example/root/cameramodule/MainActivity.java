package com.example.root.cameramodule;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, ICameraApiCallback {
    private static final String TAG = "MainActivity";
    private final int PERMISSION_CAMERA_REQUEST_CODE = 0x10;
    private boolean isCheckPermissionOk = false;

    private SurfaceView mSurfaceView;
    private ViewGroup.LayoutParams layoutParams;

    private int previewWidth = 1920;
    private int previewHeight = 1080;
    private float ratio;

    private int mSurfaceViewWidth;
    private int mSurfaceViewHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
        } else {
            isCheckPermissionOk = true;
        }


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        mSurfaceViewWidth = size.x;
        mSurfaceViewHeight = size.y;
        Log.e(TAG, "onCreate: " + size.x + "--" + size.y);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isCheckPermissionOk = true;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (isCheckPermissionOk) {
            Log.e(TAG, "surfaceCreated: ");
            CameraApi.getInstance().setCameraId(CameraApi.CAMERA_INDEX_BACK);
            CameraApi.getInstance().initCamera(this, this);
            CameraApi.getInstance().setPreviewSize(new Size(previewWidth, previewHeight));
            CameraApi.getInstance().setFps(30).configCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        ratio = (float) (previewWidth) / (float) (previewHeight);
        layoutParams = mSurfaceView.getLayoutParams();
        if (mSurfaceViewHeight > mSurfaceViewWidth) {
            //竖屏
            mSurfaceViewHeight = (int) (mSurfaceViewWidth * ratio);
        } else {
            //横屏
            mSurfaceViewWidth = (int) (mSurfaceViewHeight * ratio);
        }

        Log.e(TAG, "surfaceChanged:mSurfaceViewWidth= " + mSurfaceViewWidth);
        Log.e(TAG, "surfaceChanged:mSurfaceViewHeight= " + mSurfaceViewHeight);
        layoutParams.width = mSurfaceViewWidth;
        layoutParams.height = mSurfaceViewHeight;
        mSurfaceView.setLayoutParams(layoutParams);
        CameraApi.getInstance().startPreview(holder);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed: ");
        CameraApi.getInstance().stopCamera();
    }

    @Override
    public void onPreviewFrameCallback(byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
    }

    @Override
    public void onNotSupportErrorTip(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraInit(Camera camera) {

    }
}
