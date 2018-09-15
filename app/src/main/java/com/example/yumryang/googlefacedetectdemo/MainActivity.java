package com.example.yumryang.googlefacedetectdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
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

import com.example.root.cameramodule.CameraApi;
import com.example.root.cameramodule.ICameraApiCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback
        , ICameraApiCallback {

    private static final String TAG = "MainActivity";
    private final int PERMISSION_CAMERA_REQUEST_CODE = 0x10;
    private SurfaceView mSurfaceContent;
    private GraphicOverlay mGraphicOverlay;
    private ViewGroup.LayoutParams layoutParams;
    private SurfaceHolder mSurfaceHolder;

    private float ratio;
    private boolean isCheckPermissionOk;
    private int mSurfaceViewWidth;
    private int mSurfaceViewHeight;
    private int previewWidth = 1280;
    private int previewHeight = 720;
    public static int mCurrentCameraIndex = CameraApi.CAMERA_INDEX_BACK;

    private GoogleFaceDetectHelper mGoogleFaceDetectHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceContent = findViewById(R.id.surface_content);
        mSurfaceContent.getHolder().addCallback(this);
        mGraphicOverlay = findViewById(R.id.graphic_overlay);

        mGoogleFaceDetectHelper = new GoogleFaceDetectHelper(this);
        mGoogleFaceDetectHelper.setmOverlay(mGraphicOverlay);

        int permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
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

        mGoogleFaceDetectHelper.onStartDetect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: ");
        mGoogleFaceDetectHelper.onStopDetect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (isCheckPermissionOk) {
            Log.e(TAG, "surfaceCreated: ");
            CameraApi.getInstance().setCameraId(mCurrentCameraIndex);
            CameraApi.getInstance().initCamera(this, this);
            CameraApi.getInstance().setPreviewSize(new Size(previewWidth, previewHeight));
            CameraApi.getInstance().setFps(30).configCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
        ratio = (float) (previewWidth) / (float) (previewHeight);
        layoutParams = mSurfaceContent.getLayoutParams();
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
        mSurfaceContent.setLayoutParams(layoutParams);
        if (isCheckPermissionOk) {
            CameraApi.getInstance().startPreview(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed: ");
        CameraApi.getInstance().stopCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isCheckPermissionOk = true;
                onStartPreview();
            }
        }
    }

    private void onStartPreview() {
        CameraApi.getInstance().setCameraId(mCurrentCameraIndex);
        CameraApi.getInstance().initCamera(this, this);
        CameraApi.getInstance().setPreviewSize(new Size(previewWidth, previewHeight));
        CameraApi.getInstance().setFps(30).configCamera();
        CameraApi.getInstance().startPreview(mSurfaceHolder);
    }

    @Override
    public void onPreviewFrameCallback(byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
        mGoogleFaceDetectHelper.onReceiveFrameData(data);
    }

    @Override
    public void onNotSupportErrorTip(String message) {

    }

    @Override
    public void onCameraInit(Camera camera) {

    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }


}
