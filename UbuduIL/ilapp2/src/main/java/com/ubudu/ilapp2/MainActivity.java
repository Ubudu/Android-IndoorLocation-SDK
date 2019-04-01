package com.ubudu.ilapp2;

import android.Manifest;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.ubudu.authentication.AccountAuthenticatorActivity;
import com.ubudu.authentication.AccountGeneral;
import com.ubudu.beacon.logger.BatteryLogger;
import com.ubudu.ilapp2.fragment.BaseFragment;
import com.ubudu.ilapp2.fragment.BeaconFragment;
import com.ubudu.ilapp2.fragment.MapFragment;
import com.ubudu.ilapp2.fragment.MotionFragment;
import com.ubudu.ilapp2.fragment.PreferencesFragment;
import com.ubudu.ilapp2.fragment.RadarFragment;
import com.ubudu.ilapp2.fragment.ScanQrCodeFragment;
import com.ubudu.ilapp2.fragment.ScannedDevicesFragment;
import com.ubudu.ilapp2.fragment.UwbTagFragment;
import com.ubudu.ilapp2.model.BottomSheetItem;
import com.ubudu.ilapp2.model.UbuduApplication;
import com.ubudu.ilapp2.util.BeaconDetails;
import com.ubudu.ilapp2.util.BottomSheetItemAdapter;
import com.ubudu.ilapp2.util.FragmentUtils;
import com.ubudu.ilapp2.util.MathUtils;
import com.ubudu.ilapp2.util.MyPreferences;
import com.ubudu.ilapp2.util.ToastUtil;
import com.ubudu.ilapp2.util.UbuduApplicationsManager;
import com.ubudu.indoorlocation.ILBeacon;
import com.ubudu.indoorlocation.UbuduIndoorLocationManager;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduRangedBeaconsNotifier;
import com.ubudu.indoorlocation.UbuduResultListener;
import com.ubudu.indoorlocation.UbuduStartCallback;
import com.ubudu.indoorlocation.logger.RealTimeLogger;
import com.ubudu.iot.ConnectionListener;
import com.ubudu.iot.ble.BleDevice;
import com.ubudu.iot.ble.BleDeviceFilter;
import com.ubudu.iot.ble.DiscoveryManager;
import com.ubudu.iot.ibeacon.IBeacon;
import com.ubudu.iot.ibeacon.IBeaconIdentifiers;
import com.ubudu.ubeacon.UBeacon;
import com.ubudu.uwb.twr.model.UwbBluetoothDevice;
import com.ubudu.uwb.twr.model.UwbUARTDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements BaseFragment.ViewController, BottomSheetItemAdapter.ItemListener {

    public static final String TAG = MainActivity.class.getCanonicalName();

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION_PERMISSION = 0;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 1;

    public static final long RANGING_PERIOD_FOREGROUND = 850;
    public static final long RANGING_PERIOD_BACKGROUND = 1100;
    public static final long BETWEEN_RANGING_PERIOD_FOREGROUND = 300;
    public static final long BETWEEN_RANGING_PERIOD_BACKGROUND = 2000;

    private static final String PREF_NAME_RSSI_FILTER = "rssi_filter_value";
    private static final String PREF_NAME_NAME_FILTER = "name_mac_filter_value";

    public static final String KEY_AUTH_TOKEN = "auth_token";
    public static final String KEY_AUTH_USERNAME = "username";
    private MaterialDialog mProgressDialog;
    private UbuduIndoorLocationSDK mIndoorLocationSdk;
    private UbuduIndoorLocationManager mIndoorLocationManager;
    private BatteryLogger ubuduBatteryLogger;
    private boolean isAppReady = false;
    private Handler bottomMenuHandler;
    private RadarFragment mRadarFragment;
    private BottomSheetBehavior behavior;
    private BottomSheetDialog mBottomSheetDialog;
    private BottomSheetItemAdapter mAdapter;
    private String currentFragmentTag;
    private BeaconDetails observedBeaconDetails;
    private MapFragment mMapFragment;
    private SharedPreferences mSharedPref;
    private AccountManager mAccountManager;
    private Handler retrySettingNamespaceHandler;

    private ScannedDevicesFragment scannedDevicesFragment;
    private int rssiThreshold = 0;
    private MenuItem scanMenuItem;
    private MenuItem uartMenuItem;
    private MenuItem disconnectMenuItem;
    private Handler mButtonsHandler = new Handler();

    private Runnable retrySettingNamespaceRunnable = new Runnable() {
        @Override
        public void run() {
            initUbuduIndoorLocationSdk();
        }
    };

    @BindView(R.id.bottom_bar)
    BottomBar mBottomBar;
    @BindView(R.id.frame_content)
    FrameLayout mFrameLayout;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mAccountManager = AccountManager.get(MainActivity.this);
        AccountAuthenticatorActivity.setUILogoDrawableId(R.mipmap.ic_launcher);
        try { // hide toolbar
            getSupportActionBar().hide();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        rssiThreshold = mSharedPref.getInt(PREF_NAME_RSSI_FILTER, -90);

        initBottomBar();
        initBottomSheet();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION_PERMISSION);
        else
            onMapFragmentRequested();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACCESS_FINE_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                onMapFragmentRequested();
            else
                finish();
        } else if (requestCode == REQUEST_CODE_CAMERA_PERMISSION)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                onScanQrCodeFragmentRequested();
    }

    @Override
    public void onBackPressed() {
        int countFragmentsOnStack = getSupportFragmentManager().getBackStackEntryCount();
        if (countFragmentsOnStack > 1)
            super.onBackPressed();
        else if (countFragmentsOnStack == 1) {
            // there is no other fragments on stack and go to background
            moveTaskToBack(true);
        }
    }

    private void handleBottomMenuClick() {
        if(bottomMenuHandler==null)
            bottomMenuHandler = new Handler(Looper.getMainLooper());
        mBottomBar.setEnabled(false);
        bottomMenuHandler.removeCallbacks(null);
        bottomMenuHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBottomBar.setEnabled(true);
            }
        },300L);
    }

    private void initBottomBar() {
        mBottomBar.setActiveTabColor(ContextCompat.getColor(this, R.color.colorAccent));
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabItemId) {
                if (isAppReady) {
                    handleBottomMenuClick();
                    if (tabItemId == R.id.map_item) {
                        onMapFragmentRequested();
                    } else if (tabItemId == R.id.radar_item) {
                        onRadarFragmentRequested();
                    } else if (tabItemId == R.id.settings_item) {
                        onSettingsFragmentRequested(false);
                    } else if (tabItemId == R.id.motion_item) {
                        onMotionFragmentRequested();
                    } else if (tabItemId == R.id.uwb_item) {
                        if(!mIndoorLocationManager.isUwbTagConnected()) {
                            onScannedDevicesFragmentRequested();
                        } else {
                            onUwbTagFragmentRequested();
                        }
                    }
                }
            }
        });
    }

    private void initUbuduIndoorLocationSdk() {
        mIndoorLocationSdk = UbuduIndoorLocationSDK.getSharedInstance(getApplicationContext());
        mIndoorLocationManager = mIndoorLocationSdk.getIndoorLocationManager();

        if(mMapFragment!=null)
            mIndoorLocationManager.setParticleFilterListener(mMapFragment);

        mIndoorLocationManager.setRssiThreshold(-95);
        mIndoorLocationManager.setAccuracyThreshold(30);

//        if(mSharedPref.getBoolean("background_scanning",false)) {
//            mIndoorLocationManager.setBeaconScanningStrategy(scanningStrategy
//                    .setForegroundRangingScanPeriod(MainActivity.RANGING_PERIOD_FOREGROUND)
//                    .setBackgroundRangingScanPeriod(MainActivity.RANGING_PERIOD_FOREGROUND)
//                    .setForegroundRangingBetweenScanPeriod(MainActivity.BETWEEN_RANGING_PERIOD_FOREGROUND)
//                    .setBackgroundRangingBetweenScanPeriod(MainActivity.BETWEEN_RANGING_PERIOD_FOREGROUND));
//        } else {
//            mIndoorLocationManager.setBeaconScanningStrategy(scanningStrategy
//                    .setForegroundRangingScanPeriod(MainActivity.RANGING_PERIOD_FOREGROUND)
//                    .setBackgroundRangingScanPeriod(MainActivity.RANGING_PERIOD_BACKGROUND)
//                    .setForegroundRangingBetweenScanPeriod(MainActivity.BETWEEN_RANGING_PERIOD_FOREGROUND)
//                    .setBackgroundRangingBetweenScanPeriod(MainActivity.BETWEEN_RANGING_PERIOD_BACKGROUND));
//        }
        mIndoorLocationManager.setLoggingPositionsForAnalyticsEnabled(mSharedPref.getBoolean("log_position_history",false));
        mIndoorLocationManager.setTransitionZonesFloorSwitchingEnabled(mSharedPref.getBoolean("transition_zones_floor_switching", false));
        mIndoorLocationManager.setCanUseGPSLocationProvider(mSharedPref.getBoolean("use_gps_provider", true));
        mIndoorLocationManager.setModeOfOperation(MyPreferences.getPreferenceInt(getApplicationContext(), MyPreferences.PREFERENCE_MODE_OF_OPERATION, 0));
        mIndoorLocationManager.setRangedBeaconsNotifier(new UbuduRangedBeaconsNotifier() {
            @Override
            public void didRangeBeacons(List<ILBeacon> list) {
                if(observedBeaconDetails!=null) {
                    ILBeacon currentBeacon = observedBeaconDetails.getUbuduBeacon();
                    for (ILBeacon beacon : list) {
                        if (currentBeacon.getBluetoothDevice().getAddress().equals(beacon.getBluetoothDevice().getAddress())) {
                            Log.i(TAG,"beacon of address"
                                    + currentBeacon.getBluetoothDevice().getAddress()
                                    +" will be updated from" + currentBeacon.getMajor() +" to "+beacon.getMajor());
                            observedBeaconDetails.updateData(beacon);
                            break;
                        }
                    }
                }
                if(mRadarFragment!=null){
                    mRadarFragment.didRangeBeacons(list);
                }
                if(mSharedPref.getBoolean("monitor_beacon_batteries", false)) {
                    if(ubuduBatteryLogger==null)
                        ubuduBatteryLogger = new BatteryLogger(getApplicationContext());
                    List<UBeacon> ubeacons = new ArrayList<UBeacon>();
                    for(ILBeacon beacon : list) {
                        ubeacons.add(beacon.getUBeacon());
                    }
                    ubuduBatteryLogger.processBeacons(ubeacons);
                }
            }
        });

        if(mSharedPref.getBoolean("log_position_real_time", false)) {
            String url = mSharedPref.getString("log_position_real_time_url", "");
            if(!url.equals("")) {
                mIndoorLocationManager.setLogger(
                        new RealTimeLogger(url));
            }
        }

        String namespace = MyPreferences.getPreferenceString(getApplicationContext(), MyPreferences.PREFERENCE_KEY_NAMESPACE, "");
        if(!namespace.equals("")) {
            if(currentFragmentTag.equals(MapFragment.TAG) && mMapFragment!=null)
                mMapFragment.showLoadingLabelWithText("Loading namespace...");
            mIndoorLocationSdk.setNamespace(namespace, new UbuduResultListener() {
                @Override
                public void success() {
                    Log.i(TAG,"Indoor Location namespace set.");
                    if(mMapFragment!=null)
                        mMapFragment.showLoadingLabelWithText("Estimating initial position...");
                }

                @Override
                public void error(String message) {
                    Log.e(TAG,message);
                    if(mMapFragment!=null)
                        mMapFragment.showLoadingLabelWithText(message);

                    if(retrySettingNamespaceHandler==null)
                        retrySettingNamespaceHandler = new Handler();

                    retrySettingNamespaceHandler.postDelayed(retrySettingNamespaceRunnable,10000L);
                }
            });

            mIndoorLocationManager.start(new UbuduStartCallback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "Ubudu Indoor Location SDK started successfully");
                }

                @Override
                public void onFailure() {
                    Log.e(TAG, "Ubudu Indoor Location SDK could not be started.");
                }

                @Override
                public void onRestartedAfterContentAutoUpdated() {
                    Log.i(TAG, "Ubudu Indoor Location SDK maps data files have been updated.");
                }
            });
        } else {
            askUserForNamespace(false);
        }
    }

    public void askForRealTimeTrackingUrl(final PreferenceResult listener) {
        MaterialDialog.Builder mDialogBuilder = new MaterialDialog.Builder(this)
                .cancelable(false)
                .negativeText(getResources().getString(R.string.button_cancel))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        noNamespaceInput();
                    }
                })
                .title("Real-time tracking URL")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(getResources().getString(R.string.input_type_real_time_tracking_url_hint)
                        , mSharedPref.getString("log_position_real_time_url",""), false
                        , new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                String url = String.valueOf(input);
                                if (!input.equals("")) {

                                    if(url.startsWith("http://")
                                            || url.startsWith("https://")
                                            || url.startsWith("ws://")
                                            || url.startsWith("wss://")) {

                                        mIndoorLocationManager.setLogger(
                                                new RealTimeLogger(url));
                                        mSharedPref.edit().putString("log_position_real_time_url", url).apply();
                                        listener.userInputCompleted();
                                    } else {
                                        ToastUtil.showToast(getApplicationContext(),"Wrong URL format");
                                    }
                                }
                            }
                        });

        mDialogBuilder.show();
    }

    public void askUserForNamespace(final boolean cancelable) {
        MaterialDialog.Builder mDialogBuilder = new MaterialDialog.Builder(this)
                .title(R.string.choose_namespace_input_type)
                .items(R.array.namespace_input_type)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == 0) {
                            onScanQrCodeFragmentRequested();
                        } else if (which == 1) {
                            relog();
                        } else if (which == 2) {
                            typeNewNamespace();
                        }
                        return true;
                    }
                })
                .cancelable(false)
                .alwaysCallSingleChoiceCallback();

        if (cancelable) {
            mDialogBuilder.cancelable(true);
            mDialogBuilder.negativeText(R.string.button_cancel);
        }

        mDialogBuilder.show();
    }

    private void relog() {
        logout(new LogoutFinishedListener() {
            @Override
            public void logoutFinished() {
                login(new LoginSuccessfulListener() {
                    @Override
                    public void loginSuccessful(String userName, String authToken) {
                        onProgressDialogShowRequested("Fetching applications ...");
                        mSharedPref.edit().putString(KEY_AUTH_TOKEN, authToken).apply();
                        mSharedPref.edit().putString(KEY_AUTH_USERNAME, userName).apply();
                        if(currentFragmentTag.equals(PreferencesFragment.TAG))
                            onSettingsFragmentRequested(true);
                        showLatestApplicationsList();
                    }
                });
            }
        });
    }

    private void logout(final LogoutFinishedListener listener) {
        String authToken = mSharedPref.getString(KEY_AUTH_TOKEN,"");
        if(!authToken.equals("")) {
            onProgressDialogShowRequested("Signing out ...");
            mAccountManager.invalidateAuthToken(AccountGeneral.ACCOUNT_TYPE, authToken);
            mSharedPref.edit().putString(KEY_AUTH_TOKEN,"").apply();
            mSharedPref.edit().putString(KEY_AUTH_USERNAME, "").apply();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    onProgressDialogHideRequested();
                    listener.logoutFinished();
                }
            },1000L);
        } else {
            listener.logoutFinished();
        }
    }

    public interface LogoutFinishedListener {
        void logoutFinished();
    }

    public interface LoginSuccessfulListener {
        void loginSuccessful(String userName, String authToken);
    }

    private void login(final LoginSuccessfulListener listener) {
        mAccountManager.getAuthTokenByFeatures(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_READ_WRITE, null, this, null, null, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle resultBundle = future.getResult();
                    String accountAuthToken = resultBundle.getString(AccountManager.KEY_AUTHTOKEN);
                    if(accountAuthToken!=null && !accountAuthToken.equals("")) {
                        listener.loginSuccessful(resultBundle.getString(AccountManager.KEY_ACCOUNT_NAME), accountAuthToken);
                    }
                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                    e.printStackTrace();
                    // TODO Display alert to user
                }
            }
        }, null);
    }

    public void showLatestApplicationsList() {
        login(new LoginSuccessfulListener() {
            @Override
            public void loginSuccessful(String userName, String authToken) {
                if(authToken.equals("")) return;
                new UbuduApplicationsManager(getApplicationContext()).getUbuduApplicationsWithAuthToken(authToken, new UbuduApplicationsManager.ResponseListener() {
                    @Override
                    public void success(List<UbuduApplication> applications) {
                        onProgressDialogHideRequested();
                        displayApplicationsList(applications);
                    }

                    @Override
                    public void error(String message) {
                        onProgressDialogHideRequested();
                    }
                });
            }
        });
    }

    private void displayApplicationsList(List<UbuduApplication> applications) {
        List<BottomSheetItem> items = new ArrayList<>();
        for(UbuduApplication application : applications) {
            items.add((BottomSheetItem) application);
        }
        showBottomSheetDialog("Available applications:", items);
    }

    private void typeNewNamespace() {
        MaterialDialog.Builder mDialogBuilder = new MaterialDialog.Builder(this)
                .cancelable(false)
                .negativeText(getResources().getString(R.string.button_cancel))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        noNamespaceInput();
                    }
                })
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.input_type_namespace_hint
                        , R.string.input_prefill, false
                        , new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                if (!input.equals("")) {
                                    onMapFragmentRequested();
                                    onNamespaceChanged(String.valueOf(input));
                                }
                            }
                        });

        mDialogBuilder.show();
    }

    @Override
    public void onProgressDialogHideRequested() {
        try {
            if (mProgressDialog != null)
                mProgressDialog.dismiss();
        } catch (IllegalArgumentException e) {
            mProgressDialog = null;
        }
    }

    @Override
    public void onProgressDialogShowRequested(String text) {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        mProgressDialog = null;
        mProgressDialog = new MaterialDialog.Builder(this)
                .content(text)
                .progress(true, 0)
                .autoDismiss(false)
                .cancelable(false)
                .show();
    }

    @Override
    public void setObservedBeaconInterface(BeaconDetails beaconDetails) {
        this.observedBeaconDetails = beaconDetails;
    }

    @Override
    public void onBeaconFragmentPaused() {
        this.observedBeaconDetails = null;
    }

    @Override
    public void onBeaconFragmentRequested(ILBeacon b) {
        if(currentFragmentTag==null || !currentFragmentTag.equals(BeaconFragment.TAG)) {
            BeaconFragment beaconDetailsFragment = new BeaconFragment();
            Log.e(TAG,"selected beacon: "+b.getMajor());
            beaconDetailsFragment.setBeacon(b);
            FragmentUtils.changeFragment(this, beaconDetailsFragment, true);
        }
    }

    @Override
    public void onBeaconFragmentResumed() {
        currentFragmentTag = BeaconFragment.TAG;
    }

    @Override
    public void onMapFragmentRequested() {
        if(currentFragmentTag==null || !currentFragmentTag.equals(MapFragment.TAG)) {
            mMapFragment = new MapFragment();
            if(mIndoorLocationManager!=null)
                mIndoorLocationManager.setParticleFilterListener(mMapFragment);
            FragmentUtils.changeFragment(this, mMapFragment, true);
        }
    }

    @Override
    public void onRadarFragmentRequested() {
        if(currentFragmentTag==null || !currentFragmentTag.equals(RadarFragment.TAG)) {
            mRadarFragment = new RadarFragment();
            FragmentUtils.changeFragment(this, mRadarFragment, true);
        }
    }

    @Override
    public void onScanQrCodeFragmentRequested() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA_PERMISSION);
            return;
        }
        FragmentUtils.changeFragment(this, new ScanQrCodeFragment(), true);
    }

    @Override
    public void onSettingsFragmentRequested(boolean force) {
        if(currentFragmentTag==null || !currentFragmentTag.equals(PreferencesFragment.TAG) || force) {
            FragmentUtils.changeFragment(this, new PreferencesFragment(), true);
        }
    }

    @Override
    public void onRadarFragmentPaused() {
        mRadarFragment = null;
    }


    @Override
    public void mapFragmentResumed(MapFragment mapFragment) {
        mMapFragment = mapFragment;
        currentFragmentTag = MapFragment.TAG;
        mBottomBar.selectTabWithId(R.id.map_item);

        if(!isAppReady) {
            initUbuduIndoorLocationSdk();
            isAppReady = true;
        }
    }

    @Override
    public void mapFragmentPaused() {
        mMapFragment = null;
    }

    @Override
    public void radarFragmentResumed(RadarFragment radarFragment) {
        mRadarFragment = radarFragment;
        currentFragmentTag = RadarFragment.TAG;
        mBottomBar.selectTabWithId(R.id.radar_item);
    }

    @Override
    public void scanQrCodeFragmentResumed() {
        currentFragmentTag = ScanQrCodeFragment.TAG;

        RelativeLayout.LayoutParams layoutParams= (RelativeLayout.LayoutParams)(mFrameLayout.getLayoutParams());
        layoutParams.setMargins(0,0,0,0);

        mBottomBar.setVisibility(View.GONE);
    }

    @Override
    public void scanQrCodeFragmentPaused() {
        RelativeLayout.LayoutParams layoutParams= (RelativeLayout.LayoutParams)(mFrameLayout.getLayoutParams());
        layoutParams.setMargins(0,0,0
                , (int)(MathUtils.dpToPx(getApplicationContext()
                        ,getResources().getDimension(R.dimen.bottom_tab_bar_height)) / getResources().getDisplayMetrics().density));
        mBottomBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if(mIndoorLocationSdk!=null)
            mIndoorLocationSdk.release();
        super.onDestroy();
    }

    @Override
    public void onDialogShowRequested(String title, String text, final BaseFragment.DialogResponseListener listener, boolean showNegative) {
        MaterialDialog.Builder mDialogBuilder = new MaterialDialog.Builder(this)
                .cancelable(false)
                .content(text)
                .positiveText(getResources().getString(R.string.button_yes))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        listener.onPositive();
                    }
                });
        if(title!=null && !title.equals(""))
            mDialogBuilder.title(title);

        if(showNegative){
            mDialogBuilder
                    .negativeText(getResources().getString(R.string.button_cancel))
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            listener.onNegative();
                        }
                    });
        }
        mDialogBuilder.show();
    }

    @Override
    public void onNamespaceChanged(String newNamespace) {
        String namespace = MyPreferences.getPreferenceString(getApplicationContext(),MyPreferences.PREFERENCE_KEY_NAMESPACE,"");
        if (!namespace.equals("") && !namespace.equals(newNamespace)) {
            Log.i(TAG,"Switching Ubudu namespace from: \n" + namespace + " \n to:\n " + newNamespace);
            mIndoorLocationManager.stop();
            mMapFragment.reset();
        }
        MyPreferences.setPreferenceString(getApplicationContext(), MyPreferences.PREFERENCE_KEY_NAMESPACE, newNamespace);
        initUbuduIndoorLocationSdk();
    }

    @Override
    public void onNoQrCodeScannedOrAccepted() {
        if(MyApplication.getActivityForeground())
            noNamespaceInput();
    }

    @Override
    public void onMotionFragmentRequested() {
        if(currentFragmentTag==null || !currentFragmentTag.equals(MotionFragment.TAG)) {
            FragmentUtils.changeFragment(this, new MotionFragment(), true);
        }
    }

    @Override
    public void onMotionFragmentResumed() {
        currentFragmentTag = MotionFragment.TAG;
        mBottomBar.selectTabWithId(R.id.motion_item);
    }

    @Override
    public void onMotionFragmentPaused() {

    }

    private ConnectionListener mConnectionListener = new ConnectionListener() {
        @Override
        public void onConnected() {
            Log.i(TAG, "onConnected");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onProgressDialogHideRequested();
                    onMapFragmentRequested();
                }
            });
        }

        @Override
        public void onDisconnected() {
            Log.i(TAG, "onDisconnected");
            onProgressDialogHideRequested();
            onScannedDevicesFragmentRequested();
        }

        @Override
        public void onConnectionError(Error error) {
            Log.e(TAG, "onConnectionError: "+error.getLocalizedMessage());
            ToastUtil.showToast(getApplicationContext(), "Connection error!");
        }
    };

    @Override
    public void onConnectionRequested(BleDevice device) {
        Log.i(TAG, "onConnectionRequested: ${device!!.device.address}");
        onProgressDialogShowRequested("Connecting to TAG ...");
        mIndoorLocationManager.connectUwbTag(new UwbBluetoothDevice(device.getDevice(), device.getRssi(), device.getScanResponse()), mConnectionListener);
    }

    @Override
    public void onRssiFilterThresholdChanged(int rssiThreshold) {
        Log.i(TAG, "onRssiFilterThresholdChanged: " + rssiThreshold);
        mSharedPref.edit().putInt("rssi_filter_value", rssiThreshold).apply();
    }

    @Override
    public void onNameMacFilterChanged(String filter) {
        Log.i(TAG, "onNameMacFilterChanged: "+filter);
        mSharedPref.edit().putString("name_mac_filter_value", filter).apply();
    }

    @Override
    public void hideKeyboardRequested() {
        View view = this.findViewById(android.R.id.content);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onScannedDevicesFragmentRequested() {
        if(currentFragmentTag==null || !currentFragmentTag.equals(ScannedDevicesFragment.TAG)) {
            scannedDevicesFragment = new ScannedDevicesFragment();
            scannedDevicesFragment.setRssiThreshold(rssiThreshold);
            scannedDevicesFragment.setNameMacFilter(mSharedPref.getString(PREF_NAME_NAME_FILTER, ""));
            FragmentUtils.changeFragment(this, scannedDevicesFragment, false);
        }
    }

    @Override
    public void onScannedDevicesFragmentResumed() {
        Log.i(TAG, "onScannedDevicesFragmentResumed");

        try {
            ActionBar mActionBar = getSupportActionBar();
            mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(String.valueOf("#2399da"))));
            mActionBar.setDisplayShowTitleEnabled(true);
            getSupportActionBar().show();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        this.setTitle("Find your TAG");

        currentFragmentTag = ScannedDevicesFragment.TAG;

        if (scanMenuItem != null)
            scanMenuItem.setVisible(true);

        if (uartMenuItem != null)
            uartMenuItem.setVisible(true);

        hideKeyboardRequested();
    }

    @Override
    public void onScannedDevicesFragmentPaused() {
        try { // hide toolbar
            getSupportActionBar().hide();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        Log.i(TAG, "onScannedDevicesFragmentPaused");

        if (scanMenuItem != null)
            scanMenuItem.setVisible(false);

        if (uartMenuItem != null)
            uartMenuItem.setVisible(false);
    }

    @Override
    public void onScanningRequested() {
        Log.i(TAG, "onScanningRequested");

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            return;
        }

        scannedDevicesFragment.onScanningStarted();

        findBleDevice();
    }

    @Override
    public void onUwbTagFragmentRequested() {
        if(currentFragmentTag==null || !currentFragmentTag.equals(UwbTagFragment.TAG)) {
            FragmentUtils.changeFragment(this, new UwbTagFragment(), false);
        }
    }

    @Override
    public void onUwbTagFragmentResumed() {
        currentFragmentTag = UwbTagFragment.TAG;
        mBottomBar.selectTabWithId(R.id.uwb_item);
    }

    @Override
    public void onUwbTagFragmentPaused() {

    }

    private void animateScanMenuItem() {
        // Do animation start
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView)inflater.inflate(R.layout.scan_refresh, null);
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);
        scanMenuItem.setActionView(iv);
    }

    private void stopAnimatingScanMenuItem() {
        if (scanMenuItem.getActionView() != null) {
            // Remove the animation.
            scanMenuItem.getActionView().clearAnimation();
            initScanMenuItemActionView();
        }
    }

    /**
     *
     */
    private void scanForBleDevicesAction() {
        scanMenuItem.setEnabled(false);
        uartMenuItem.setEnabled(false);
        hideKeyboardRequested();
        onScanningRequested();
    }

    /**
     *
     */
    private void initScanMenuItemActionView() {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView)inflater.inflate(R.layout.scan_refresh, null);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanForBleDevicesAction();
            }
        });
        scanMenuItem.setActionView(iv);
    }

    private BleDeviceFilter bleDeviceFilter = new BleDeviceFilter() {

        @Override
        public boolean isCorrect(BluetoothDevice device, int rssi, byte[] scanRecord) {
            IBeaconIdentifiers identifiers = IBeacon.getIBeaconIdetifiers(scanRecord);
            if(identifiers != null) {
                return device != null && identifiers.getProximityUuid().equalsIgnoreCase("BAF582F0-DC29-0135-8DA3-12A802939FA0");
            }
            return false;
        }
    };

    private void findBleDevice() {

        animateScanMenuItem();

        DiscoveryManager.discover(getApplicationContext(), 3000, bleDeviceFilter, new DiscoveryManager.DiscoveryListener() {
            @Override
            public boolean onBleDeviceFound(BleDevice device) {
                if (device != null) {
                    Log.i(TAG, "Ble device found: " + device.getDevice().getAddress());
                    scannedDevicesFragment.updateDevice(device);
                }
                return false;
            }

            @Override
            public void onDiscoveryError(Error error) {
                Log.e(TAG, error.getLocalizedMessage());
                ToastUtil.showToast(getApplicationContext(), error.getLocalizedMessage());
                scannedDevicesFragment.onScanningFinished();
            }

            @Override
            public void onDiscoveryStarted() {
                Log.i(TAG, "BLE discovery started");
            }

            @Override
            public void onDiscoveryFinished() {
                Log.i(TAG, "BLE discovery finished");
                stopAnimatingScanMenuItem();
                scannedDevicesFragment.onScanningFinished();
                scanMenuItem.setEnabled(true);
                uartMenuItem.setEnabled(true);
            }
        });
    }

    private void noNamespaceInput() {
        String namespace = MyPreferences.getPreferenceString(getApplicationContext(), MyPreferences.PREFERENCE_KEY_NAMESPACE, "");
        if(namespace.equals(""))
            askUserForNamespace(false);
    }

    public void onSettingsFragmentResumed() {
        currentFragmentTag = PreferencesFragment.TAG;
        mBottomBar.selectTabWithId(R.id.settings_item);
    }

    private void initBottomSheet() {
        View bottomSheet = findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new BottomSheetItemAdapter(new ArrayList<BottomSheetItem>(), this);
        recyclerView.setAdapter(mAdapter);
    }

    private void showBottomSheetDialog(String sheetTitle, List<BottomSheetItem> items) {
        final View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheet.setVisibility(View.VISIBLE);
        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        mBottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.sheet, null);

        TextView sheetTitleView = (TextView) view.findViewById(R.id.sheetTitle);
        sheetTitleView.setText(sheetTitle);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new BottomSheetItemAdapter(items, new BottomSheetItemAdapter.ItemListener() {
            @Override
            public void onItemClick(final BottomSheetItem item) {
                if (mBottomSheetDialog != null) {
                    mBottomSheetDialog.dismiss();
                    onMapFragmentRequested();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onNamespaceChanged(((UbuduApplication) item).getNamespace_uid());
                        }
                    },1000L);
                }
            }
        }));

        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                bottomSheet.setVisibility(View.GONE);
                mBottomSheetDialog = null;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        scanMenuItem = menu.findItem(R.id.action_scan);
        uartMenuItem = menu.findItem(R.id.action_uart);
        disconnectMenuItem = menu.findItem(R.id.action_disconnect);
        initScanMenuItemActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        item.setEnabled(false);
        mButtonsHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                item.setEnabled(true);
            }
        }, 100L);

        switch (item.getItemId()) {
            case R.id.action_disconnect:
                onDisconnectRequested();
                break;
            case R.id.action_uart:
                onProgressDialogShowRequested("Connecting to TAG via UART ...");
                mIndoorLocationManager.connectUwbTag(new UwbUARTDevice(), mConnectionListener);
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onDisconnectRequested() {
        Log.i(TAG, "onDisconnectRequested");
        onProgressDialogShowRequested(getString(R.string.disconnecting));
        mIndoorLocationManager.disconnectTag();
    }

    @Override
    public void onItemClick(BottomSheetItem item) {

    }

    public void askUserForLogout() {
        onDialogShowRequested("Sing out", "Do you want to sign out?", new BaseFragment.DialogResponseListener() {
            @Override
            public void onPositive() {
                logout(new LogoutFinishedListener() {
                    @Override
                    public void logoutFinished() {
                        onSettingsFragmentRequested(true);
                    }
                });
            }

            @Override
            public void onNegative() {

            }
        }, true);
    }

    public void askForModeOfOperation(int initiallySelectedIndex, final MaterialDialog.ListCallbackSingleChoice listener) {
        MaterialDialog.Builder mDialogBuilder = new MaterialDialog.Builder(this)
                .title(R.string.choose_namespace_input_type)
                .items(R.array.modes_of_operation)
                .itemsCallbackSingleChoice(initiallySelectedIndex, listener)
                .alwaysCallSingleChoiceCallback()
                .negativeText(R.string.button_cancel)
                .cancelable(true);

        mDialogBuilder.show();
    }


    public interface PreferenceResult {
        void userInputCompleted();
    }

}
