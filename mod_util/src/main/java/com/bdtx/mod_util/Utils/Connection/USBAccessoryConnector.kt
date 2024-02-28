package com.bdtx.mod_util.Utils.Connection

import android.hardware.usb.UsbAccessory
import android.util.Log
import com.bdtx.mod_util.Utils.Transfer.USB.USBAccessoryTransferUtils

class USBAccessoryConnector:BaseConnector() {

    init {
        TAG = "USBAccessoryConnector"
        Log.e(TAG, "USBAccessory连接器" )
    }

    override fun connect(device: Any) {
        if(device !is UsbAccessory){
            Log.e(TAG, "非法对象")
            return
        }
        connectDeviceWithCondition(
            device,
            before = {
                // 前置操作
            },
            // 设置连接前置条件
            condition = object :()->Boolean{
                override fun invoke(): Boolean {
                    // 判断设备连接权限
                    return USBAccessoryTransferUtils.getInstance().checkPermission(device)
                }
            },
            connectWithTransfer = {
                // 连接设备
                USBAccessoryTransferUtils.getInstance().connectDevice(it)
            },
            after = {
                // 连接后操作
            }
        )
    }

    override fun disconnect(){
        USBAccessoryTransferUtils.getInstance().disconnect()  // 断开蓝牙
        BaseConnector.setConnector(null)  // 初始化连接器
    }

    override suspend fun getDevices(): List<Any>? {
        return getDevicesWithCondition(search = {
            USBAccessoryTransferUtils.getInstance().refreshDevice()
        })?.let { it as MutableList<UsbAccessory> }
    }


    // 实现发送消息
    override fun sendMessage(targetCardNumber: String, type: Int, content_str: String) {

    }
}