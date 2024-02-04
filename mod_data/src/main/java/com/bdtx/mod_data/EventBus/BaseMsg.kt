package com.bdtx.mod_data.EventBus

class BaseMsg<T>(var type:Int = MSG_AUTH,var message: T?=null) {

    companion object{
        val MSG_AUTH = 0;  // 授权处理消息
        val MSG_UPDATE_MESSAGE = 1;  // 更新消息列表
        val MSG_UPDATE_CONTACT = 2;  // 更新联系人
//        val MSG_AUTH = 0;
    }
}