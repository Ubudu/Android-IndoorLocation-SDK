package com.ubudu.gmaps.factory;

import com.ubudu.gmaps.util.ZoneLabelOptions;

/**
 * Created by mgasztold on 11/01/2017.
 */
public class ZoneLabelOptionsFactory {

    public static ZoneLabelOptions defaultLabelOptions() {
        return new ZoneLabelOptions();
    }

    public static ZoneLabelOptions defaultHighlightLabelOptions() {
        return new ZoneLabelOptions()
                .labelSize(60);
    }
}
