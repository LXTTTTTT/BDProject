package com.bdtx.mod_main.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnCameraChangeListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.maps.offlinemap.OfflineMapActivity
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.ViewModel.CommunicationVM
import com.bdtx.mod_main.Base.BaseMVVMActivity
import com.bdtx.mod_main.R
import com.bdtx.mod_main.databinding.ActivityMapBinding
import com.bdtx.mod_util.Utils.DataUtils

@Route(path = Constant.MAP_ACTIVITY)
class MapActivity : BaseMVVMActivity<ActivityMapBinding,CommunicationVM>(false) {

    lateinit var aMap:AMap
    var all_location: ArrayList<LatLng> = ArrayList() // 所有位置
    var all_addr: ArrayList<String> = ArrayList() // 所有位置
    var all_remark: ArrayList<String> = ArrayList() // 所有备注，如果没有的话就加入 ""
    var all_time: ArrayList<Long> = ArrayList() // 所有备注，如果没有的话就加入 ""
    var all_marker: ArrayList<Marker> = ArrayList() // 所有标记点管理对象
    var now_terminal = 0

    override fun beforeSetLayout() {}
    override fun initView(savedInstanceState: Bundle?) {
        viewBinding.map.onCreate(savedInstanceState);
        init_map()
        init_control()
    }

    override fun initData() {
        super.initData()
        viewModel.getContact().observe(this,{
            it?.let {
                loge("获取到联系人数量："+it.size)
                all_location.clear()
                all_addr.clear();
                all_remark.clear();
                all_time.clear();
                it.forEach {  contact ->
                    if(contact.longitude!=0.0){
                        val location = LatLng(contact.latitude, contact.longitude)
                        all_location.add(location) // 加入位置
                        all_addr.add(contact.number) // 加入名字
                        if (contact.remark != null) {  // 加入备注，如果没有就加入空字符串
                            all_remark.add(contact.remark)
                        } else {
                            all_remark.add("")
                        }
                        all_time.add(contact.updateTime) // 加入更新时间
                        Log.e("有位置的名字是：", contact.number.toString() + "位置：" + contact.latitude + "/" + contact.longitude)
                    }
                }
                init_marker()
            }
        })
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
        myLocationStyle.interval(2000)
        myLocationStyle.showMyLocation(true)
        //设置定位蓝点的Style
        aMap.myLocationStyle = myLocationStyle
        aMap.isMyLocationEnabled = true
    // 初始化地图事件 ----------------------------------------------------
        // marker 点击事件
        aMap.setOnMarkerClickListener { marker ->
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, aMap.maxZoomLevel - 6))  // 移动到 marker 中心，放大图层
            // 更新设备信息
            val index = marker.getObject() as Int // 获取 marker 的index
            // 如果这个设备有备注就设置
            if (all_remark.get(index) !== "") { viewBinding.addr.setText(all_addr.get(index).toString() + "(" + all_remark.get(index) + ")") }
            else { viewBinding.addr.setText(all_addr.get(index))}
            // 设置位置
            viewBinding.location.setText(all_location.get(index).longitude.toString() + " E " + all_location.get(index).latitude + " N")
            // 设置定位时间
            viewBinding.time.setText(DataUtils.timeStampToString(all_time.get(index)))

            // 显示设备信息窗口
            if (viewBinding.terminalInfo.getVisibility() != View.VISIBLE) {
                viewBinding.terminalInfo.visibility = View.VISIBLE
            }

            // 把当前选中的设备改为现在的 index
            now_terminal = index
            true
        }
        // 地图视角移动事件
        aMap.setOnCameraChangeListener(object : OnCameraChangeListener {
            override fun onCameraChange(cameraPosition: CameraPosition) {}
            override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
                Log.e("我的位置：", "当前经纬度: " + aMap.myLocation)
            }
        })
        // 地图点击事件
        aMap.setOnMapClickListener {
            // 关闭信息窗口
            if (viewBinding.terminalInfo.getVisibility() == View.VISIBLE) {
                viewBinding.terminalInfo.visibility = View.GONE
            }
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
//            my_position = LatLng(MainApp.getInstance().latitude, MainApp.getInstance().longitude)
//            if (my_position.latitude != 0.0) {
//                aMap.animateCamera(CameraUpdateFactory.newLatLng(my_position))
//                return@OnClickListener
//            } else {
//                if (aMap.myLocation != null && aMap.myLocation.latitude != 0.0) {
//                    val my_loc = LatLng(aMap.myLocation.latitude, aMap.myLocation.longitude)
//                    aMap.animateCamera(CameraUpdateFactory.newLatLng(my_loc))
//                } else {
//                    Toast.makeText(this@MapActivity, "当前无位置信息", Toast.LENGTH_SHORT).show()
//                }
//            }
        })
        // 发消息按键：跳转到对应设备的聊天窗口
        viewBinding.sendGroup.setOnClickListener(View.OnClickListener {
            if (all_addr.size == 0) { return@OnClickListener }
            val addr: String = all_addr.get(now_terminal)
            loge("点击了 $addr")
//            val intent = Intent(this@MapActivity, ChatActivity::class.java)
//            intent.putExtra("id", addr)
//            startActivity(intent)
        })

    }


    fun init_marker() {
        // 清除所有 marker
        for (marker in all_marker) { marker.remove() }
        for (i in all_location.indices) {
            val latLng = all_location[i]
            val marker = MarkerOptions()
            marker.title(all_addr[i])  // 把这个 marker 的 title 设置为 “设备的卡号”
            marker.displayLevel(1) // 设置终端marker标识（1）
            marker.position(latLng)
            marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_icon)) // 设置图标
            val terminal_mrk = aMap.addMarker(marker)
            terminal_mrk.setObject(i)  // 设置这个 marker 的唯一标识： i
            all_marker.add(terminal_mrk)  // 把添加的 marker 对象加到数组里面方便管理
        }
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