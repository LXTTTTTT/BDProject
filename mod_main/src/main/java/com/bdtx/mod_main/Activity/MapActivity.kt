package com.bdtx.mod_main.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Route
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnCameraChangeListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.maps.offlinemap.OfflineMapActivity
import com.bdtx.mod_data.EventBus.BaseMsg
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.NormalBean.MapContactLocation
import com.bdtx.mod_data.ViewModel.CommunicationVM
import com.bdtx.mod_data.ViewModel.MainVM
import com.bdtx.mod_main.Base.BaseMVVMActivity
import com.bdtx.mod_main.R
import com.bdtx.mod_main.databinding.ActivityMapBinding
import com.bdtx.mod_util.Utils.ApplicationUtils
import com.bdtx.mod_util.Utils.CoordinateSystemUtils
import com.bdtx.mod_util.Utils.DataUtils
import com.bdtx.mod_util.Utils.GlobalControlUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = Constant.MAP_ACTIVITY)
class MapActivity : BaseMVVMActivity<ActivityMapBinding,CommunicationVM>(false) {

    lateinit var aMap:AMap
    var all_contact:ArrayList<MapContactLocation> = ArrayList()  // 所有联系人位置对象
    var all_marker: ArrayList<Marker> = ArrayList() // 所有标记点管理对象
    var now_contact = 0
    var my_position:LatLng? = null  // 自身位置
    var mainVM:MainVM? = null

    override fun beforeSetLayout() {}
    override suspend fun initDataSuspend() {}
    override fun enableEventBus(): Boolean { return true }
    companion object{
        @JvmStatic
        fun start(context: Context, longitude: Double, latitude: Double) {
            val intent = Intent(context, MapActivity::class.java)
            intent.putExtra(Constant.MAP_LONGITUDE, longitude)
            intent.putExtra(Constant.MAP_LATITUDE, latitude)
            (context as Activity).startActivity(intent)
        }
    }
    override fun initView(savedInstanceState: Bundle?) {
        viewBinding.map.onCreate(savedInstanceState);
        init_map()
        init_control()
    }

