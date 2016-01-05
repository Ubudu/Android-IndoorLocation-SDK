package com.ubudu.sampleapp.map;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by mgasztold on 14/12/15.
 */
public class MapBearingManager implements SensorEventListener {

    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] mRotationMatrix = new float[16];
    private BearingListener listener;
    private float mDeclination = 0;

    public MapBearingManager(Context ctx, BearingListener listener){
        mContext = ctx;
        mSensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        this.listener = listener;
    }

    public void updateDeclination(LatLng currentLocation) {
        GeomagneticField field = new GeomagneticField(
                (float) currentLocation.latitude,
                (float) currentLocation.longitude,
                (float) 0,
                System.currentTimeMillis()
        );
        mDeclination = field.getDeclination();
    }

    public void register() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void unregister(){
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);

            if(listener!=null)
                listener.bearing((float) Math.toDegrees(orientation[0]) + mDeclination);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface BearingListener {

        void bearing(float bearing);

    }
}
