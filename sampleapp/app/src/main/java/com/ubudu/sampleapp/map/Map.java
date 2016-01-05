package com.ubudu.sampleapp.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
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
import com.ubudu.sampleapp.map.network.HttpDownloader;
import com.ubudu.sampleapp.map.network.InputStreamWithContentLength;
import com.ubudu.sampleapp.map.storage.AppFileSystemUtil;
import com.ubudu.sampleapp.map.storage.WriteInputStreamToFileProgressListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;

public class Map implements GoogleMap.OnMapLoadedCallback, MapBearingManager.BearingListener {

    private static final long OVERLAY_VALID_TIMEOUT_MILLIS = 30 * 60 * 1000;
    private static final double OVERLAY_DOWNSCALE_FACTOR = 0.5;
    private int DEFAULT_MAP_ZOOM = 19;
    private final String MAP_FILE_NAME = "mapoverlay";
    private final long ANIMATE_POSITION_CHANGE_DURATION = 500; // ms
    private int INITIAL_IN_SAMPLE_SIZE_FOR_BITMAP_COMPRESSION = 1;

    private boolean googleMapReady = false;

    private long mapOverlayTimeStamp = -1;

    private GoogleMap mGoogleMap;

    private GroundOverlay mGroundOverlay = null;

    protected MapEventListener mMapEventListener;

    private LatLng currentLocation;

    private Marker mCurrentLocationMarker;
    private List<Marker> activeBeaconsMarkers;
    private List<Marker> mapBeaconsMarkers;

    private List<Polygon> zonesPolygons;
    private List<Marker> zonesMarkers;

    private List<Polyline> pathPolylines;

    private LatLngBounds mapBounds;
    private LatLng middle;

    private Context mContext;

    private String mapUrl = "";
    private String assetsMapFileName = "";

    private AppFileSystemUtil mAppFileSystemUtil;

    private MapMarkers markers;

    private float currentCompassBearing;

