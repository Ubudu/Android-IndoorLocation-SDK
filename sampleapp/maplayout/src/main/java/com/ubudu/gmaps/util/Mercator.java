package com.ubudu.gmaps.util;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by mgasztold on 11/04/16.
 */
public class Mercator {
    //offset is defined at zoom level 21 which means that this Mercator is valid for 21 zoom levels
    private static double OFFSET = 268435456d;
    private static double RADIUS = OFFSET/ Math.PI;

    public static double xToLng(int x, int zoom) {
        return ((double)((x<<(21 - zoom)) - Mercator.OFFSET)/Mercator.RADIUS)*180.0/Math.PI;
    }

    public static double yToLat(int y, int zoom) {
        return (Math.PI/2-2*Math.atan(Math.exp((double)((y<<(21 - zoom))-Mercator.OFFSET)/Mercator.RADIUS)))*180.0/Math.PI;
    }

    /**
     *
     * @param x x cartesian coordinate
     * @param y y cartesian coordinate
     * @param zoom zoom level
     * @return coordnates in degrees
     */
    public static LatLng fromPixelTo2DCoordinates(int x, int y, int zoom) {
        double lng= xToLng(x, zoom);
        double lat= yToLat(y, zoom);
        return new LatLng(lat, lng);
    }
}

