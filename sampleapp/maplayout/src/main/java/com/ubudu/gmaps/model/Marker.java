package com.ubudu.gmaps.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ubudu.gmaps.util.MarkerOptionsStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mgasztold on 12/01/2017.
 */

public class Marker {

    private String title;
    private LatLng location;
    private MarkerOptionsStrategy markerOptionsStrategy;
    private boolean isHighLighted;
    private List<String> tags;

    public Marker(String title, LatLng location) {
        tags = new ArrayList<>();
        this.title = title;
        this.location = location;
        isHighLighted = false;
    }

    public void setTags(List<String> tags) {
        this.tags = new ArrayList<>(tags);
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public boolean hasTag(String tag){
        return tags.contains(tag);
    }

    public void removeTag(String tag){
        tags.remove(tag);
    }

    public List<String> getTags(){
        return tags;
    }

    public void setMarkerOptionsStrategy(MarkerOptionsStrategy markerOptionsStrategy) {
        this.markerOptionsStrategy = markerOptionsStrategy;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHighLighted() {
        return isHighLighted;
    }

    public void setHighLighted(boolean highLighted) {
        isHighLighted = highLighted;
    }

    public LatLng getLocation() {
        return location;
    }

    public MarkerOptionsStrategy getMarkerOptionsStrategy() {
        return markerOptionsStrategy;
    }

    public MarkerOptions getOptions() {
        if(isHighLighted)
            return markerOptionsStrategy.getHighlightedMarkerOptions();
        else
            return markerOptionsStrategy.getNormalMarkerOptions();
    }
}
