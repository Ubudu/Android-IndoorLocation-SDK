package com.ubudu.ilapp2.util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ubudu.ilapp2.R;
import com.ubudu.iot.ble.BleDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mgasztold on 08/09/2017.
 */

public class ScannedDevicesAdapter extends ArrayAdapter<BleDevice> {

    private int textColor;
    private final ArrayList<BleDevice> devices = new ArrayList<BleDevice>();

    public ScannedDevicesAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public int getCount() {
        return this.devices.size();
    }

    public BleDevice getItem(int index) {
        return this.devices.get(index);
    }

    public void addOrUpdate(BleDevice device) {
        if(devices.contains(device)) {
            devices.set(devices.indexOf(device), device);
        } else {
            devices.add(device);
        }
        notifyDataSetChanged();
    }

    @Override
    public void add(BleDevice object) {
        devices.add(object);
        super.add(object);
    }

    @Override
    public void remove(BleDevice object) {
        boolean removed = devices.remove(object);
        if(!removed)
            Log.e("Test","device "+object.getDevice().getAddress()+" not found");
        super.remove(object);
    }

    @Override
    public void clear() {
        super.clear();
        devices.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolderItem viewHolderItem;

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_scanned_device, parent, false);

            // well set up the ViewHolder
            viewHolderItem = new ViewHolderItem();
            viewHolderItem.name = (TextView) view.findViewById(R.id.device_name);
            viewHolderItem.mac = (TextView) view.findViewById(R.id.device_mac);
            viewHolderItem.rssi = (TextView) view.findViewById(R.id.device_rssi);

            // store the holder with the view.
            view.setTag(viewHolderItem);
        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolderItem = (ViewHolderItem) view.getTag();
        }

        BleDevice device = getItem(position);
        if(device.getDevice().getName() != null) {
            viewHolderItem.name.setText(device.getDevice().getName());
            viewHolderItem.name.setTextColor(textColor);
        } else {
            viewHolderItem.name.setText("null");
            viewHolderItem.name.setTextColor(getContext().getResources().getColor(R.color.colorWhitenedText));
        }

        viewHolderItem.rssi.setText(String.valueOf((int) device.getRssi()));
        viewHolderItem.mac.setText(device.getDevice().getAddress());


        viewHolderItem.rssi.setTextColor(textColor);
        viewHolderItem.mac.setTextColor(textColor);

        return view;
    }

    public void setDevices(List<BleDevice> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
        notifyDataSetChanged();
    }

    public void setColor(int color) {
        this.textColor = color;
    }

    private static class ViewHolderItem {
        TextView name;
        TextView mac;
        TextView rssi;
    }
}
