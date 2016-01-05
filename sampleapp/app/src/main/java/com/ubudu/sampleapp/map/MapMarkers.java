package com.ubudu.sampleapp.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by mgasztold on 14/12/15.
 */
public class MapMarkers {

    public static Bitmap userLocationMarkerBitmap;
    public static Bitmap greenMarkerBitmap;
    public static Bitmap beaconMarkerBitmap;
    public static Bitmap mapBeaconMarkerBitmap;

    private Canvas userLocationMarkerCanvas;
    private Bitmap.Config userLocationMarkerBitmapConfig;
    private Paint userLocationMarkerPaint;

    private Canvas greenMarkerCanvas;
    private Bitmap.Config greenMarkerBitmapConfig;
    private Paint greenMarkerPaint;

    private Canvas beaconMarkerCanvas;
    private Bitmap.Config beaconMarkerBitmapConfig;
    private Paint beaconMarkerPaint;

    private Canvas mapBeaconMarkerCanvas;
    private Bitmap.Config mapBeaconMarkerBitmapConfig;
    private Paint mapBeaconMarkerPaint;

    private static MapMarkers instance;
    public static MapMarkers getInstance(){
        if(instance==null)
            instance = new MapMarkers();
        return instance;
    }

    private MapMarkers(){
        initBeaconMarker();
        initGreenMarker();
        initPositionMarker();
        initmapBeaconMarker();
    }

    private void initPositionMarker() {
        //init paint
        userLocationMarkerPaint = new Paint();
        userLocationMarkerBitmapConfig = Bitmap.Config.ARGB_8888;
        userLocationMarkerBitmap = Bitmap.createBitmap(40, 40, userLocationMarkerBitmapConfig);
        userLocationMarkerCanvas = new Canvas(userLocationMarkerBitmap);
        // paint circle
        userLocationMarkerPaint.setColor(Color.parseColor("#ffffff"));
        userLocationMarkerCanvas.drawCircle(20, 20, 20, userLocationMarkerPaint);
        userLocationMarkerPaint.setColor(Color.parseColor("#4285F4"));
        userLocationMarkerCanvas.drawCircle(20, 20, 16, userLocationMarkerPaint);
    }

    private void initGreenMarker() {
        //init paint
        greenMarkerPaint = new Paint();
        greenMarkerBitmapConfig = Bitmap.Config.ARGB_8888;
        greenMarkerBitmap = Bitmap.createBitmap(40, 40, greenMarkerBitmapConfig);
        greenMarkerCanvas = new Canvas(greenMarkerBitmap);
        // paint circle
        greenMarkerPaint.setColor(Color.parseColor("#ffffff"));
        greenMarkerCanvas.drawCircle(20, 20, 20, greenMarkerPaint);
        greenMarkerPaint.setColor(Color.parseColor("#8bff8f"));
        greenMarkerCanvas.drawCircle(20, 20, 16, greenMarkerPaint);
    }

    private void initBeaconMarker() {
        //init paint
        beaconMarkerPaint = new Paint();
        beaconMarkerBitmapConfig = Bitmap.Config.ARGB_8888;
        beaconMarkerBitmap = Bitmap.createBitmap(40, 40, beaconMarkerBitmapConfig);
        beaconMarkerCanvas = new Canvas(beaconMarkerBitmap);
        // paint circle
        beaconMarkerPaint.setColor(Color.parseColor("#ffffff"));
        beaconMarkerCanvas.drawCircle(20, 20, 20, beaconMarkerPaint);
        beaconMarkerPaint.setColor(Color.parseColor("#FFF48642"));
        beaconMarkerCanvas.drawCircle(20, 20, 16, beaconMarkerPaint);
    }

    private void initmapBeaconMarker() {
        //init paint
        mapBeaconMarkerPaint = new Paint();
        mapBeaconMarkerBitmapConfig = Bitmap.Config.ARGB_8888;
        mapBeaconMarkerBitmap = Bitmap.createBitmap(40, 40, mapBeaconMarkerBitmapConfig);
        mapBeaconMarkerCanvas = new Canvas(mapBeaconMarkerBitmap);
        // paint circle
        mapBeaconMarkerPaint.setColor(Color.parseColor("#ffffff"));
        mapBeaconMarkerCanvas.drawCircle(20, 20, 20, mapBeaconMarkerPaint);
        mapBeaconMarkerPaint.setColor(Color.parseColor("#FF838383"));
        mapBeaconMarkerCanvas.drawCircle(20, 20, 16, mapBeaconMarkerPaint);
    }
}
