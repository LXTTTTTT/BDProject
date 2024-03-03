package com.bdtx.mod_main.Activity

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.Global.Variable
import com.bdtx.mod_main.Adapter.SwiftListAdapter2
import com.bdtx.mod_main.Base.BaseViewBindingActivity
import com.bdtx.mod_main.databinding.ActivityCommunicationLinkBinding
import com.bdtx.mod_main.databinding.ActivityMessageTypeBinding
import com.bdtx.mod_util.Utils.Connection.*
import com.bdtx.mod_util.Utils.GlobalControlUtils

@Route(path = Constant.COMMUNICATION_LINK_ACTIVITY)
class CommunicationLinkActivity : BaseViewBindingActivity<ActivityCommunicationLinkBinding>() {

    override fun beforeSetLayout() {}

    override fun initView(savedInstanceState: Bundle?) {
        setTitle("通信链路");
        init_control()
    }

    override fun initData() {}
    override suspend fun initDataSuspend() {}

    fun init_control(){
        // 低功耗蓝牙
        viewBinding.bleConnection.setOnClickListener {
            // 初始化连接器
            val connector = BLEConnector()
            BaseConnector.setConnector(connector)
            ARouter.getInstance().build(Constant.CONNECT_BLUETOOTH_ACTIVITY).withInt(ConnectBluetoothActivity.CONNECTION_MODE,ConnectBluetoothActivity.MODE_BLE).navigation()  // 页面跳转
            finish()
        }
        // 经典蓝牙
        viewBinding.clsbConnection.setOnClickListener {
            // 初始化连接器
            val connector = CLSBConnector()
            BaseConnector.setConnector(connector)
            ARouter.getInstance().build(Constant.CONNECT_BLUETOOTH_ACTIVITY).withInt(ConnectBluetoothActivity.CONNECTION_MODE,ConnectBluetoothActivity.MODE_CLSB).navigation()  // 页面跳转
            finish()
        }
        // USB Host
        viewBinding.usbHostConnection.setOnClickListener {
            // 初始化连接器
            val connector = USBHostConnector()
            BaseConnector.setConnector(connector)
            ARouter.getInstance().build(Constant.CONNECT_USB_HOST_ACTIVITY).navigation()  // 页面跳转
            finish()
        }
        // USB Accessory
        viewBinding.usbAccessoryConnection.setOnClickListener {
            // 初始化连接器
            val connector = USBAccessoryConnector()
            BaseConnector.setConnector(connector)
            ARouter.getInstance().build(Constant.CONNECT_USB_ACCESSORY_ACTIVITY).navigation()  // 页面跳转
            finish()
        }
        // 串口
        viewBinding.serialPortConnection.setOnClickListener {
            // 初始化连接器
            val connector = SerialPortConnector()
            BaseConnector.setConnector(connector)
//            ARouter.getInstance().build(Constant.CONNECT_SERIAL_PORT).navigation()  // 页面跳转
            // 无权获取可用串口参数，只能根据已知串口地址直接打开
            BaseConnector.connector?.connect("/dev/ttyS0")
            finish()
        }
        viewBinding.back.setOnClickListener {
//            BaseConnector.connector?.disconnect()
//            BaseConnector.setConnector(null)
            finish()
        }
    }

}