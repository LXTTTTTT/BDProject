package com.bdtx.mod_main.Activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.Global.Variable
import com.bdtx.mod_main.Adapter.SwiftListAdapter2
import com.bdtx.mod_main.Base.BaseViewBindingActivity
import com.bdtx.mod_main.databinding.ActivityMessageTypeBinding
import com.bdtx.mod_util.Utils.GlobalControlUtils

@Route(path = Constant.SWIFT_MESSAGE_ACTIVITY)
class SwiftMessageActivity : BaseViewBindingActivity<ActivityMessageTypeBinding>() {

    var swiftListAdapter2: SwiftListAdapter2? = null
    var messageList = arrayListOf<ArrayList<Any>>()
    val messageListCustom = arrayListOf<ArrayList<Any>>()
    override fun beforeSetLayout() {}

    override fun initView(savedInstanceState: Bundle?) {
        setTitle("快捷消息设置");
        init_message_list()
        init_message_data()
        // 添加按键
        viewBinding.add.setOnClickListener {
            val message = viewBinding.messageContent.text.toString()
            if (message.isEmpty()) return@setOnClickListener
            Variable.addSwiftMsg(message)
            viewBinding.messageContent.setText("")
            init_message_data()
        }
        viewBinding.back.setOnClickListener {
            finish()
        }
    }

    override fun initData() {

    }

    fun init_message_list(){
        swiftListAdapter2 = SwiftListAdapter2()
        swiftListAdapter2!!.onDeleteClickListener = object : (Int)->Unit{
            override fun invoke(p1: Int) {
                GlobalControlUtils.showAlertDialog("删除自定义消息","是否删除", onYesClick = (object : ()->Unit{
                    override fun invoke() {
                        val position = p1 - messageListCustom.size
                        Variable.removeSwiftMsg(position)
                        init_message_data()
                        loge("删除第$position 项")
                    }
                }))
            }
        }
        viewBinding.swiftList.layoutManager = LinearLayoutManager(my_context, LinearLayoutManager.VERTICAL, false)
        viewBinding.swiftList.adapter = swiftListAdapter2

        // 添加固定消息
        val message1 = arrayListOf<Any>()
        message1.add("我在这里，一切正常");message1.add(false)
        messageListCustom.add(message1)
        val message2 = arrayListOf<Any>()
        message2.add("麻烦各位队友报一下自己的位置");message2.add(false)
        messageListCustom.add(message2)
        val message3 = arrayListOf<Any>()
        message3.add("我已安全到达目的地");message3.add(false)
        messageListCustom.add(message3)
        val message4 = arrayListOf<Any>()
        message4.add("任务已完成");message4.add(false)
        messageListCustom.add(message4)
    }

    fun init_message_data(){
        messageList.clear()
        messageList.addAll(messageListCustom)
        // 添加自定义消息
        val messages = Variable.getSwiftMsg()
        if(!messages.isNullOrEmpty()){
            val messages_array = messages.split(Constant.SWIFT_MESSAGE_SYMBOL)
//            val message_list = Arrays.asList(messages_array)
            messages_array.forEach {
                if(!it.isEmpty()){
                    val message = arrayListOf<Any>()
                    message.add(it);message.add(true)
                    messageList.add(message)
                }
            }
        }
        swiftListAdapter2?.let {
            it.setData(messageList)
            if (it.itemCount > 1) {
                viewBinding.swiftList.smoothScrollToPosition(it.itemCount - 1)
            }
        }
    }
}