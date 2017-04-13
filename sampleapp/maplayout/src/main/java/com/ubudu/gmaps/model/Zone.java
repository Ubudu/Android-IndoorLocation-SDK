package com.ubudu.gmaps.model;

import com.google.android.gms.maps.model.LatLng;
import com.ubudu.gmaps.util.ZoneOptions;
import com.ubudu.gmaps.util.ZoneOptionsStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mgasztold on 11/01/2017.
 */

public class Zone {

    private String name;
    private List<LatLng> coords;
    private ZoneOptionsStrategy zoneOptionsStrategy;
    private boolean isHighLighted;

    public Zone(String name, List<LatLng> coords) {
        this.name = name;
        this.coords = new ArrayList<>(coords);
        zoneOptionsStrategy = new ZoneOptionsStrategy();
        isHighLighted = false;
    }

    public boolean isHighLighted() {
        return isHighLighted;
    }

    public void setHighLighted(boolean highLighted) {
        isHighLighted = highLighted;
    }

    public String getName() {
        return name;
    }

    public List<LatLng> getCoords() {
        return coords;
    }

    public void setOptionsStrategy(ZoneOptionsStrategy zoneOptionsStrategy) {
        this.zoneOptionsStrategy = zoneOptionsStrategy;
    }

    public ZoneOptions getOptions() {
        if(isHighLighted)
            return zoneOptionsStrategy.getHighlightedZoneOptions();
        else
            return zoneOptionsStrategy.getNormalZoneOptions();
    }
}
