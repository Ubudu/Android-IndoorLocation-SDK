package com.ubudu.ilapp2.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ubudu.ilapp2.R;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduUser;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.Collection;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mgasztold on 05/06/2017.
 */

public class AttractorCalibrationView extends LinearLayout {

    private Context mContext;

    @BindView(R.id.attractor_rssi_difference_text_view)
    TextView attractorRssiDifferenceThresholdTextView;
    @BindView(R.id.attractor_rssi_threshold_seekbar)
    DiscreteSeekBar attractorRssiDifferenceThresholdSeekBar;

    @BindView(R.id.attractor_checks_count_text_view)
    TextView attractorChecksCountTextView;
    @BindView(R.id.attractor_checks_count_seekbar)
    DiscreteSeekBar attractorChecksCountSeekBar;

    private int rssiDifferenceThreshold;
    private int checksCount;

    SharedPreferences mSharedPref;

    // CONSTRUCTORS:

    public AttractorCalibrationView(Context context) {
        this(context, null);
    }

    public AttractorCalibrationView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public AttractorCalibrationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void init(Context context) {
        mContext = context;

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        inflate(mContext, R.layout.layout_calibrate_attractor, this);

        ButterKnife.bind(this);

        attractorRssiDifferenceThresholdSeekBar.setIndicatorPopupEnabled(false);

        rssiDifferenceThreshold = mSharedPref.getInt("attractor_rssi_threshold", 10);

        updateIndoorLocationAttractorThreshold(rssiDifferenceThreshold);

        attractorRssiDifferenceThresholdSeekBar.setProgress(rssiDifferenceThreshold);
        updateTextView(rssiDifferenceThreshold);

        attractorRssiDifferenceThresholdSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                rssiDifferenceThreshold =value;
                mSharedPref.edit().putInt("attractor_rssi_threshold",rssiDifferenceThreshold).apply();
                updateTextView(rssiDifferenceThreshold);
                updateIndoorLocationAttractorThreshold(rssiDifferenceThreshold);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        attractorChecksCountSeekBar.setIndicatorPopupEnabled(false);

        checksCount = mSharedPref.getInt("attractor_checks_count", 10);

        updateIndoorLocationAttractorChecksCount(checksCount);

        attractorChecksCountSeekBar.setProgress(checksCount);
        attractorChecksCountTextView.setText("Checks count: "+checksCount);

        attractorChecksCountSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                checksCount = value;
                mSharedPref.edit().putInt("attractor_checks_count",checksCount).apply();
                attractorChecksCountTextView.setText("Checks count: "+checksCount);
                updateIndoorLocationAttractorChecksCount(checksCount);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

    }

    private void updateTextView(int rssiDifferenceThreshold) {
        attractorRssiDifferenceThresholdTextView.setText("Attractor rssi differen e threshold: "+String.valueOf(rssiDifferenceThreshold) + " dBm");
    }

    private void updateIndoorLocationAttractorThreshold(int rssiDiff) {
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setAttractorRssiDifferenceThreshold(rssiDiff);
    }

    private void updateIndoorLocationAttractorChecksCount(int checksCount) {
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setAttractorChecks(checksCount);
    }

}
