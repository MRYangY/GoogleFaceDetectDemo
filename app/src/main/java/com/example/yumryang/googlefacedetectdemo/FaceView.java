package com.example.yumryang.googlefacedetectdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.List;

/**
 * View which displays a bitmap containing a face along with overlay graphics that identify the
 * locations of detected facial landmarks.
 */
public class FaceView extends View {
    private Bitmap mBitmap;
    private List<FirebaseVisionFace> mFaces;

    private int[] landMarkIds = new int[]{
            FirebaseVisionFaceLandmark.BOTTOM_MOUTH,
            FirebaseVisionFaceLandmark.LEFT_CHEEK,
            FirebaseVisionFaceLandmark.LEFT_EAR,
            FirebaseVisionFaceLandmark.LEFT_EYE,
            FirebaseVisionFaceLandmark.RIGHT_CHEEK,
            FirebaseVisionFaceLandmark.RIGHT_EAR,
            FirebaseVisionFaceLandmark.RIGHT_MOUTH,
            FirebaseVisionFaceLandmark.NOSE_BASE,
            FirebaseVisionFaceLandmark.RIGHT_EYE,
            FirebaseVisionFaceLandmark.LEFT_MOUTH
    };

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets the bitmap background and the associated face detections.
     */
    void setContent(Bitmap bitmap, List<FirebaseVisionFace> faces) {
        mBitmap = bitmap;
        mFaces = faces;
        invalidate();
    }

    /**
     * Draws the bitmap background and the associated face landmarks.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((mBitmap != null) && (mFaces != null)) {
            double scale = drawBitmap(canvas);
            drawFaceAnnotations(canvas, scale);
        }
    }

    /**
     * Draws the bitmap background, scaled to the device size.  Returns the scale for future use in
     * positioning the facial landmark graphics.
     */
    private double drawBitmap(Canvas canvas) {
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = mBitmap.getWidth();
        double imageHeight = mBitmap.getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

        Rect destBounds = new Rect(0, 0, (int) (imageWidth * scale), (int) (imageHeight * scale));
        canvas.drawBitmap(mBitmap, null, destBounds, null);
        return scale;
    }

    /**
     * Draws a small circle for each detected landmark, centered at the detected landmark position.
     * <p>
     * <p>
     * Note that eye landmarks are defined to be the midpoint between the detected eye corner
     * positions, which tends to place the eye landmarks at the lower eyelid rather than at the
     * pupil position.
     */
    private void drawFaceAnnotations(Canvas canvas, double scale) {
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        for (int i = 0; i < mFaces.size(); ++i) {
            FirebaseVisionFace face = mFaces.get(i);
            for (int id : landMarkIds
                    ) {
                FirebaseVisionFaceLandmark landmark = face.getLandmark(id);
                if (landmark != null) {
                    int cx = (int) (landmark.getPosition().getX() * scale);
                    int cy = (int) (landmark.getPosition().getY() * scale);
                    canvas.drawCircle(cx, cy, 10, paint);
                }

            }
//            for (Landmark landmark : face.get) {
//                int cx = (int) (landmark.getPosition().x * scale);
//                int cy = (int) (landmark.getPosition().y * scale);
//                canvas.drawCircle(cx, cy, 10, paint);
//            }
        }
    }


}

