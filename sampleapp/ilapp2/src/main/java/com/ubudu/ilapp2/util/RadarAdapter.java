package com.ubudu.ilapp2.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ubudu.indoorlocation.UbuduBeacon;
import com.ubudu.ilapp2.R;

import java.util.ArrayList;


/**
 * Created by mgasztold on 03/03/16.
 */
public class RadarAdapter extends ArrayAdapter<UbuduBeacon> {

    private ArrayList<UbuduBeacon> beacons = new ArrayList<UbuduBeacon>();

    @Override
    public void add(UbuduBeacon object) {
        beacons.add(object);
        super.add(object);
    }

    @Override
    public void clear(){
        super.clear();
        beacons.clear();
    }

    public void putLogs(ArrayList<UbuduBeacon> m){
        beacons.clear();
        beacons.addAll(m);
    }

    public RadarAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public int getCount() {
        return this.beacons.size();
    }

    public UbuduBeacon getItem(int index) {
        return this.beacons.get(index);
    }

    public void recoverMsgs(ArrayList<UbuduBeacon> c){
        beacons = c;
    }

    public ArrayList<UbuduBeacon> getElements(){
        return beacons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_beacon, parent, false);
        }

        UbuduBeacon b = getItem(position);

        TextView name = (TextView) view.findViewById(R.id.beacon_name);
        name.setText(b.name());

        TextView rssi = (TextView) view.findViewById(R.id.beacon_rssi_value);
        if(b.rssi()<-85d){
            TextView rssiDbm = (TextView) view.findViewById(R.id.beacon_rssi_dbm);
            rssiDbm.setTextColor(this.getContext().getResources().getColor(R.color.colorWarning));
            rssi.setTextColor(this.getContext().getResources().getColor(R.color.colorWarning));
        } else {
            TextView rssiDbm = (TextView) view.findViewById(R.id.beacon_rssi_dbm);
            rssiDbm.setTextColor(this.getContext().getResources().getColor(R.color.colorPrimaryDark));
            rssi.setTextColor(this.getContext().getResources().getColor(R.color.colorPrimaryDark));
        }
        rssi.setText(String.valueOf(b.rssi()));

        TextView major = (TextView) view.findViewById(R.id.beacon_major_value);
        major.setText(String.valueOf(b.major()));

        TextView minor = (TextView) view.findViewById(R.id.beacon_minor_value);
        minor.setText(String.valueOf(b.minor()));

        TextView uuid = (TextView) view.findViewById(R.id.beacon_proximity_uuid_value);
        String proximity_uuid = b.proximityUUID();
        uuid.setText(String.valueOf(proximity_uuid.substring(proximity_uuid.length()-12,proximity_uuid.length())));

        TextView distance = (TextView) view.findViewById(R.id.beacon_distance_value);
        distance.setText(String.valueOf(b.accuracy()+" m"));

        TextView txPower = (TextView) view.findViewById(R.id.beacon_tx_power_value);
        txPower.setText(String.valueOf((int) b.calibration()));

        if ((position % 2) == 0) {
            // number is even
            view.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimaryLight));
        } else {
            // number is odd
            view.setBackgroundColor(getContext().getResources().getColor(R.color.colorAccentLight));
        }

        return view;
    }

}