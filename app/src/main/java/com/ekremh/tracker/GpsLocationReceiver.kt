package com.ekremh.tracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.nfc.Tag
import android.util.Log
import android.widget.Toast

/**
 * Created by ekremh on 8.01.2018.
 */

class GpsLocationReceiver : BroadcastReceiver() {
    val TAG = GpsLocationReceiver::class.java.simpleName
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG,"Receiver worked")

        if(intent.action.equals( LocationManager.PROVIDERS_CHANGED_ACTION)){
              val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Log.d(TAG,"GPS ON !")
            }
            else {
                Log.d(TAG,"GPS OFF !")

            }

        }
    }
}
