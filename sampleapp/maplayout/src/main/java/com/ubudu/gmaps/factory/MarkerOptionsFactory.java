package com.ubudu.gmaps.factory;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by mgasztold on 10/01/2017.
 */

public class MarkerOptionsFactory {

    public static MarkerOptions circleMarkerOptions(int radius, int color){
        return circleMarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(MarkerBitmapFactory.getMarkerBitmap(radius,color)));
    }

    public static MarkerOptions circleMarkerOptions(int radius, String color){
        return circleMarkerOptions(radius,Color.parseColor(color));
    }

    public static MarkerOptions circleWithHaloMarkerOptions(int radius, int color, int haloRadius, int haloColor){
        return circleMarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(MarkerBitmapFactory.getMarkerWithHaloBitmap(radius,color,haloRadius,haloColor)));
    }

    public static MarkerOptions circleWithHaloMarkerOptions(int radius, String color, int haloRadius, String haloColor){
        return circleWithHaloMarkerOptions(radius, Color.parseColor(color),haloRadius,Color.parseColor(haloColor));
    }

    public static MarkerOptions bitmapMarkerOptions(Bitmap bitmap){
        return circleMarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(MarkerBitmapFactory.getMarkerWithHaloBitmap(bitmap,0,0)));
    }

    public static MarkerOptions bitmapWithHaloMarkerOptions(Bitmap bitmap, int haloRadius, String haloColor){
        return bitmapWithHaloMarkerOptions(bitmap,haloRadius,Color.parseColor(haloColor));
    }

    public static MarkerOptions bitmapWithHaloMarkerOptions(Bitmap bitmap, int haloRadius, int haloColor){
        return circleMarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(MarkerBitmapFactory.getMarkerWithHaloBitmap(bitmap,haloRadius,haloColor)));
    }

    public static MarkerOptions defaultMarkerOptions(){
        return new MarkerOptions();
    }

    public static MarkerOptions circleMarkerOptions(){
        return new MarkerOptions()
                .anchor(0.5f, 0.5f);
    }
}
