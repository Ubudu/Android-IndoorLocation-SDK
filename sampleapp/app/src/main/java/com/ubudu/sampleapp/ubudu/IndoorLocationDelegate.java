package com.ubudu.sampleapp.ubudu;

import android.annotation.SuppressLint;

import com.google.android.gms.maps.model.LatLng;
import com.ubudu.indoorlocation.UbuduBeacon;
import com.ubudu.indoorlocation.UbuduCoordinates2D;
import com.ubudu.indoorlocation.UbuduIndoorLocationDelegate;
import com.ubudu.indoorlocation.UbuduIndoorLocationManager;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduMap;
import com.ubudu.indoorlocation.UbuduPoint;
import com.ubudu.indoorlocation.UbuduPositionUpdate;
import com.ubudu.indoorlocation.UbuduZone;
import com.ubudu.sampleapp.utils.MathUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ubudu.sampleapp.MyApplication.getAppContext;

public class IndoorLocationDelegate implements UbuduIndoorLocationDelegate {

    @SuppressWarnings("unused")
    private static final String TAG = "ubudu.IndoorLocationDelegate";

    private UbuduManager mManager;
    UbuduIndoorLocationManager mIndoorLocationManager;

    public UbuduPoint lastPosition, currentSmoothedPositionNavPoint;
    public List<UbuduPoint> path;
    private UbuduMap ubuduMap;

    public IndoorLocationDelegate(UbuduManager manager) {
        mIndoorLocationManager = UbuduIndoorLocationSDK.getSharedInstance(getAppContext()).getIndoorLocationManager();
        mManager = manager;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void positionChanged(UbuduPositionUpdate ubuduPositionUpdate) {

        if (mManager.mapDisplayReady()) {
            if (ubuduMap != null) {
                if (lastPosition != null && !mManager.shouldReset()) {
                    currentSmoothedPositionNavPoint = ubuduMap.getClosestNavigablePointFromPosition(ubuduPositionUpdate.getSmoothedPosition());
                    path = ubuduMap.path(lastPosition, currentSmoothedPositionNavPoint);
                    if (path != null) {
                        List<LatLng> pathGeoCoords = new ArrayList<>();
                        Iterator<UbuduPoint> iter = path.iterator();
                        UbuduCoordinates2D geoCoords;
                        while (iter.hasNext()) {
                            geoCoords = mIndoorLocationManager.geoCoordinates(iter.next());
                            pathGeoCoords.add(new LatLng(geoCoords.latitude(), geoCoords.longitude()));
                        }
                        mManager.drawPath(pathGeoCoords);
                    }
                }
                lastPosition = ubuduPositionUpdate.getClosestNavigablePoint();

                UbuduCoordinates2D geoCoords = mIndoorLocationManager.geoCoordinates(lastPosition);
                mManager.setLocationOnMap(geoCoords.latitude(), geoCoords.longitude());
                mManager.reseted();
            } else {
                try {
                    ubuduMap = UbuduIndoorLocationSDK.getSharedInstance(getAppContext()).getIndoorLocationManager().map();
                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    public void closestBeaconChanged(UbuduPositionUpdate ubuduPositionUpdate) {
        if (ubuduPositionUpdate.getClosestBeacon() != null)
            mManager.printf("Closest beacon is now:\n  minor:"
                    + ubuduPositionUpdate.getClosestBeacon().minor() + "\n distance: "+ubuduPositionUpdate.getClosestBeacon().accuracy());
    }

    @Override
    public void closestZoneChanged(UbuduPositionUpdate ubuduPositionUpdate) {
        UbuduZone closestZone = ubuduPositionUpdate.getClosestZone();
        if (ubuduPositionUpdate.getClosestZone() != null)
            mManager.printf("Closest zone is now: " + closestZone.name() + ". Distance: "
                    + MathUtils.round(mIndoorLocationManager
                    .convertPixelDistanceToMeters(closestZone.distanceToPoint(ubuduPositionUpdate.getClosestNavigablePoint())), 2));
    }

    @Override
    public void zonesChanged(List<UbuduZone> list) {
        mManager.highlightZones(list);
        String result = "Zones change: ";
        if (list.size() > 0) {
            Iterator<UbuduZone> it = list.iterator();
            while (it.hasNext()) {
                result += '\n' + it.next().name();
            }
        } else {
            result += "\noutside";
        }
        mManager.printf(result);
    }

    @Override
    public void closestNavigablePointChanged(UbuduPositionUpdate ubuduPositionUpdate) {

    }

    @SuppressLint("LongLogTag")
    @Override
    public void beaconsUpdated(List<UbuduBeacon> beacons) {
        mManager.updateBeacons(beacons);
    }

    @Override
    public void startSucceed() {
        ubuduMap = mIndoorLocationManager.map();
        if (mManager != null) {
            mManager.tellAppILStarted();
        }
    }

    @Override
    public void startFailed() {
        if (mManager != null) {
            mManager.tellAppILStartFailed();
        }
    }
}
