package com.bdtx.mod_main.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bdtx.mod_data.Database.Entity.Contact
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_main.R
import com.bdtx.mod_main.databinding.AdapterContactItemBinding
import com.bdtx.mod_main.databinding.AdapterSwiftItemBinding
import com.sum.framework.adapter.BaseBindViewHolder
import com.sum.framework.adapter.BaseRecyclerViewAdapter


class SwiftListAdapter : BaseRecyclerViewAdapter<String,AdapterSwiftItemBinding>() {

    // 拿到布局
    override fun getViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): AdapterSwiftItemBinding {
        return AdapterSwiftItemBinding.inflate(layoutInflater,parent,false)
    }

    // 子项实现
    override fun onBindDefViewHolder(
        holder: BaseBindViewHolder<AdapterSwiftItemBinding>,
        item: String?,
        position: Int
    ) {
        if (item == null) return
        holder.binding.apply {
            swiftMessage.text = item
        }
    }

}