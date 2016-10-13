package com.ubudu.ubudumaplayout.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by mgasztold on 06/10/15.
 */
public class MapMarkers {

    public static Bitmap getMarkerBitmap(int radius, String color){
        return getMarkerBitmap(radius,Color.parseColor(color));
    }

    public static Bitmap getMarkerBitmap(int radius, int color) {
        Paint paint = new Paint();
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(radius, radius, bitmapConfig);
        Canvas canvas = new Canvas(bitmap);
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawCircle((int)(radius/2),(int)(radius/2),(int)(radius/2),paint);
        paint.setColor(color);
        canvas.drawCircle((int)(radius/2),(int)(radius/2),(int)(0.8*radius/2),paint);
        return bitmap;
    }
}