    override fun initData() {
        super.initData()
        init_view_model()
        // 如果是在聊天页点进来的需要切换视角
        val initialLongitude = intent.getDoubleExtra(Constant.MAP_LONGITUDE,0.0)
        val initialLatitude = intent.getDoubleExtra(Constant.MAP_LATITUDE,0.0)
        if(initialLongitude!=0.0){
            val initialLocation = CoordinateSystemUtils.castToOtherPoint(LatLng(initialLatitude, initialLongitude),1)  // 需要进行坐标系转换
            loge("切换视角（地球坐标系）：$initialLongitude $initialLatitude")
            Handler().postDelayed({
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 17.0f))
            },1000)
        }
    }

    // 数据变化监听
    fun init_view_model(){
        // 联系人列表变化监听
        viewModel.getContact().observe(this,{
            it?.let {
                loge("获取到联系人数量："+it.size)
                all_contact.clear()
                it.forEach {  contact ->
                    if(contact.longitude!=0.0){
                        val location = CoordinateSystemUtils.castToOtherPoint(LatLng(contact.latitude, contact.longitude),1)  // 需要进行坐标系转换
                        val remark = if(contact.remark != null){contact.remark}else{contact.number}
                        var contactLocation = MapContactLocation(location,contact.number,remark,contact.updateTime)
                        all_contact.add(contactLocation)
                        Log.e("有位置的联系人卡号：", contact.number.toString() + " 位置：" + contact.latitude + "/" + contact.longitude)
                    }
                }
                init_marker()
            }
        })

        mainVM = ApplicationUtils.getGlobalViewModel(MainVM::class.java)
        mainVM?.let {
            // 全局位置变化监听（改变自身位置）
            mainVM!!.deviceLatitude.observe(this,{
                it?.let {
                    if(it==0.0){return@observe}
                    // 改变我的位置，设备获取的坐标系是地球坐标系需要转换为火星坐标系
                    my_position = CoordinateSystemUtils.castToOtherPoint(LatLng(it, mainVM!!.deviceLongitude.value!!),1)
                }
            })

            mainVM!!.systemLatitude.observe(this,{  latitude->
                // 优先使用设备位置
                if(mainVM!!.deviceLatitude.value!=0.0){return@observe}
                latitude?.let {
                    if(it==0.0){return@observe}
                    // 改变我的位置，设备获取的坐标系是地球坐标系需要转换为火星坐标系
                    my_position = CoordinateSystemUtils.castToOtherPoint(LatLng(it, mainVM!!.systemLongitude.value!!),1)
                }
            })
        }
    }

    fun init_map(){
    // 初始化地图默认参数 ------------------------------------------
        if (!::aMap.isInitialized) {
            aMap = viewBinding.map.getMap()
            Log.e(TAG, "onCreate: 地图生成成功")
        }
        aMap.setMapType(AMap.MAP_TYPE_NIGHT)  // 夜景地图，aMap是地图控制器对象。
        aMap.getUiSettings().setZoomControlsEnabled(false)  // 隐藏地图放大缩小按键
        aMap.getUiSettings().setRotateGesturesEnabled(false)  // 关闭地图旋转手势
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.59904847756762, 107.27347920602494), 3f))  // 移动地图到全中国

    // 初始化自身定位蓝点样式 ---------------------------------------
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.strokeColor(resources.getColor(R.color.primary_2))
        myLocationStyle.radiusFillColor(resources.getColor(R.color.primary_3))
        // 设置小蓝点的图标，在xml文件中控制大小
        myLocationStyle.myLocationIcon(
            BitmapDescriptorFactory.fromView(
                layoutInflater.inflate(
                    R.layout.layout_mylocation,
                    null
                )
            )
        )
        // 连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。
        //（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
        //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.interval(10000)
        myLocationStyle.showMyLocation(true)
        //设置定位蓝点的Style
        aMap.myLocationStyle = myLocationStyle
        aMap.isMyLocationEnabled = true

    // 初始化地图事件 ----------------------------------------------------
        // marker 点击事件
        aMap.setOnMarkerClickListener { marker ->
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 17.0F))  // 移动到 marker 中心，放大图层
            // 更新设备信息
            marker.`object`?.let {
                val index = marker.getObject() as Int // 获取 marker 的index
                val contact = all_contact[index]
                viewBinding.addr.setText(contact.number)
                // 设置位置（只显示4位）
                val longitude = DataUtils.roundDouble(contact.location.longitude,6,false)
                val latitude = DataUtils.roundDouble(contact.location.latitude,6,false)
                viewBinding.location.setText(longitude.toString() + " E   " + latitude.toString() + " N")
                // 设置定位时间
                viewBinding.time.setText(DataUtils.timeStampToString(contact.time))
                // 显示设备信息窗口
                viewBinding.terminalInfo.isVisible = true  // kotlin 的快捷方法
                // 把当前选中的设备改为现在的 index
                now_contact = index
            }
            true
        }

        // 地图视角移动事件
        aMap.setOnCameraChangeListener(object : OnCameraChangeListener {
            override fun onCameraChange(cameraPosition: CameraPosition) {}
            override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
                loge("当前视角经纬度: " + aMap.myLocation)
            }
        })

        // 地图点击事件
        aMap.setOnMapClickListener {
            // 关闭信息窗口
            viewBinding.terminalInfo.isVisible = false
        }
    }

    fun init_control(){
        // 放大地图按键
        viewBinding.zoomUp.setOnClickListener(View.OnClickListener {
            aMap.animateCamera(CameraUpdateFactory.zoomTo(aMap.cameraPosition.zoom + 1))
        })
        // 缩小地图按键
        viewBinding.zoomDown.setOnClickListener(View.OnClickListener {
            aMap.animateCamera(CameraUpdateFactory.zoomTo(aMap.cameraPosition.zoom - 1))
        })
        // 下载离线地图按键：跳转到离线地图页面
        viewBinding.download.setOnClickListener(View.OnClickListener {
            // 高德官方的 离线地图 页
            startActivity(Intent(this@MapActivity, OfflineMapActivity::class.java))
        })
        // 我的定位按键：点击视角移动到自身位置
        viewBinding.myLocation.setOnClickListener(View.OnClickListener {
            if(my_position!=null){
                if (my_position!!.latitude != 0.0) {
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(my_position,17.0F))
                } else {
                    GlobalControlUtils.showToast("暂无设备位置信息",0)
                }
            }else{
                GlobalControlUtils.showToast("暂无设备位置信息",0)
            }

        })
        // 发消息按键：跳转到对应设备的聊天窗口
        viewBinding.sendGroup.setOnClickListener(View.OnClickListener {
            if (all_contact.isNullOrEmpty()) { return@OnClickListener }
            val number: String = all_contact[now_contact].number
            loge("点击了 $number")
            ChatActivity.start(this@MapActivity,number)
        })

    }


    fun init_marker() {
        if(all_contact.isNullOrEmpty()){return}
        // 清除原有标记点
        if(!all_marker.isNullOrEmpty()){
            for (marker in all_marker) { marker.remove() }
            all_marker.clear();
        }
        for (i in all_contact.indices) {
            val contact = all_contact[i]
            val latLng = contact.location
            val marker = MarkerOptions()
            marker.title(contact.number)  // 把这个 marker 的 title 设置为 “设备的卡号”
            marker.displayLevel(1) // 设置终端marker标识（1）
            marker.position(latLng)
            marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_icon)) // 设置图标
            val terminal_mrk = aMap.addMarker(marker)
            terminal_mrk.setObject(i)  // 设置这个 marker 的唯一标识： i
            all_marker.add(terminal_mrk)  // 把添加的 marker 对象加到数组里面方便管理
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onEvent(eventMsg: BaseMsg<Any>){
        loge("收到广播，类型：${eventMsg.type}")
        if(eventMsg.type== BaseMsg.MSG_UPDATE_CONTACT){
            viewModel.upDateContact()
        }
    }

    override fun onBackPressed() {
        if(viewBinding.terminalInfo.isVisible){ viewBinding.terminalInfo.isVisible=false }
        else{ super.onBackPressed() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewBinding.map.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        viewBinding.map.onPause()
        super.onPause()
    }

    override fun onResume() {
        viewBinding.map.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        viewBinding.map.onDestroy()
        super.onDestroy()
    }
}