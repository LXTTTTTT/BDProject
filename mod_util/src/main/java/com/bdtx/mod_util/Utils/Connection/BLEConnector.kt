package com.bdtx.mod_util.Utils.Connection

import android.bluetooth.BluetoothDevice
import android.util.Log
import com.bdtx.mod_util.Utils.Transfer.BluetoothTransferUtils

class BLEConnector:BaseConnector() {

    init {
        TAG = "BLEConnector"
        Log.e(TAG, "低功耗蓝牙连接器" )
    }

    override fun connect(device: Any) {
        if(device !is BluetoothDevice){
            Log.e(TAG, "非法对象")
            return
        }
        connectDeviceWithCondition(
            device,
            // 蓝牙连接前置条件
            condition = object :()->Boolean{
                override fun invoke(): Boolean {
                    return true
                }
            },
            connectWithTransfer = {
                // 连接蓝牙
                BluetoothTransferUtils.getInstance().connectDevice(it)
            },
            after = {
                // 连接蓝牙后操作
            }
        )
    }

    override fun disconnect(){
        BluetoothTransferUtils.getInstance().disconnectDevice()  // 断开蓝牙
        BaseConnector.setConnector(null)  // 初始化连接器
    }

    override suspend fun getDevices(): List<Any>? {
        return null
    }


    // 实现发送消息
    override fun sendMessage(targetCardNumber: String, type: Int, content_str: String) {
        BluetoothTransferUtils.getInstance().sendMessage(targetCardNumber, type, content_str)
    }
}