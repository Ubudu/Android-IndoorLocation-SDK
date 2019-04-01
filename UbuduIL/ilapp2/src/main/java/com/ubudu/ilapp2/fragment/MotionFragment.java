package com.ubudu.ilapp2.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.ubudu.ilapp2.R;
import com.ubudu.ilapp2.view.UserHeightAndGenderConfigurationView;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduMotionMonitorListener;
import com.ubudu.indoorlocation.UbuduStep;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class MotionFragment extends BaseFragment implements OnChartValueSelectedListener, View.OnTouchListener {

    public static final String TAG = MotionFragment.class.getSimpleName();

    private LineChart mChart;

    @BindView(R.id.speed_val)
    TextView speedTextView;
    @BindView(R.id.numsteps_val)
    TextView numStepsTextView;
    @BindView(R.id.mode_value)
    TextView modeTextView;
    @BindView(R.id.orientation_value)
    TextView orientationTextView;
    @BindView(R.id.step_length_val)
    TextView stepLengthView;
    @BindView(R.id.compass)
    ImageView compassImageView;

    @BindView(R.id.user_config)
    UserHeightAndGenderConfigurationView userCalibrationView;

    private final int MAX_CHART_ENTRIES = 50;

    private static int numSteps = 0;
    private static double stepLength = 0;
    private static String holdingMode = "-";
    private static float speed = 0;

    public MotionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout mRootView = (LinearLayout) inflater.inflate(R.layout.fragment_motion, container, false);
        ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        getViewController().onMotionFragmentPaused();
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setMotionListener(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewController().onMotionFragmentResumed();
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setMotionListener(new UbuduMotionMonitorListener() {

            @Override
            public void onVerticalAccelerationUpdate(final float verticalAcceleration) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LineData data = mChart.getData();

                            if (data != null) {
                                ILineDataSet set = data.getDataSetByIndex(0);

                                if (set == null) {
                                    set = createSet();
                                    data.addDataSet(set);
                                }

                                // add a new x-value first
                                data.addXValue(String.valueOf(set.getEntryCount()));
                                data.addEntry(new Entry(verticalAcceleration, set.getEntryCount()), 0);

                                // let the chart know it's data has changed
                                mChart.notifyDataSetChanged();

                                // limit the number of visible entries
                                mChart.setVisibleXRangeMaximum(MAX_CHART_ENTRIES);

                                // move to the latest entry
                                mChart.moveViewToX(data.getXValCount() - MAX_CHART_ENTRIES + 1);
                            }
                        }
                    });
                }
            }

            @Override
            public void onStepDetected(final UbuduStep step) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                numSteps++;
                                stepLength = step.length;
                                updateStepsUI();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }

            @Override
            public void onOrientationUpdated(double yaw, double pitch, double roll){
                if(yaw<0)
                    yaw = 360 + yaw;
                orientationTextView.setText("[ " + Math.round(yaw) + "\u00b0" + ", " + Math.round(pitch) + "\u00b0" + ", " + Math.round(roll) + "\u00b0 ]");
            }

            @Override
            public void onHoldingModeChanged(String newMode) {
                holdingMode = newMode;
                modeTextView.setText(holdingMode);
            }

            @Override
            public void onSpeedChanged(float newSpeed) {
                speed = newSpeed;
                speedTextView.setText(String.valueOf(speed));
            }

            @Override
            public void onCourseChanged(double course) {
                if(compassImageView!=null)
                    compassImageView.setRotation(-Math.round(course));
            }

            @Override
            public void onMovementChanged(boolean isMoving) {

            }
        });
    }

    private void updateStepsUI() {
        numStepsTextView.setText(String.valueOf(numSteps));
        stepLengthView.setText(stepLength + " m");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mChart = (LineChart) getActivity().findViewById(R.id.chart);
        mChart.setOnTouchListener(this);
        mChart.setOnChartValueSelectedListener(this);
        LineData data = new LineData();
        mChart.setData(data);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMaxValue(2f);
        leftAxis.setAxisMinValue(-2f);

        numStepsTextView.setText(String.valueOf(numSteps));
        stepLengthView.setText(String.valueOf(stepLength) + " m");
        modeTextView.setText(holdingMode);

        userCalibrationView.init(getContext());

        numStepsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numSteps = 0;
                updateStepsUI();
            }
        });
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Vertical acceleration");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(getContext().getResources().getColor(R.color.colorAccent));
        set.setCircleColor(getContext().getResources().getColor(R.color.colorAccent));
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setFillAlpha(65);
        set.setFillColor(getContext().getResources().getColor(R.color.colorAccent));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(getContext().getResources().getColor(R.color.colorAccent));
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }
}

