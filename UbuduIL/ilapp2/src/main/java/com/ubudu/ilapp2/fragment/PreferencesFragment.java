package com.ubudu.ilapp2.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreferenceFix;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompatFix;
import android.support.v7.preference.PreferenceGroup;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ubudu.ilapp2.MyApplication;
import com.ubudu.ilapp2.util.MyPreferences;
import com.ubudu.indoorlocation.Configuration;
import com.ubudu.indoorlocation.UbuduIndoorLocationManager;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.ilapp2.MainActivity;
import com.ubudu.ilapp2.R;
import com.ubudu.indoorlocation.logger.RealTimeLogger;

public class PreferencesFragment extends PreferenceFragmentCompatFix implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = PreferencesFragment.class.getCanonicalName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSummary(getPreferenceScreen());
    }

    private void initPrefsState() {
        Preference operationModePref = findPreference("operation_mode");
        operationModePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity) getActivity()).askForModeOfOperation(
                        MyPreferences.getPreferenceInt(getContext(), MyPreferences.PREFERENCE_MODE_OF_OPERATION, 0)
                        , new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                MyPreferences.setPreferenceInt(getContext(), MyPreferences.PREFERENCE_MODE_OF_OPERATION, which);
                                UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setModeOfOperation(which);
                                return true;
                            }
                });
                return true;
            }
        });

        Preference appNamespace = findPreference("change_application");
        appNamespace.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity) getActivity()).askUserForNamespace(true);
                return true;
            }
        });

        Preference myGeoPref = findPreference("geoloc_permission");
        myGeoPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                return true;
            }
        });

        final Preference realTimeTrackingUrlPref = findPreference("log_position_real_time_url");
        realTimeTrackingUrlPref.setSummary(getResources().getString(R.string.summary_log_position_real_time_url)
                +"\nCurrent url: "
                + PreferenceManager.getDefaultSharedPreferences(
                getContext()).getString("log_position_real_time_url",null));

        final Preference realTimeTrackingPref = findPreference("log_position_real_time");
        realTimeTrackingUrlPref.setEnabled(((CheckBoxPreference)realTimeTrackingPref).isChecked());
        realTimeTrackingPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                realTimeTrackingUrlPref.setEnabled(((CheckBoxPreference)realTimeTrackingPref).isChecked());
                return true;
            }
        });

        realTimeTrackingUrlPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity) getActivity()).askForRealTimeTrackingUrl(new MainActivity.PreferenceResult() {
                    @Override
                    public void userInputCompleted() {
                        realTimeTrackingUrlPref.setSummary(getResources().getString(R.string.summary_log_position_real_time_url)
                                +"\nCurrent url: "
                                + PreferenceManager.getDefaultSharedPreferences(
                                getContext()).getString("log_position_real_time_url",null));
                    }
                });
                return true;
            }
        });

        Preference appVer = findPreference("app_version");
        appVer.setSummary(MyApplication.getVersion());

        Preference ilSdkVer = findPreference("indoorlocation_sdk_version");
        ilSdkVer.setSummary(UbuduIndoorLocationSDK.getVersion()+" ("+UbuduIndoorLocationSDK.getVersionCode()+")");


        appNamespace.setSummary(getResources().getString(R.string.summary_change_application)
                +"\nCurrent namespace: "
                + MyPreferences.getPreferenceString(getContext(), MyPreferences.PREFERENCE_KEY_NAMESPACE, null));

        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("account");
        String authToken = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(MainActivity.KEY_AUTH_TOKEN,"");
        Preference myLogoutPref = findPreference("logout");
        Preference accountDetailsPreference = findPreference("account_details");
        Preference applicationsListPreference = findPreference("applications_list");
        if(authToken.equals("")) {
            preferenceCategory.setVisible(false);
            myLogoutPref.setVisible(false);
            accountDetailsPreference.setVisible(false);
            applicationsListPreference.setVisible(false);
        } else {
            preferenceCategory.setVisible(true);
            myLogoutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    ((MainActivity) getActivity()).askUserForLogout();
                    return true;
                }
            });

            applicationsListPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    ((MainActivity) getActivity()).showLatestApplicationsList();
                    return true;
                }
            });

            String userName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(MainActivity.KEY_AUTH_USERNAME,"");
            accountDetailsPreference.setSummary("Account: "+userName);
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).onSettingsFragmentResumed();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        initPrefsState();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference p = findPreference(key);
        updatePrefSummary(p);

        if(key.equals("log_position_history")) {
            UbuduIndoorLocationSDK.getSharedInstance(getContext())
                    .getIndoorLocationManager()
                    .setLoggingPositionsForAnalyticsEnabled(((CheckBoxPreference)p)
                            .isChecked());
        }

        if(key.equals("transition_zones_floor_switching")) {
            UbuduIndoorLocationSDK.getSharedInstance(getContext())
                    .getIndoorLocationManager()
                    .setTransitionZonesFloorSwitchingEnabled(((CheckBoxPreference)p)
                            .isChecked());
        }

        if(key.equals("use_gps_provider")) {
            UbuduIndoorLocationSDK.getSharedInstance(getContext())
                    .getIndoorLocationManager()
                    .setCanUseGPSLocationProvider(((CheckBoxPreference)p)
                            .isChecked());
        }

        if(key.equals("log_position_real_time")) {
            if(!((CheckBoxPreference)p).isChecked()) {
                UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setLogger(null);
            } else {
                String url = PreferenceManager.getDefaultSharedPreferences(
                        getContext()).getString("log_position_real_time_url","");
                if(!url.equals("")) {
                    UbuduIndoorLocationSDK.getSharedInstance(getContext())
                            .getIndoorLocationManager().setLogger(new RealTimeLogger(url));
                }
            }
        }

        if(key.equals("stable_mode")) {
            int mode;
            if(((CheckBoxPreference)p).isChecked()){
                mode = Configuration.MODE_STABLE;
            } else
                mode = Configuration.MODE_BLE_CORRECTIVE;

            UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setModeOfOperation(mode);
        }

        if(key.equals("background_scanning")) {
            UbuduIndoorLocationManager manager = UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager();
            if(((CheckBoxPreference)p).isChecked()) {
                manager.setForegroundScanPeriod(MainActivity.RANGING_PERIOD_FOREGROUND);
                manager.setBackgroundScanPeriod(MainActivity.RANGING_PERIOD_FOREGROUND);
                manager.setForegroundBetweenScanPeriod(MainActivity.BETWEEN_RANGING_PERIOD_FOREGROUND);
                manager.setBackgroundBetweenScanPeriod(MainActivity.BETWEEN_RANGING_PERIOD_FOREGROUND);
            } else {
                manager.setForegroundScanPeriod(MainActivity.RANGING_PERIOD_FOREGROUND);
                manager.setBackgroundScanPeriod(MainActivity.RANGING_PERIOD_BACKGROUND);
                manager.setForegroundBetweenScanPeriod(MainActivity.BETWEEN_RANGING_PERIOD_FOREGROUND);
                manager.setBackgroundBetweenScanPeriod(MainActivity.BETWEEN_RANGING_PERIOD_BACKGROUND);
            }
        }
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        }
        if (p instanceof EditTextPreferenceFix) {
            EditTextPreferenceFix editTextPref = (EditTextPreferenceFix) p;
            if (p.getTitle().toString().toLowerCase().contains("password")) {
                p.setSummary("******");
            } else if (editTextPref.getText() != null && !editTextPref.getText().equals("")) {
                p.setSummary(editTextPref.getText());
            }
        }
    }
}