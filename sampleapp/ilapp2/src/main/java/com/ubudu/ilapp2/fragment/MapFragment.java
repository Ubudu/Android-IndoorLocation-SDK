package com.ubudu.ilapp2.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.google.android.gms.maps.model.LatLng;
import com.ubudu.ilapp2.R;
import com.ubudu.indoorlocation.UbuduBeacon;
import com.ubudu.indoorlocation.UbuduCompassListener;
import com.ubudu.indoorlocation.UbuduCoordinates2D;
import com.ubudu.indoorlocation.UbuduIndoorLocationDelegate;
import com.ubudu.indoorlocation.UbuduIndoorLocationManager;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduMap;
import com.ubudu.indoorlocation.UbuduPoint;
import com.ubudu.indoorlocation.UbuduPositionUpdate;
import com.ubudu.indoorlocation.UbuduZone;
import com.ubudu.ubudumaplayout.UbuduMapLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mgasztold on 05/10/16.
 */

public class MapFragment extends BaseFragment implements UbuduIndoorLocationDelegate, UbuduMapLayout.UbuduMapLayoutEventListener {

    public static final String TAG = MapFragment.class.getCanonicalName();

    private static final String TAG_BEACON_MARKER = "beacon_marker";
    private static final String TAG_UNDETECTED_BEACON_MARKER = "undetected_beacon_marker";

    private DrawerLayout mRootView;

    private boolean showUndetectedBeacons = false;
    private static String lastLoadingLabelMessage = "";

    @BindView(R.id.map)
    UbuduMapLayout mMapView;
    @BindView(R.id.loading_label)
    LinearLayout loadingLabelLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = (DrawerLayout) inflater.inflate(R.layout.fragment_map, container, false);
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
        lastLoadingLabelMessage = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewController().mapFragmentResumed();

        UbuduIndoorLocationManager mIndoorLocationManager = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager();
        mIndoorLocationManager.setIndoorLocationDelegate(this);

        mIndoorLocationManager.setCompassListener(new UbuduCompassListener() {
            @Override
            public void azimuthUpdated(float azimuth) {
                if(mMapView!=null)
                    mMapView.updateBearing(azimuth);
            }
        });
        if(mIndoorLocationManager.map()!=null) {
            if(mIndoorLocationManager.getLastPositionUpdate()!=null)
                zonesChanged(mIndoorLocationManager.map().getZonesForPosition(mIndoorLocationManager.getLastPositionUpdate().getClosestNavigablePoint().getCartesianCoordinates()));
            setTilesOverlay(mIndoorLocationManager.map().uuid());
        }

        if(!lastLoadingLabelMessage.equals(""))
            showLoadingLabelWithText(lastLoadingLabelMessage);
    }

    @Override
    public void onPause() {
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setIndoorLocationDelegate(null);
        super.onPause();
    }

    @Override
    public void positionChanged(UbuduPositionUpdate ubuduPositionUpdate) {
        if(mMapView!=null) {
            mMapView.markPosition(ubuduPositionUpdate.getClosestNavigablePoint().getGeographicalCoordinates().latitude()
                    , ubuduPositionUpdate.getClosestNavigablePoint().getGeographicalCoordinates().longitude());
            mMapView.updateCamera(true);
        }
        if(ubuduPositionUpdate.getUpdateOrigin()!=(UbuduPositionUpdate.UPDATE_ORIGIN_MOTION))
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
            mMapView.clearHighlightedZones();
            Iterator<UbuduZone> iter = zones.iterator();
            while (iter.hasNext()) {
                UbuduZone zone = iter.next();
                List<LatLng> coords = new ArrayList<>();
                Iterator<UbuduPoint> iter1 = zone.coordinates().iterator();
                while (iter1.hasNext()) {
                    UbuduPoint p = iter1.next();
                    UbuduCoordinates2D c = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().map().geoCoordinates(p);
                    if(c!=null)
                        coords.add(new LatLng(c.latitude(), c.longitude()));
                }
                mMapView.highlightZone(coords, zone.name(), zone.color());
            }
        }
    }

    @Override
    public void closestNavigablePointChanged(UbuduPositionUpdate ubuduPositionUpdate) {

    }

    @Override
    public void beaconsUpdated(List<UbuduBeacon> beacons) {
        mMapView.clearCustomMarkersWithTag(TAG_BEACON_MARKER);
        boolean noBeaconValidForPositioning = true;
        for(UbuduBeacon beacon : beacons) {
            LatLng coordinates = new LatLng(beacon.geographicalPosition().latitude(), beacon.geographicalPosition().longitude());

            String color = "#FFF48642";
            if (beacon.isValidatedForPositionCalculation()) {
                color = "#8bff8f";
                noBeaconValidForPositioning = false;
            }

            mMapView.addCustomMarker(TAG_BEACON_MARKER,coordinates,beacon.major()+"/"+beacon.minor(),40,color);
        }
        if(beacons.size()==0 || noBeaconValidForPositioning)
            showLoadingLabelWithText("No beacons in range...");
        else
            onProgressDialogHideRequested();
    }

    @Override
    public void mapChanged(String uuid, int level) {
        Log.i(TAG,"mapChanged");
        mMapView.reset();
        setTilesOverlay(uuid);
        if(showUndetectedBeacons){
            showUndetectedBeacons();
        }
    }

    @Override
    public void movementChanged(boolean b) {

    }

    private void showUndetectedBeacons() {
        mMapView.clearCustomMarkersWithTag(TAG_UNDETECTED_BEACON_MARKER);
        UbuduMap ubuduMap = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().map();
        if(ubuduMap==null)
            return;
        List<UbuduBeacon> mapBeacons = ubuduMap.beacons();
        for(UbuduBeacon beacon : mapBeacons){
            mMapView.addCustomMarker(TAG_UNDETECTED_BEACON_MARKER
                    ,new LatLng(beacon.geographicalPosition().latitude(), beacon.geographicalPosition().longitude())
                    ,beacon.major()+"/"+beacon.minor()
                    ,40
                    ,"#50a5c4e1");
        }
    }

    private void setTilesOverlay(String uuid) {
        try {
            UbuduMap ubuduMap = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().map();
            if(ubuduMap!=null) {
                UbuduCoordinates2D bottomRightAnchorCoordinates = ubuduMap.bottomRightAnchorCoordinates().toDeg();
                UbuduCoordinates2D topLeftAnchorCoordinates = ubuduMap.topLeftAnchorCoordinates().toDeg();
                mMapView.addTileOverlay(ubuduMap.getTilesBaseUrl()
                        , new LatLng(bottomRightAnchorCoordinates.latitude(), topLeftAnchorCoordinates.longitude())
                        , new LatLng(topLeftAnchorCoordinates.latitude(), bottomRightAnchorCoordinates.longitude()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoadingLabelWithText(String text) {
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

    private void onProgressDialogHideRequested() {
        lastLoadingLabelMessage = "";
        loadingLabelLayout.setVisibility(View.GONE);
    }

    public void reset() {
        Log.i(TAG,"map fragment reset");
        if(mMapView!=null)
            mMapView.reset();
    }

    @Override
    public void onMapReady() {
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(mSharedPref.getBoolean("undetected_beacons", false)) {
            if(!showUndetectedBeacons)
                showUndetectedBeacons();
            showUndetectedBeacons = true;
        } else {
            if(showUndetectedBeacons)
                if(mMapView!=null)
                    mMapView.clearCustomMarkersWithTag(TAG_UNDETECTED_BEACON_MARKER);
            showUndetectedBeacons = false;
        }
    }
}
