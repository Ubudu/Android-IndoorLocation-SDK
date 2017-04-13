package com.ubudu.gmaps.factory;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by mgasztold on 06/10/15.
 */
public class MarkerBitmapFactory {

    public static Bitmap getMarkerBitmap(int radius, String color){
        return getMarkerBitmap(radius,Color.parseColor(color));
    }

    public static Bitmap getMarkerWithHaloBitmap(int radius, String color, int haloRadius, String haloColor){
        return getMarkerWithHaloBitmap(radius,Color.parseColor(color), haloRadius, Color.parseColor(haloColor));
    }

    public static Bitmap getMarkerWithHaloBitmap(Bitmap bitmap, int haloRadius, int haloColor) {
        if(2*haloRadius > bitmap.getWidth() || 2*haloRadius > bitmap.getHeight()){
            Paint paint = new Paint();
            Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
            Bitmap newBitmap = Bitmap.createBitmap(2*haloRadius, 2*haloRadius, bitmapConfig);
            Canvas canvas = new Canvas(newBitmap);
            paint.setColor(haloColor);
            canvas.drawCircle(haloRadius,haloRadius,haloRadius,paint);
            canvas.drawBitmap(bitmap,haloRadius-bitmap.getWidth()/2,haloRadius-bitmap.getHeight()/2,null);
            return newBitmap;
        }
        return bitmap;
    }

    public static Bitmap getMarkerWithHaloBitmap(Bitmap bitmap, int haloRadius, String haloColor) {
        return getMarkerWithHaloBitmap(bitmap, haloRadius,Color.parseColor(haloColor));
    }

    public static Bitmap getMarkerBitmap(int radius, int color) {
        return getMarkerWithHaloBitmap(radius,color,0,0);
    }

    public static Bitmap getMarkerWithHaloBitmap(int radius, int color, int haloRadius, int haloColor) {
        Paint paint = new Paint();
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        int bitmapSize = 2*radius;
        if(haloRadius > radius)
            bitmapSize = haloRadius*2;
        Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, bitmapConfig);
        Canvas canvas = new Canvas(bitmap);
        paint.setColor(haloColor);
        canvas.drawCircle((int)(bitmapSize/2),(int)(bitmapSize/2),haloRadius,paint);
        paint.setColor(Color.parseColor("#80ffffff"));
        canvas.drawCircle((int)(bitmapSize/2),(int)(bitmapSize/2),radius,paint);
        paint.setColor(color);
        canvas.drawCircle((int)(bitmapSize/2),(int)(bitmapSize/2),(int)(0.8*radius),paint);
        return bitmap;
    }
}
