package com.ubudu.sampleapp.ubudu;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.ubudu.indoorlocation.UbuduBeacon;
import com.ubudu.indoorlocation.UbuduCoordinates2D;
import com.ubudu.indoorlocation.UbuduIndoorLocationManager;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduPoint;
import com.ubudu.indoorlocation.UbuduZone;
import com.ubudu.sampleapp.map.MapInterface;

import java.util.List;

import static com.ubudu.sampleapp.MyApplication.getAppContext;


/**
 * Created by mgasztold on 01/11/15.
 */
public class UbuduManager {

    public static final String MAP_KEY = "0c6e343044640133cedd1ad861f40fb6";
    public static final String OVERLAY_FILE_NAME = "wut.png";
    public static final String MAP_FILE_NAME = "wut.json";

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

        if(mIndoorLocationManager.localizationUpdatesRunning()) {
            tellAppILStarted();
        }
    }

    public UbuduManager() {
        mIndoorLocationDelegate = new IndoorLocationDelegate(this);

        mIndoorLocationManager = UbuduIndoorLocationSDK.getSharedInstance(getAppContext()).getIndoorLocationManager();

        mIndoorLocationManager.setIndoorLocationDelegate(mIndoorLocationDelegate);
        //mIndoorLocationManager.loadMapWithKey(MAP_KEY);
        mIndoorLocationManager.loadMapFromAssetsFile(MAP_FILE_NAME);
        mIndoorLocationManager.setAutomaticServiceRestart(false);
        mIndoorLocationManager.setRangingScanPeriods(1100,1100);

        if (!mIndoorLocationManager.localizationUpdatesRunning() && mIndoorLocationManager.automaticServiceRestartIsEnabled()) {
            start();
        }
    }

    public void start(){
        if(mIndoorLocationManager!=null){
            mIndoorLocationManager.start();
        }
    }

    public void stop(){
        if(mIndoorLocationManager!=null){
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
            return mIndoorLocationManager.localizationUpdatesRunning();
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

    public void setMapFileName(String mapFileName) {
        if(mIndoorLocationManager!=null)
            mIndoorLocationManager.loadMapFromAssetsFile(mapFileName);
    }

    public String getMapUrl() {
        if(mIndoorLocationManager!=null)
            return mIndoorLocationManager.map().imageUrl();
        else return null;
    }

    public UbuduCoordinates2D getGeoCoordinates(UbuduPoint point) {
        if(mIndoorLocationManager!=null)
            return mIndoorLocationManager.geoCoordinates(point);
        else return null;
    }

    public UbuduCoordinates2D bottomRightAnchorCoordinates() {
        if(mIndoorLocationManager!=null)
            return mIndoorLocationManager.map().bottomRightAnchorCoordinates().toDeg();
        else return null;
    }

    public UbuduCoordinates2D topLeftAnchorCoordinates() {
        if(mIndoorLocationManager!=null)
            return mIndoorLocationManager.map().topLeftAnchorCoordinates().toDeg();
        else return null;
    }

    public List<UbuduBeacon> mapBeacons() {
        if(mIndoorLocationManager!=null)
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

    public void reset() {
        if(mIndoorLocationManager!=null){
            mIndoorLocationManager.reset();
            reset = true;
        }
    }

    public void reseted(){
        reset = false;
    }

    public boolean shouldReset() {
        return reset;
    }
}
