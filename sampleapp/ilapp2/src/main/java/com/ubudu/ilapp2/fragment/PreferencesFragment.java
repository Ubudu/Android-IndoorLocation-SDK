package com.ubudu.ilapp2.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.EditTextPreferenceFix;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompatFix;
import android.support.v7.preference.PreferenceGroup;

import com.ubudu.ilapp2.MainActivity;
import com.ubudu.ilapp2.R;
import com.ubudu.ilapp2.util.MyPreferences;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;

public class PreferencesFragment extends PreferenceFragmentCompatFix implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = PreferencesFragment.class.getCanonicalName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSummary(getPreferenceScreen());

        Preference myPref = findPreference("change_application");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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

        Preference myShowUndetectedBeacons = findPreference("undetected_beacons");
        myShowUndetectedBeacons.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                MyPreferences.setPreferenceBoolean(getContext(), "show_undetected_beacons", (Boolean) newValue);

                return preference.isSelectable();
            }
        });

        Preference appVer = findPreference("app_version");
        try {
            appVer.setSummary(getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName
                    + " (" + getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionCode + ")");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Preference ilSdkVer = findPreference("indoorlocation_sdk_version");
        ilSdkVer.setSummary(UbuduIndoorLocationSDK.getVersion()+" ("+ UbuduIndoorLocationSDK.getVersionCode()+")");
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