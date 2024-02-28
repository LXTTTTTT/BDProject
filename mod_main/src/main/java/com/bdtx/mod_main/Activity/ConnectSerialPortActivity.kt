package com.bdtx.mod_main.Activity

import android.hardware.usb.UsbAccessory
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_main.Adapter.SerialPortListAdapter
import com.bdtx.mod_main.Adapter.USBAccessoryListAdapter
import com.bdtx.mod_main.Adapter.USBHostListAdapter
import com.bdtx.mod_main.Base.BaseViewBindingActivity
import com.bdtx.mod_main.databinding.ActivityConnectUsbBinding
import com.bdtx.mod_util.Utils.Connection.BaseConnector
import com.bdtx.mod_util.Utils.GlobalControlUtils
import com.bdtx.mod_util.Utils.Transfer.USB.USBSerial.driver.UsbSerialDriver

@Route(path = Constant.CONNECT_SERIAL_PORT)
class ConnectSerialPortActivity : BaseViewBindingActivity<ActivityConnectUsbBinding>() {

    private lateinit var serialPortListAdapter: SerialPortListAdapter
    override fun beforeSetLayout() {}
    override fun initView(savedInstanceState: Bundle?) {
        init_usb_list()
    }

    override fun initData() {}
    override suspend fun initDataSuspend() {
        serialPortListAdapter.setData((BaseConnector.connector?.getDevices()) as MutableList<String>)
    }

    private fun init_usb_list() {
        serialPortListAdapter = SerialPortListAdapter()
        // 列表点击
        serialPortListAdapter.onItemClickListener = { view: View?, integer: Int? ->
            BaseConnector.connector?.connect(serialPortListAdapter.getItem(integer!!)!!)
        }
        viewBinding.usbList.layoutManager = LinearLayoutManager(my_context, LinearLayoutManager.VERTICAL, false)
        viewBinding.usbList.adapter = serialPortListAdapter
    }
}