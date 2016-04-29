package com.ubudu.sampleapp.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ubudu.indoorlocation.UbuduBeacon;
import com.ubudu.indoorlocation.UbuduCoordinates2D;
import com.ubudu.sampleapp.map.storage.AppFileSystemUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Map implements GoogleMap.OnMapLoadedCallback {

    private int DEFAULT_MAP_ZOOM = 19;
    private final long ANIMATE_POSITION_CHANGE_DURATION = 500; // ms


    private GoogleMap mGoogleMap;

    protected MapEventListener mMapEventListener;

    private LatLng currentLocation;

    private Marker mCurrentLocationMarker;
    private List<Marker> activeBeaconsMarkers;
    private List<Marker> mapBeaconsMarkers;

    private List<Polygon> zonesPolygons;
    private List<Marker> zonesMarkers;

    private List<Polyline> pathPolylines;

    private Context mContext;

    private MapMarkers markers;

    private float currentCompassBearing;

    private String overlayUuid;

    private Marker clickedPositionMarker;

    private LatLngBounds mapBounds;

    /**
     * Constructor
     *
     * @param eventListener listener
     * @param ctx           application context
     */
    public Map(MapEventListener eventListener, Context ctx) {
        markers = MapMarkers.getInstance();
        mMapEventListener = eventListener;
        mContext = ctx;
        initOnMapLoadedCallback();
    }

    private void initOnMapLoadedCallback() {
        final Map instance = this;
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mGoogleMap != null) {
                    mGoogleMap.setOnMapLoadedCallback(instance);
                } else
                    handler.postDelayed(this, 500);
            }
        });
    }

    public void setMapBeaconsMarkers(final List<UbuduBeacon> mapBeacons) {
        if (mGoogleMap != null) {
            clearMapBeconsMarkers();
            Iterator<UbuduBeacon> iter = mapBeacons.iterator();
            while (iter.hasNext()) {
                UbuduBeacon b = iter.next();
                if (b.geographicalPosition() != null) {
                    LatLng latLng = new LatLng(b.geographicalPosition().latitude(), b.geographicalPosition().longitude());
                    String title = "major: " + b.major() + ", minor: " + b.minor();
                    addMarker(latLng, title, markers.mapBeaconMarkerBitmap);
                }
            }
        }
    }

    private void addMarker(final LatLng latLng, final String title, final Bitmap bitmap) {
        try {
            if (mGoogleMap != null) {
                if (mapBeaconsMarkers == null)
                    mapBeaconsMarkers = new ArrayList<>();
                synchronized (mapBeaconsMarkers) {
                    mapBeaconsMarkers.add(mGoogleMap.addMarker(new MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .position(latLng)
                            .title(title)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearMapBeconsMarkers() {
        if (mapBeaconsMarkers != null) {
            synchronized (mapBeaconsMarkers) {
                Iterator<Marker> iter = mapBeaconsMarkers.iterator();
                while (iter.hasNext()) {
                    Marker m = iter.next();
                    m.remove();
                    iter.remove();
                }
            }
        }
    }

    private void clearActiveBeaconsMarkers() {
        if (activeBeaconsMarkers != null) {
            synchronized (activeBeaconsMarkers) {
                Iterator<Marker> iter = activeBeaconsMarkers.iterator();
                while (iter.hasNext()) {
                    Marker m = iter.next();
                    m.remove();
                    iter.remove();
                }
            }
        }
    }

    public void setActiveBeaconsMarkers(final List<UbuduBeacon> activeBeaconsPositions) {
        removeDissapearedBeaconsMarkers(activeBeaconsPositions);
        addNewActiveBeaconsMarkers(activeBeaconsPositions);
    }

    private void removeDissapearedBeaconsMarkers(List<UbuduBeacon> beaconsPositions) {
        if (activeBeaconsMarkers == null)
            activeBeaconsMarkers = new ArrayList<>();
        synchronized (activeBeaconsMarkers) {
            Iterator<Marker> currentMarkersIter = activeBeaconsMarkers.iterator();
            while (currentMarkersIter.hasNext()) {
                Marker m = currentMarkersIter.next();
                boolean shouldBeDisplayed = false;
                Iterator<UbuduBeacon> beaconIter = beaconsPositions.iterator();
                while (beaconIter.hasNext()) {
                    UbuduBeacon b = beaconIter.next();
                    String title = "major: " + b.major() + ", minor: " + b.minor();
                    if (m.getTitle().equals(title)) {
                        shouldBeDisplayed = true;
                    }
                }
                if (!shouldBeDisplayed) {
                    m.remove();
                    currentMarkersIter.remove();
                }
            }
        }
    }

    private void addNewActiveBeaconsMarkers(List<UbuduBeacon> activeBeaconsPositions) {
        try {
            int numGreen = 0;
            Iterator<UbuduBeacon> newActiveBeaconsPositionsIter = activeBeaconsPositions.iterator();
            while (newActiveBeaconsPositionsIter.hasNext()) {
                UbuduBeacon b = newActiveBeaconsPositionsIter.next();
                if (b.geographicalPosition() != null) {
                    if (mGoogleMap != null) {
                        String title = "major: " + b.major() + ", minor: " + b.minor();

                        BitmapDescriptor iconBitmapDescriptor;
                        if (b.accuracy() < 13.0 && numGreen<4) {
                            iconBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(markers.greenMarkerBitmap);
                            numGreen++;
                        } else
                            iconBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(markers.beaconMarkerBitmap);

                        if (activeBeaconsMarkers == null)
                            activeBeaconsMarkers = new ArrayList<>();

                        boolean shouldBeAdded = true;
                        // check if marker for this beacon already exists. If it exists adjust its bitmap and return
                        Iterator<Marker> markerIter = activeBeaconsMarkers.iterator();
                        while (markerIter.hasNext()) {
                            Marker m = markerIter.next();
                            if (m.getTitle().equals(title)) {
                                m.setIcon(iconBitmapDescriptor);
                                shouldBeAdded = false;
                            }
                        }
                        // If the marker is not present on the map, add it:
                        if (shouldBeAdded) {
                            synchronized (activeBeaconsMarkers) {
                                activeBeaconsMarkers.add(mGoogleMap.addMarker(new MarkerOptions()
                                        .anchor(0.5f, 0.5f)
                                        .position(new LatLng(b.geographicalPosition().latitude(), b.geographicalPosition().longitude()))
                                        .title(title)
                                        .icon(iconBitmapDescriptor)));
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException npe){
            npe.printStackTrace();
        }
    }

    public void onDestroy() {
        clearMap();
    }

    private void clearMap() {
        if (mGoogleMap != null)
            mGoogleMap.clear();
    }

    public void setReferenceToGoogleMap(GoogleMap mGoogleMap) {
        this.mGoogleMap = mGoogleMap;
    }

    public void reset() {
        currentLocation = null;
        clearCurrentLocationMarker();
        clearMapBeconsMarkers();
        clearActiveBeaconsMarkers();
        clearHighlightedZones();
        clearPathPolyline();
        clearMap();
    }

    private void clearCurrentLocationMarker() {
        if(mCurrentLocationMarker!=null)
            mCurrentLocationMarker.remove();
        mCurrentLocationMarker = null;
    }

    private void clearPathPolyline() {
        if (pathPolylines != null)
            synchronized (pathPolylines) {
                Iterator<Polyline> iter = pathPolylines.iterator();
                while (iter.hasNext()) {
                    Polyline p = iter.next();
                    p.remove();
                    iter.remove();
                }
            }
    }

    public void bearing(float bearing) {
        currentCompassBearing = bearing;
    }

    @Override
    public void onMapLoaded() {
        if (mGoogleMap != null) {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            initZoneLabelMarker();

            mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    marker.hideInfoWindow();
                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    LatLng pos = marker.getPosition();
                    marker.setTitle("lat: " + pos.latitude + ", lng: "+pos.longitude);
                }
            });

            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if(clickedPositionMarker!=null){
                        clickedPositionMarker.remove();
                    }
                    clickedPositionMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(latLng).title("lat: " + latLng.latitude + ", lng: "+latLng.longitude));
                    clickedPositionMarker.setDraggable(true);
                }
            });
        }
    }

    public boolean setLocationOnMap(double latitude, double longitude) {
        try {
            if (mGoogleMap != null) {
                LatLng prev = currentLocation;
                currentLocation = new LatLng(latitude, longitude);
                synchronized (this) {
                    if (mCurrentLocationMarker == null) {
                        mCurrentLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                                .anchor(0.5f, 0.5f)
                                .position(currentLocation).title("Current Position")
                                .icon(BitmapDescriptorFactory.fromBitmap(markers.userLocationMarkerBitmap)));
                    } else if (prev.latitude != currentLocation.latitude && prev.longitude != currentLocation.longitude) {
                        animateMarker(mCurrentLocationMarker, currentLocation, false);
                    }
                }
                updateCamera();
            }
            return true;
        } catch(NullPointerException npe){
            npe.printStackTrace();
            return false;
        }
    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mGoogleMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / ANIMATE_POSITION_CHANGE_DURATION);
                marker.setPosition(getNewPosition(t));
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
            private LatLng getNewPosition(float t){
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                return new LatLng(lat,lng);
            }

        });
    }

    private void updateCamera() {
        if (mGoogleMap.getCameraPosition().zoom < DEFAULT_MAP_ZOOM) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(currentLocation)
                    .bearing(currentCompassBearing).zoom(DEFAULT_MAP_ZOOM).build()));
        } else {
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(currentLocation)
                    .bearing(currentCompassBearing).zoom(mGoogleMap.getCameraPosition().zoom).build()));
        }
    }

    private Canvas zoneLabelCanvas;
    private float zoneLabelBaseline;
    private Paint zonesLabelPaint;
    private Bitmap zoneLabelBitmap;

    private void initZoneLabelMarker() {
        float textSize = 40;
        zonesLabelPaint = new Paint();
        zonesLabelPaint.setTextSize(textSize);
        zonesLabelPaint.setColor(Color.BLACK);
        zonesLabelPaint.setTextAlign(Paint.Align.LEFT);
        zoneLabelBaseline = -zonesLabelPaint.ascent(); // ascent() is negative
    }

    public void highlightZone(List<LatLng> coords, String name) {
        if(zonesLabelPaint!=null) {
            PolygonOptions rectOptions = new PolygonOptions()
                    .addAll(new ArrayList<>(coords))
                    .fillColor(0x80C5E1A5)
                    .strokeWidth(0)
                    .geodesic(false);

            if (zonesPolygons == null)
                zonesPolygons = new ArrayList<>();
            // Get back the mutable Polygon
            synchronized (zonesPolygons) {
                zonesPolygons.add(mGoogleMap.addPolygon(rectOptions));
            }

            int width = (int) (zonesLabelPaint.measureText(name) + 0.5f); // round
            int height = (int) (zoneLabelBaseline + zonesLabelPaint.descent() + 0.5f);

            zoneLabelBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            zoneLabelCanvas = new Canvas(zoneLabelBitmap);
            zoneLabelCanvas.drawText(name, 0, zoneLabelBaseline, zonesLabelPaint);

            MarkerOptions options = new MarkerOptions().position(centroid(coords))
                    .icon(BitmapDescriptorFactory.fromBitmap(zoneLabelBitmap));

            if (zonesMarkers == null)
                zonesMarkers = new ArrayList<>();
            zonesMarkers.add(mGoogleMap.addMarker(options));
        }
    }

    public static LatLng centroid(List<LatLng> points) {
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

    public void clearHighlightedZones() {
        if (zonesPolygons != null) {
            synchronized (zonesPolygons) {
                Iterator<Polygon> iter = zonesPolygons.iterator();
                while (iter.hasNext()) {
                    Polygon p = iter.next();
                    p.remove();
                    iter.remove();
                }
            }
        }
        if (zonesMarkers != null) {
            synchronized (zonesMarkers) {
                Iterator<Marker> iter2 = zonesMarkers.iterator();
                while (iter2.hasNext()) {
                    Marker m = iter2.next();
                    m.remove();
                    iter2.remove();
                }
            }
        }
    }

    public void drawPath(List<LatLng> path) {
        if (pathPolylines == null) {
            pathPolylines = new ArrayList<>();
        }
        if (pathPolylines.size() > 0) {
            Polyline p = pathPolylines.get(0);
            p.remove();
            pathPolylines.remove(p);
        }
        pathPolylines.add(mGoogleMap.addPolyline(new PolylineOptions()
                .addAll(path)
                .width(5)
                .color(0x803781CA)));
    }

    public String getLoadedMapUuid() {
        return overlayUuid;
    }

    public void setLoadedMapUuid(String newUuid){
        overlayUuid = newUuid;
    }

    public void updateBearing(float azimuth) {
        currentCompassBearing = azimuth;
    }

    public void onPause() {

    }

    public void onResume() {

    }

    public void setTilesBaseUrl(final String tilesUrlBase) {
        mGoogleMap.addTileOverlay(new CachingUrlTileProvider(mContext, 256, 256) {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                try {
                    int ymax = 1 << zoom;
                    int y_m = ymax-y-1;
                    UbuduCoordinates2D s_w =  Mercator.fromPixelTo2DCoordinates(x*256, (y+1)*256, zoom);
                    UbuduCoordinates2D n_e =  Mercator.fromPixelTo2DCoordinates((x+1)*256, y*256, zoom);
                    LatLngBounds tileBounds = new LatLngBounds(new LatLng(s_w.latitude(),s_w.longitude()), new LatLng(n_e.latitude(),n_e.longitude()));
                    String url = "http://imagescdn.ubudu.com/u_maps_tiles/none.png";
                    if(intersects(tileBounds, mapBounds)){
                        return new URL(tilesUrlBase.replace("{z}", "" + zoom).replace("{x}", "" + x)
                                .replace("{y}", "" + y_m)).toString();
                    }
                    return url;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.createTileOverlayOptions()).setZIndex(-1f);
    }

    /**
     * Check whether bounds1 intersects with bounds2.
     *
     * @param bounds1 The bounds to check
     * @param bounds2 The bounds to check
     * @return true if the given bounds intersect themselves, false otherwise
     */
    public boolean intersects(LatLngBounds bounds1, LatLngBounds bounds2) {
        final boolean latIntersects = (bounds1.northeast.latitude >= bounds2.southwest.latitude) && (bounds1.southwest.latitude <= bounds2.northeast.latitude);
        final boolean lngIntersects = (bounds1.northeast.longitude >= bounds2.southwest.longitude) && (bounds1.southwest.longitude <= bounds2.northeast.longitude);
        return latIntersects && lngIntersects;
    }

    public void setMapOverlayBounds(LatLng southWest, LatLng northEast) {
        mapBounds = new LatLngBounds(
                southWest,       // South west image corner
                northEast);      // North east image corner
    }
}
