package com.ubudu.gmaps.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mgasztold on 18/01/2017.
 */

public class MarkerSearchPattern {

    String title;
    List<String> tags;

    public MarkerSearchPattern(){
        title = "";
        tags = new ArrayList<>();
    }

    public MarkerSearchPattern title(String title) {
        this.title = title;
        return this;
    }

    public MarkerSearchPattern tags(List<String> tags) {
        for(String tag : tags) {
            if(!this.tags.contains(tag))
                this.tags.add(tag);
        }
        return this;
    }

    public MarkerSearchPattern tag(String tag) {
        if(!tags.contains(tag))
            tags.add(tag);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getTags() {
        return tags;
    }
}
