package com.bdtx.mod_data.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bdtx.mod_data.Database.DaoUtil
import com.bdtx.mod_data.Database.Entity.Contact

// 全局使用 ViewModel
class MainVM : BaseViewModel() {

    val isConnectDevice : MutableLiveData<Boolean?> = MutableLiveData()  // 是否连接蓝牙
    val deviceCardID : MutableLiveData<String?> = MutableLiveData()  // 卡号
    val deviceCardFrequency : MutableLiveData<Int?> = MutableLiveData()  // 频度
    val deviceCardLevel : MutableLiveData<Int?> = MutableLiveData()  // 通信等级
    val deviceBatteryLevel : MutableLiveData<Int?> = MutableLiveData()  // 电量
    val signal : MutableLiveData<IntArray?> = MutableLiveData()  // 信号

    val unreadMessageCount : MutableLiveData<Int?> = MutableLiveData()  // 未读消息数量
    init {
        initParameter()
    }

    // 初始化默认参数
    fun initParameter(){

        isConnectDevice.postValue(false)
        deviceCardID.postValue("-")
        deviceCardFrequency.postValue(-1)
        deviceCardLevel.postValue(-1)
        deviceBatteryLevel.postValue(-1)
        signal.postValue(intArrayOf(0,0,0,0,0,0,0,0,0,0))
        unreadMessageCount.postValue(0)
//        isConnectDevice.value = false
//        deviceCardID.value = "-"
//        deviceCardFrequency.value = -1
//        deviceCardLevel.value = -1
//        deviceBatteryLevel.value = -1
//        signal.value = intArrayOf(0,0,0,0,0,0,0,0,0,0)
    }

    fun getUnreadMessageCount() : LiveData<Int?> {
        launchUIWithResult(
            responseBlock = {
                DaoUtil.getContacts()
            },
            successBlock = {
                // 计算未读消息数量
                it?.let {  contacts ->
                    var count = 0
                    contacts.forEach {  contact ->
                        if(contact.unreadCount>0){
                            count += contact.unreadCount
                        }
                    }
                    unreadMessageCount.value = count
                }
            }
        )
        return unreadMessageCount
    }

}