package com.ubudu.ilapp2.fragment

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.ubudu.ilapp2.R
import com.ubudu.ilapp2.util.ScannedDevicesAdapter

import com.ubudu.iot.ble.BleDevice

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

import kotlinx.android.synthetic.main.fragment_scanned_devices.*

import java.util.ArrayList

/**
 * Created by mgasztold on 09/09/2017.
 */

class ScannedDevicesFragment : BaseFragment() {

    companion object {

        const val TAG = "ScannedDevicesFragment"
    }

    private var rssiThreshold: Int = 0
    private var nameMacFilter: String? = null
    private var adapter: ScannedDevicesAdapter? = null
    private val removedDevices = ArrayList<BleDevice>()
    private val mListHandler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scanned_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLogList()

        rssi_filter_seek_bar!!.progress = 100 + rssiThreshold
        filter_value!!.text = "$rssiThreshold dBm"
        rssi_filter_seek_bar!!.setIndicatorPopupEnabled(false)
        rssi_filter_seek_bar!!.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: DiscreteSeekBar, value: Int, fromUser: Boolean) {
                rssiThreshold = -(100 - value)
                viewController.onRssiFilterThresholdChanged(rssiThreshold)
                filter_value!!.text =  "$rssiThreshold dBm"
                filterDevices(name_mac_filter_edit_text!!.text.toString(), rssiThreshold)
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {

            }
        })

        scanned_devices_list!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->

            scanned_devices_list.isEnabled = false
            mListHandler.postDelayed({
                scanned_devices_list.isEnabled = true
            },100L)

            viewController.onConnectionRequested(adapter!!.getItem(i))
        }

        name_mac_filter_edit_text!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                nameMacFilter = charSequence.toString()
                filterDevices(nameMacFilter, rssiThreshold)
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })
        name_mac_filter_edit_text!!.setText(nameMacFilter)
        viewController.hideKeyboardRequested()

    }

    private fun filterDevices(filter: String?, rssiThreshold: Int) {

        run {
            var i = 0
            while (i < removedDevices.size) {
                val device = removedDevices[i]
                Log.e(TAG, "should BRING BACK device: " + device.device.name)
                if ((filter!!.isEmpty()
                        || device.device.name != null && device.device.name.contains(filter)
                        || device.device.address.contains(filter)) && device.rssi >= rssiThreshold) {
                    adapter!!.add(device)
                    removedDevices.removeAt(i)
                    i--
                    Log.e(TAG, "YES")
                } else {
                    Log.e(TAG, "NO")
                }
                i++
            }
        }

        var i = 0
        while (i < adapter!!.count) {
            val device = adapter!!.getItem(i)
            Log.e(TAG, "should REMOVE device: " + device!!.device.name)
            Log.i(TAG, "filter empty: " + filter!!.isEmpty())
            if (!filter.isEmpty() && (device.device.name == null || device.device.name != null && !device.device.name.contains(filter) && !device.device.address.contains(filter)) || device.rssi < rssiThreshold) {
                removedDevices.add(device)
                adapter!!.remove(device)
                i--
                Log.e(TAG, "YES")
            } else {
                Log.e(TAG, "NO")
            }
            i++
        }

        adapter!!.notifyDataSetChanged()

    }

    override fun onPause() {
        super.onPause()
        viewController.onNameMacFilterChanged(nameMacFilter)
        viewController.onScannedDevicesFragmentPaused()
    }

    override fun onResume() {
        super.onResume()
        viewController.onScannedDevicesFragmentResumed()
    }

    fun setRssiThreshold(rssiThreshold: Int) {
        this.rssiThreshold = rssiThreshold
    }

    private fun initLogList() {
        adapter = ScannedDevicesAdapter(context,
                R.layout.list_item_scanned_device)
        scanned_devices_list!!.adapter = adapter
    }

    fun updateDevice(device: BleDevice) {
        if (device.rssi >= rssiThreshold && (nameMacFilter!!.isEmpty() || device.device.name != null && device.device.name.contains(nameMacFilter!!) || device.device.address.contains(nameMacFilter!!)))
            adapter!!.addOrUpdate(device)
    }

    fun requestScanning() {
        viewController.onScanningRequested()
    }

    fun onScanningStarted() {
        scanned_devices_list!!.isEnabled = false
        scanning_animation_layout!!.visibility = View.VISIBLE
        adapter!!.clear()
        adapter!!.setColor(resources.getColor(R.color.colorDivider))
    }

    fun onScanningFinished() {
        scanned_devices_list?.isEnabled = true
        scanning_animation_layout?.visibility = View.GONE
        adapter?.setColor(resources.getColor(R.color.colorSecondaryText))
    }

    fun setNameMacFilter(macNameFilter: String) {
        nameMacFilter = macNameFilter
    }
}
