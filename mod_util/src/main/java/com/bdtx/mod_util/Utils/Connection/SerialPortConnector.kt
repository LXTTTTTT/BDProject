package com.bdtx.mod_util.Utils.Connection

import android.util.Log
import com.bdtx.mod_util.Utils.GlobalControlUtils
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
                GlobalControlUtils.showLoadingDialog("正在连接设备")
                SerialPortTransferUtils.getInstance().setSerialPortParameters(device,115200)  // 设置连接参数
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
        SerialPortTransferUtils.getInstance().close()  // 断开连接
        BaseConnector.setConnector(null)  // 初始化连接器
    }

    override suspend fun getDevices(): List<Any>? {
        return getDevicesWithCondition(search = {
            SerialPortTransferUtils.getInstance().serialPortPaths
        }) as MutableList<String>
    }

    override fun initDevice() {
        SerialPortTransferUtils.getInstance().init_device()
    }


    // 实现发送消息
    override fun sendMessage(targetCardNumber: String, type: Int, content_str: String) {
        SerialPortTransferUtils.getInstance().sendMessage(targetCardNumber, type, content_str)
    }
}