package com.example.root.cameramodule;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

public class CameraApi implements Camera.PreviewCallback {
    private static final String TAG = "CameraApi";
    private static volatile CameraApi CAMERA_INSTANCE = null;
    public static final int CAMERA_INDEX_BACK = 0;
    public static final int CAMERA_INDEX_FRONT = 1;
    private final int DEFAULT_FPS = 30;
    private final int DEFAULT_PREVIEW_WIDTH = 1920;
    private final int DEFAULT_PREVIEW_HEIGHT = 1080;
    /**
     * default buffer number is 5
     */
    private final int BUFFER_COUNT = 5;

    private int mCameraId = CAMERA_INDEX_BACK;
    private int mPreviewWidth = DEFAULT_PREVIEW_WIDTH;
    private int mPreviewHeight = DEFAULT_PREVIEW_HEIGHT;
    private int fps = DEFAULT_FPS;

    private Context context;
    private ICameraApiCallback cameraApiCallback;

    private Camera mCamera = null;
    private Camera.Parameters mCameraParameters;

    private int mRotation;

    private CameraApi() {
    }

    public static CameraApi getInstance() {
        if (CAMERA_INSTANCE == null) {
            synchronized (CameraApi.class) {
                if (CAMERA_INSTANCE == null) {
                    CAMERA_INSTANCE = new CameraApi();
                }
            }
        }
        return CAMERA_INSTANCE;
    }

    /**
     * set fps
     *
     * @param fps
     * @return
     */
    public CameraApi setFps(int fps) {
        this.fps = fps;
        return CAMERA_INSTANCE;
    }

    /**
     * set PreviewSize
     *
     * @param size
     * @return
     */
    public CameraApi setPreviewSize(Size size) {
        mPreviewWidth = size.getWidth();
        mPreviewHeight = size.getHeight();
        return CAMERA_INSTANCE;
    }

    /**
     * set camera id
     *
     * @param id
     * @return
     */
    public CameraApi setCameraId(int id) {
        this.mCameraId = id;
        return CAMERA_INSTANCE;
    }


    public synchronized void initCamera(Context context, ICameraApiCallback cameraApiCallback) {
        this.context = context;
        this.cameraApiCallback = cameraApiCallback;
        if (mCamera == null) {
            if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (this.cameraApiCallback != null) {
                    this.cameraApiCallback.onNotSupportErrorTip("no permission");
                    return;
                }
            }
            int cameraNumber = Camera.getNumberOfCameras();
            if (cameraNumber == 0) {
                Log.e(TAG, "initCamera: This Devices is no Camera");
                return;
            }
            mCamera = Camera.open(mCameraId);
            cameraApiCallback.onCameraInit(mCamera);
        }
    }


    public synchronized CameraApi configCamera() {
        if (mCamera == null) {
            if (cameraApiCallback != null)
                cameraApiCallback.onNotSupportErrorTip(context.getString(R.string.init_camera_error));
            return this;
        }
        mCameraParameters = mCamera.getParameters();
        List<Integer> supportFormat = mCameraParameters.getSupportedPreviewFormats();
        for (Integer f : supportFormat
                ) {
            Log.e(TAG, "configCamera: f = " + f + "--");
        }
        List<Camera.Size> supportedPreviewSizes = mCameraParameters.getSupportedPreviewSizes();
        if (!isPreviewSizeSupport(supportedPreviewSizes)) {
            mPreviewWidth = DEFAULT_PREVIEW_WIDTH;
            mPreviewHeight = DEFAULT_PREVIEW_HEIGHT;
        }
        mCameraParameters.setPreviewFormat(ImageFormat.NV21);
        mCameraParameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
        setCameraFps(mCameraParameters, fps);
        mCamera.setParameters(mCameraParameters);
        setupOrientaition(context);
        for (int i = 0; i < BUFFER_COUNT; i++) {
            mCamera.addCallbackBuffer(new byte[mPreviewHeight * mPreviewWidth * 3 / 2]);
        }
        return this;
    }

    private boolean isPreviewSizeSupport(List<Camera.Size> sizes) {
        boolean isSupport = false;
        for (Camera.Size s : sizes
                ) {
            if (s.width == mPreviewWidth && s.height == mPreviewHeight) {
                isSupport = true;
            }
            Log.e(TAG, "isPreviewSizeSupport: supportPreviewSize = " + s.width + "--" + s.height + ";");
        }
        return isSupport;
    }

    private void setupOrientaition(Context context) {
        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int degrees = 0;
        int displayRotation;
        Log.e(TAG, "setupOrientaition: rotation=" + rotation);
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
            default:
                break;
        }

        android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, cameraInfo);

        //根据前置与后置摄像头的不同，设置预览方向，否则会发生预览图像倒过来的情况。
        if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayRotation = (cameraInfo.orientation + degrees) % 360;
            displayRotation = (360 - displayRotation) % 360; // compensate
        } else {
            displayRotation = (cameraInfo.orientation - degrees + 360) % 360;
        }
        mRotation = displayRotation;
        this.mCamera.setDisplayOrientation(displayRotation);
    }

    public synchronized void startPreview(SurfaceHolder holder) {
        mCamera.setPreviewCallbackWithBuffer(this);
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stopCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void addCallbackBuffer(byte[] bytes){
        mCamera.addCallbackBuffer(bytes);
    }

    public int getRotation(){
        return mRotation;
    }

    /**
     * preview frame data callback
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (cameraApiCallback != null) {
            cameraApiCallback.onPreviewFrameCallback(data, camera);
        }
    }

    private void setCameraFps(Camera.Parameters p, int mFramerate) {
        List<int[]> range = p.getSupportedPreviewFpsRange();
        int i;
        int size = range.size();
        int[] r = range.get(0);
        int fps = mFramerate * 1000;
        boolean found = false;

        /*find fixed fps range*/
        for (i = 0; i < size; i++) {
            r = range.get(i);
            Log.d(TAG, "camera PFS[" + r[0] + " " + r[1] + "]");
            if (fps == r[0] && fps == r[1]) {
                Log.d(TAG, "Found fixed fps range, use it");
                found = true;
                break;
            }
        }

        /*find variable fps range*/
        for (i = 0; i < size && !found; i++) {
            r = range.get(i);
            Log.d(TAG, "camera PFS[" + r[0] + " " + r[1] + "]");
            if (fps >= r[0] && fps <= r[1]) {
                Log.d(TAG, "Found variable fps range, use it");
                found = true;
                break;
            }
        }

        if (!found) {
            Log.d(TAG, "Use first FPS range");
            r = range.get(0);
        }

        Log.d(TAG, "Set camera PFS[" + r[0] + " " + r[1] + "]");
        p.setPreviewFpsRange(r[0], r[1]);
    }
}
