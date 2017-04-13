package com.ubudu.gmaps.factory;

import android.graphics.Color;

import com.google.android.gms.maps.model.PolygonOptions;
import com.ubudu.gmaps.MapLayout;

/**
 * Created by mgasztold on 11/01/2017.
 */
public class PolygonOptionsFactory {

    public static PolygonOptions defaultPolygonOptions() {
        return new PolygonOptions()
                .fillColor(Color.parseColor("#306ed88a"))
                .strokeWidth(0)
                .strokeColor(Color.BLACK)
                .geodesic(false)
                .clickable(true)
                .zIndex(MapLayout.ZONE_Z_INDEX);
    }

    public static PolygonOptions defaultHighlightPolygonOptions() {
        return new PolygonOptions()
                .fillColor(Color.parseColor("#80C5E1A5"))
                .strokeWidth(10)
                .strokeColor(Color.BLACK)
                .geodesic(false)
                .clickable(true)
                .zIndex(MapLayout.ZONE_Z_INDEX);
    }
}
