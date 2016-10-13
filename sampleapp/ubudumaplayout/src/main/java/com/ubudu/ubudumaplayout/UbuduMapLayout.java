package com.ubudu.ubudumaplayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
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
import com.ubudu.ubudumaplayout.util.CachingUrlTileProvider;
import com.ubudu.ubudumaplayout.util.MapMarkers;
import com.ubudu.ubudumaplayout.util.MathUtils;
import com.ubudu.ubudumaplayout.util.Mercator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by mgasztold on 05/10/16.
 */
public class UbuduMapLayout extends RelativeLayout {

    public static final String TAG = UbuduMapLayout.class.getCanonicalName();

    // ---------------------------------------------------------------------------------------------
    // CONSTANTS:

    private final long ANIMATE_POSITION_CHANGE_DURATION = 500; // ms
    private final int DEFAULT_MAP_ZOOM = 17;
    private final int TILES_OVERLAY_Z_INDEX = 1;
    private final int INDOOR_LOCATION_ZONES_Z_INDEX = 2;

    // ---------------------------------------------------------------------------------------------
    // PRIVATE VARIABLES:

    private Context mContext;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private Marker mCurrentLocationMarker;
    private static LatLng lastPosition;
    private static float lastBearing;
    private static float lastZoom;
    private TileOverlay mTileOverlay;
    private LatLngBounds mapBounds;
    private List<Polygon> indoorLocationZonesPolygons;
    private List<Marker> indoorLocationZonesLabels;
    private Map<String,List<Marker>> customMarkersMap;
    private TileOverlayOptions mTileOverlayOptions;

    // ---------------------------------------------------------------------------------------------
    // CONSTRUCTORS:

    public UbuduMapLayout(Context context) {
        this(context, null);
    }

    public UbuduMapLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public UbuduMapLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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

