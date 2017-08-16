package com.ubudu.ilapp2.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.ubudu.gmaps.MapLayout;
import com.ubudu.gmaps.factory.MarkerBitmapFactory;
import com.ubudu.gmaps.factory.MarkerOptionsFactory;
import com.ubudu.gmaps.factory.PolylineOptionsFactory;
import com.ubudu.gmaps.model.Marker;
import com.ubudu.gmaps.model.Zone;
import com.ubudu.gmaps.util.MarkerOptionsStrategy;
import com.ubudu.gmaps.util.MarkerSearchPattern;
import com.ubudu.ilapp2.R;
import com.ubudu.indoorlocation.ILBeacon;
import com.ubudu.indoorlocation.UbuduCompassListener;
import com.ubudu.indoorlocation.UbuduCoordinates2D;
import com.ubudu.indoorlocation.UbuduIndoorLocationDelegate;
import com.ubudu.indoorlocation.UbuduIndoorLocationManager;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduMap;
import com.ubudu.indoorlocation.UbuduParticle;
import com.ubudu.indoorlocation.UbuduParticleFilterListener;
import com.ubudu.indoorlocation.UbuduPoint;
import com.ubudu.indoorlocation.UbuduPosition;
import com.ubudu.indoorlocation.UbuduPositionUpdate;
import com.ubudu.indoorlocation.UbuduZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by mgasztold on 05/10/16.
 */

public class MapFragment extends BaseFragment implements UbuduIndoorLocationDelegate, MapLayout.EventListener, UbuduParticleFilterListener {

    public static final String TAG = MapFragment.class.getCanonicalName();

    private static final String TAG_BEACON_MARKER = "beacon_marker";
    private static final String TAG_PARTICLE_MARKER = "particle_marker";

    private final long TIME_OFFSET = 20L;

    private boolean showParticles = false;
    private boolean showBeacons = false;
    private boolean showUndetectedBeacons = false;
    private static String lastLoadingLabelMessage = "";
    private boolean isMoving = false;
    private boolean isPathEnabled = false;

    private boolean isCameraMoving = false;

    private final Handler mainThreadHandler = new Handler();

    SharedPreferences mSharedPref;


    @BindView(R.id.map)
    MapLayout mMapView;
    @BindView(R.id.loading_label)
    LinearLayout loadingLabelLayout;
    @BindView(R.id.go_to_my_position)
    FancyButton myPositionButton;

