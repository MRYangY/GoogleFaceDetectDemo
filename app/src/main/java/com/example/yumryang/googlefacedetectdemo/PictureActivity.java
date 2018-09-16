package com.example.yumryang.googlefacedetectdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.InputStream;
import java.util.List;


public class PictureActivity extends Activity {
    private static final String TAG = "PictureActivity";
    private FaceView faceview;
    private Button detect;
    private TextView mFaceBound;
    private TextView mSmileLevel;
    private TextView mEyeLevel;


    private FirebaseVisionFaceDetector faceDetector;
    private FirebaseVisionFaceDetectorOptions options;
    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        detect = findViewById(R.id.bt_detect);
        faceview = findViewById(R.id.iv_image);
        mFaceBound = findViewById(R.id.face_bound);
        mSmileLevel = findViewById(R.id.classify_eye);
        mEyeLevel = findViewById(R.id.classify_smile);

        detect.setOnClickListener(mDetectListener);

        options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setTrackingEnabled(false)
                //是否设置分类器，如果设置的话，可以检测获得人脸的微笑和正否睁眼的“可能性”
                // ，会返回一个float型的值0.0-1.0 值越大，可能性就越大
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build();
        //获得face detector
        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        InputStream stream = getResources().openRawResource(R.raw.girl);
        bitmap = BitmapFactory.decodeStream(stream);

    }

    private View.OnClickListener mDetectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            faceDetector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                @Override
                public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                    faceview.setContent(bitmap, firebaseVisionFaces);
                    FirebaseVisionFace face = firebaseVisionFaces.get(0);
                    float leftEyeOpenProbability = face.getLeftEyeOpenProbability();
                    float rightEyeOpenProbability = face.getRightEyeOpenProbability();
                    float smilingProbability = face.getSmilingProbability();

                    mFaceBound.setText("Face Bound:  " + "--" + face.getBoundingBox().left + "--" + face.getBoundingBox().top
                            + "--" + face.getBoundingBox().right + "--" + face.getBoundingBox().bottom);
                    mEyeLevel.setText("Eye Open Possible:  " + "leftEyeOpenProbability=" + leftEyeOpenProbability +
                            "--rightEyeOpenProbability=" + rightEyeOpenProbability);
                    mSmileLevel.setText("Smile Possible:  " + "smilingProbability=" + smilingProbability);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    };
}
