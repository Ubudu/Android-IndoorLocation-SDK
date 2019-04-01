package com.ubudu.ilapp2.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ubudu.ilapp2.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mgasztold on 06/10/16.
 */

public class UwbTagFragment extends BaseFragment {

    public static final String TAG = UwbTagFragment.class.getCanonicalName();

    @BindView(R.id.button_disconnect)
    Button disconnectButton;

    @BindView(R.id.tag_details_text_view)
    TextView tagDetailsTextView;

    public UwbTagFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RelativeLayout mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_uwb_tag, container, false);
        ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getViewController().onDisconnectRequested();
                getViewController().onScannedDevicesFragmentRequested();
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        getViewController().onUwbTagFragmentResumed();
    }

    @Override
    public void onPause() {
        super.onPause();
        getViewController().onUwbTagFragmentPaused();
    }
}
