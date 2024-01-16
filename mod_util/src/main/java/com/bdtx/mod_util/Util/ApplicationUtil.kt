package com.bdtx.mod_util.Util

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.bdtx.mod_data.ViewModel.MainVM
import com.bdtx.mod_util.Extension.saveAs

// 应用程序工具
object ApplicationUtil {
    private lateinit var app: Application
    private var isDebug = false

    fun init(application: Application, isDebug: Boolean) {
        app = application
        ApplicationUtil.isDebug = isDebug
    }

    // 获取全局应用
    fun getApplication() = app

    // 当前是否为debug环境
    fun isDebug() = isDebug

    private var mainVM : ViewModel? = null
    fun <T : ViewModel> getGlobalViewModel(viewModelClass: Class<T>) : T{
        if(mainVM==null){
            mainVM = ViewModelProvider.AndroidViewModelFactory(app).create(viewModelClass)
        }
        return mainVM as T
    }

    // 获取全局 viewModel
    inline fun <reified T : ViewModel> getGlobalViewModel(): T? {
        val activity = ActivityManagementUtil.getInstance().top() as FragmentActivity
        return ViewModelProvider(activity).get(T::class.java)
    }
    fun <T : ViewModel> getGlobalViewModelJava(viewModelClass: Class<T>): T? {
        val activity = ActivityManagementUtil.getInstance().top() as FragmentActivity
        return ViewModelProvider(activity).get(viewModelClass)
    }

}