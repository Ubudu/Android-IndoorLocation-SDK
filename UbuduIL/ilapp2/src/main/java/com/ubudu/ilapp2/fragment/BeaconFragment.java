package com.ubudu.ilapp2.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ubudu.ilapp2.R;
import com.ubudu.ilapp2.util.BeaconDetails;
import com.ubudu.indoorlocation.ILBeacon;
import com.ubudu.indoorlocation.UbuduIndoorLocationManager;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduMap;

/**
 * Created by mgasztold on 10/03/16.
 */
public class BeaconFragment extends BaseFragment implements BeaconDetails {

    public static final String TAG = BeaconFragment.class.getSimpleName();

    private ILBeacon beacon;

    private double distanceVariation = 0;
    private double maxDistance = 0;
    private double minDistance = 1000;

    Button backButton;
    ImageView beaconStatusImage;
    TextView proximityUuid;
    TextView name;
    TextView major;
    TextView minor;
    TextView rssi;
    TextView distance;
    TextView tx;
    TextView battery;
    TextView beaconStatusTextView;
    TextView macAddressTextView;
    TextView distanceVariationTextView;

    public BeaconFragment() {
    }

    public void setBeacon(ILBeacon item){
        beacon = item;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_beacon, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewController().onBeaconFragmentResumed();
    }

    @Override
    public void onPause() {
        getViewController().onBeaconFragmentPaused();
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getViewController().setObservedBeaconInterface(this);

        beaconStatusImage = (ImageView) view.findViewById(R.id.beacon_details_image);
        proximityUuid = (TextView) view.findViewById(R.id.proximity_uuid_val);
        name = (TextView) view.findViewById(R.id.name_val);
        macAddressTextView = (TextView) view.findViewById(R.id.mac_val);
        major = (TextView) view.findViewById(R.id.major_val);
        minor = (TextView) view.findViewById(R.id.minor_val);
        rssi = (TextView) view.findViewById(R.id.rssi_val);
        distance = (TextView) view.findViewById(R.id.distance_val);
        tx = (TextView) view.findViewById(R.id.tx_val);
        battery = (TextView) view.findViewById(R.id.battery_val);
        beaconStatusTextView = (TextView) view.findViewById(R.id.status_val);
        distanceVariationTextView = (TextView) view.findViewById(R.id.distance_variation_val);

        backButton = (Button) view.findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getViewController().onRadarFragmentRequested();
            }
        });
        if(beacon!=null)
            updateData(beacon);

        final Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                maxDistance = 0;
                minDistance = 1000;
                h.postDelayed(this,5000);
            }
        });
    }

    @Override
    public void updateData(final ILBeacon beacon) {
        if(beacon.getDistance()<minDistance)
            minDistance = beacon.getDistance();
        if(beacon.getDistance()>maxDistance)
            maxDistance = beacon.getDistance();

        if(maxDistance!=1000) {
            distanceVariation = maxDistance - minDistance;
            if(distanceVariation>0)
                distanceVariationTextView.setText(String.format("%.2f", distanceVariation) + " m");
        }

        beaconStatusImage.setVisibility(View.VISIBLE);
        if(beacon.getMapUuid()==null) {
            beaconStatusImage.setColorFilter(this.getContext().getResources().getColor(R.color.button_off), PorterDuff.Mode.SRC_ATOP);
            beaconStatusTextView.setText("Beacon is not assigned to any floor");
        } else {
            UbuduIndoorLocationManager manager = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager();
            UbuduMap map = manager.getMap();
            if(map!=null && map.getUuid().equals(beacon.getMapUuid())) {
                beaconStatusImage.setColorFilter(this.getContext().getResources().getColor(R.color.button_on), PorterDuff.Mode.SRC_ATOP);
                beaconStatusTextView.setText("Beacon matching current floor \nLevel: "+map.getLevel()
                        +"\nExt. level: "+map.getExternalLevelId()
                        +"\nuuid: "+beacon.getMapUuid());
            } else {
                UbuduMap mapMatchingBeacon = manager.getMapWithUuid(beacon.getMapUuid());
                if(mapMatchingBeacon!=null) {
                    beaconStatusTextView.setText("Beacon matching other floor \nLevel: "+mapMatchingBeacon.getLevel()
                            +"\nExt. level: "+mapMatchingBeacon.getExternalLevelId()
                            +"\nuuid: "+beacon.getMapUuid());
                    beaconStatusImage.setColorFilter(this.getContext().getResources().getColor(R.color.button_warning), PorterDuff.Mode.SRC_ATOP);
                } else {
                    beaconStatusTextView.setText("Beacon matching an unknown floor \nuuid: " + beacon.getMapUuid());
                    beaconStatusImage.setColorFilter(this.getContext().getResources().getColor(R.color.colorWarning), PorterDuff.Mode.SRC_ATOP);
                }
            }
        }

        proximityUuid.setText(beacon.getProximityUuid());
        name.setText(beacon.getBluetoothDevice().getName());
        macAddressTextView.setText(beacon.getBluetoothDevice().getAddress());
        major.setText(String.valueOf(beacon.getMajor()));
        minor.setText(String.valueOf(beacon.getMinor()));
        rssi.setText(String.valueOf(beacon.getRssi()) + " dBm");
        distance.setText(String.valueOf(beacon.getDistance()) + " m");
        tx.setText(String.valueOf(beacon.getTxPower()) + " dBm");
        battery.setText(String.valueOf(beacon.getBatteryLevel() + " %"));
    }

    @Override
    public ILBeacon getUbuduBeacon() {
        return beacon;
    }
}
