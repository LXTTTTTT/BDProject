package com.bdtx.mod_util.Utils

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.ViewModel.MainVM

// 系统定位监听工具
object SystemLocationUtils {

    val TAG = "SystemLocationUtils"
    val minimumPeriod = 30000L  // 位置更新的最小周期

    fun init(application: Application){
        if (ActivityCompat.checkSelfPermission(application, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(application, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityManagementUtils.getInstance().top()?.let {
                val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
                ActivityCompat.requestPermissions(it, perms, Constant.REQUEST_CODE_LOCATION)
            }
            GlobalControlUtils.showToast("请先授予权限！",0)
            return
        }
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