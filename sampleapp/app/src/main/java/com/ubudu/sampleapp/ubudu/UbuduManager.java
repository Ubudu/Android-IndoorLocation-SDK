package com.ubudu.sampleapp.ubudu;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.LatLng;
import com.ubudu.indoorlocation.UbuduBeacon;
import com.ubudu.indoorlocation.UbuduCoordinates2D;
import com.ubudu.indoorlocation.UbuduIndoorLocationManager;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduMap;
import com.ubudu.indoorlocation.UbuduPoint;
import com.ubudu.indoorlocation.UbuduRangedBeaconsNotifier;
import com.ubudu.indoorlocation.UbuduStartCallback;
import com.ubudu.indoorlocation.UbuduZone;
import com.ubudu.sampleapp.map.MapInterface;

import java.io.InputStream;
import java.util.List;

import static com.ubudu.sampleapp.MyApplication.getAppContext;

/**
 * Created by mgasztold on 01/11/15.
 */
public class UbuduManager {

    private static final String TAG = "UbuduManager";

    private static boolean ENABLE_MAP_OVERLAYS_FETCHING = true;

    private static UbuduManager instance;

    private IndoorLocationDelegate mIndoorLocationDelegate;
    private UbuduIndoorLocationManager mIndoorLocationManager;

    private MapInterface appInterface;

    private boolean reset = true;

    public static synchronized UbuduManager getInstance() {
        if (instance == null) {
            synchronized (UbuduManager.class) {
                if (instance == null)
                    instance = new UbuduManager();
            }
        }
        return instance;
    }

    public void setDelegateAppInterface(MapInterface appInterface){
        this.appInterface = appInterface;

        if(mIndoorLocationManager.positionUpdatesRunning()) {
            tellAppILStarted();
        }
    }

    public static boolean mapOverlaysFetchingEnabled(){
        return ENABLE_MAP_OVERLAYS_FETCHING;
    }

    public UbuduManager() {
        mIndoorLocationDelegate = new IndoorLocationDelegate(this);

        UbuduIndoorLocationSDK mSdk = UbuduIndoorLocationSDK.getSharedInstance(getAppContext());

        mSdk.enableMapOverlaysFetching(ENABLE_MAP_OVERLAYS_FETCHING);
        mSdk.setNamespace("7c62cb6cc409004dc879f3fd7c4d838f0d07dbc8");

        mIndoorLocationManager = mSdk.getIndoorLocationManager();

        mIndoorLocationManager.setIndoorLocationDelegate(mIndoorLocationDelegate);
        //mIndoorLocationManager.loadMapFromAssetsFile(MAP_FILE_NAME);
        mIndoorLocationManager.setAutomaticServiceRestart(false);
        mIndoorLocationManager.setRangingScanPeriods(2000, 1100);
        mIndoorLocationManager.setRangingBetweenScanPeriods(200, 200);

        mIndoorLocationManager.setRangedBeaconsNotifier(new UbuduRangedBeaconsNotifier() {
            @Override
            public void didRangeBeacons(List<UbuduBeacon> rangedBeacons) {
                if (rangedBeacons != null) {
                    android.util.Log.e(TAG, "num of beacons: " + rangedBeacons.size());
                }
            }
        });

        if (!mIndoorLocationManager.positionUpdatesRunning() && mIndoorLocationManager.automaticServiceRestartIsEnabled()) {
            start();
        }
    }

    public void onResume(){
        if(mIndoorLocationDelegate==null)
            mIndoorLocationDelegate = new IndoorLocationDelegate(this);
        mIndoorLocationManager = UbuduIndoorLocationSDK.getSharedInstance(getAppContext()).getIndoorLocationManager();

        mIndoorLocationManager.setIndoorLocationDelegate(mIndoorLocationDelegate);

        if(mIndoorLocationManager.map()!=null) {
            mIndoorLocationDelegate.setMap(mIndoorLocationManager.map());
            tellAppILStarted();
            loadMapOverlay(mIndoorLocationManager.map().uuid(), false);
        }

    }

    public void start(){
        if(mIndoorLocationManager!=null){
            mIndoorLocationManager.start(new UbuduStartCallback() {
                @Override
                public void success() {
                    mIndoorLocationDelegate.setMap(mIndoorLocationManager.map());
                    tellAppILStarted();
                    mIndoorLocationManager.startAzimuthUpdates();
                }

                @Override
                public void failure() {
                    tellAppILStartFailed();
                }

                @Override
                public void restartedAfterContentAutoUpdated() {
                    tellAppILRestartedAfterUpdate();
                }
            });
        }
    }

