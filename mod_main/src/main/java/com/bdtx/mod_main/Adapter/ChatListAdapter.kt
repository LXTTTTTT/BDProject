package com.bdtx.mod_main.Adapter

import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewbinding.ViewBinding
import com.bdtx.mod_data.Database.Entity.Message
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_main.databinding.AdapterChatReceiveBinding
import com.bdtx.mod_main.databinding.AdapterChatSendBinding
import com.bdtx.mod_util.Utils.AudioTrackUtils
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
        // 发送方
        if(ioType==Constant.TYPE_SEND){
            var viewBinding = holder as BaseBindViewHolder<AdapterChatSendBinding>
            viewBinding.binding.apply {
                // 文本消息
                if(item.messageType==Constant.MESSAGE_TEXT){
                    content.visibility = View.VISIBLE  // 显示文本组
                    voice.visibility = View.GONE  // 隐藏语音组
                    content.text = item.content  // 消息文本
                }
                // 语音消息
                else{
                    content.visibility = View.GONE  // 隐藏文本组
                    voice.visibility = View.VISIBLE  // 显示语音组
                    content.text = "语音消息"  // 消息文本
                    voice.setOnClickListener {
                        // 播放语音直接在 adapter 中处理
                        playVoice(item.voicePath,voiceImg)
                    }
                }
                // 状态
                if(item.state==Constant.STATE_SUCCESS){
                    state.visibility = View.GONE
                }else{
                    state.visibility = View.VISIBLE
                    // 发送中
                    if(item.state==Constant.STATE_SENDING){
                        fail.visibility = View.GONE
                        sending.visibility = View.VISIBLE
                    }
                    // 失败
                    else{
                        fail.visibility = View.VISIBLE
                        sending.visibility = View.GONE
                        fail.setOnClickListener{
                            onMessageClick?.let { it.onResendClick(item) }  // 重发消息
                        }
                    }
                }
            }
        }
        // 接收方
        else{
            var viewBinding = holder as BaseBindViewHolder<AdapterChatReceiveBinding>
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
                            onMessageClick?.let { it.onResendClick(item) }  // 重发消息
                        }
                    }else{state.visibility = View.GONE}
                }
                // 语音消息
                else{
                    textGroup.visibility = View.GONE  // 隐藏文本组
                    voiceGroup.visibility = View.VISIBLE  // 显示语音组
                    content.text = "语音消息"  // 消息文本
                    // 状态
                    if(item.state==Constant.STATE_FAILURE){
                        state.visibility = View.VISIBLE
                        state.setOnClickListener{
                            onMessageClick?.let { it.onResendClick(item) }  // 重发消息
                        }
                    }else{state.visibility = View.GONE}
                    voiceGroup.setOnClickListener {
                        // 播放语音直接在 adapter 中处理
                        playVoice(item.voicePath,voiceImg)
                    }
                }
            }
        }
    }

    override fun getDefItemViewType(position: Int): Int {
        return getItem(position)?.ioType ?: 0
    }

    private var anim: AnimationDrawable? = null
    // 播放语音同时开启播放动画
    private fun playVoice(path: String?, imageView: ImageView) {
        if (AudioTrackUtils.getInstance().isStart) {
            AudioTrackUtils.getInstance().stopPlay()
        }
        AudioTrackUtils.getInstance().startPlay(path, object : AudioTrackUtils.PlayListener {
            override fun start() {
                anim = imageView.background as AnimationDrawable
                anim?.start()
            }
            override fun stop() {
                anim?.also {
                    it.selectDrawable(0)
                    it.stop()
                }
            }
        })
    }

// 接口 ----------------------------------------------------------
    private var onMessageClick : OnMessageClick? = null
    fun setOnMessageClick(listener: OnMessageClick) { onMessageClick = listener }
    interface OnMessageClick {
        fun onResendClick(message: Message)
    }

}