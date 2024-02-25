package com.bdtx.mod_main.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bdtx.mod_data.Database.Entity.Contact
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_main.R
import com.bdtx.mod_main.databinding.AdapterContactItemBinding
import com.sum.framework.adapter.BaseBindViewHolder
import com.sum.framework.adapter.BaseRecyclerViewAdapter


class ContactListAdapter : BaseRecyclerViewAdapter<Contact,AdapterContactItemBinding>() {

    val TAG = "ContactListAdapter"
    // 拿到布局
    override fun getViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): AdapterContactItemBinding {
        return AdapterContactItemBinding.inflate(layoutInflater,parent,false)
    }

    // 子项实现
    override fun onBindDefViewHolder(
        holder: BaseBindViewHolder<AdapterContactItemBinding>,
        item: Contact?,
        position: Int
    ) {
        if (item == null) return
        val remark = if (item.remark.isNullOrEmpty()) item.number else item.remark
        holder.binding.apply {
            // 联系人名称
            contactName.text = remark
            // 最后一条消息
            if(!item.lastContent.isNullOrEmpty()){lastContent.text = item.lastContent}
            // 是否指挥中心
            if(item.number.equals(Constant.PLATFORM_IDENTIFIER)){
                icon.setImageResource(R.mipmap.platform_icon)
            }else{
                icon.setImageResource(R.mipmap.contact_icon)
            }
            // 未读消息数量
            if(item.unreadCount > 0){
                Log.e(TAG, "有未读消息${item.unreadCount}" )
                unreadCount.visibility = View.VISIBLE
                unreadCount.text = item.unreadCount.toString()
            }else{
                Log.e(TAG, "无未读消息" )
                unreadCount.visibility = View.GONE
            }
        }
    }


}