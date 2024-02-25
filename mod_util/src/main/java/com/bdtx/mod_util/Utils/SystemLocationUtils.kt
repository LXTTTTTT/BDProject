package com.bdtx.mod_util.Utils

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.bdtx.mod_data.ViewModel.MainVM

// 系统定位监听工具
object SystemLocationUtils {

    val TAG = "SystemLocationUtils"
    val minimumPeriod = 30000L  // 位置更新的最小周期

    fun init(application: Application){
        var locManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minimumPeriod, 0F, object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                Log.i(TAG, "系统位置变化: ${loc.longitude}/${loc.latitude}" )
                val mainVM = ApplicationUtils.getGlobalViewModel(MainVM::class.java)
                mainVM?.let {
                    it.systemLongitude.postValue(loc.longitude)
                    it.systemLatitude.postValue(loc.latitude)
                    it.systemAltitude.postValue(loc.altitude)
                }
            }
        })
        Log.e(TAG, "初始化系统定位变化监听" )
    }
}