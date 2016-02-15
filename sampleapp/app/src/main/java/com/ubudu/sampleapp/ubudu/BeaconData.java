package com.ubudu.sampleapp.ubudu;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mgasztold on 02/02/16.
 */
public class BeaconData implements Parcelable {

    public String proximity_UUID;
    public int major;
    public int minor;
    public double rssi;
    public double distance;

    public BeaconData(String proximity_UUID, int major, int minor, double rssi, double distance){
        this.proximity_UUID = proximity_UUID;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.distance = distance;
    }

    protected BeaconData(Parcel in) {
        proximity_UUID = in.readString();
        major = in.readInt();
        minor = in.readInt();
        rssi = in.readDouble();
        distance = in.readDouble();
    }

    public static final Creator<BeaconData> CREATOR = new Creator<BeaconData>() {
        @Override
        public BeaconData createFromParcel(Parcel in) {
            return new BeaconData(in);
        }

        @Override
        public BeaconData[] newArray(int size) {
            return new BeaconData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(proximity_UUID);
        dest.writeInt(major);
        dest.writeInt(minor);
        dest.writeDouble(rssi);
        dest.writeDouble(distance);
    }
}
