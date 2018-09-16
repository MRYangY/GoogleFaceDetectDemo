package com.example.yumryang.googlefacedetectdemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.root.cameramodule.CameraApi;
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
    private byte[] paddingBuffer = null;
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
                //是否开启追踪模式，开启追踪模式后，才可以获得的unique id
                .setTrackingEnabled(true)
                //设置检测模式类型 FAST_MODE or ACCURATE_MODE
                //FAST_MODE 速度快，准确度不高；ACCURATE_MODE 准确度高，速度会慢点
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                //设置可检测脸的最小尺寸
                .setMinFaceSize(0.15f)
                //是否设置分类器，如果设置的话，可以检测获得人脸的微笑和正否睁眼的“可能性”
                // ，会返回一个float型的值0.0-1.0 值越大，可能性就越大
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                //设置是否检测脸部特征如：眼睛，嘴巴，耳朵等位置。
                // NO_LANDMARKS 表示 不检测、ALL_LANDMARKS表示检测
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .build();

        metadata = new FirebaseVisionImageMetadata.Builder()
                //设置格式
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setWidth(previewWidth)
                .setHeight(previewHeight)
                .setRotation(CameraApi.getInstance().getRotation())
                .build();

        //获得face detector
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
