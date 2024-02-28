package com.bdtx.mod_util.Utils.Connection

import android.util.Log
import com.bdtx.mod_util.Utils.Transfer.USB.USBHostTransferUtils
import com.bdtx.mod_util.Utils.Transfer.USB.USBSerial.driver.UsbSerialDriver
import com.bdtx.mod_util.Utils.Transfer.USB.USBSerial.driver.UsbSerialPort

class USBHostConnector:BaseConnector() {

    init {
        TAG = "USBHostConnector"
        Log.e(TAG, "USBHost连接器" )
    }

    override fun connect(device: Any) {
        if(device !is UsbSerialDriver){
            Log.e(TAG, "非法对象")
            return
        }
        connectDeviceWithCondition(
            device,
            before = {
                // 设置连接参数
                USBHostTransferUtils.getInstance().setConnectionParameters(115200,8, UsbSerialPort.STOPBITS_1,UsbSerialPort.PARITY_NONE)
            },
            // 设置连接前置条件
            condition = object :()->Boolean{
                override fun invoke(): Boolean {
                    // 判断设备连接权限
                    return USBHostTransferUtils.getInstance().checkDevicePermission(device)
                }
            },
            connectWithTransfer = {
                // 连接设备
                USBHostTransferUtils.getInstance().connectDevice(it)
            },
            after = {
                // 连接后操作
            }
        )
    }

    override fun disconnect(){
        USBHostTransferUtils.getInstance().disconnect()  // 断开连接
        BaseConnector.setConnector(null)  // 初始化连接器
    }

    override suspend fun getDevices(): List<Any>? {
        return getDevicesWithCondition(search = {
            USBHostTransferUtils.getInstance().refreshDevice()
        }) as MutableList<UsbSerialDriver>
    }


    // 实现发送消息
    override fun sendMessage(targetCardNumber: String, type: Int, content_str: String) {
//        USBHostTransferUtil.getInstance().sendMessage(targetCardNumber, type, content_str)
    }
}