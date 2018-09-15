package com.example.root.cameramodule;

import android.content.Context;

public class DensityUtils {

    private static float scale;

    /**
     *  dp to px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        if (scale == 0) {
            scale = context.getResources().getDisplayMetrics().density;
        }
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     *  px to dp
     */
    public static int px2dip(Context context, float pxValue) {
        if (scale == 0) {
            scale = context.getResources().getDisplayMetrics().density;
        }
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * px to sp
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }
}
