package com.bdtx.mod_util.Utils.Connection

import android.bluetooth.BluetoothDevice
import android.util.Log
import com.bdtx.mod_util.Utils.GlobalControlUtils
import com.bdtx.mod_util.Utils.Transfer.BluetoothSocketTransferUtils
import com.bdtx.mod_util.Utils.Transfer.BluetoothTransferUtils

class CLSBConnector:BaseConnector() {

    init {
        TAG = "CLSBConnector"
        Log.e(TAG, "经典蓝牙连接器" )
    }

    override fun connect(device: Any) {
        if(device !is BluetoothDevice){
            Log.e(TAG, "非法对象")
            return
        }
        connectDeviceWithCondition(
            device,
            before = {
                GlobalControlUtils.showLoadingDialog("正在连接设备")
            },
            // 蓝牙连接前置条件
            condition = object :()->Boolean{
                override fun invoke(): Boolean {
                    return true
                }
            },
            connectWithTransfer = {
                // 连接蓝牙
                BluetoothSocketTransferUtils.getInstance().connect(it)
            },
            success = {
                // 连接成功操作
                initDevice()
                GlobalControlUtils.hideLoadingDialog()
            },
            fail = {
                // 连接失败操作
                GlobalControlUtils.hideLoadingDialog()
                GlobalControlUtils.showToast("连接失败",0)
            }
        )
    }

    override fun disconnect(){
        BluetoothSocketTransferUtils.getInstance().disconnect()  // 断开蓝牙
        BaseConnector.setConnector(null)  // 初始化连接器
    }

    override suspend fun getDevices(): List<Any>? {
        return null
    }

    override fun initDevice() {
        BluetoothSocketTransferUtils.getInstance().init_device()
    }


    // 实现发送消息
    override fun sendMessage(targetCardNumber: String, type: Int, content_str: String) {
        BluetoothSocketTransferUtils.getInstance().send_message(targetCardNumber,type,content_str)
    }
}