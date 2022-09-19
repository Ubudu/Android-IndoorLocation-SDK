package com.ubudu.indoorlocation.reference

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.indoorlocation.core.IndoorLocation
import io.indoorlocation.core.IndoorLocationProviderListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_PERMISSION_LOCATION = 1
    }

    private lateinit var indoorLocationProvider: MyIndoorLocationProvider

    override fun onPause() {
        indoorLocationProvider.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        indoorLocationProvider.onResume()
    }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE
                    )
                } else {
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
            }
            requestPermissions(permissions, REQUEST_CODE_PERMISSION_LOCATION)
        } else {
            indoorLocationProvider.start()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                indoorLocationProvider.start()
            }
        }
    }

    private fun appendLog(log: String) {
        illogs.text = "${illogs.text }\n$log"
        if(illogs.text.length > 1000) {
            illogs.text.substring(illogs.text.indexOf('\n')+1);
        }
    }
}