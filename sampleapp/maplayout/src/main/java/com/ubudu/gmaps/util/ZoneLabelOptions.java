package com.ubudu.gmaps.util;

import android.graphics.Color;

import com.google.android.gms.maps.model.MarkerOptions;
import com.ubudu.gmaps.factory.MarkerOptionsFactory;
import com.ubudu.gmaps.factory.ZoneLabelOptionsFactory;

/**
 * Created by mgasztold on 11/01/2017.
 */

public class ZoneLabelOptions {

    private boolean displayLabel;
    private MarkerOptions labelMarkerOptions;
    private int labelSize;
    private int labelColor;

    public ZoneLabelOptions() {
        displayLabel = true;
        labelMarkerOptions = MarkerOptionsFactory.circleMarkerOptions();
        labelSize = 40;
        labelColor = Color.BLACK;
    }

    public boolean isDisplayLabel() {
        return displayLabel;
    }

    public ZoneLabelOptions displayLabel(boolean displayLabel) {
        this.displayLabel = displayLabel;
        return this;
    }

    public MarkerOptions getLabelMarkerOptions() {
        return labelMarkerOptions;
    }

    public ZoneLabelOptions labelMarkerOptions(MarkerOptions labelMarkerOptions) {
        this.labelMarkerOptions = labelMarkerOptions;
        return this;
    }

    public int getLabelSize() {
        return labelSize;
    }

    public ZoneLabelOptions labelSize(int labelSize) {
        this.labelSize = labelSize;
        return this;
    }

    public int getLabelColor() {
        return labelColor;
    }

    public ZoneLabelOptions labelColor(int labelColor) {
        this.labelColor = labelColor;
        return this;
    }

}
