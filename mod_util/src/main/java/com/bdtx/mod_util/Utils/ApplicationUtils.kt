package com.bdtx.mod_util.Utils

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.bdtx.mod_data.ViewModel.MainVM
import kotlin.reflect.KClass

// 应用程序工具
object ApplicationUtils {
    private lateinit var app: Application
    private var isDebug = false

    fun init(application: Application, isDebug: Boolean) {
        app = application
        ApplicationUtils.isDebug = isDebug
    }

    // 获取全局应用
    fun getApplication() = app
    // 当前是否为debug环境
    fun isDebug() = isDebug
    // 全局使用 ViewModelStore
    var globalViewModelStore : ViewModelStore? = null  // 一般是 MainApplication 的

    // 获取全局单例 viewModel
//    private var mainVM : ViewModel? = null
//    fun <T : ViewModel> getGlobalViewModel(viewModelClass: Class<T>) : T{
//        if(mainVM==null){
//            mainVM = ViewModelProvider.AndroidViewModelFactory(app).create(viewModelClass)
//        }
//        return mainVM as T
//    }

    fun <T : ViewModel> getGlobalViewModel(viewModelClass: Class<T>) : T?{
        var viewModel: T? = null
        globalViewModelStore?.let {
            viewModel =
                ViewModelLazy(
                    viewModelClass.kotlin,
                    { it },
                    { ViewModelProvider.AndroidViewModelFactory(app) }
                ).value
        }
        return viewModel
    }


    // 获取全局 viewModel
    inline fun <reified T : ViewModel> getGlobalViewModel(): T? {
        val activity = ActivityManagementUtils.getInstance().top() as FragmentActivity
        return ViewModelProvider(activity).get(T::class.java)
    }
    fun <T : ViewModel> getGlobalViewModelJava(viewModelClass: Class<T>): T? {
        val activity = ActivityManagementUtils.getInstance().top() as FragmentActivity
        return ViewModelProvider(activity).get(viewModelClass)
    }

}