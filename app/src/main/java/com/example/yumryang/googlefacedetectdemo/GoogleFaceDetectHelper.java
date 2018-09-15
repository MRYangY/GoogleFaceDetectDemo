package com.example.yumryang.googlefacedetectdemo;

import android.content.Context;
import android.graphics.ImageFormat;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class GoogleFaceDetectHelper {
    private static final String TAG = "GoogleFaceDetectHelper";
    private GraphicOverlay mOverlay;
    private Context mContext;

    private int previewWidth;
    private int previewHeight;
    private byte[] mDate = null;
    byte[] paddingBuffer = null;
    private volatile boolean isStart = false;

    private final byte[] mLock = new byte[]{0};

    private FaceDetectThread mDetectThread = null;

    private FirebaseVisionFaceDetectorOptions options;
    private FirebaseVisionImageMetadata metadata;
    private FirebaseVisionFaceDetector faceDetector;
    private FirebaseVisionImage visionImage;


    public GoogleFaceDetectHelper(Context mContext) {
        this.mContext = mContext;
        init();

    }

    private void init() {
        previewWidth = ((MainActivity) mContext).getPreviewWidth();
        previewHeight = ((MainActivity) mContext).getPreviewHeight();
        paddingBuffer = new byte[previewWidth * previewHeight * 3 / 2];
        options = new FirebaseVisionFaceDetectorOptions.Builder()
                //是否开启追踪模式
                .setTrackingEnabled(true)
                //设置检测模式类型 FAST_MODE or ACCURATE_MODE
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                //设置可检测脸的最小尺寸
                .setMinFaceSize(0.15f)
                //设置是否检测脸部特征如：眼睛，嘴巴，耳朵等位置。
                // NO_LANDMARKS 表示 不检测、ALL_LANDMARKS表示检测
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                //是否设置分类器，如果设置的话，可以检测人脸的微笑level和正否睁眼的level
                .setClassificationType(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS).build();

        metadata = new FirebaseVisionImageMetadata.Builder()
                .setFormat(ImageFormat.NV21)
                .setWidth(previewWidth).setHeight(previewHeight)
//                .setRotation(CameraApi.getInstance().getRotation())
                .build();

        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options);

    }

    public void setmOverlay(GraphicOverlay mOverlay) {
        this.mOverlay = mOverlay;
        mOverlay.setCameraInfo(previewWidth, previewHeight, MainActivity.mCurrentCameraIndex);
    }

    public void onStartDetect() {
        isStart = true;
        if (mDetectThread == null) {
            mDetectThread = new FaceDetectThread("FaceDetectThread");
            mDetectThread.start();
        }
    }

    public void onStopDetect() {
        isStart = false;
    }


    /**
     * 接收处理camera 数据
     *
     * @param bytes
     */
    public void onReceiveFrameData(byte[] bytes) {
        synchronized (mLock) {
            if (mDate == null) {
//                Log.e(TAG, "onReceiveFrameData: bytes = " + bytes);
                mDate = bytes;
                mLock.notifyAll();
            }
        }
    }


    /**
     * 专属线程
     */
    class FaceDetectThread extends Thread {
        public FaceDetectThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            super.run();
            while (isStart) {
                synchronized (mLock) {
                    while (mDate == null) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
                System.arraycopy(mDate, 0, paddingBuffer, 0, mDate.length);
                mDate = null;
                visionImage = FirebaseVisionImage.fromByteArray(paddingBuffer, metadata);
                faceDetector.detectInImage(visionImage).addOnSuccessListener(onSuccessListener)
                        .addOnFailureListener(onFailureListener);

            }
        }
    }


    /**
     * detect success callback
     */
    private OnSuccessListener<List<FirebaseVisionFace>> onSuccessListener = new OnSuccessListener<List<FirebaseVisionFace>>() {
        @Override
        public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
            mOverlay.clear();
            for (int i = 0; i < firebaseVisionFaces.size(); ++i) {
                FirebaseVisionFace face = firebaseVisionFaces.get(i);
                FaceGraphic faceGraphic = new FaceGraphic(mOverlay);
                mOverlay.add(faceGraphic);
                faceGraphic.updateFace(face, MainActivity.mCurrentCameraIndex);
            }
        }
    };

    /**
     * detect fail callback
     */
    private OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Log.e(TAG, "onFailure: face detect error");
        }
    };
}
