package com.bdtx.mod_main.Base

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bdtx.mod_main.R
import com.bdtx.mod_util.Utils.ApplicationUtils
import java.util.*

// 基类
abstract class BaseActivity : AppCompatActivity(){

    val TAG : String? = this::class.java.simpleName
//    var APP : Application = ApplicationUtil.getApplication()
    val APP : Application by lazy { ApplicationUtils.getApplication() }
    lateinit var activity_window : Window
    lateinit var my_context : Context
    var title_textview: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity_window = window
        my_context = this
        beforeSetLayout()
        supportActionBar?.let {it.hide()}  // 隐藏标题栏
        setActivityLayout()  // 绑定布局
        setOrientationPortrait()  // 锁定垂直布局
        initView(savedInstanceState);  // 初始化页面
        initData();  // 初始化数据
        try {
            title_textview = findViewById<TextView>(R.id.title)
        } catch (e: Exception) {
            loge("这个页面没有 title bar")
        }
    }


    abstract fun setLayout(): Any?
    abstract fun beforeSetLayout()
    abstract fun initView(savedInstanceState: Bundle?)
    abstract fun initData()

    // 绑定布局
    open fun setActivityLayout(){
        loge("设置布局")
        setLayout()?.let {
            if (setLayout() is Int) {
                setContentView((setLayout() as Int?)!!) // 手动绑定 R.layout.id
            } else {
                setContentView(setLayout() as View?) // 使用 ViewBinding （手动设置）
            }
        }
    }

    // 打印 loge
    fun loge(log: String?) {
        Log.e(TAG, log!!)
    }

    // 设置页面标题
    fun setTitle(title : String){
        title_textview?.let { it.text = title }
    }

    // 设置页面垂直
    fun setOrientationPortrait() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // 页面返回响应
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loge("页面返回，请求码是：$requestCode 响应码是：$resultCode")
    }


    // 权限申请响应
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        loge("请求权限码是：" + requestCode + " / 权限列表：" + Arrays.toString(permissions) + " / 结果列表：" + Arrays.toString(grantResults))
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}