    private MapBearingManager mMapRotationSensorManager;

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
        mAppFileSystemUtil = new AppFileSystemUtil(ctx);
        initOnMapLoadedCallback();
        mMapRotationSensorManager = new MapBearingManager(ctx, this);
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
            Iterator<UbuduBeacon> newActiveBeaconsPositionsIter = activeBeaconsPositions.iterator();
            while (newActiveBeaconsPositionsIter.hasNext()) {
                UbuduBeacon b = newActiveBeaconsPositionsIter.next();
                if (b.geographicalPosition() != null) {
                    if (mGoogleMap != null) {
                        String title = "major: " + b.major() + ", minor: " + b.minor();

                        BitmapDescriptor iconBitmapDescriptor;
                        if (b.accuracy() < 15.0)
                            iconBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(markers.greenMarkerBitmap);
                        else
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

    public void setMapOverlayBounds(LatLng southWest, LatLng northEast) {
        mapBounds = new LatLngBounds(
                southWest,       // South west image corner
                northEast);      // North east image corner
        middle = new LatLng(southWest.latitude + (northEast.latitude - southWest.latitude) / 2, southWest.longitude + (northEast.longitude - southWest.longitude) / 2);
    }

    public void onPause() {
        mMapRotationSensorManager.unregister();
    }

    public void onResume() {
        mMapRotationSensorManager.register();
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
        removeGroundOverlay();
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

    private void removeGroundOverlay() {
        if (mGroundOverlay != null)
            mGroundOverlay.remove();

        removeGroundOverlayFile();
        mapOverlayTimeStamp = -1;
    }

    private void removeGroundOverlayFile() {
        AppFileSystemUtil mAppFileSystemUtil = new AppFileSystemUtil(mContext);
        mAppFileSystemUtil.removeFile(MAP_FILE_NAME);
    }

    @Override
    public void bearing(float bearing) {
        currentCompassBearing = bearing;
    }

    private class LoadMapOverlayFromUrlTask extends AsyncTask<Void, Integer, Void> {

        HttpDownloader mHttpDownloader;

        WriteInputStreamToFileProgressListener mapFetchListener = new WriteInputStreamToFileProgressListener() {
            @Override
            public void publishFileWritingProgress(int progress) {
                publishProgress(progress);
            }
        };

        @Override
        protected Void doInBackground(Void... params) {

            if (mAppFileSystemUtil.fileExists(MAP_FILE_NAME) && mapOverlayTimeStamp > System.currentTimeMillis() - Map.OVERLAY_VALID_TIMEOUT_MILLIS) {
                putMapOverlay();
            } else {
                boolean fileDownloadSuccess = downloadAndSaveBitmapFile();
                if (fileDownloadSuccess) {
                    mapOverlayTimeStamp = System.currentTimeMillis();
                    notifyMapOverlayFetched();
                    putMapOverlay();
                } else {
                    notifyMapOverlayNotFetched("Overlay could not be fetched due to network connection issues. Please make sure that map's overlay URL is correct.");
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mMapEventListener.notifyMapOverlayDownloadProgress(progress[0]);
        }

        private boolean downloadAndSaveBitmapFile() {
            if (!assetsMapFileName.equals("")) {
                return readMapOverlayFile();
            } else if (!mapUrl.equals("")) {
                return downloadMapOverlay();
            }
            return false;
        }

        private boolean readMapOverlayFile() {
            try {
                InputStream input = mContext.getAssets().open(assetsMapFileName);
                mAppFileSystemUtil.writeInputStreamToFile(input, MAP_FILE_NAME, input.available(), mapFetchListener);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean downloadMapOverlay() {
            mHttpDownloader = new HttpDownloader(mContext);
            if (mHttpDownloader.isNetworkAvailable()) {
                mHttpDownloader.openHttpUrlConnection(mapUrl);
                InputStreamWithContentLength in = mHttpDownloader.getInputStream();
                mHttpDownloader.closeHttpUrlConnection();
                return mAppFileSystemUtil.writeInputStreamToFile(in.inputStream, MAP_FILE_NAME, in.contentLength, mapFetchListener);
            }
            return false;
        }

        private void writeOverlayBitmapToFile(Bitmap bitmap) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                InputStream inputstream = new ByteArrayInputStream(baos.toByteArray());
                mAppFileSystemUtil.writeInputStreamToFile(inputstream, MAP_FILE_NAME, inputstream.available(), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Bitmap readOverlayFromFile(int inSampleSize) {
            File inputFile = new File(mContext.getFilesDir(), MAP_FILE_NAME);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = inSampleSize;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(inputFile.getAbsolutePath(), options);
        }

        private void compressMapOverlayAndTryAgain(int inSampleSize) {
            try {
                // get an overlay bitmap currently stored in the local filesystem
                Bitmap overlayBitmap = readOverlayFromFile(inSampleSize);

                if (overlayBitmap != null) {
                    overlayBitmap = downscaleBitmap(overlayBitmap);
                    if (overlayBitmap != null) {
                        // save compressed bitmap to file
                        writeOverlayBitmapToFile(overlayBitmap);
                        //now try to put overlay to the map again
                        putMapOverlay();
                    } else {
                        compressMapOverlayAndTryAgain(inSampleSize++);
                    }
                }
            } catch (OutOfMemoryError e) {
                compressMapOverlayAndTryAgain(inSampleSize++);
            }
        }

        private Bitmap downscaleBitmap(Bitmap bitmap) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > 0 && height > 0) {
                bitmap = Bitmap.createScaledBitmap(bitmap
                        , (int) (width * Map.OVERLAY_DOWNSCALE_FACTOR)
                        , (int) (height * Map.OVERLAY_DOWNSCALE_FACTOR)
                        , false);
                return bitmap;
            } else {
                return null;
            }
        }

        private void notifyRescalingImage() {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mMapEventListener.notifyRescalingImageStarted();
                }
            });
        }

        private void notifyMapReady() {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mMapEventListener.onMapReady();
                }
            });
        }

        private void notifyMapOverlayFetched() {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mMapEventListener.notifyMapOverlayFetchedSuccessfully();
                }
            });
        }

        private void notifyMapOverlayNotFetched(final String errorMsg) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mMapEventListener.notifyMapOverlayFetchingError(errorMsg);
                }
            });
        }

        private void putMapOverlay() {
            try {
                File inputFile = new File(mContext.getFilesDir(), MAP_FILE_NAME);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(inputFile.getAbsolutePath());
                final GroundOverlayOptions mapOptions = new GroundOverlayOptions()
                        .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .positionFromBounds(mapBounds)
                        .transparency(0.2f);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (mGroundOverlay != null)
                                mGroundOverlay.remove();
                            mGroundOverlay = mGoogleMap.addGroundOverlay(mapOptions);
                            notifyMapReady();
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(middle, DEFAULT_MAP_ZOOM));
                        } catch (OutOfMemoryError e) {
                        }
                    }
                });
            } catch (OutOfMemoryError e) {
                notifyRescalingImage();
                compressMapOverlayAndTryAgain(INITIAL_IN_SAMPLE_SIZE_FOR_BITMAP_COMPRESSION);
            }
        }
    }

    public void initMapOverlayFromUrl(String mapUrl) {
        this.mapUrl = mapUrl;
        assetsMapFileName = "";
        new LoadMapOverlayFromUrlTask().executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    public void initMapOverlayFromFile(String filePath) {
        mapUrl = "";
        assetsMapFileName = filePath;
        new LoadMapOverlayFromUrlTask().executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    @Override
    public void onMapLoaded() {
        if (mGoogleMap != null) {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            initZoneLabelMarker();
        }
        googleMapReady = true;
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
            // update declination for compass bearing calculation
            mMapRotationSensorManager.updateDeclination(currentLocation);
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
        if(googleMapReady) {
            PolygonOptions rectOptions = new PolygonOptions()
                    .addAll(new ArrayList<>(coords))
                    .fillColor(0x80C5E1A5)
                    .strokeWidth(0)
                    .geodesic(false);

            // Get back the mutable Polygon
            if (zonesPolygons == null)
                zonesPolygons = new ArrayList<>();
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
}
