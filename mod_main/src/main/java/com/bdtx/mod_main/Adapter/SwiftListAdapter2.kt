package com.bdtx.mod_main.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bdtx.mod_main.databinding.AdapterSwiftItem2Binding


class SwiftListAdapter2 : BaseRecyclerViewAdapter<ArrayList<Any>, AdapterSwiftItem2Binding>() {

    // 拿到布局
    override fun getViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): AdapterSwiftItem2Binding {
        return AdapterSwiftItem2Binding.inflate(layoutInflater,parent,false)
    }

    // 子项实现
    override fun onBindDefViewHolder(
        holder: BaseBindViewHolder<AdapterSwiftItem2Binding>,
        item: ArrayList<Any>?,
        position: Int
    ) {
        if (item == null) return
        holder.binding.apply {
            val swift_message = item[0]
            val deleteable = item[1]
            message.text = swift_message.toString()
            if(deleteable as Boolean){
                delete.visibility = View.VISIBLE
                delete.setOnClickListener { onDeleteClickListener?.let { it.invoke(position) } }
            }else{
                delete.visibility = View.INVISIBLE
            }
        }
    }

    var onDeleteClickListener: ((Int) -> Unit)? = null
//    @JvmName("setOnDeleteClickListenerJ")
//    fun setOnDeleteClickListener(l: () -> Unit) {
//        onDeleteClickListener = l
//    }


}