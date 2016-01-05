package com.ubudu.sampleapp.map;

/**
 * Created by mgasztold on 10/12/15.
 */
public abstract class MapEventListener {

    public abstract void notifyRescalingImageStarted();

    public abstract void notifyMapOverlayFetchingError(String errorMsg);

    public abstract void notifyMapOverlayDownloadProgress(int progressPercent);

    public abstract void notifyMapOverlayFetchedSuccessfully();

    public abstract void onMapReady();
}
