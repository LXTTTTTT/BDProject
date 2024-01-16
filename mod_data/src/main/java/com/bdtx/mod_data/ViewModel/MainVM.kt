package com.bdtx.mod_data.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bdtx.mod_data.Database.DaoUtil
import com.bdtx.mod_data.Database.Entity.Contact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

// 全局使用 ViewModel
class MainVM : BaseViewModel() {

    val isConnectDevice : MutableLiveData<Boolean?> = MutableLiveData()  // 是否连接蓝牙
    val deviceCardID : MutableLiveData<String?> = MutableLiveData()  // 卡号
    val deviceCardFrequency : MutableLiveData<Int?> = MutableLiveData()  // 频度
    val deviceCardLevel : MutableLiveData<Int?> = MutableLiveData()  // 通信等级
    val deviceBatteryLevel : MutableLiveData<Int?> = MutableLiveData()  // 电量
    val signal : MutableLiveData<IntArray?> = MutableLiveData()  // 信号

    val waitTime : MutableLiveData<Int?> = MutableLiveData()  // 等待时间

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
//        unreadMessageCount.postValue(0)
        waitTime.postValue(-1)
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

    // 倒计时
    fun countDownCoroutines(
        total: Int,
        scope: CoroutineScope,
        onTick: (Int) -> Unit,
        onStart: (() -> Unit)? = null,
        onFinish: (() -> Unit)? = null,
    ): Job {
        return flow {
            for (i in total downTo 0) {
                emit(i)
                delay(1000)
            }
        }
            .flowOn(Dispatchers.Main)
            .onStart { onStart?.invoke() }
            .onCompletion { onFinish?.invoke() }  // like java finally
            .onEach { onTick.invoke(it) }  // 每次倒计时时执行
            .launchIn(scope)
    }

}