package com.ubudu.ilapp2;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.ubudu.beacon.ScanningStrategy;
import com.ubudu.ilapp2.fragment.BaseFragment;
import com.ubudu.ilapp2.fragment.MapFragment;
import com.ubudu.ilapp2.fragment.PreferencesFragment;
import com.ubudu.ilapp2.fragment.RadarFragment;
import com.ubudu.ilapp2.fragment.ScanQrCodeFragment;
import com.ubudu.ilapp2.util.FragmentUtils;
import com.ubudu.ilapp2.util.MathUtils;
import com.ubudu.ilapp2.util.MyPreferences;
import com.ubudu.indoorlocation.UbuduIndoorLocationManager;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduResultListener;
import com.ubudu.indoorlocation.UbuduStartCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements BaseFragment.ViewController {

    public static final String TAG = MainActivity.class.getCanonicalName();

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION_PERMISSION = 0;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 1;

    private MaterialDialog mProgressDialog;

    private UbuduIndoorLocationSDK mIndoorLocationSdk;
    private UbuduIndoorLocationManager mIndoorLocationManager;

    private boolean isAppReady = false;
    private Handler bottomMenuHandler;

    private String currentFragmentTag;

    private MapFragment mMapFragment;

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

        // hide toolbar
        try {
            getSupportActionBar().hide();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        initBottomBar();

        onMapFragmentRequested();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACCESS_FINE_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                initUbuduIndoorLocationSdk();
        } else if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                onScanQrCodeFragmentRequested();
        }
    }

    @Override
    public void onBackPressed() {
        int countFragmentsOnStack = getSupportFragmentManager().getBackStackEntryCount();
        if (countFragmentsOnStack > 1) {
            super.onBackPressed();
        } else if (countFragmentsOnStack == 1) {
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
                        onSettingsFragmentRequested();
                    }
                }
            }
        });
    }

    private void initUbuduIndoorLocationSdk() {
        mIndoorLocationSdk = UbuduIndoorLocationSDK.getSharedInstance(getApplicationContext());
        mIndoorLocationManager = mIndoorLocationSdk.getIndoorLocationManager();
        mIndoorLocationManager.setBeaconScanningStrategy(new ScanningStrategy()
                .setForegroundRangingScanPeriod(850)
                .setBackgroundRangingScanPeriod(2000L)
                .setForegroundRangingBetweenScanPeriod(300)
                .setBackgroundRangingBetweenScanPeriod(7000));

        String namespace = MyPreferences.getPreferenceString(getApplicationContext(), MyPreferences.PREFERENCE_KEY_NAMESPACE, "");
        if(!namespace.equals("")) {
            if(currentFragmentTag.equals(MapFragment.TAG)){
                mMapFragment.showLoadingLabelWithText("Loading namespace...");
            }
            mIndoorLocationSdk.setNamespace(namespace, new UbuduResultListener() {
                @Override
                public void success() {
                    Log.i(TAG,"Indoor Location namespace set.");
                }

                @Override
                public void error() {
                    Log.e(TAG,"Namespace could not be set.");
                }
            });


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION_PERMISSION);
                return;
            }

            mIndoorLocationManager.start(new UbuduStartCallback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "Ubudu Indoor Location SDK started successfully");
                    mMapFragment.showLoadingLabelWithText("Waiting for initial position...");
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

    public void askUserForNamespace(final boolean cancelable) {
        MaterialDialog.Builder mDialogBuilder = new MaterialDialog.Builder(this)
                .title(R.string.choose_namespace_input_type)
                .items(R.array.namespace_input_type)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == 0) {
                            typeNewNamespace();
                        } else if (which == 1) {
                            onScanQrCodeFragmentRequested();
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
    public void onMapFragmentRequested() {
        if(currentFragmentTag==null || !currentFragmentTag.equals(MapFragment.TAG)) {
            mMapFragment = new MapFragment();
            FragmentUtils.changeFragment(this, mMapFragment, true);
        }
    }

    @Override
    public void onRadarFragmentRequested() {
        if(currentFragmentTag==null || !currentFragmentTag.equals(RadarFragment.TAG)) {
            FragmentUtils.changeFragment(this, new RadarFragment(), true);
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
    public void onSettingsFragmentRequested() {
        if(currentFragmentTag==null || !currentFragmentTag.equals(PreferencesFragment.TAG)) {
            FragmentUtils.changeFragment(this, new PreferencesFragment(), true);
        }
    }


    @Override
    public void mapFragmentResumed() {
        currentFragmentTag = MapFragment.TAG;
        mBottomBar.selectTabWithId(R.id.map_item);

        if(!isAppReady) {
            initUbuduIndoorLocationSdk();
            isAppReady = true;
        }
    }

    @Override
    public void radarFragmentResumed() {
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
        String namespace = MyPreferences.getPreferenceString(getApplicationContext(), MyPreferences.PREFERENCE_KEY_NAMESPACE,"");
        if (!namespace.equals("") && !namespace.equals(newNamespace)) {
            Log.i(TAG,"Switching Ubudu Indoor Location namespace from: \n" + namespace + " \n to:\n " + newNamespace);
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

    private void noNamespaceInput() {
        String namespace = MyPreferences.getPreferenceString(getApplicationContext(), MyPreferences.PREFERENCE_KEY_NAMESPACE, "");
        if(namespace.equals(""))
            askUserForNamespace(false);
    }

    public void onSettingsFragmentResumed() {
        currentFragmentTag = PreferencesFragment.TAG;
        mBottomBar.selectTabWithId(R.id.settings_item);
    }
}
