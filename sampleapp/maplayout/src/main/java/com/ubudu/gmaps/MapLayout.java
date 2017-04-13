package com.ubudu.gmaps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.ubudu.gmaps.factory.MarkerOptionsFactory;
import com.ubudu.gmaps.factory.MarkerOptionsStrategyFactory;
import com.ubudu.gmaps.model.Zone;
import com.ubudu.gmaps.util.CachingUrlTileProvider;
import com.ubudu.gmaps.util.MarkerOptionsStrategy;
import com.ubudu.gmaps.util.MarkerSearchPattern;
import com.ubudu.gmaps.util.MathUtils;
import com.ubudu.gmaps.util.Mercator;
import com.ubudu.gmaps.util.ZoneLabelOptions;
import com.ubudu.gmaps.util.ZoneOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mgasztold on 05/10/16.
 */
public class MapLayout extends RelativeLayout implements GoogleMap.OnPolygonClickListener
        , GoogleMap.OnMarkerClickListener {

    public static final String TAG = MapLayout.class.getCanonicalName();

    // ---------------------------------------------------------------------------------------------
    // CONSTANTS:

    private final static long ANIMATE_LOCATION_CHANGE_DURATION = 400; // ms
    private final static int DEFAULT_MAP_ZOOM = 19;
    private final static int TILES_OVERLAY_Z_INDEX = 1;
    public final static int ZONE_Z_INDEX = 2;
    private final static String TITLE_LOCATION_MARKER = "Current location";

    // ---------------------------------------------------------------------------------------------
    // PRIVATE VARIABLES:

    private MarkerOptionsStrategy locationMarkerOptionsStrategy;
    private MarkerOptionsStrategy markerOptionsStrategy;
    private Context mContext;
    private GoogleMap mGoogleMap;
    private Marker mLocationMarker;
    private static LatLng location;
    private static float lastBearing;
    private static float lastZoom;
    private TileOverlay mTileOverlay;
    private LatLngBounds mapBounds;
    private ConcurrentHashMap<Zone,Pair<Marker,Polygon>> zoneVsMarkerAndPolygonMap;
    private ConcurrentHashMap<com.ubudu.gmaps.model.Marker,Marker> customMarkersMap;
    private TileOverlayOptions mTileOverlayOptions;
    private EventListener eventListener;

    // ---------------------------------------------------------------------------------------------
    // CONSTRUCTORS:

    public MapLayout(Context context) {
        this(context, null);
    }

    public MapLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public MapLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    // ---------------------------------------------------------------------------------------------
    // API METHODS:

    /**
     * Ubudu Map Layout initialization
     * @param context application's context
     */
    public void init(Context context) {
        mContext = context;
        inflate(mContext, R.layout.layout_map, this);

        MapView mMapView = (MapView) findViewById(R.id.googlemapview);
        mMapView.onCreate(null);
        mMapView.onResume(); //without this, map showed but was empty
        final MapLayout thisMapLayout = this;
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.i(TAG,"Google Map instance ready.");
                mGoogleMap = googleMap;
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                //Disable Map Toolbar:
                mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
                mGoogleMap.setBuildingsEnabled(false);
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        lastZoom = mGoogleMap.getCameraPosition().zoom;
                    }
                });

                mGoogleMap.setOnMarkerClickListener(thisMapLayout);
                mGoogleMap.setOnPolygonClickListener(thisMapLayout);

                if(mTileOverlayOptions!=null) {
                    setTileOverlay();
                }

                if (location != null) {
                    removeLocationMarker();
                    markLocation(location);
                    updateCamera(false,lastZoom);
                }

                if(eventListener!=null)
                    eventListener.onMapReady();
            }
        });
    }

    /**
     *
     * @return Google Map
     */
    public GoogleMap getMap() {
        return mGoogleMap;
    }

    /**
     * Sets map layout event listener
     * @param listener listener instance
     */
    public void setEventListener(EventListener listener) {
        eventListener = listener;
    }

    /**
     * Marks location on map
     * @param latitude latitude in degrees
     * @param longitude longitude in degrees
     */
    public void markLocation(double latitude, double longitude){
        markLocation(new LatLng(latitude,longitude));
    }

    /**
     * Marks location on map
     * @param coordinates coordinates of the location to mark on the map
     * @return true if location has been successfully marked, false otherwise
     */
    public boolean markLocation(LatLng coordinates){
        try {
            if (mGoogleMap != null) {
                if (mLocationMarker == null) {
                    MarkerOptions markerOptions = getLocationMarkerOptionsStrategy().getNormalMarkerOptions();
                    markerOptions.position(coordinates);
                    markerOptions.title(TITLE_LOCATION_MARKER);
                    mLocationMarker = addMarkerToGoogleMap(markerOptions);

                } else if (location !=null
                        && location.latitude != coordinates.latitude
                        && location.longitude != coordinates.longitude) {
                    animateMarker(mLocationMarker, coordinates);
                }
                location = coordinates;
            }
            return true;
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            return false;
        }
    }

    /**
     * Adds zone to the map
     * @param zone zone to be added
     */
    public void addZone(Zone zone) {
        if (mGoogleMap == null)
            return;

        if (zoneVsMarkerAndPolygonMap != null && zoneVsMarkerAndPolygonMap.containsKey(zone.getName())) {
            Log.e(TAG, "Cannot add zone. Zone of name " + zone.getName() + " already exists");
            return;
        }

        if (zone.getCoords() == null || zone.getCoords().size() == 0) {
            Log.e(TAG, "Cannot add zone. Empty vertex coordinates list");
            return;
        }

        try {
            ZoneOptions zoneOptions = zone.getOptions();

            PolygonOptions rectOptions = zoneOptions.getPolygonOptions();
            rectOptions.addAll(zone.getCoords());
            // Get back the mutable Polygon
            Polygon polygon = mGoogleMap.addPolygon(rectOptions);

            ZoneLabelOptions zoneLabelOptions = zoneOptions.getZoneLabelOptions();
            Marker marker = null;
            MarkerOptions markerOptions = null;
            if(zoneLabelOptions.isDisplayLabel()) {
                markerOptions = zoneLabelOptions.getLabelMarkerOptions();
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getLabelBitmap(zone.getName(),zoneLabelOptions)));
            } else {
                markerOptions = zoneLabelOptions.getLabelMarkerOptions();
                if(markerOptions.getIcon()==null){
                    markerOptions = null;
                }
            }
            if(markerOptions!=null) {
                markerOptions.position(MathUtils.getPolygonCenterPoint(zone.getCoords()));
                markerOptions.title(zone.getName());
                marker = addMarkerToGoogleMap(markerOptions);
            }
            if (zoneVsMarkerAndPolygonMap == null)
                zoneVsMarkerAndPolygonMap = new ConcurrentHashMap<>();
            synchronized (zoneVsMarkerAndPolygonMap) {
                zoneVsMarkerAndPolygonMap.put(zone, new Pair<>(marker, polygon));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adds zone
     *
     * @param name name of the zone
     * @param coords coordinates list of the zone's edges
     */
    public void addZone(String name, List<LatLng> coords) {
        Zone zone = new Zone(name, coords);
        addZone(zone);
    }

    public void removeZone(String name) {
        if (zoneVsMarkerAndPolygonMap != null) {
            synchronized (zoneVsMarkerAndPolygonMap) {
                for (Zone zone : zoneVsMarkerAndPolygonMap.keySet()) {
                    if (zone.getName().equals(name)) {
                        Pair<Marker,Polygon> markerAndPolygon = zoneVsMarkerAndPolygonMap.get(zone);
                        Marker m = markerAndPolygon.first;
                        Polygon p = markerAndPolygon.second;
                        m.remove();
                        p.remove();
                        zoneVsMarkerAndPolygonMap.remove(zone);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Removes all zones
     */
    public void removeZones() {
        if (zoneVsMarkerAndPolygonMap != null) {
            synchronized (zoneVsMarkerAndPolygonMap) {
                for (Zone zone : zoneVsMarkerAndPolygonMap.keySet()) {
                    Pair<Marker,Polygon> markerAndPolygon = zoneVsMarkerAndPolygonMap.get(zone);
                    Marker m = markerAndPolygon.first;
                    Polygon p = markerAndPolygon.second;
                    m.remove();
                    p.remove();
                    zoneVsMarkerAndPolygonMap.remove(zone);
                }
            }
        }
    }

    /**
     *
     * @param bearing compass bearing
     */
    public void updateBearing(float bearing) {
        lastBearing = bearing;
    }

    /**
     * Sets tile overlay with the given base url
     * @param tilesBaseUrl tiles base url
     * @param southWestBound south west overlay bound coordinates
     * @param northEastBound north east overlay bound coordinates
     */
    public void addTileOverlay(final String tilesBaseUrl, LatLng southWestBound, LatLng northEastBound) {

        mapBounds = new LatLngBounds(
                southWestBound,       // South west image corner
                northEastBound);      // North east image corner

        if(tilesBaseUrl!=null) {

            mTileOverlayOptions = new CachingUrlTileProvider(mContext, 256, 256) {
                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    try {
                        int ymax = 1 << zoom;
                        int y_m = ymax - y - 1;
                        LatLng s_w = Mercator.fromPixelTo2DCoordinates(x * 256, (y + 1) * 256, zoom);
                        LatLng n_e = Mercator.fromPixelTo2DCoordinates((x + 1) * 256, y * 256, zoom);
                        LatLngBounds tileBounds = new LatLngBounds(new LatLng(s_w.latitude, s_w.longitude), new LatLng(n_e.latitude, n_e.longitude));
                        String noneUrl = "https://imagesd.ubudu.com/u_maps_tiles/none.png";
                        if (MathUtils.intersects(tileBounds, mapBounds)) {
                            return new URL(tilesBaseUrl.replace("{z}", "" + zoom).replace("{x}", "" + x)
                                    .replace("{y}", "" + y_m)).toString();
                        }
                        return noneUrl;

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.createTileOverlayOptions();

            if(mGoogleMap!=null) {
                setTileOverlay();
            }
        } else {
            mTileOverlayOptions = null;
            removeTilesOverlay();
        }
    }

    /**
     * Resets the map view by removing all markers, polygons and overlays
     */
    public void reset() {
        location = null;
        lastZoom = DEFAULT_MAP_ZOOM;
        lastBearing = 0;
        removeAllMarkers();
        removeZones();
        removeLocationMarker();
        removeTilesOverlay();
        if (mGoogleMap != null)
            mGoogleMap.clear();
    }

    /**
     * Updates camera
     */
    public void updateCamera(boolean shouldAnimate) {
        if(mGoogleMap!=null){
            if (mGoogleMap.getCameraPosition().zoom < DEFAULT_MAP_ZOOM) {
                updateCamera(shouldAnimate,DEFAULT_MAP_ZOOM);
            } else {
                updateCamera(shouldAnimate,mGoogleMap.getCameraPosition().zoom);
            }
        }
    }

    /**
     * Adds a marker to the map
     *
     * @param marker marker to be added
     *
     * @return true if marker has been successfully added, false otherwise
     */
    public boolean addMarker(com.ubudu.gmaps.model.Marker marker){
        if(marker.getLocation()==null)
            return false;
        if(customMarkersMap ==null)
            customMarkersMap = new ConcurrentHashMap<>();
        if(marker.getTitle()==null)
            marker.setTitle("");
        MarkerOptions markerOptions = marker.getMarkerOptionsStrategy().getNormalMarkerOptions();
        markerOptions.position(marker.getLocation());
        markerOptions.title(marker.getTitle());
        Marker customMarker = addMarkerToGoogleMap(markerOptions);
        if(customMarker!=null)
            customMarkersMap.put(marker,customMarker);
        return true;
    }

    /**
     * Adds a marker to the map
     *
     * @param tags marker tags
     * @param coordinates marker location
     * @param title marker title
     * @return true if marker has been successfully added, false otherwise
     */
    public boolean addMarker(List<String> tags, LatLng coordinates, String title){
        return addMarker(tags,coordinates,title, getMarkerOptionsStrategy());
    }

    /**
     * Adds a marker to the map
     *
     * @param tag marker tag
     * @param coordinates marker location
     * @param title marker title
     */
    public boolean addMarker(String tag, LatLng coordinates, String title){
        List<String> tags = new ArrayList<>();
        tags.add(tag);
        return addMarker(tags ,coordinates,title, getMarkerOptionsStrategy());
    }

    /**
     * Adds a marker to the map
     *
     * @param tag marker tag
     * @param coordinates marker location
     * @param title marker title
     * @param markerOptionsStrategy strategy for marker appearance
     * @return true if marker has been successfully added, false otherwise
     */
    public boolean addMarker(String tag, LatLng coordinates, String title,  MarkerOptionsStrategy markerOptionsStrategy){
        List<String> tags = new ArrayList<>();
        tags.add(tag);
        return addMarker(tags ,coordinates,title, markerOptionsStrategy);
    }

    /**
     *
     * Adds a marker to the map
     *
     * @param tags marker tags
     * @param coordinates marker location
     * @param title marker title
     * @param markerOptionsStrategy strategy for marker appearance
     * @return true if marker has been successfully added, false otherwise
     */
    public boolean addMarker(List<String> tags, LatLng coordinates, String title, MarkerOptionsStrategy markerOptionsStrategy){
        com.ubudu.gmaps.model.Marker marker = new com.ubudu.gmaps.model.Marker(title,coordinates);
        marker.setMarkerOptionsStrategy(markerOptionsStrategy);
        marker.setTags(tags);
        return addMarker(marker);
    }

    /**
     * Removes all custom markers from map
     *
     * @return number of removed markers
     */
    public int removeAllMarkers() {
        int removedCount = 0;
        if (customMarkersMap != null) {
            synchronized (customMarkersMap) {
                for(com.ubudu.gmaps.model.Marker marker : customMarkersMap.keySet()) {
                    customMarkersMap.get(marker).remove();
                    removedCount++;
                }
                customMarkersMap.clear();
                customMarkersMap = null;
            }
        }
        return removedCount;
    }

    /**
     * Note: To remove all markers put empty 'new MarkerSearchPattern()' as argument.
     *
     * @param searchPattern pattern
     * @return number of removed markers that were found according to the given search pattern
     */
    public int removeMarkers(MarkerSearchPattern searchPattern) {

        if(customMarkersMap ==null)
            return 0;

        if(searchPattern.getTags().size()==0 && searchPattern.getTitle().equals(""))
            return removeAllMarkers();

        int removedCount = 0;
        if (customMarkersMap != null) {
            synchronized (customMarkersMap) {
                for(com.ubudu.gmaps.model.Marker marker : customMarkersMap.keySet()) {

                    boolean matchingTags = true;
                    if(searchPattern.getTags().size()>0){
                        for(String tag : searchPattern.getTags()){
                            if(!marker.getTags().contains(tag)) {
                                matchingTags = false;
                                break;
                            }
                        }
                    }

                    boolean matchingTitle = true;
                    if(!searchPattern.getTitle().equals("")){
                        if(!marker.getTitle().equals(searchPattern.getTitle()))
                            matchingTitle = false;
                    }

                    if(matchingTitle && matchingTags) {
                        customMarkersMap.get(marker).remove();
                        customMarkersMap.remove(marker);
                        removedCount++;
                    }
                }
            }
        }
        return removedCount;
    }

    /**
     * Note: To get all markers as is put empty 'new MarkerSearchPattern()' as argument.
     *
     * @param searchPattern pattern
     * @return markers found according to the given search pattern
     */
    public List<Marker> findMarkers(MarkerSearchPattern searchPattern) {

        if(customMarkersMap ==null)
            return new ArrayList<>();

        if(searchPattern.getTags().size()==0 && searchPattern.getTitle().equals(""))
            return new ArrayList<>(customMarkersMap.values());

        List<Marker> markersMatchingTag = new ArrayList<>(customMarkersMap.values());
        if(searchPattern.getTags().size()>0)
            markersMatchingTag = new ArrayList<>(getMarkersWithTags(searchPattern.getTags()));

        if(searchPattern.getTitle().equals(""))
            return markersMatchingTag;

        Iterator<Marker> iter = markersMatchingTag.iterator();
        while (iter.hasNext()) {
            Marker marker = iter.next();
            if (!marker.getTitle().contains(searchPattern.getTitle()))
                iter.remove();
        }

        return markersMatchingTag;
    }

    /**
     *
     * @return location of the location marker
     */
    public LatLng getLocation(){
        return location;
    }

    /**
     *
     * @param markerOptionsStrategy marker options to be used for current location marker
     */
    public void setLocationMarkerOptionsStrategy(MarkerOptionsStrategy markerOptionsStrategy) {
        locationMarkerOptionsStrategy = markerOptionsStrategy;
        if(mLocationMarker !=null) {
            removeLocationMarker();
            markLocation(location);
        }
    }

    /**
     *
     * @param markerOptionsStrategy strategy to be used for marker appearance
     */
    public void setMarkerOptionsStrategy(MarkerOptionsStrategy markerOptionsStrategy) {
        this.markerOptionsStrategy = markerOptionsStrategy;
    }

    // ---------------------------------------------------------------------------------------------
    // PRIVATE METHODS:

    /**
     *
     * @param title title to look for
     * @return list of markers matching the given title
     */
    private List<Marker> getMarkersWithTitle(String title){
        List<Marker> result = new ArrayList<>();
        if (customMarkersMap != null) {
            synchronized (customMarkersMap) {
                for (com.ubudu.gmaps.model.Marker marker : customMarkersMap.keySet()) {
                    if(marker.getTitle().equals(title))
                        result.add(customMarkersMap.get(marker));
                }
            }
        }
        return result;
    }

    /**
     *
     * @param tags tags to look for
     * @return list of markers matching the given tag
     */
    private List<Marker> getMarkersWithTags(List<String> tags){
        List<Marker> result = new ArrayList<>();
        if (customMarkersMap != null) {
            synchronized (customMarkersMap) {
                for (com.ubudu.gmaps.model.Marker marker : customMarkersMap.keySet()) {
                    boolean markerMatchesTags = true;
                    for(String tag : tags) {
                        if (!marker.getTags().contains(tag)) {
                            markerMatchesTags = false;
                            break;
                        }
                    }
                    if(markerMatchesTags)
                        result.add(customMarkersMap.get(marker));
                }
            }
        }
        return result;
    }

    /**
     *
     * @param title title to look for
     * @param tags tags to look for
     * @return list of markers matching the given title
     */
    private List<Marker> getMarkersWithTitleAndTag(String title, List<String> tags) {
        List<Marker> markersMatchingTag = new ArrayList<>(getMarkersWithTags(tags));
        Iterator<Marker> iter = markersMatchingTag.iterator();
        while (iter.hasNext()) {
            Marker marker = iter.next();
            if (marker.getTitle().equals(title))
                iter.remove();
        }
        return markersMatchingTag;
    }

    /**
     * Sets the tile overlay to Google Map
     */
    private void setTileOverlay() {
        mTileOverlay = mGoogleMap.addTileOverlay(mTileOverlayOptions);
        mTileOverlay.setZIndex(TILES_OVERLAY_Z_INDEX);
    }

    /**
     * Updates camera
     * @param shouldAnimate flag indicating if the camera should be animated or moved immediately
     * @param zoom zoom level
     */
    private void updateCamera(boolean shouldAnimate, float zoom){

        if(location ==null)
            return;

        CameraPosition.Builder cameraPositionBuilder = new CameraPosition.Builder().target(location).bearing(lastBearing);

        cameraPositionBuilder.zoom(zoom);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPositionBuilder.build());

        if(shouldAnimate)
            mGoogleMap.animateCamera(cameraUpdate);
        else
            mGoogleMap.moveCamera(cameraUpdate);
    }

    /**
     * Removes tiles overlay
     */
    private void removeTilesOverlay() {
        if (mTileOverlay != null)
            mTileOverlay.remove();
    }

    /**
     * Removes location marker from map
     */
    private void removeLocationMarker() {
        if (mLocationMarker != null)
            mLocationMarker.remove();
        mLocationMarker = null;
    }

    /**
     * Adds new marker to google map.
     *
     * @param markerOptions marker options
     * @return reference to the added Marker
     */
    private Marker addMarkerToGoogleMap(MarkerOptions markerOptions) {
        if(mGoogleMap!=null)
            return mGoogleMap.addMarker(markerOptions);
        else return null;
    }

    /**
     * Animates marker to the new location
     * @param marker marker to move
     * @param toLocation target location of the marker
     */
    private void animateMarker(final Marker marker, final LatLng toLocation) {
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
                        / ANIMATE_LOCATION_CHANGE_DURATION);
                marker.setPosition(getNewPosition(t));
                if (t < 1.0)
                    handler.postDelayed(this, 16);
            }

            private LatLng getNewPosition(float t) {
                double lat = (toLocation.latitude - startLatLng.latitude) * t + startLatLng.latitude;
                double lng = (toLocation.longitude - startLatLng.longitude) * t + startLatLng.longitude;
                return new LatLng(lat, lng);
            }
        });
    }

    /**
     *
     * @return location marker appearance options
     */
    private MarkerOptionsStrategy getLocationMarkerOptionsStrategy() {
        if(locationMarkerOptionsStrategy ==null) {
            return new MarkerOptionsStrategy()
                    .setNormalMarkerOptions(MarkerOptionsFactory
                            .circleMarkerOptions(15,"#4285F4"));
        }
        return locationMarkerOptionsStrategy;
    }

    /**
     *
     * @return strategy to be used for marker appearance
     */
    private MarkerOptionsStrategy getMarkerOptionsStrategy() {
        if(markerOptionsStrategy ==null)
            markerOptionsStrategy = MarkerOptionsStrategyFactory.defaultMarkerOptionsStrategy();
        return markerOptionsStrategy;
    }

    /**
     * Refreshes the marker according to its appearance strategy.
     *
     * @param marker marker object
     */
    private void refreshMarker(com.ubudu.gmaps.model.Marker marker) {
        Marker m = customMarkersMap.get(marker);
        MarkerOptions mO = marker.getOptions();
        m.setIcon(mO.getIcon());
        m.setZIndex(mO.getZIndex());
        m.setVisible(mO.isVisible());
        m.setAlpha(mO.getAlpha());
        m.setDraggable(mO.isDraggable());
        m.setFlat(mO.isFlat());
        m.setAnchor(mO.getAnchorV(),mO.getAnchorU());
        m.setRotation(mO.getRotation());
        m.setSnippet(mO.getSnippet());
        m.setTitle(marker.getTitle());
    }

    /**
     * Refreshes the polygon and  label marker of the given zone according to zone's appearance options.
     *
     * @param zone zone object
     */
    private void refreshZone(Zone zone) {
        Pair<Marker,Polygon> markerAndPolygon = zoneVsMarkerAndPolygonMap.get(zone);
        ZoneOptions zoneOptions = zone.getOptions();
        //update polygon
        Polygon p = markerAndPolygon.second;
        PolygonOptions polygonOptions = zoneOptions.getPolygonOptions();
        p.setStrokeColor(polygonOptions.getStrokeColor());
        p.setStrokeWidth(polygonOptions.getStrokeWidth());
        p.setFillColor(polygonOptions.getFillColor());
        p.setGeodesic(polygonOptions.isGeodesic());
        p.setHoles(polygonOptions.getHoles());
        p.setVisible(polygonOptions.isVisible());
        p.setZIndex(polygonOptions.getZIndex());
        p.setClickable(polygonOptions.isClickable());

        Log.i(TAG,"updated polygon id: "+p.getId());

        //update label marker
        Marker m = markerAndPolygon.first;
        ZoneLabelOptions zoneLabelOptions = zoneOptions.getZoneLabelOptions();
        MarkerOptions mO = zoneLabelOptions.getLabelMarkerOptions();

        if(zoneLabelOptions.isDisplayLabel())
            mO.icon(BitmapDescriptorFactory.fromBitmap(getLabelBitmap(zone.getName(),zoneLabelOptions)));

        m.setIcon(mO.getIcon());
        m.setZIndex(mO.getZIndex());
        m.setVisible(mO.isVisible());
        m.setAlpha(mO.getAlpha());
        m.setAnchor(mO.getAnchorV(),mO.getAnchorU());
        m.setDraggable(mO.isDraggable());
        m.setFlat(mO.isFlat());
        m.setInfoWindowAnchor(mO.getInfoWindowAnchorV(),mO.getInfoWindowAnchorU());
        m.setRotation(mO.getRotation());
        m.setSnippet(mO.getSnippet());
        m.setTitle(zone.getName());
    }

    /**
     *
     * @param name name of the zone
     * @param zoneLabelOptions appearance options for the zone label marker
     * @return Bitmap with the zone label
     */
    private Bitmap getLabelBitmap(String name, ZoneLabelOptions zoneLabelOptions){
        Paint zonesLabelPaint = new Paint();
        zonesLabelPaint.setTextSize(zoneLabelOptions.getLabelSize());
        zonesLabelPaint.setColor(zoneLabelOptions.getLabelColor());
        zonesLabelPaint.setTextAlign(Paint.Align.LEFT);
        float zoneLabelBaseline = -zonesLabelPaint.ascent();

        int width = (int) (zonesLabelPaint.measureText(name) + 0.5f); // round
        int height = (int) (zoneLabelBaseline + zonesLabelPaint.descent() + 0.5f);

        Bitmap zoneLabelBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas zoneLabelCanvas = new Canvas(zoneLabelBitmap);
        zoneLabelCanvas.drawText(name, 0, zoneLabelBaseline, zonesLabelPaint);
        return zoneLabelBitmap;
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        for (Zone zone : zoneVsMarkerAndPolygonMap.keySet()) {
            if (zoneVsMarkerAndPolygonMap.get(zone).second.equals(polygon)) {
                zone.setHighLighted(!zone.isHighLighted());
                refreshZone(zone);
                if (eventListener != null)
                    eventListener.onZoneClicked(zone, polygon);
                break;
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // check if not position marker
        if(marker.getTitle().equals(TITLE_LOCATION_MARKER))
            return true;

        // check if zone label marker
        if(zoneVsMarkerAndPolygonMap!=null)
            for(Zone zone : zoneVsMarkerAndPolygonMap.keySet()) {
                if(zone.getName().equals(marker.getTitle())){
                    Pair<Marker,Polygon> markerAndPolygon = zoneVsMarkerAndPolygonMap.get(zone);
                    onPolygonClick(markerAndPolygon.second);
                    return true;
                }
            }

        // check marker click
        if(customMarkersMap!=null)
            for(com.ubudu.gmaps.model.Marker myMarker : customMarkersMap.keySet()) {
                if (customMarkersMap.get(myMarker).equals(marker)) {
                    myMarker.setHighLighted(!myMarker.isHighLighted());
                    refreshMarker(myMarker);
                    if (eventListener != null)
                        eventListener.onMarkerClicked(myMarker, marker);
                    return !myMarker.getMarkerOptionsStrategy().isInforWindowEnabled();
                }
            }
        return false;
    }

    public interface EventListener {
        void onMapReady();
        void onZoneClicked(Zone zone, Polygon polygon);
        void onMarkerClicked(com.ubudu.gmaps.model.Marker marker, Marker googleMarker);
    }
}
