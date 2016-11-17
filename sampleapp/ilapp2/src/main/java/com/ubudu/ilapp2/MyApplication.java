package com.ubudu.ilapp2;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by mgasztold on 10/10/16.
 */

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks {

    public static final String TAG = MyApplication.class.getCanonicalName();

    private static boolean isActivityForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        registerActivityLifecycleCallbacks(this);
    }

    public static boolean getActivityForeground() {
        return isActivityForeground;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        isActivityForeground = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        isActivityForeground = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
