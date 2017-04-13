package com.ubudu.ilapp2.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.ubudu.gmaps.MapLayout;
import com.ubudu.gmaps.factory.MarkerBitmapFactory;
import com.ubudu.gmaps.factory.MarkerOptionsFactory;
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
import com.ubudu.indoorlocation.UbuduPoint;
import com.ubudu.indoorlocation.UbuduPosition;
import com.ubudu.indoorlocation.UbuduPositionUpdate;
import com.ubudu.indoorlocation.UbuduZone;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by mgasztold on 05/10/16.
 */

public class MapFragment extends BaseFragment implements UbuduIndoorLocationDelegate, MapLayout.EventListener {

    public static final String TAG = MapFragment.class.getCanonicalName();

    private static final String TAG_BEACON_MARKER = "beacon_marker";
    private static final String TAG_UNDETECTED_BEACON_MARKER = "undetected_beacon_marker";

    private boolean showUndetectedBeacons = false;
    private static String lastLoadingLabelMessage = "";

    @BindView(R.id.map)
    MapLayout mMapView;
    @BindView(R.id.loading_label)
    LinearLayout loadingLabelLayout;
    @BindView(R.id.go_to_my_position)
    public FancyButton myPositionButton;

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
                if(mMapView!=null)
                    mMapView.updateBearing(azimuth);
            }
        });
        if(mIndoorLocationManager.getMap()!=null) {
            if(mIndoorLocationManager.getLastPositionUpdate()!=null)
                zonesChanged(mIndoorLocationManager.getMap().getZonesForPosition(mIndoorLocationManager.getLastPositionUpdate().getClosestNavigablePoint().getCartesianCoordinates()));
            setTilesOverlay(mIndoorLocationManager.getMap().getUuid());
        } else {
            mapChanged(null,0);
        }

        if(lastLoadingLabelMessage!=null && !lastLoadingLabelMessage.equals(""))
            showLoadingLabelWithText(lastLoadingLabelMessage);
    }

    @Override
    public void onPause() {
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setIndoorLocationDelegate(null);
        getViewController().mapFragmentPaused();
        super.onPause();
    }

    @Override
    public void positionChanged(UbuduPositionUpdate ubuduPositionUpdate) {
        if(mMapView!=null) {
            mMapView.markLocation(ubuduPositionUpdate.getClosestNavigablePoint().getGeographicalCoordinates().getLatitude()
                    , ubuduPositionUpdate.getClosestNavigablePoint().getGeographicalCoordinates().getLongitude());
            if(waitingForInitialUpdate)
                mMapView.updateCamera(!waitingForInitialUpdate);
            if(waitingForInitialUpdate)
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
        if(zones!=null && mMapView !=null) {
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
        List<String> cutTitlesOfRemovedMarkers = new ArrayList<>();
        // removing old markers of beacons not present in the new list of beacons
        List<com.google.android.gms.maps.model.Marker> markersMatchingTag = mMapView.findMarkers(new MarkerSearchPattern().tag(TAG_BEACON_MARKER));
        for(com.google.android.gms.maps.model.Marker marker : markersMatchingTag) {
            boolean shouldRemove = true;
            String markerTitle = marker.getTitle();
            String cutTitle = markerTitle.substring(0,markerTitle.indexOf("/",markerTitle.indexOf("/",0)+1));
            int batteryLevel = Integer.parseInt(markerTitle
                    .substring(markerTitle
                            .indexOf("/",markerTitle.indexOf("/",0)+1)+1,markerTitle
                            .indexOf("%")));
            double rssi = Double.parseDouble(markerTitle.substring(markerTitle.indexOf("%")+2,markerTitle.indexOf("d")-1));

            for(ILBeacon beacon : beacons){
                if( (cutTitle.equals(beacon.getMajor() + "/" + beacon.getMinor()) )
                        && beacon.getBatteryLevel()==batteryLevel
                        && Math.abs(beacon.getRssi()-rssi)<0.1) {
                    shouldRemove = false;
                }
            }
            if(shouldRemove) {
                if(marker.isInfoWindowShown())
                    cutTitlesOfRemovedMarkers.add(cutTitle);
                mMapView.removeMarkers(new MarkerSearchPattern().title(markerTitle).tag(TAG_BEACON_MARKER));

            }
        }
        // adding and updating markers
        for (ILBeacon beacon : beacons) {
            String title = beacon.getMajor() + "/" + beacon.getMinor() + "/" + beacon.getBatteryLevel() + "%" + "/" + beacon.getRssi() + "dBm";
            String color = "#FFF48642";
            if (beacon.isValidatedForPositionCalculation())
                color = "#8bff8f";
            List<com.google.android.gms.maps.model.Marker> markersMatchingBeacon = mMapView.findMarkers(new MarkerSearchPattern().title(beacon.getMajor() + "/" + beacon.getMinor()).tag(TAG_BEACON_MARKER));
            if (markersMatchingBeacon.size() > 0) {
                for (com.google.android.gms.maps.model.Marker marker : markersMatchingBeacon) {
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(MarkerBitmapFactory.getMarkerBitmap(20, color)));
                }
            } else {
                if(beacon.getGeographicalPosition()==null) continue;
                LatLng coordinates = new LatLng(beacon.getGeographicalPosition().getLatitude(), beacon.getGeographicalPosition().getLongitude());
                Marker mapLayoutMarker = new Marker(title, coordinates);
                mapLayoutMarker.addTag(TAG_BEACON_MARKER);
                mapLayoutMarker.setMarkerOptionsStrategy(new MarkerOptionsStrategy()
                        .setNormalMarkerOptions(MarkerOptionsFactory.circleMarkerOptions(20, color)));
                if(mMapView.addMarker(mapLayoutMarker)) {
                    com.google.android.gms.maps.model.Marker newMarker = mMapView.findMarkers(new MarkerSearchPattern().title(title)).get(0);
                    String newMarkerTitle = newMarker.getTitle();
                    String newMarkercutTitle = newMarkerTitle.substring(0, newMarkerTitle.indexOf("/", newMarkerTitle.indexOf("/", 0) + 1));

                    if (cutTitlesOfRemovedMarkers.contains(newMarkercutTitle)) {
                        newMarker.showInfoWindow();
                    }
                }
            }

        }
    }

    @Override
    public void mapChanged(String uuid, int level) {
        LatLng lastPosition = mMapView.getLocation();
        mMapView.reset();
        if(lastPosition!=null)
            mMapView.markLocation(lastPosition);
        waitingForInitialUpdate = true;
        setTilesOverlay(uuid);
        if(showUndetectedBeacons){
            showUndetectedBeacons();
        }
    }

    @Override
    public void movementChanged(boolean isMoving) {

    }

    private void showUndetectedBeacons() {
        mMapView.removeMarkers(new MarkerSearchPattern().tag(TAG_UNDETECTED_BEACON_MARKER));
        UbuduMap ubuduMap = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getMap();
        if(ubuduMap==null)
            return;
        List<ILBeacon> mapBeacons = ubuduMap.getBeacons();
        for(ILBeacon beacon : mapBeacons){
            Marker mapLayoutMarker = new Marker(beacon.getMajor()+"/"+beacon.getMinor()
                    ,new LatLng(beacon.getGeographicalPosition().getLatitude(), beacon.getGeographicalPosition().getLongitude()));
            mapLayoutMarker.addTag(TAG_UNDETECTED_BEACON_MARKER);
            mapLayoutMarker.setMarkerOptionsStrategy(new MarkerOptionsStrategy()
                    .setNormalMarkerOptions(MarkerOptionsFactory.circleMarkerOptions(20,"#50a5c4e1")));
            mMapView.addMarker(mapLayoutMarker);
//
        }
    }

    private void setTilesOverlay(String uuid) {
        try {
            UbuduMap ubuduMap = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getMap();
            if(ubuduMap!=null) {
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                lastLoadingLabelMessage = text;
                ImageView imageView = (ImageView) loadingLabelLayout.findViewById(R.id.img_loading);
                if(imageView.getAnimation()==null || !imageView.getAnimation().isInitialized()) {
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                lastLoadingLabelMessage = "";
                loadingLabelLayout.setVisibility(View.GONE);
            }
        });
    }

    public void reset() {
        Log.i(TAG,"map fragment reset");
        if(mMapView!=null)
            mMapView.reset();
    }

    @Override
    public void onMapReady() {

        myPositionButton.setVisibility(View.VISIBLE);

        myPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapView.updateCamera(true);
            }
        });

        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(mSharedPref.getBoolean("undetected_beacons", false)) {
            if(!showUndetectedBeacons)
                showUndetectedBeacons();
            showUndetectedBeacons = true;
        } else {
            if(showUndetectedBeacons)
                if(mMapView!=null)
                    mMapView.removeMarkers(new MarkerSearchPattern().tag(TAG_UNDETECTED_BEACON_MARKER));
            showUndetectedBeacons = false;
        }

        UbuduMap map = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getMap();
        UbuduPositionUpdate lastUpdate = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getLastPositionUpdate();
        if(lastUpdate!=null) {
            UbuduPosition lastPosition = lastUpdate.getClosestNavigablePoint();
            if (lastPosition != null && map != null) {
                List<UbuduZone> zones = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getMap().getZonesForPosition(lastPosition.getCartesianCoordinates());
                zonesChanged(zones);
            }
        }
    }

    @Override
    public void onZoneClicked(Zone zone, com.google.android.gms.maps.model.Polygon polygon) {

    }

    @Override
    public void onMarkerClicked(Marker marker, com.google.android.gms.maps.model.Marker marker1) {

    }
}
