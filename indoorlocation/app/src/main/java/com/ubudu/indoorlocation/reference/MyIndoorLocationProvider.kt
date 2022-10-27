package com.ubudu.indoorlocation.reference

import android.content.Context
import android.os.Handler
import android.util.Log
import com.ubudu.indoorlocation.*
import io.indoorlocation.core.IndoorLocation
import io.indoorlocation.core.IndoorLocationProvider
import java.util.*

class MyIndoorLocationProvider(private val context: Context) : IndoorLocationProvider(), UbuduIndoorLocationDelegate {

    companion object {
        const val TAG = "UBUDU_LOCATION_PROVIDER"
        const val UBUDU_APP_NAMESPACE = "bb80ac3a01b73f039fdd0155f0b06d0cc1996742"
    }

    private val mIndoorLocationManager: UbuduIndoorLocationManager = UbuduIndoorLocationSDK.getSharedInstance(context).indoorLocationManager
    private val indoorLocation: IndoorLocation = IndoorLocation(TAG,0.0,0.0,0.0,System.currentTimeMillis())

    override fun supportsFloor(): Boolean {
        return true
    }

    override fun start() {

        mIndoorLocationManager.setBackgroundOperationEnabled(
            false,
            BackgroundNotification.getForegroundServiceNotification(context)
        )

        mIndoorLocationManager.setIndoorLocationDelegate(this)

        mIndoorLocationManager.setRssiThreshold(-95)
        mIndoorLocationManager.setAccuracyThreshold(30)
        mIndoorLocationManager.setParticleFilterSpread(0.3,0.15)

        mIndoorLocationManager.configuration = object : Configuration {

            val configuration = ModeStableConfiguration()

            /**
             * @return millisecond timeout after which the beacon is forgotten if no new advertising arrived
             */
            override fun getBeaconTimeout(): Long {
                return configuration.beaconTimeout
            }

            /**
             * @return minimum number of beacons to compute a position. Default is 1.
             */
            override fun getMinNumberOfBeaconsForComputation(): Int {
                return configuration.minNumberOfBeaconsForComputation
            }

            /**
             * @return number of position computations that must be consecutively farer than getImprobableJump() meters from the last position in order to have the filtered position be moved to the new raw position. This is to prevent the position getting stuck in weird places like e.g. corners of non navigable zones.
             */
            override fun getHoldOffCounterMax(): Int {
                return configuration.holdOffCounterMax
            }

            /**
             * @return the meters distance between the filtered position and raw position which triggers immediate reset of the filtered position into the raw position
             */
            override fun getImprobableDistanceBetweenFilteredUpdateAndBleUpdate(): Double {
                return configuration.improbableDistanceBetweenFilteredUpdateAndBleUpdate
            }

            /**
             * @return improbable position change in meters (a distance between previous position and new position).
             */
            override fun getImprobableJump(): Double {
                return configuration.improbableJump
            }

            /**
             * @return max GPS error reported by Android API below which GPS can be used (if BLE positioning is not possible)
             */
            override fun getMaxGpsError(): Double {
                return configuration.maxGpsError
            }

            /**
             * @return if true then attractor mode will be used, meaning that positions will be only computed at coordinates of the closest beacon.
             */
            override fun getAttractorModeFlag(): Boolean {
                return configuration.attractorModeFlag
            }

        }

        mIndoorLocationManager.setRangedBeaconsNotifier {
            Log.i(TAG,"detected beacons count ${it.size}")
        }

        mIndoorLocationManager.setCompassListener {
            indoorLocation.bearing = it
        }

        val mIndoorLocationSdk = UbuduIndoorLocationSDK.getSharedInstance(context)
        mIndoorLocationSdk.setNamespace(UBUDU_APP_NAMESPACE, object :
            UbuduResultListener {
            override fun error(error: String?) {
                Log.e(TAG,"Ubudu application error: $error")
                Handler().postDelayed({start()},5000L)
                listeners?.forEach {
                    it.onProviderError(Error(error))
                }
            }

            override fun success() {
                Log.d(TAG,"Ubudu application fetched and ready")

                mIndoorLocationManager.start(object : UbuduStartCallback {
                    override fun onAllMapsReady() {
                        Log.d(TAG,"On all maps ready")
                    }

                    override fun onSuccess() {
                        Log.d(TAG,"Ubudu Indoor Location SDK started successfully")
                        listeners?.forEach {
                            it.onProviderStarted()
                        }
                    }

                    override fun onRestartedAfterContentAutoUpdated() {
                        Log.d(TAG,"Ubudu Indoor Location SDK maps data files have been updated")
                    }

                    override fun onFailure() {
                        val msg = "Ubudu Indoor Location SDK could not be started"
                        Log.e(TAG,msg)
                        listeners?.forEach {
                            it.onProviderError(Error(msg))
                        }
                    }

                })
            }
        })
    }

    override fun stop() {
        mIndoorLocationManager.stop()
        listeners?.forEach {
            it.onProviderStopped()
        }
    }

    override fun isStarted(): Boolean {
        return mIndoorLocationManager.isStarted
    }

    override fun closestBeaconChanged(positionUpdate: UbuduPositionUpdate?) {

    }

    override fun closestZoneChanged(positionUpdate: UbuduPositionUpdate?) {

    }

    override fun closestNavigablePointChanged(positionUpdate: UbuduPositionUpdate?) {

    }

    override fun mapChanged(uuid: String?, currentFloor: Int) {
        Log.d(TAG,"new map uuid: $uuid , currentFloor: $currentFloor")
    }

    override fun beaconsUpdated(beacons: MutableList<ILBeacon>?) {
        Log.d(TAG,"beaconsUpdated: beacons count ${beacons?.size}")
    }

    override fun zonesChanged(zones: MutableList<UbuduZone>?) {

    }

    override fun movementChanged(isMoving: Boolean) {

    }

    override fun positionChanged(positionUpdate: UbuduPositionUpdate?) {
        if(positionUpdate!=null) {
            val location = positionUpdate.closestNavigablePoint
            Log.d(TAG,"new geo pos: ${location.geographicalCoordinates.latitude}, ${location.geographicalCoordinates.longitude}")

            indoorLocation.latitude = location.geographicalCoordinates.latitude
            indoorLocation.longitude = location.geographicalCoordinates.longitude
            indoorLocation.floor = location.level.toDouble()
            indoorLocation.time = Date().time
            indoorLocation.provider = when(positionUpdate.updateOrigin) {
                UbuduPositionUpdate.UPDATE_ORIGIN_BEACONS -> "BLE_CENTROID"
                UbuduPositionUpdate.UPDATE_ORIGIN_BEACONS_ATTRACTOR -> "BLE_ATTRACTOR"
                UbuduPositionUpdate.UPDATE_ORIGIN_GPS -> "GPS"
                UbuduPositionUpdate.UPDATE_ORIGIN_MOTION -> "MOTION"
                UbuduPositionUpdate.UPDATE_ORIGIN_UWB -> "UWB"
                else -> ""
            }

            listeners?.forEach {
                it.onIndoorLocationChange(indoorLocation)
            }
        }
    }

    fun onPause() {
        mIndoorLocationManager.unbind()
    }

    fun onResume() {
        mIndoorLocationManager.bind()
    }
}