    private boolean waitingForInitialUpdate = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DrawerLayout mRootView = (DrawerLayout) inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView.init(getContext());
        mMapView.setEventListener(this);
        // setup location marker options

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    /**
     * Sets proper color of the location marker drawable according to app color theme
     */
    private void initLocationMarker() {

        mMapView.setLocationMarkerOptionsStrategy(new MarkerOptionsStrategy()
                .setNormalMarkerOptions(MarkerOptionsFactory
                        .bitmapMarkerOptions(BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_location_marker))
                        .anchor(0.5f, 0.73f).zIndex(3)));
    }

    @Override
    public void onDestroy() {
        lastLoadingLabelMessage = "";
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewController().mapFragmentResumed(this);

        UbuduIndoorLocationManager mIndoorLocationManager = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager();
        mIndoorLocationManager.setIndoorLocationDelegate(this);

        mIndoorLocationManager.setCompassListener(new UbuduCompassListener() {
            @Override
            public void onAzimuthUpdated(float azimuth) {
                if (mMapView != null)
                    mMapView.updateLocationBearing(azimuth);
            }
        });
        if (mIndoorLocationManager.getMap() != null) {
            if (mIndoorLocationManager.getLastPositionUpdate() != null)
                zonesChanged(mIndoorLocationManager.getMap().getZonesForPosition(mIndoorLocationManager.getLastPositionUpdate().getClosestNavigablePoint().getCartesianCoordinates()));
            setTilesOverlay(mIndoorLocationManager.getMap().getUuid());
        } else {
            mapChanged(null, 0);
        }

        if (lastLoadingLabelMessage != null && !lastLoadingLabelMessage.equals(""))
            showLoadingLabelWithText(lastLoadingLabelMessage);
    }

    @Override
    public void onPause() {
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setIndoorLocationDelegate(null);
        getViewController().mapFragmentPaused();
        super.onPause();
    }

    @Override
    public void positionChanged(UbuduPositionUpdate positionUpdate) {

        LatLng newPosition = new LatLng(positionUpdate.getClosestNavigablePoint().getGeographicalCoordinates().getLatitude()
                , positionUpdate.getClosestNavigablePoint().getGeographicalCoordinates().getLongitude());

        if (mMapView != null) {

            if (isPathEnabled && mMapView.getLocation() != null && !mMapView.getLocation().equals(newPosition)) {

                int color = 1;
                if (positionUpdate.getUpdateOrigin() == UbuduPositionUpdate.UPDATE_ORIGIN_BEACONS)
                    color = getResources().getColor(R.color.colorAccent);
                else if (positionUpdate.getUpdateOrigin() == UbuduPositionUpdate.UPDATE_ORIGIN_MOTION)
                    color = getResources().getColor(R.color.colorWarning);
                else if (positionUpdate.getUpdateOrigin() == UbuduPositionUpdate.UPDATE_ORIGIN_GPS)
                    color = getResources().getColor(R.color.button_on);

                mMapView.addPolylineToPath("path"
                        , PolylineOptionsFactory.polylineWithColor(color)
                                .add(mMapView.getLocation())
                                .add(newPosition));
            }

            mMapView.markLocation(newPosition, 0);
            if (waitingForInitialUpdate)
                mMapView.updateCamera(!waitingForInitialUpdate);
            if (waitingForInitialUpdate)
                waitingForInitialUpdate = false;
        }
        onProgressDialogHideRequested();
    }

    @Override
    public void closestBeaconChanged(UbuduPositionUpdate ubuduPositionUpdate) {

    }

    @Override
    public void closestZoneChanged(UbuduPositionUpdate ubuduPositionUpdate) {

    }

    @Override
    public void zonesChanged(List<UbuduZone> zones) {

        if (zones != null && mMapView != null) {
            mMapView.removeZones();
            for (UbuduZone zone : zones) {
                List<LatLng> coords = new ArrayList<>();
                for (UbuduPoint p : zone.getCoordinates()) {
                    UbuduCoordinates2D c = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getMap().getGeoCoordinates(p);
                    if (c != null)
                        coords.add(new LatLng(c.getLatitude(), c.getLongitude()));
                }
                Zone mapLayoutZone = new Zone(zone.getName(), coords);
                mMapView.addZone(mapLayoutZone);
            }
        }
    }

    @Override
    public void closestNavigablePointChanged(UbuduPositionUpdate ubuduPositionUpdate) {

    }

    @Override
    public void beaconsUpdated(List<ILBeacon> beacons) {

        if (isCameraMoving) {
            return;
        }
        if (!showBeacons) return;

        // changing markers of beacons not present in the new list of beacons
        Map<Marker, com.google.android.gms.maps.model.Marker> markersMatchingTag = mMapView.findMarkers(new MarkerSearchPattern().tag(TAG_BEACON_MARKER));
        for (Marker marker : markersMatchingTag.keySet()) {

            boolean beaconDetected = false;
            for (ILBeacon beacon : beacons) {
                if (marker.getTitle().equals(beacon.getMajor() + "/" + beacon.getMinor())) {
                    beaconDetected = true;
                    break;
                }
            }
            if (!beaconDetected) {
                com.google.android.gms.maps.model.Marker googleMarker = markersMatchingTag.get(marker);
                String color = "#a5c4e1";
                marker.getMarkerOptionsStrategy().getNormalMarkerOptions()
                        .snippet(null)
                        .icon(getBeaconMarkerBitmapDescriptorForColor(color));
                marker.getMarkerOptionsStrategy().getHighlightedMarkerOptions()
                        .snippet(null)
                        .icon(getBeaconMarkerBitmapDescriptorForColor(color));

                googleMarker.setIcon(getBeaconMarkerBitmapDescriptorForColor(color));
                googleMarker.setSnippet(null);
                if (!showUndetectedBeacons)
                    googleMarker.setVisible(false);
            }
        }
        // adding and updating markers
        long timeDelay = TIME_OFFSET;
        for (final ILBeacon beacon : beacons) {
            timeDelay = timeDelay + TIME_OFFSET;
            mainThreadHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String title = String.format(Locale.US, "%d/%d", beacon.getMajor(), beacon.getMinor());
                    String snippet = String.format(Locale.US, "rssi: %.2f dBm\ndistance: %.2f m\nbattery: %d %%", beacon.getRssi(), beacon.getDistance(), beacon.getBatteryLevel());
                    String color = "#FF9300";
                    if (beacon.isValidatedForPositionCalculation()) {
                        if (beacon.wasUsedForPositionComputation())
                            color = "#e841f4";
                        else
                            color = "#7CBE31";
                    }
                    Map<Marker, com.google.android.gms.maps.model.Marker> markersMatchingBeacon = mMapView
                            .findMarkers(new MarkerSearchPattern().title(beacon.getMajor() + "/" + beacon.getMinor()).tag(TAG_BEACON_MARKER));

                    if (markersMatchingBeacon.size() > 0) {
                        for (Marker marker : markersMatchingBeacon.keySet()) {
                            com.google.android.gms.maps.model.Marker googleMarker = markersMatchingBeacon.get(marker);
                            googleMarker.setVisible(true);
                            marker.setTitle(title);
                            marker.getMarkerOptionsStrategy().getNormalMarkerOptions()
                                    .snippet(snippet)
                                    .icon(getBeaconMarkerBitmapDescriptorForColor(color));
                            marker.getMarkerOptionsStrategy().getHighlightedMarkerOptions()
                                    .snippet(snippet)
                                    .icon(getBeaconMarkerBitmapDescriptorForColor(color));

                            googleMarker.setIcon(getBeaconMarkerBitmapDescriptorForColor(color));
                            googleMarker.setTitle(title);
                            googleMarker.setSnippet(snippet);
                            if (googleMarker.isInfoWindowShown()) {
                                googleMarker.showInfoWindow();
                            }
                        }
                    } else {
                        if (beacon.getGeographicalPosition() == null) return;
                        LatLng coordinates = new LatLng(beacon.getGeographicalPosition().getLatitude(), beacon.getGeographicalPosition().getLongitude());
                        Marker mapLayoutMarker = new Marker(title, coordinates);
                        mapLayoutMarker.addTag(TAG_BEACON_MARKER);
                        mapLayoutMarker.setMarkerOptionsStrategy(new MarkerOptionsStrategy()
                                .setNormalMarkerOptions(MarkerOptionsFactory.circleMarkerOptions()
                                        .icon(getBeaconMarkerBitmapDescriptorForColor(color))
                                        .zIndex(4).snippet(snippet)));
                        mMapView.addMarker(mapLayoutMarker);
                    }
                }
            }, timeDelay);
        }
    }

    private HashMap<String, BitmapDescriptor> customMarkerBitmaps;

    private BitmapDescriptor getBeaconMarkerBitmapDescriptorForColor(String color) {
        if (customMarkerBitmaps == null)
            customMarkerBitmaps = new HashMap<>();

        if (!customMarkerBitmaps.containsKey(color)) {
            customMarkerBitmaps.put(color, BitmapDescriptorFactory.fromBitmap(MarkerBitmapFactory.getMarkerWithHaloBitmap(14, color, 16, "#ffffff")));
        }
        return customMarkerBitmaps.get(color);
    }

    @Override
    public void mapChanged(String uuid, int level) {
        if (mMapView != null) {
            LatLng lastPosition = mMapView.getLocation();
            mMapView.reset();
            if (lastPosition != null)
                mMapView.markLocation(lastPosition, mMapView.getLocationAccuracy());
        }
        waitingForInitialUpdate = true;
        setTilesOverlay(uuid);
        showUndetectedBeacons(showUndetectedBeacons);
    }

    @Override
    public void movementChanged(boolean isMoving) {
        this.isMoving = isMoving;
    }

    private void showUndetectedBeacons(boolean show) {
        final Map<Marker, com.google.android.gms.maps.model.Marker> markersMatchingTag = mMapView.findMarkers(new MarkerSearchPattern().tag(TAG_BEACON_MARKER));
        for (com.google.android.gms.maps.model.Marker googleMarker : markersMatchingTag.values()) {
            if (googleMarker.getSnippet() == null) {
                googleMarker.setVisible(show);
            }
        }
        if (!show) return;

        UbuduMap ubuduMap = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getMap();
        if (ubuduMap == null)
            return;
        List<ILBeacon> mapBeacons = ubuduMap.getBeacons();
        long timeDelay = TIME_OFFSET;
        for (final ILBeacon beacon : mapBeacons) {
            timeDelay = timeDelay + TIME_OFFSET;
            mainThreadHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String title = beacon.getMajor() + "/" + beacon.getMinor();
                    boolean markerForBeaconAlreadyExists = false;
                    for (Marker marker : markersMatchingTag.keySet()) {
                        if (marker.getTitle().equals(title)) {
                            markerForBeaconAlreadyExists = true;
                        }
                    }
                    if (!markerForBeaconAlreadyExists) {
                        Marker mapLayoutMarker = new Marker(beacon.getMajor() + "/" + beacon.getMinor()
                                , new LatLng(beacon.getGeographicalPosition().getLatitude(), beacon.getGeographicalPosition().getLongitude()));
                        mapLayoutMarker.addTag(TAG_BEACON_MARKER);
                        mapLayoutMarker.setMarkerOptionsStrategy(new MarkerOptionsStrategy()
                                .setNormalMarkerOptions(MarkerOptionsFactory.circleMarkerOptions()
                                        .icon(getBeaconMarkerBitmapDescriptorForColor("#a5c4e1"))
                                        .zIndex(4)));
                        mMapView.addMarker(mapLayoutMarker);
                    }
                }
            }, timeDelay);
        }
    }

    private void setTilesOverlay(String uuid) {
        try {
            UbuduMap ubuduMap = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getMap();
            if (ubuduMap != null) {
                UbuduCoordinates2D bottomRightAnchorCoordinates = ubuduMap.getBottomRightAnchorCoordinates().toDeg();
                UbuduCoordinates2D topLeftAnchorCoordinates = ubuduMap.getTopLeftAnchorCoordinates().toDeg();
                mMapView.addTileOverlay(ubuduMap.getTilesBaseUrl()
                        , new LatLng(bottomRightAnchorCoordinates.getLatitude(), topLeftAnchorCoordinates.getLongitude())
                        , new LatLng(topLeftAnchorCoordinates.getLatitude(), bottomRightAnchorCoordinates.getLongitude()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoadingLabelWithText(final String text) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                lastLoadingLabelMessage = text;
                ImageView imageView = (ImageView) loadingLabelLayout.findViewById(R.id.img_loading);
                if (imageView.getAnimation() == null || !imageView.getAnimation().isInitialized()) {
                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.progress);
                    imageView.startAnimation(animation);
                }
                TextView textView = (TextView) loadingLabelLayout.findViewById(R.id.loading_label_text);
                textView.setText(text);
                loadingLabelLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onProgressDialogHideRequested() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                lastLoadingLabelMessage = "";
                loadingLabelLayout.setVisibility(View.GONE);
            }
        });
    }

    public void reset() {
        Log.i(TAG, "map fragment reset");
        if (mMapView != null)
            mMapView.reset();
    }

    @Override
    public void onMapReady() {

        initLocationMarker();

        mMapView.getMap().setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                isCameraMoving = true;
            }
        });

        mMapView.getMap().setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                isCameraMoving = false;
            }
        });

        mMapView.getMap().setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(com.google.android.gms.maps.model.Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(com.google.android.gms.maps.model.Marker marker) {
                Context context = getContext();
                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                info.addView(title);

                if (marker.getSnippet() != null && !marker.getSnippet().equals("")) {
                    TextView snippet = new TextView(context);
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(snippet);
                }
                return info;
            }
        });

        myPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapView.updateCamera(true);
            }
        });

        if (mMapView != null)
            mMapView.updateCamera(false);

        showUndetectedBeacons = mSharedPref.getBoolean("show_undetected_beacons", false);
        showUndetectedBeacons(showUndetectedBeacons);

        if (mSharedPref.getBoolean("show_particles", false)) {
            showParticles = true;
        } else {
            if (mMapView != null)
                mMapView.removeMarkers(new MarkerSearchPattern().tag(TAG_PARTICLE_MARKER));
            showParticles = false;
        }

        if (mSharedPref.getBoolean("show_beacons", false)) {
            showBeacons = true;
        } else {
            hideAllDetectedBeaconMarkers();
            showBeacons = false;
        }

        UbuduMap map = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getMap();
        UbuduPositionUpdate lastUpdate = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getLastPositionUpdate();
        if (lastUpdate != null) {
            UbuduPosition lastPosition = lastUpdate.getClosestNavigablePoint();
            if (lastPosition != null && map != null) {
                List<UbuduZone> zones = map.getZonesForPosition(lastPosition.getCartesianCoordinates());
                zonesChanged(zones);
            }
        }
    }

    private void hideAllDetectedBeaconMarkers() {
        if (mMapView == null) return;
        Map<Marker, com.google.android.gms.maps.model.Marker> markersMatchingTag = mMapView.findMarkers(new MarkerSearchPattern().tag(TAG_BEACON_MARKER));
        for (com.google.android.gms.maps.model.Marker googleMarker : markersMatchingTag.values()) {
            if (googleMarker.getSnippet() != null)
                googleMarker.setVisible(false);
        }
    }

    @Override
    public void onZoneClicked(Zone zone, com.google.android.gms.maps.model.Polygon polygon) {

    }

    @Override
    public void onMarkerClicked(Marker marker, com.google.android.gms.maps.model.Marker marker1) {

    }

    @Override
    public void didUpdateParticles(final List<UbuduParticle> particles) {

        if (!showParticles) {
            mMapView.removeMarkers(new MarkerSearchPattern().tag(TAG_PARTICLE_MARKER));
            return;
        }

        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                UbuduMap map = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getMap();

                if (map == null)
                    return;

                for (UbuduParticle particle : particles) {
                    String title = String.format(Locale.US, "particle%s_", particle.getId());
                    UbuduCoordinates2D coords = map.getGeoCoordinates(particle.getPoint());
                    if (coords == null)
                        continue;
                    coords = coords.toDeg();
                    LatLng location = new LatLng(coords.getLatitude(), coords.getLongitude());

                    Map<Marker, com.google.android.gms.maps.model.Marker> particleMarker = mMapView.findMarkers(new MarkerSearchPattern().title(title).tag(TAG_PARTICLE_MARKER));

                    if (particleMarker == null || particleMarker.size() == 0) {
                        Marker mapLayoutMarker = new Marker(title, location);
                        try {
                            mapLayoutMarker.addTag(TAG_PARTICLE_MARKER);
                            mapLayoutMarker.setMarkerOptionsStrategy(new MarkerOptionsStrategy()
                                    .setNormalMarkerOptions(MarkerOptionsFactory.circleMarkerOptions(4, getResources().getColor(R.color.colorAccent))));
                            mMapView.addMarker(mapLayoutMarker);
                        } catch (IllegalStateException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        for (Marker marker : particleMarker.keySet()) {
                            particleMarker.get(marker).setPosition(location);
                        }
                    }
                }
            }
        });
    }
}