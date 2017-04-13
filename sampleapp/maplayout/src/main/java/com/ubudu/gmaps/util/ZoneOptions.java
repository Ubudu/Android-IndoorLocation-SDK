package com.ubudu.gmaps.util;

import com.google.android.gms.maps.model.PolygonOptions;
import com.ubudu.gmaps.factory.PolygonOptionsFactory;

/**
 * Created by mgasztold on 11/01/2017.
 */

public class ZoneOptions {

    private PolygonOptions polygonOptions;
    private ZoneLabelOptions zoneLabelOptions;

    public ZoneOptions() {
        polygonOptions = PolygonOptionsFactory.defaultPolygonOptions();
        zoneLabelOptions = new ZoneLabelOptions();
    }

    public PolygonOptions getPolygonOptions() {
        return polygonOptions;
    }

    public ZoneLabelOptions getZoneLabelOptions() {
        return zoneLabelOptions;
    }

    public ZoneOptions polygonOptions(PolygonOptions polygonOptions) {
        this.polygonOptions = polygonOptions;
        return this;
    }

    public ZoneOptions zoneLabelOptions(ZoneLabelOptions zoneLabelOptions) {
        this.zoneLabelOptions = zoneLabelOptions;
        return this;
    }
}
