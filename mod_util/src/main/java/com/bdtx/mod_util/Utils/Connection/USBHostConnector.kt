package com.bdtx.mod_util.Utils.Connection

import android.util.Log
import com.bdtx.mod_util.Utils.GlobalControlUtils
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
                GlobalControlUtils.showLoadingDialog("正在连接设备")
                USBHostTransferUtils.getInstance().setConnectionParameters(115200,8, UsbSerialPort.STOPBITS_1,UsbSerialPort.PARITY_NONE)  // 设置连接参数
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
            conditionFail = {
                GlobalControlUtils.hideLoadingDialog()
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
        USBHostTransferUtils.getInstance().disconnect()  // 断开连接
        BaseConnector.setConnector(null)  // 初始化连接器
    }

    override suspend fun getDevices(): List<Any>? {
        return getDevicesWithCondition(
            condition = {return@getDevicesWithCondition true},
            search = {
                USBHostTransferUtils.getInstance().refreshDevice()
            }
        ) as MutableList<UsbSerialDriver>
    }

    override fun initDevice() {
        USBHostTransferUtils.getInstance().init_device()
    }


    // 实现发送消息
    override fun sendMessage(targetCardNumber: String, type: Int, content_str: String) {
        USBHostTransferUtils.getInstance().sendMessage(targetCardNumber, type, content_str)
    }
}