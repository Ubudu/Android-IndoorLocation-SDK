package com.ubudu.ilapp2.util;

import com.ubudu.indoorlocation.ILBeacon;

/**
 * Created by mgasztold on 11/03/16.
 */
public interface BeaconDetails {

    void updateData(final ILBeacon beacon);
    ILBeacon getUbuduBeacon();

}
