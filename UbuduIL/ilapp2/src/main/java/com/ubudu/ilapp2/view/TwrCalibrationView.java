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
 * Created by mgasztold on 14/02/2018.
 */

public class TwrCalibrationView extends LinearLayout {

    private Context mContext;

    @BindView(R.id.twr_interval_label)
    TextView twrIntervalTextView;
    @BindView(R.id.twr_interval_seekbar)
    DiscreteSeekBar twrIntervalSeekBar;

    @BindView(R.id.twr_distance_smoothing_label)
    TextView twrDistanceSmoothingLabel;
    @BindView(R.id.twr_distance_smoothing_seekbar)
    DiscreteSeekBar twrDistanceSmoothingSeekBar;

    private long twrInterval;
    private int distanceAvgCoeff;

    SharedPreferences mSharedPref;

    // CONSTRUCTORS:

    public TwrCalibrationView(Context context) {
        this(context, null);
    }

    public TwrCalibrationView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public TwrCalibrationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void init(Context context) {
        mContext = context;

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        inflate(mContext, R.layout.layout_twr, this);

        ButterKnife.bind(this);

        twrIntervalSeekBar.setIndicatorPopupEnabled(false);
        twrDistanceSmoothingSeekBar.setIndicatorPopupEnabled(false);

        twrInterval = mSharedPref.getLong("twr_interval", 400L);
        distanceAvgCoeff = mSharedPref.getInt("twr_distance_avg_coeff", 7);

        updateIndoorLocationTwrInterval();

        twrIntervalSeekBar.setProgress((int)twrInterval);
        twrIntervalTextView.setText("TWR interval: "+String.valueOf(twrInterval) + " ms");

        twrIntervalSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                twrInterval = value;
                mSharedPref.edit().putLong("twr_interval",twrInterval).apply();
                twrIntervalTextView.setText("TWR interval: "+String.valueOf(twrInterval) + " ms");
                updateIndoorLocationTwrInterval();
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        updateDistanceAvgCoeffInIndoorLocationSDK();

        twrDistanceSmoothingSeekBar.setProgress(distanceAvgCoeff);
        twrDistanceSmoothingLabel.setText("TWR distance avg coeff: "+String.valueOf(distanceAvgCoeff));

        twrDistanceSmoothingSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                distanceAvgCoeff = value;
                mSharedPref.edit().putInt("twr_distance_avg_coeff",distanceAvgCoeff).apply();
                twrDistanceSmoothingLabel.setText("TWR distance avg coeff: "+String.valueOf(distanceAvgCoeff));
                updateDistanceAvgCoeffInIndoorLocationSDK();
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
    }

    private void updateIndoorLocationTwrInterval() {
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setTwrInterval(twrInterval);
    }

    private void updateDistanceAvgCoeffInIndoorLocationSDK() {
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setTwrDistanceAvgCoefficient(distanceAvgCoeff);
    }
}
