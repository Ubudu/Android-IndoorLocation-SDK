package com.ubudu.gmaps.util;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

/**
 * Created by mgasztold on 05/10/16.
 */

public class MathUtils {

    public static final String TAG = MathUtils.class.getCanonicalName();

    public static LatLng getPolygonCenterPoint(List<LatLng> points) {
        double[] centroid = {0.0, 0.0};

        for (int i = 0; i < points.size(); i++) {
            centroid[0] += points.get(i).latitude;
            centroid[1] += points.get(i).longitude;
        }

        int totalPoints = points.size();
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;

        return new LatLng(centroid[0], centroid[1]);
    }

    /**
     * Check whether bounds1 intersects with bounds2.
     *
     * @param bounds1 The bounds to check
     * @param bounds2 The bounds to check
     * @return true if the given bounds intersect themselves, false otherwise
     */
    public static boolean intersects(LatLngBounds bounds1, LatLngBounds bounds2) {
        final boolean latIntersects = (bounds1.northeast.latitude >= bounds2.southwest.latitude) && (bounds1.southwest.latitude <= bounds2.northeast.latitude);
        final boolean lngIntersects = (bounds1.northeast.longitude >= bounds2.southwest.longitude) && (bounds1.southwest.longitude <= bounds2.northeast.longitude);
        return latIntersects && lngIntersects;
    }

}
