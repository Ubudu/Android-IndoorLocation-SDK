package com.ubudu.gmaps.util;

import com.ubudu.gmaps.factory.ZoneOptionsFactory;

/**
 * Created by mgasztold on 11/01/2017.
 */

public class ZoneOptionsStrategy {

    private ZoneOptions normalZoneOptions;
    private ZoneOptions highlightedZoneOptions;

    public ZoneOptionsStrategy(){
        normalZoneOptions = ZoneOptionsFactory.defaultZoneOptions();
        highlightedZoneOptions = ZoneOptionsFactory.defaultHighlightedZoneOptions();
    }

    public ZoneOptions getNormalZoneOptions() {
        return normalZoneOptions;
    }

    public ZoneOptionsStrategy normalZoneOptions(ZoneOptions normalZoneOptions) {
        this.normalZoneOptions = normalZoneOptions;
        return this;
    }

    public ZoneOptions getHighlightedZoneOptions() {
        return highlightedZoneOptions;
    }

    public ZoneOptionsStrategy highlightedZoneOptions(ZoneOptions highlightedZoneOptions) {
        this.highlightedZoneOptions = highlightedZoneOptions;
        return this;
    }
}