    private void tellAppILRestartedAfterUpdate() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                loadMapOverlay(mIndoorLocationManager.map().uuid(), true);
            }
        });
    }

    public void stop(){
        if(mIndoorLocationManager!=null){
            mIndoorLocationManager.stopAzimuthUpdates();
            mIndoorLocationManager.stop();
        }
    }

    public void printf(String formatControl, Object... arguments) {
        if(appInterface!=null)
            appInterface.printLog(formatControl, arguments);
    }

    public Context context() {
        return null;
    }

    public void tellAppILStarted() {
        if(appInterface!=null)
            appInterface.indoorLocationStarted();
    }

    public void tellAppILStartFailed() {
        if(appInterface!=null)
            appInterface.indoorLocationStartFailed();
    }

    public void highlightZones(final List<UbuduZone> list) {
        if(appInterface!=null)
            appInterface.highlightZones(list);
    }

    public boolean isStarted() {
        if(mIndoorLocationManager!=null){
            return mIndoorLocationManager.positionUpdatesRunning();
        } else{
            return false;
        }
    }

    public IndoorLocationDelegate delegate(){
        return mIndoorLocationDelegate;
    }


    public void updateBeacons(List<UbuduBeacon> beacons) {
        if(appInterface!=null)
            appInterface.updateActiveBeacons(beacons);
    }

    public String getMapUrl() {
        if(mIndoorLocationManager!=null && mIndoorLocationManager.map()!=null)
            return mIndoorLocationManager.map().imageUrl();
        else return null;
    }

    public UbuduCoordinates2D getGeoCoordinates(UbuduPoint point) {
        if(mIndoorLocationManager!=null && mIndoorLocationManager.map()!=null)
            return mIndoorLocationManager.map().geoCoordinates(point);
        else return null;
    }

    public boolean isMapAvailable(){
        if(mIndoorLocationManager.map()!=null)
            return true;
        else return false;
    }

    public UbuduCoordinates2D bottomRightAnchorCoordinates() {
        if(mIndoorLocationManager!=null && mIndoorLocationManager.map()!=null)
            return mIndoorLocationManager.map().bottomRightAnchorCoordinates().toDeg();
        else return null;
    }

    public UbuduCoordinates2D topLeftAnchorCoordinates() {
        if(mIndoorLocationManager!=null && mIndoorLocationManager.map()!=null)
            return mIndoorLocationManager.map().topLeftAnchorCoordinates().toDeg();
        else return null;
    }

    public List<UbuduBeacon> mapBeacons() {
        if(mIndoorLocationManager!=null && mIndoorLocationManager.map()!=null)
            return mIndoorLocationManager.map().beacons();
        else return null;
    }

    public boolean mapDisplayReady() {
        if(appInterface.readyForPositionUpdates())
            return true;
        else
            return false;
    }

    public void drawPath(List<LatLng> pathGeoCoords) {
        appInterface.drawPath(pathGeoCoords);
    }

    public void setLocationOnMap(double latitude, double longitude) {
        appInterface.setLocationOnMap(latitude, longitude);
    }

    public void reseted(){
        reset = false;
    }

    public boolean shouldReset() {
        return reset;
    }

    public void loadMapOverlay(String uuid, boolean force) {
        if(appInterface!=null)
            appInterface.reloadMapOverlay(uuid, force);
    }

    public InputStream getMapOverlayInputStream(){
        return mIndoorLocationManager.getCurrentMapOverlayInputStream();
    }

    public UbuduMap getMap(){
        return mIndoorLocationManager.map();
    }

    public void stepDetected() {
        if(appInterface!=null)
            appInterface.stepDetected();
    }

    private boolean transitionZonesFloorSwitchingEnabled = false;

    public boolean toggleTransitionZonesFloorSwitchingMode() {
        if(transitionZonesFloorSwitchingEnabled){
            transitionZonesFloorSwitchingEnabled = false;
            mIndoorLocationManager.setFloorSwitchingWhenInTransitionZoneOnly(transitionZonesFloorSwitchingEnabled);
        } else{
            transitionZonesFloorSwitchingEnabled = true;
            mIndoorLocationManager.setFloorSwitchingWhenInTransitionZoneOnly(transitionZonesFloorSwitchingEnabled);
        }
        return transitionZonesFloorSwitchingEnabled;
    }

    public void azimuthUpdated(float azimuth) {
        appInterface.azimuthUpdated(azimuth);
    }
}
