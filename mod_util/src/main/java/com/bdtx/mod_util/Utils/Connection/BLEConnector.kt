package com.bdtx.mod_util.Utils.Connection

import android.bluetooth.BluetoothDevice
import android.util.Log
import com.bdtx.mod_util.Utils.GlobalControlUtils
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
            before = {
                GlobalControlUtils.showLoadingDialog("正在连接");
            },
            // 蓝牙连接前置条件：权限判断
            condition = object :()->Boolean{
                override fun invoke(): Boolean {
                    return true
                }
            },
            connectWithTransfer = {
                // 连接蓝牙
                BluetoothTransferUtils.getInstance().connectDevice(it)
            },
            success = {
                // 连接成功操作
                // ble连接之后还要进行分析/选择服务，无法在连接的第一时间判断设备是否连接成功，在别的地方进行成功/失败处理
            },
            fail = {
                // 连接失败操作
                disconnect()
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

    override fun initDevice() {
        BluetoothTransferUtils.getInstance().init_device()
    }


    // 实现发送消息
    override fun sendMessage(targetCardNumber: String, type: Int, content_str: String) {
        BluetoothTransferUtils.getInstance().sendMessage(targetCardNumber, type, content_str)
    }
}