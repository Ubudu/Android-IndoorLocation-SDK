package com.ubudu.ilapp2.fragment;

import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ubudu.ilapp2.R;
import com.ubudu.ilapp2.util.MyRecyclerViewAdapter;
import com.ubudu.indoorlocation.ILBeacon;
import com.ubudu.ubeacon.UBeacon;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mgasztold on 06/10/16.
 */

public class RadarFragment extends BaseFragment {

    public static final String TAG = RadarFragment.class.getCanonicalName();

    // Log list view:
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

//    private boolean canUpdateList = true;

    private MyRecyclerViewAdapter recyclerViewAdapter;

    @BindView(R.id.searching)
    com.wang.avi.AVLoadingIndicatorView searching;

    public RadarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_radar, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRadarList();
    }

    private void initRadarList() {

//        lv = (ListView) getActivity().findViewById(R.id.radar_list);
//        adapter = new RadarAdapter(getContext(),
//                R.layout.list_item_beacon);
//
//        lv.setAdapter(adapter);
//
//        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                if(scrollState == SCROLL_STATE_IDLE){
//                    canUpdateList = true;
//                } else {
//                    canUpdateList = false;
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//
//            }
//        });
//
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                getViewController().onBeaconFragmentRequested(adapter.getItem(position));
//            }
//        });

        recyclerViewAdapter = new MyRecyclerViewAdapter(getContext()
                , new ArrayList<ILBeacon>()
                , new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildAdapterPosition(v);
                Log.e(TAG,"selected index in recyclerview: "+itemPosition);
                Log.e(TAG,"selected beacon: "+recyclerViewAdapter.getItems().get(itemPosition).getMajor());
                getViewController().onBeaconFragmentRequested(recyclerViewAdapter.getItems().get(itemPosition));
            }
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerViewAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
        getViewController().radarFragmentResumed(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getViewController().onRadarFragmentPaused();
    }

    public void didRangeBeacons(final List<ILBeacon> beacons) {
//        if(!canUpdateList) return;
//        if (getActivity() != null) {
//            if (beacons == null || beacons.isEmpty()) {
//                adapter.clear();
//                adapter.notifyDataSetChanged();
//                searching.show();
//            } else {
//                adapter.refreshBeacons(beacons, new RadarAdapter.RadarAdapterRefreshBeaconsListener() {
//                    @Override
//                    public void beaconsReady() {
//                        if (searching.isShown())
//                            searching.hide();
//                    }
//                });
//            }
//        }
        final List<ILBeacon> oldBeacons = new ArrayList<>(recyclerViewAdapter.getItems());

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new MyDiffCallback(oldBeacons, beacons));
        recyclerViewAdapter.setItems(beacons);
        result.dispatchUpdatesTo(recyclerViewAdapter);
    }

    public class MyDiffCallback extends DiffUtil.Callback {

        private final List<ILBeacon> mOldUBeaconList;
        private final List<ILBeacon> mNewUBeaconList;

        public MyDiffCallback(List<ILBeacon> mOldUBeaconList, List<ILBeacon> mNewUBeaconList) {
            this.mOldUBeaconList = mOldUBeaconList;
            this.mNewUBeaconList = mNewUBeaconList;
        }

        @Override
        public int getOldListSize() {
            return mOldUBeaconList.size();
        }

        @Override
        public int getNewListSize() {
            return mNewUBeaconList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return mOldUBeaconList.get(oldItemPosition).getUBeacon().getBluetoothAddress().equals(mNewUBeaconList.get(newItemPosition).getUBeacon().getBluetoothAddress());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return mOldUBeaconList.get(oldItemPosition).getUBeacon().getRunningAverageRssi() == mNewUBeaconList.get(newItemPosition).getUBeacon().getRunningAverageRssi()
            && mOldUBeaconList.get(oldItemPosition).getUBeacon().getBatteryLevel() == mNewUBeaconList.get(newItemPosition).getUBeacon().getBatteryLevel()
                    && mOldUBeaconList.get(oldItemPosition).getMajor() == mNewUBeaconList.get(newItemPosition).getMajor()
                    && mOldUBeaconList.get(oldItemPosition).getMinor() == mNewUBeaconList.get(newItemPosition).getMinor();
        }
    }

}
