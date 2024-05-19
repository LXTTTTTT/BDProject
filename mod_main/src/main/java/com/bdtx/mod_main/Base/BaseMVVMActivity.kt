package com.bdtx.mod_main.Base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.viewbinding.ViewBinding
import com.bdtx.mod_util.Utils.ApplicationUtils
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

// MVVM 基类
abstract class BaseMVVMActivity<VB : ViewBinding ,VM : ViewModel>(global_model:Boolean = false) : BaseViewBindingActivity<VB>(){

    val global = global_model  // 是否全局使用的 ViewModel
    lateinit var viewModel: VM

    // 第一次重写，子类还要重写一次并且必须调用super方法执行下面的初始化
    override fun initData() {
        viewModel = if(global){
            // 使用全局 ViewModel
//            ApplicationUtils.getGlobalViewModel(argument[1] as Class<VM>)!!
            val superClass = this::class.supertypes
            loge("构造函数：${superClass[0]}")
            val kArgument = superClass[0].arguments
            loge("ViewModel对象：${kArgument[1]}")
            val classVM = kArgument[1].type?.classifier
            val globalViewModelStore : ViewModelStore =
                if(ApplicationUtils.globalViewModelStore==null){
                    ApplicationUtils.globalViewModelStore = viewModelStore
                    viewModelStore
                }else{
                    loge("已有 globalViewModelStore ")
                    ApplicationUtils.globalViewModelStore!!
                }
            ViewModelLazy(
                classVM as KClass<VM>,
                { globalViewModelStore },
                { defaultViewModelProviderFactory }
            ).value
        }else{
            val argument = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
            ViewModelProvider(this).get(argument[1] as Class<VM>)
        }
    }

}