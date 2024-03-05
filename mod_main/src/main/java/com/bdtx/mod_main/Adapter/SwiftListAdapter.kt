package com.bdtx.mod_main.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bdtx.mod_main.databinding.AdapterSwiftItemBinding


class SwiftListAdapter : BaseRecyclerViewAdapter<String, AdapterSwiftItemBinding>() {

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
        if (item.isNullOrEmpty()) return
        holder.binding.apply {
            swiftMessage.text = item
        }
    }

}