        mMapView = (MapView) findViewById(R.id.googlemapview);
        mMapView.onCreate(null);
        mMapView.onResume(); //without this, map showed but was empty

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.i(TAG,"Google Map instance ready.");
                mGoogleMap = googleMap;
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mGoogleMap.setBuildingsEnabled(false);
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        lastZoom = mGoogleMap.getCameraPosition().zoom;
                    }
                });

                if(mTileOverlayOptions!=null) {
                    setTileOverlay();
                }

                if (lastPosition != null) {
                    clearPositionMarker();
                    markPosition(lastPosition);
                    updateCamera(false,lastZoom);
                }
            }
        });
    }

    /**
     * Marks position on map
     * @param latitude position latitude
     * @param longitude position longitude
     */
    public void markPosition(double latitude, double longitude){
        markPosition(new LatLng(latitude,longitude));
    }

    /**
     * Marks position on map
     * @param coordinates coordinates of the position to mark on the map
     * @return true if position has been successfully marked, false otherwise
     */
    public boolean markPosition(LatLng coordinates){
        try {
            if (mGoogleMap != null) {
                if (mCurrentLocationMarker == null) {
                    mCurrentLocationMarker = addMarkerToGoogleMap(coordinates,"Current Position",MapMarkers.getMarkerBitmap(30,"#4285F4"));
                } else if (lastPosition!=null
                        && lastPosition.latitude != coordinates.latitude
                        && lastPosition.longitude != coordinates.longitude) {
                    animateMarker(mCurrentLocationMarker, coordinates);
                }
                lastPosition = coordinates;
            }
            return true;
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            return false;
        }
    }

    /**
     * Highlights indoor location zone
     *
     * @param coords coordinates list of the zone's edges
     * @param name name of the zone
     * @param color zone's color
     */
    public void highlightZone(List<LatLng> coords, String name, int color) {
        if(mGoogleMap==null)
            return;

        if(color==0)
            color = 0x80C5E1A5;

        try {

            PolygonOptions rectOptions = new PolygonOptions()
                    .addAll(new ArrayList<>(coords))
                    .fillColor(color)
                    .strokeWidth(0)
                    .geodesic(false)
                    .zIndex(INDOOR_LOCATION_ZONES_Z_INDEX);

            if (indoorLocationZonesPolygons == null)
                indoorLocationZonesPolygons = new ArrayList<>();
            // Get back the mutable Polygon
            synchronized (indoorLocationZonesPolygons) {
                indoorLocationZonesPolygons.add(mGoogleMap.addPolygon(rectOptions));
            }

            float textSize = 40;
            Paint zonesLabelPaint = new Paint();
            zonesLabelPaint.setTextSize(textSize);
            zonesLabelPaint.setColor(Color.BLACK);
            zonesLabelPaint.setTextAlign(Paint.Align.LEFT);
            float zoneLabelBaseline = -zonesLabelPaint.ascent();

            int width = (int) (zonesLabelPaint.measureText(name) + 0.5f); // round
            int height = (int) (zoneLabelBaseline + zonesLabelPaint.descent() + 0.5f);

            Bitmap zoneLabelBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas zoneLabelCanvas = new Canvas(zoneLabelBitmap);
            zoneLabelCanvas.drawText(name, 0, zoneLabelBaseline, zonesLabelPaint);

            if (indoorLocationZonesLabels == null)
                indoorLocationZonesLabels = new ArrayList<>();
            indoorLocationZonesLabels.add(addMarkerToGoogleMap(MathUtils.getPolygonCenterPoint(coords),"",zoneLabelBitmap));
        } catch (Exception e) {

        }
    }

    /**
     * Clears all currently highlighted indoor location zones
     */
    public void clearHighlightedZones() {
        if (indoorLocationZonesPolygons != null) {
            synchronized (indoorLocationZonesPolygons) {
                Iterator<Polygon> iter = indoorLocationZonesPolygons.iterator();
                while (iter.hasNext()) {
                    Polygon p = iter.next();
                    p.remove();
                    iter.remove();
                }
            }
        }
        if (indoorLocationZonesLabels != null) {
            synchronized (indoorLocationZonesLabels) {
                Iterator<Marker> iter2 = indoorLocationZonesLabels.iterator();
                while (iter2.hasNext()) {
                    Marker m = iter2.next();
                    m.remove();
                    iter2.remove();
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
            clearTilesOverlay();
        }
    }

    /**
     * Resets the map view by removing all markers, polygons and overlays
     */
    public void reset() {
        lastPosition = null;
        lastZoom = DEFAULT_MAP_ZOOM;
        lastBearing = 0;
        clearCustomMarkers();
        clearHighlightedZones();
        clearPositionMarker();
        clearTilesOverlay();
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
     * Adds custom marker to the map
     * @param coordinates custom marker coordinates
     * @param title custom marker title
     * @param radius custom marker radius
     * @param color custom marker color
     * @return added marker
     */
    public Marker addCustomMarker(String tag, LatLng coordinates, String title, int radius, String color){
        try {
            return addCustomMarker(tag, coordinates, title, radius, Color.parseColor(color));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds custom marker to the map
     * @param coordinates custom marker coordinates
     * @param title custom marker title
     * @param radius custom marker radius
     * @param color custom marker color
     * @return added marker
     */
    public Marker addCustomMarker(String tag, LatLng coordinates, String title, int radius, int color){
        if(coordinates==null || tag==null)
            return null;
        if(customMarkersMap ==null)
            customMarkersMap = new HashMap<>();
        if(title==null)
            title = "";

        Marker customMarker = addMarkerToGoogleMap(coordinates, title, MapMarkers.getMarkerBitmap(radius, color));
        if(customMarkersMap.containsKey(tag)){
            customMarkersMap.get(tag).add(customMarker);
        } else {
            List<Marker> newCustomMarkersList = new ArrayList<>();
            newCustomMarkersList.add(customMarker);
            customMarkersMap.put(tag, newCustomMarkersList);
        }
        return customMarker;
    }

    /**
     * Removes all custom markers from map
     */
    public void clearCustomMarkers(){
        if (customMarkersMap != null) {
            synchronized (customMarkersMap) {
                for(Iterator<Map.Entry<String, List<Marker>>> it = customMarkersMap.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String,  List<Marker>> entry = it.next();
                    for(Marker marker : entry.getValue())
                        marker.remove();
                    it.remove();
                }
            }
        }
    }

    /**
     * Removes all markers with the given tag
     * @param tag tag
     */
    public void clearCustomMarkersWithTag(String tag){
        if (customMarkersMap != null) {
            synchronized (customMarkersMap) {
                for(Iterator<Map.Entry<String, List<Marker>>> it = customMarkersMap.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, List<Marker>> entry = it.next();
                    if(entry.getKey().equals(tag)) {
                        for(Marker marker : entry.getValue())
                            marker.remove();
                        it.remove();
                    }
                }
            }
        }
    }

    public LatLng getLastPosition(){
        return lastPosition;
    }
    // ---------------------------------------------------------------------------------------------
    // PRIVATE METHODS:

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

        CameraPosition.Builder cameraPositionBuilder = new CameraPosition.Builder().target(lastPosition).bearing(lastBearing);

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
    private void clearTilesOverlay() {
        if (mTileOverlay != null)
            mTileOverlay.remove();
    }

    /**
     * Removes position marker from map
     */
    private void clearPositionMarker() {
        if (mCurrentLocationMarker != null)
            mCurrentLocationMarker.remove();
        mCurrentLocationMarker = null;
    }

    /**
     * Adds new marker to google map. Generic method
     * @param latLng coordinates
     * @param title marker title
     * @param bitmap bitmap
     * @return reference to the added Marker
     */
    private Marker addMarkerToGoogleMap(LatLng latLng, String title, Bitmap bitmap) {
        return mGoogleMap.addMarker(new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
    }

    /**
     * Animates marker to the new position
     * @param marker marker to move
     * @param toPosition target position of the marker
     */
    private void animateMarker(final Marker marker, final LatLng toPosition) {
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
                if (t < 1.0)
                    handler.postDelayed(this, 16);
                else
                    marker.setVisible(true);
            }

            private LatLng getNewPosition(float t) {
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                return new LatLng(lat, lng);
            }
        });
    }
}
