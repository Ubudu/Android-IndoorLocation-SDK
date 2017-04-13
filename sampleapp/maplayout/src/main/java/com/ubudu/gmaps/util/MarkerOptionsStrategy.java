package com.ubudu.gmaps.util;

import com.google.android.gms.maps.model.MarkerOptions;
import com.ubudu.gmaps.factory.MarkerOptionsFactory;

/**
 * Created by mgasztold on 11/01/2017.
 */

public class MarkerOptionsStrategy {

    MarkerOptions normalMarkerOptions;
    MarkerOptions highlightedMarkerOptions;
    boolean inforWindowEnabled;

    public MarkerOptionsStrategy() {
        normalMarkerOptions = MarkerOptionsFactory.circleMarkerOptions();
        highlightedMarkerOptions = normalMarkerOptions;
        inforWindowEnabled = true;
    }

    public MarkerOptions getNormalMarkerOptions() {
        return normalMarkerOptions;
    }

    public MarkerOptionsStrategy setNormalMarkerOptions(MarkerOptions normalMarkerOptions) {
        this.normalMarkerOptions = normalMarkerOptions;
        highlightedMarkerOptions = normalMarkerOptions;
        return this;
    }

    public MarkerOptions getHighlightedMarkerOptions() {
        return highlightedMarkerOptions;
    }

    public MarkerOptionsStrategy setHighlightedMarkerOptions(MarkerOptions highlightedMarkerOptions) {
        this.highlightedMarkerOptions = highlightedMarkerOptions;
        return this;
    }

    public boolean isInforWindowEnabled() {
        return inforWindowEnabled;
    }

    public MarkerOptionsStrategy setInforWindowEnabled(boolean inforWindowEnabled) {
        this.inforWindowEnabled = inforWindowEnabled;
        return this;
    }
}
