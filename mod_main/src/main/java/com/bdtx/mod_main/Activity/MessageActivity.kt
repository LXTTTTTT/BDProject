package com.bdtx.mod_main.Activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bdtx.mod_data.Database.Entity.Contact
import com.bdtx.mod_data.EventBus.AuthMsg
import com.bdtx.mod_data.EventBus.BaseMsg
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.ViewModel.CommunicationVM
import com.bdtx.mod_main.Adapter.ContactListAdapter
//import com.bdtx.mod_data.ViewModel.MessageActivityVM
import com.bdtx.mod_main.Base.BaseMVVMActivity
import com.bdtx.mod_main.databinding.ActivityMessageBinding
import com.bdtx.mod_util.Utils.ZDCompressionUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = Constant.MESSAGE_ACTIVITY)
class MessageActivity : BaseMVVMActivity<ActivityMessageBinding, CommunicationVM>(false) {

    lateinit var contactListAdapter : ContactListAdapter

    override fun beforeSetLayout() {}
    override fun enableEventBus(): Boolean { return true }

    override fun initView(savedInstanceState: Bundle?) {
        setTitle("所有消息")
        init_contact_list()
        init_control()
    }

    override fun initData() {
        super.initData()
        viewModel.getContact().observe(this,object : Observer<MutableList<Contact>?>{
            override fun onChanged(t: MutableList<Contact>?) {
                t?.let { contactListAdapter.setData(it) }
            }
        })

    }

    fun init_contact_list(){

        contactListAdapter = ContactListAdapter()
        contactListAdapter.onItemClickListener = object : (View, Int)->Unit{
            override fun invoke(view: View, position: Int) {
                loge("点击了 $position")
                contactListAdapter.getItem(position)?.let {
                    loge("卡号是： ${it.number}")
                    ChatActivity.start(this@MessageActivity,it.number)
                }
            }
        }
//        contactListAdapter.onItemClickListener = { view: View, i: Int ->
//
//        }
        viewBinding.contactList.layoutManager = LinearLayoutManager(my_context, LinearLayoutManager.VERTICAL, false)
        viewBinding.contactList.adapter = contactListAdapter


    }
    fun init_control(){
        viewBinding.addChat.setOnClickListener {
            ChatActivity.start(this@MessageActivity,Constant.NEW_CHAT)
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onEvent(eventMsg: BaseMsg<Any>){
        loge("收到广播，类型：${eventMsg.type}")
        if(eventMsg.type==BaseMsg.MSG_UPDATE_CONTACT){
            viewModel.upDateContact()
        }
    }


}


