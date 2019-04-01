package com.ubudu.ilapp2.util;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ubudu.ilapp2.R;
import com.ubudu.indoorlocation.ILBeacon;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduMap;

import java.util.List;
import java.util.Locale;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    private List<ILBeacon> beaconsList;
    private Context context;
    private View.OnClickListener myOnClickListener;

    public List<ILBeacon> getItems() {
        return beaconsList;
    }

    public void setItems(List<ILBeacon> newBeacons) {
        beaconsList.clear();
        beaconsList.addAll(newBeacons);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, rssi, rssiDbm, major, minor, distance, batteryLevelTextView, batteryLevelLabelTextView, mac;
        ImageView beaconPicImageView;

        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.beacon_name);
            rssi = view.findViewById(R.id.beacon_rssi_value);
            rssiDbm = view.findViewById(R.id.beacon_rssi_dbm);
            minor = view.findViewById(R.id.beacon_minor_value);
            major = view.findViewById(R.id.beacon_major_value);
            distance = view.findViewById(R.id.beacon_distance_value);
            batteryLevelTextView = view.findViewById(R.id.beacon_battery_level_value);
            batteryLevelLabelTextView = view.findViewById(R.id.beacon_battery_level_name);
            mac = view.findViewById(R.id.mac_value);
            beaconPicImageView = view.findViewById(R.id.beacon_pin);
        }
    }


    public MyRecyclerViewAdapter(Context context, List<ILBeacon> beaconsList, View.OnClickListener myOnClickListener) {
        this.context = context;
        this.beaconsList = beaconsList;
        this.myOnClickListener = myOnClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_beacon, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        myViewHolder.itemView.setOnClickListener(myOnClickListener);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ILBeacon b = beaconsList.get(position);
//        holder.title.setText(movie.getTitle());
//        holder.genre.setText(movie.getGenre());
//        holder.year.setText(movie.getYear());

        holder.name.setText(b.getBluetoothDevice().getName());
        if (b.getRssi() < -85d) {
            holder.rssiDbm.setTextColor(context.getResources().getColor(R.color.colorWarning));
            holder.rssi.setTextColor(context.getResources().getColor(R.color.colorWarning));
        } else {
            holder.rssiDbm.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            holder.rssi.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }
        holder.rssi.setText(String.format(Locale.US,"%.2f",b.getUBeacon().getRunningAverageRssi()));
        holder.major.setText(String.valueOf(b.getMajor()));
        holder.minor.setText(String.valueOf(b.getMinor()));
        holder.distance.setText(String.format(Locale.US,"%.2f m",b.getDistance()));
        holder.mac.setText(b.getUBeacon().getBluetoothAddress());

        holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorAccentLight));

        if (b.getBatteryLevel() > 0) {
            holder.batteryLevelTextView.setVisibility(View.VISIBLE);
            holder.batteryLevelLabelTextView.setVisibility(View.VISIBLE);
            String batteryLevel = String.valueOf(b.getBatteryLevel()) + " %";
            holder.batteryLevelTextView.setText(batteryLevel);
        } else {
            holder.batteryLevelTextView.setVisibility(View.INVISIBLE);
            holder.batteryLevelLabelTextView.setVisibility(View.INVISIBLE);
        }

        if(b.getMapUuid()==null) {
            holder.beaconPicImageView.setColorFilter(context.getResources().getColor(R.color.button_off), PorterDuff.Mode.SRC_ATOP);
        } else {
            UbuduMap map = UbuduIndoorLocationSDK.getSharedInstance(context).getIndoorLocationManager().getMap();
            if(map!=null && map.getUuid().equals(b.getMapUuid()))
                holder.beaconPicImageView.setColorFilter(context.getResources().getColor(R.color.button_on), PorterDuff.Mode.SRC_ATOP);
            else {
                UbuduMap mapMatchingBeacon = UbuduIndoorLocationSDK.getSharedInstance(context).getIndoorLocationManager().getMapWithUuid(b.getMapUuid());
                if(mapMatchingBeacon!=null) {
                    holder.beaconPicImageView.setColorFilter(context.getResources().getColor(R.color.button_warning), PorterDuff.Mode.SRC_ATOP);
                } else {
                    holder.beaconPicImageView.setColorFilter(context.getResources().getColor(R.color.colorWarning), PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return beaconsList.size();
    }
}