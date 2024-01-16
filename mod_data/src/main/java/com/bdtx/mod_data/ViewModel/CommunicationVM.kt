package com.bdtx.mod_data.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdtx.mod_data.Database.DaoUtil
import com.bdtx.mod_data.Database.Entity.Contact
import com.bdtx.mod_data.Database.Entity.Message
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommunicationVM : BaseViewModel() {

    private val TAG = "CommunicationVM"
    private val contactList : MutableLiveData<MutableList<Contact>?> = MutableLiveData()  // 联系人列表
    private val messageList : MutableLiveData<MutableList<Message>?> = MutableLiveData()  // 消息列表

    // 获取联系人列表数据
    fun getContact() : LiveData<MutableList<Contact>?> {
        launchUIWithResult(
            responseBlock = {
                DaoUtil.getContacts()
            },
            successBlock = {
                it?.let { contacts ->
                    Log.e(TAG, "成功查询到: ${contacts.size} contact" )
                    contactList.value = contacts
                }
            }
        )
        return contactList
    }

    fun getMessage(number:String) : LiveData<MutableList<Message>?> {
        launchUIWithResult(
            responseBlock = {
                DaoUtil.getMessages(number)
            },
            successBlock = {
                it?.let { messages ->
                    Log.e(TAG, "成功查询到: ${messages.size} messages" )
                    messageList.value = messages
                }
            }
        )
        return messageList
    }




}