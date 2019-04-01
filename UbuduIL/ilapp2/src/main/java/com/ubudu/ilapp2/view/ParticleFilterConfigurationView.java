package com.ubudu.ilapp2.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ubudu.ilapp2.R;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mgasztold on 05/06/2017.
 */

public class ParticleFilterConfigurationView extends LinearLayout {

    private Context mContext;

    @BindView(R.id.speed_coefficient)
    TextView speedCoefficientTextView;
    @BindView(R.id.speed_coefficient_seekbar)
    DiscreteSeekBar speedCoefficientSeekBar;
    @BindView(R.id.static_coefficient)
    TextView staticCoefficientTextView;
    @BindView(R.id.static_coefficient_seekbar)
    DiscreteSeekBar staticCoefficientSeekBar;
    private float speedCoefficient;
    private float staticCoefficient;
    SharedPreferences mSharedPref;

    // CONSTRUCTORS:

    public ParticleFilterConfigurationView(Context context) {
        this(context, null);
    }

    public ParticleFilterConfigurationView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public ParticleFilterConfigurationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void init(Context context) {
        mContext = context;

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        inflate(mContext, R.layout.layout_calibrate_particle_filter, this);

        ButterKnife.bind(this);

        speedCoefficientSeekBar.setIndicatorPopupEnabled(false);
        staticCoefficientSeekBar.setIndicatorPopupEnabled(false);

        speedCoefficient = mSharedPref.getFloat("pf_speed_coefficeient", 0.3f);
        staticCoefficient = mSharedPref.getFloat("pf_static_coefficeient", 0.15f);

        updateIndoorLocationUserInformation();

        speedCoefficientSeekBar.setProgress((int)(100*speedCoefficient/1.0));
        speedCoefficientTextView.setText("Speed coefficient: "+String.valueOf(speedCoefficient));

        speedCoefficientSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                speedCoefficient = (float)((double)value/100d);
                mSharedPref.edit().putFloat("pf_speed_coefficeient",speedCoefficient).apply();
                speedCoefficientTextView.setText("Speed coefficient: "+String.valueOf(speedCoefficient));
                updateIndoorLocationUserInformation();
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        staticCoefficientSeekBar.setProgress((int)(100*staticCoefficient/1.0));
        staticCoefficientTextView.setText("Static coefficient: "+String.valueOf(staticCoefficient));

        staticCoefficientSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                staticCoefficient = (float)((double)value/100d);
                mSharedPref.edit().putFloat("pf_static_coefficeient",staticCoefficient).apply();
                staticCoefficientTextView.setText("Static coefficient: "+String.valueOf(staticCoefficient));
                updateIndoorLocationUserInformation();
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
    }

    private void updateIndoorLocationUserInformation() {
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setParticleFilterSpread(speedCoefficient,staticCoefficient);
    }
}
