package com.ubudu.ilapp2.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by mgasztold on 10/10/16.
 */

public class MyPreferences {

    public static final String TAG = MyPreferences.class.getCanonicalName();

    public static final String PREFERENCE_KEY_NAMESPACE = "namespace";
    public static final String PREFERENCE_MODE_OF_OPERATION = "PREFERENCE_MODE_OF_OPERATION";

    /**
     *
     * @param context
     * @param name
     * @param value
     */
    public static void setPreferenceString(Context context, String name, String value){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=mSharedPref.edit();
        editor.putString(name,value);
        editor.apply();
    }

    /**
     *
     * @param context
     * @param name
     * @param defaultValue
     * @return
     */
    public static String getPreferenceString(Context context, String name, String defaultValue){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return mSharedPref.getString(name, defaultValue);
    }

    /**
     *
     * @param context
     * @param name
     * @param value
     */
    public static void setPreferenceBoolean(Context context, String name, boolean value){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=mSharedPref.edit();
        editor.putBoolean(name,value);
        editor.apply();
    }

    /**
     *
     * @param context
     * @param name
     * @param value
     */
    public static void setPreferenceInt(Context context, String name, int value){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=mSharedPref.edit();
        editor.putInt(name,value);
        editor.apply();
    }

    /**
     *
     * @param context
     * @param showParticlesPref
     * @param defaultBoolean
     * @return
     */
    public static boolean getPreferenceBoolean(Context context, String showParticlesPref, boolean defaultBoolean) {
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return mSharedPref.getBoolean(showParticlesPref, defaultBoolean);
    }

    public static int getPreferenceInt(Context context, String name, int value) {
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return mSharedPref.getInt(name, value);
    }
}
