package com.bdtx.mod_main.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bdtx.mod_data.Database.Entity.Contact
import com.bdtx.mod_main.databinding.AdapterContactItemBinding
import com.sum.framework.adapter.BaseBindViewHolder
import com.sum.framework.adapter.BaseRecyclerViewAdapter


class ContactListAdapter : BaseRecyclerViewAdapter<Contact,AdapterContactItemBinding>() {

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
            cardNumber.text = remark
            if(!item.lastContent.isNullOrEmpty()){lastContent.text = item.lastContent}
            if(item.unreadCount > 0){
                unredCount.visibility = View.VISIBLE
                unredCount.text = item.unreadCount.toString()
            }else{unredCount.visibility = View.GONE}
        }
    }


}