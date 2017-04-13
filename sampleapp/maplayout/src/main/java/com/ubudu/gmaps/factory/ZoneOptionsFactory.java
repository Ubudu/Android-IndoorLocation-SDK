package com.ubudu.gmaps.factory;

import com.ubudu.gmaps.util.ZoneOptions;

/**
 * Created by mgasztold on 11/01/2017.
 */

public class ZoneOptionsFactory {

    public static ZoneOptions defaultZoneOptions(){
        return new ZoneOptions();
    }

    public static ZoneOptions defaultHighlightedZoneOptions(){
        return new ZoneOptions()
                .zoneLabelOptions(ZoneLabelOptionsFactory.defaultHighlightLabelOptions())
                .polygonOptions(PolygonOptionsFactory.defaultHighlightPolygonOptions());
    }
}
