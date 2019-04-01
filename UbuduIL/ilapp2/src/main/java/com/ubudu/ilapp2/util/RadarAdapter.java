package com.ubudu.ilapp2.util;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ubudu.indoorlocation.ILBeacon;
import com.ubudu.ilapp2.R;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


/**
 * Created by mgasztold on 03/03/16.
 */
public class RadarAdapter extends ArrayAdapter<ILBeacon> {

    protected ArrayList<ILBeacon> beacons = new ArrayList<ILBeacon>();

    @Override
    public void add(ILBeacon object) {
        beacons.add(object);
        super.add(object);
    }

    @Override
    public void clear(){
        super.clear();
        beacons.clear();
    }

    private static class RefreshBeaconsListTask extends AsyncTask<List<ILBeacon>,Void,Void> {

        private RadarAdapterRefreshBeaconsListener listener;
        private Handler mHandler = new Handler();
        private RadarAdapter radarAdapter;

        public RefreshBeaconsListTask(RadarAdapterRefreshBeaconsListener listener, RadarAdapter radarAdapter) {
            this.listener = listener;
            this.radarAdapter = radarAdapter;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<ILBeacon>... params) {
            List<ILBeacon> newBeacons = params[0];
            Collections.sort(newBeacons, new Comparator<ILBeacon>() {
                @Override
                public int compare(final ILBeacon object1, final ILBeacon object2) {
                    try {
                        String object1Name = "";
                        String object2Name = "";
                        if (object1.getBluetoothDevice().getName() != null)
                            object1Name = object1.getBluetoothDevice().getName();
                        if (object2.getBluetoothDevice().getName() != null)
                            object2Name = object2.getBluetoothDevice().getName();
                        return object1Name.compareTo(object2Name);
                    } catch (Exception ex){
                        return 0;
                    }
                }
            });
            radarAdapter.beacons.clear();
            radarAdapter.beacons.addAll(newBeacons);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listener.beaconsReady();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    radarAdapter.notifyDataSetChanged();
                }
            },500L);
        }
    }

    public void refreshBeacons(List<ILBeacon> beacons, RadarAdapterRefreshBeaconsListener listener) {
        new RefreshBeaconsListTask(listener, this).execute(beacons);
    }

    public RadarAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public int getCount() {
        return this.beacons.size();
    }

    public ILBeacon getItem(int index) {
        return this.beacons.get(index);
    }

    public void recoverMsgs(ArrayList<ILBeacon> c){
        beacons = c;
    }

    public ArrayList<ILBeacon> getElements(){
        return beacons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolderItem viewHolderItem;

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_beacon, parent, false);

            // well set up the ViewHolder
            viewHolderItem = new ViewHolderItem();
            viewHolderItem.nameTextView = (TextView) view.findViewById(R.id.beacon_name);
            viewHolderItem.rssiTextView = (TextView) view.findViewById(R.id.beacon_rssi_value);
            viewHolderItem.rssiDbmTextView = (TextView) view.findViewById(R.id.beacon_rssi_dbm);
            viewHolderItem.majorTextView = (TextView) view.findViewById(R.id.beacon_major_value);
            viewHolderItem.minorTextView = (TextView) view.findViewById(R.id.beacon_minor_value);
            viewHolderItem.distanceTextView = (TextView) view.findViewById(R.id.beacon_distance_value);
            viewHolderItem.batteryLabelTextView = (TextView) view.findViewById(R.id.beacon_battery_level_name);
            viewHolderItem.batteryLevelTextView = (TextView) view.findViewById(R.id.beacon_battery_level_value);
            viewHolderItem.beaconPicImageView = (ImageView) view.findViewById(R.id.beacon_pin);

            // store the holder with the view.
            view.setTag(viewHolderItem);
        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolderItem = (ViewHolderItem) view.getTag();
        }

        if ((position % 2) == 0) {
            // number is even
            view.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimaryLight));
        } else {
            // number is odd
            view.setBackgroundColor(getContext().getResources().getColor(R.color.colorAccentLight));
        }

        ILBeacon b = getItem(position);
        viewHolderItem.nameTextView.setText(b.getBluetoothDevice().getName());
        if(b.getRssi()<-85d){
            viewHolderItem.rssiDbmTextView.setTextColor(this.getContext().getResources().getColor(R.color.colorWarning));
            viewHolderItem.rssiTextView.setTextColor(this.getContext().getResources().getColor(R.color.colorWarning));
        } else {
            viewHolderItem.rssiDbmTextView.setTextColor(this.getContext().getResources().getColor(R.color.colorPrimaryDark));
            viewHolderItem.rssiTextView.setTextColor(this.getContext().getResources().getColor(R.color.colorPrimaryDark));
        }
        viewHolderItem.rssiTextView.setText(String.format(Locale.US,"%.2f",b.getRssi()));
        viewHolderItem.majorTextView.setText(String.valueOf(b.getMajor()));
        viewHolderItem.minorTextView.setText(String.valueOf(b.getMinor()));
        viewHolderItem.distanceTextView.setText(String.format(Locale.US,"%.2f m",b.getDistance()));
        if(b.getBatteryLevel()>0) {
            viewHolderItem.batteryLabelTextView.setVisibility(View.VISIBLE);
            viewHolderItem.batteryLevelTextView.setVisibility(View.VISIBLE);
            viewHolderItem.batteryLevelTextView.setText(String.format(Locale.US, "%d %%",b.getBatteryLevel()));
        } else {
            viewHolderItem.batteryLabelTextView.setVisibility(View.INVISIBLE);
            viewHolderItem.batteryLevelTextView.setVisibility(View.INVISIBLE);
        }

        if(b.getMapUuid()==null) {
            viewHolderItem.beaconPicImageView.setColorFilter(this.getContext().getResources().getColor(R.color.button_off), PorterDuff.Mode.SRC_ATOP);
        } else {
            UbuduMap map = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getMap();
            if(map!=null && map.getUuid().equals(b.getMapUuid()))
                viewHolderItem.beaconPicImageView.setColorFilter(this.getContext().getResources().getColor(R.color.button_on), PorterDuff.Mode.SRC_ATOP);
            else {
                UbuduMap mapMatchingBeacon = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().getMapWithUuid(b.getMapUuid());
                if(mapMatchingBeacon!=null) {
                    viewHolderItem.beaconPicImageView.setColorFilter(this.getContext().getResources().getColor(R.color.button_warning), PorterDuff.Mode.SRC_ATOP);
                } else {
                    viewHolderItem.beaconPicImageView.setColorFilter(this.getContext().getResources().getColor(R.color.colorWarning), PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
        return view;
    }

    public interface RadarAdapterRefreshBeaconsListener {
        void beaconsReady();
    }

    private static class ViewHolderItem {
        TextView nameTextView;
        TextView rssiTextView;
        TextView rssiDbmTextView;
        TextView majorTextView;
        TextView minorTextView;
        TextView distanceTextView;
        TextView batteryLabelTextView;
        TextView batteryLevelTextView;
        ImageView beaconPicImageView;
    }
}