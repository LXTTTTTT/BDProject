package com.bdtx.mod_main.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.bdtx.mod_data.Database.Entity.Message
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_main.databinding.AdapterChatReceiveBinding
import com.bdtx.mod_main.databinding.AdapterChatSendBinding
import com.bdtx.mod_main.databinding.AdapterContactItemBinding
import com.sum.framework.adapter.BaseBindViewHolder
import com.sum.framework.adapter.BaseRecyclerViewAdapter

class ChatListAdapter : BaseRecyclerViewAdapter<Message,ViewBinding>() {

//    lateinit var viewBinding:ViewBinding
    override fun getViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewBinding {
        if(viewType==Constant.TYPE_SEND){
            return AdapterChatSendBinding.inflate(layoutInflater,parent,false)
        }else{
            return AdapterChatReceiveBinding.inflate(layoutInflater,parent,false)
        }
    }

    override fun onBindDefViewHolder(
        holder: BaseBindViewHolder<ViewBinding>,
        item: Message?,
        position: Int
    ) {
        if (item == null) return
        val ioType = item.ioType
        if(ioType==Constant.TYPE_SEND){
            var viewBinding = holder as BaseBindViewHolder<AdapterChatSendBinding>
            viewBinding.binding.apply {
                // 文本消息
                if(item.messageType==Constant.MESSAGE_TEXT){
                    textGroup.visibility = View.VISIBLE  // 显示文本组
                    voiceGroup.visibility = View.GONE  // 隐藏语音组
                    content.text = item.content  // 消息文本
                    // 状态
                    if(item.state==Constant.STATE_FAILURE){
                        state.visibility = View.VISIBLE
                        state.setOnClickListener{
                            onRecentClickListener?.let { it.invoke() }  // 重发消息
                        }
                    }else{state.visibility = View.GONE}
                }
            }
        }
        else{
            var viewBinding = holder as BaseBindViewHolder<AdapterChatReceiveBinding>
            viewBinding.binding.apply {

            }
        }
    }

    override fun getDefItemViewType(position: Int): Int {
        return getItem(position)?.ioType ?: 0
    }

// 接口 ----------------------------------------------------------
    var onRecentClickListener: (() -> Unit)? = null  // 点击

}