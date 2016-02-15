package com.ubudu.sampleapp.map;

import com.google.android.gms.maps.model.LatLng;
import com.ubudu.indoorlocation.UbuduBeacon;
import com.ubudu.indoorlocation.UbuduZone;

import java.util.List;

public interface MapInterface {

	/**
	 * printLog function allowing to log app's events (for debug purposes)
 	 */
	void printLog(String formatControl, Object... arguments);

	/**
	 * Informs the app that Ubudu Indoor Location SDK has been started successfully
	 */
	void indoorLocationStarted();

	/**
	 * Informs the app that Ubudu Indoor Location SDK start failed
	 */
	void indoorLocationStartFailed();

	/**
	 *
	 * @return true if the app is ready for position updates, false otherwise
	 */
	boolean readyForPositionUpdates();

	/**
	 * Sets new user location on map
	 * @param latitude latitude in [deg]
	 * @param longitude longitude in [deg]
	 */
	void setLocationOnMap(double latitude, double longitude);

	/**
	 * Draws a path on map
	 * @param pathGeoCoords path
	 */
	void drawPath(List<LatLng> pathGeoCoords);

	/**
	 * Highlights the zones on map
	 * @param zones list of currently active zones
	 */
	void highlightZones(List<UbuduZone> zones);

	/**
	 *
	 * @param beacons list of currently active beacons
	 */
	void updateActiveBeacons(List<UbuduBeacon> beacons);

	/**
	 * prepare map overlay and beaocns display
	 * @param uuid
	 * @param force
	 */
	void reloadMapOverlay(String uuid, boolean force);

	void stepDetected();
}
