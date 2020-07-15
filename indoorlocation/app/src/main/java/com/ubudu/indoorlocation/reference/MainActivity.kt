package com.ubudu.indoorlocation.reference

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.indoorlocation.core.IndoorLocation
import io.indoorlocation.core.IndoorLocationProviderListener
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Error

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION = 1
    }

    private lateinit var indoorLocationProvider: MyIndoorLocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        indoorLocationProvider = MyIndoorLocationProvider(applicationContext)

        indoorLocationProvider.addListener(object : IndoorLocationProviderListener {
            override fun onProviderStopped() {
                appendLog("IL provider stopped")
            }

            override fun onProviderStarted() {
                appendLog("IL provider started")
            }

            override fun onProviderError(error: Error?) {
                appendLog("IL provider error: ${error?.message}")
            }

            override fun onIndoorLocationChange(indoorLocation: IndoorLocation?) {
                appendLog("onIndoorLocationChange: ${indoorLocation.toString()}")
            }

        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION)
        } else {
            indoorLocationProvider.start()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                indoorLocationProvider.start()
            }
        }
    }

    private fun appendLog(log: String) {
        illogs.text = "${illogs.text }\n$log"
    }
}