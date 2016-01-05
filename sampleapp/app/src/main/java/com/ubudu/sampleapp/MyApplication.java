package com.ubudu.sampleapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by mgasztold on 01/11/15.
 */
public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks {

    public static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    public static final int PERMISSION_BLUETOOTH = 2;
    public static final int PERMISSION_INTERNET = 3;
    public static final int TURN_ON_LOCATION_SERVICES = 4;

    private static Context context;

    private static Activity mCurrentActivity = null;

    private static volatile boolean isAppInBackground = false;

    @Override
    public void onCreate() {
        super.onCreate();

        MyApplication.context = getApplicationContext();

        if (getAppContext() instanceof Application) {
            registerActivityLifecycleCallbacks(this);
        }
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }

    public static Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public static void setAppInBackground(boolean b) {
        isAppInBackground = b;
    }

    public static boolean isAppInBackground() {
        return isAppInBackground;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        setAppInBackground(false);
        setCurrentActivity(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        setAppInBackground(true);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

}