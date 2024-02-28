package com.bdtx.mod_util.Utils.Connection

import android.util.Log
import com.bdtx.mod_util.Utils.Transfer.SerialPortTransferUtils
import com.bdtx.mod_util.Utils.Transfer.USB.USBHostTransferUtils
import com.bdtx.mod_util.Utils.Transfer.USB.USBSerial.driver.UsbSerialDriver
import com.bdtx.mod_util.Utils.Transfer.USB.USBSerial.driver.UsbSerialPort

class SerialPortConnector:BaseConnector() {

    init {
        TAG = "SerialPortConnector"
        Log.e(TAG, "串口连接器" )
    }

    override fun connect(device: Any) {
        if(device !is String){
            Log.e(TAG, "非法对象")
            return
        }
        connectDeviceWithCondition(
            device,
            before = {
                // 设置连接参数
                SerialPortTransferUtils.getInstance().setSerialPortParameters(device,115200)
            },
            // 设置连接前置条件
            condition = object :()->Boolean{
                override fun invoke(): Boolean {
                    return true
                }
            },
            connectWithTransfer = {
                // 打开串口
                SerialPortTransferUtils.getInstance().openSerialPort()  // 打开串口前需要进行上电操作(未实现)
            },
            after = {
                // 连接后操作
            }
        )
    }

    override fun disconnect(){
        SerialPortTransferUtils.getInstance().close()  // 断开连接
        BaseConnector.setConnector(null)  // 初始化连接器
    }

    override suspend fun getDevices(): List<Any>? {
        return getDevicesWithCondition(search = {
            SerialPortTransferUtils.getInstance().serialPortPaths
        }) as MutableList<String>
    }


    // 实现发送消息
    override fun sendMessage(targetCardNumber: String, type: Int, content_str: String) {
//        USBHostTransferUtil.getInstance().sendMessage(targetCardNumber, type, content_str)
    }
}