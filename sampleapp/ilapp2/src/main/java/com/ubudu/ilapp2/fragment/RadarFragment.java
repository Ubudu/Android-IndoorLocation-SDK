package com.ubudu.ilapp2.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ubudu.indoorlocation.UbuduBeacon;
import com.ubudu.indoorlocation.UbuduIndoorLocationSDK;
import com.ubudu.indoorlocation.UbuduRangedBeaconsNotifier;
import com.ubudu.ilapp2.R;
import com.ubudu.ilapp2.util.RadarAdapter;

import java.util.List;

/**
 * Created by mgasztold on 06/10/16.
 */

public class RadarFragment extends BaseFragment implements UbuduRangedBeaconsNotifier {

    public static final String TAG = RadarFragment.class.getCanonicalName();

    // Log list view:
    private RadarAdapter adapter;
    private ListView lv;

    public RadarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_radar, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRadarList();
    }

    private void initRadarList() {
        lv = (ListView) getActivity().findViewById(R.id.radar_list);
        adapter = new RadarAdapter(getContext(),
                R.layout.list_item_beacon);

        lv.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewController().radarFragmentResumed();
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().setRangedBeaconsNotifier(this);
    }

    @Override
    public void onPause() {
        UbuduIndoorLocationSDK.getSharedInstance(getContext()).getIndoorLocationManager().removeRangedBeaconsNotifier(this);
        super.onPause();
    }

    @Override
    public void didRangeBeacons(final List<UbuduBeacon> beacons) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.clear();
                    for (UbuduBeacon beacon : beacons) {
                        adapter.add(beacon);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
    }
}
