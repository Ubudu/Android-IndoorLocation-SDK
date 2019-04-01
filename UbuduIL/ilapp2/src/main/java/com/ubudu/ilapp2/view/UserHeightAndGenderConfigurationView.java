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

public class UserHeightAndGenderConfigurationView extends LinearLayout {

    private Context mContext;

    @BindView(R.id.user_height)
    TextView userHeightTextView;
    @BindView(R.id.height_seekbar)
    DiscreteSeekBar userHeightSeekBar;
    @BindView(R.id.male_radio_button)
    RadioButton maleRadioButton;
    @BindView(R.id.female_radio_button)
    RadioButton femaleRadioButton;
    private float userHeight;
    private String userGender = UbuduUser.GENDER_UNKNOWN;
    SharedPreferences mSharedPref;

    // CONSTRUCTORS:

    public UserHeightAndGenderConfigurationView(Context context) {
        this(context, null);
    }

    public UserHeightAndGenderConfigurationView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public UserHeightAndGenderConfigurationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void init(Context context) {
        mContext = context;

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        inflate(mContext, R.layout.layout_calibrate_user, this);

        ButterKnife.bind(this);

        userHeightSeekBar.setIndicatorPopupEnabled(false);

        userHeight = Float.valueOf(mSharedPref.getString("user_height", String.valueOf(UbuduUser.DEFAULT_USER_HEIGHT)));

        updateIndoorLocationUserInformation();

        userHeightSeekBar.setProgress((int)(100*(userHeight-0.4)/(2.5-0.4)));
        userHeightTextView.setText("User height: "+String.valueOf(userHeight) + " m");

        userHeightSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                double metersTimes10 = 4d + ((double)value*(25d-4d))/100d;
                userHeight = (float) (Math.floor(metersTimes10)/10);
                mSharedPref.edit().putString("user_height",String.valueOf(userHeight)).apply();
                userHeightTextView.setText("User height: "+String.valueOf(userHeight) + " m");
                updateIndoorLocationUserInformation();
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        userGender = mSharedPref.getString("user_gender", String.valueOf(UbuduUser.DEFAULT_USER_HEIGHT));

        if(userGender.equals(UbuduUser.GENDER_MALE))
            maleRadioButton.setChecked(true);
        else if(userGender.equals(UbuduUser.GENDER_FEMALE))
            femaleRadioButton.setChecked(true);

        maleRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                femaleRadioButton.setChecked(!isChecked);
                if(isChecked)
                    userGender = UbuduUser.GENDER_MALE;

                mSharedPref.edit().putString("user_gender",userGender).apply();
                updateIndoorLocationUserInformation();
            }
        });

        femaleRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maleRadioButton.setChecked(!isChecked);
                if(isChecked)
                    userGender = UbuduUser.GENDER_FEMALE;

                mSharedPref.edit().putString("user_gender",userGender).apply();
                updateIndoorLocationUserInformation();
            }
        });
    }

    private void updateIndoorLocationUserInformation() {
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).setUserInformation(new UbuduUser() {
            @Override
            public String getId() {
                return "UbuduIL";
            }

            @Override
            public Map<String, String> getProperties() {
                return null;
            }

            @Override
            public Collection<String> getTags() {
                return null;
            }

            @Override
            public double getHeight() {
                return userHeight;
            }

            @Override
            public String getGender() {
                return userGender;
            }
        });
    }
}
