package com.ubudu.gmaps.factory;

import com.ubudu.gmaps.util.ZoneOptionsStrategy;

/**
 * Created by mgasztold on 12/01/2017.
 */

public class ZoneOptionsStrategyFactory {

    public static ZoneOptionsStrategy defaultZoneOptionsStrategy(){
        return new ZoneOptionsStrategy();
    }

}
